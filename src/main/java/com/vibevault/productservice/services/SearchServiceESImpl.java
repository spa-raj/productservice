package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.search.InvalidSearchParameterException;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.models.ProductDocument;
import com.vibevault.productservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service("searchServiceESImpl")
@RequiredArgsConstructor
public class SearchServiceESImpl implements SearchService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "price", "createdAt", "lastModifiedAt"
    );
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_SUGGESTIONS = 10;

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductRepository productRepository;

    @Override
    public Page<Product> searchProducts(String query, Double minPrice, Double maxPrice,
                                         Currency currency, UUID categoryId, String categoryName,
                                         Date createdAfter, Date createdBefore,
                                         int page, int size, String sortBy, String sortDir)
            throws InvalidSearchParameterException {

        validateSearchParameters(minPrice, maxPrice, createdAfter, createdBefore, page, size, sortBy);

        Sort.Direction direction = sortDir != null && sortDir.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String esSortField = mapSortField(sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, esSortField));

        NativeQueryBuilder queryBuilder = NativeQuery.builder().withPageable(pageRequest);

        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder boolBuilder =
                new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();

        // Exclude deleted products
        boolBuilder.filter(f -> f.term(t -> t.field("deleted").value(false)));

        // Full-text search on name and description with fuzzy matching
        if (query != null && !query.isBlank()) {
            boolBuilder.must(m -> m.multiMatch(mm -> mm
                    .query(query)
                    .fields("name^3", "description")
                    .fuzziness("AUTO")
            ));
        }

        // Price range filters
        if (minPrice != null || maxPrice != null) {
            boolBuilder.filter(f -> f.range(r -> {
                var numberRange = r.number(n -> {
                    if (minPrice != null) n.gte(minPrice);
                    if (maxPrice != null) n.lte(maxPrice);
                    return n.field("price");
                });
                return numberRange;
            }));
        }

        // Currency filter
        if (currency != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("currency").value(currency.name())));
        }

        // Category filters
        if (categoryId != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("categoryId").value(categoryId.toString())));
        }
        if (categoryName != null && !categoryName.isBlank()) {
            boolBuilder.filter(f -> f.term(t -> t.field("categoryName").value(categoryName.toLowerCase())));
        }

        // Date range filters
        if (createdAfter != null || createdBefore != null) {
            boolBuilder.filter(f -> f.range(r -> {
                var dateRange = r.date(d -> {
                    if (createdAfter != null) d.gte(createdAfter.toInstant().toString());
                    if (createdBefore != null) d.lte(createdBefore.toInstant().toString());
                    return d.field("createdAt");
                });
                return dateRange;
            }));
        }

        queryBuilder.withQuery(q -> q.bool(boolBuilder.build()));

        NativeQuery nativeQuery = queryBuilder.build();
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        // Fetch full Product entities from MySQL using the IDs from ES
        List<UUID> productIds = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> UUID.fromString(doc.getId()))
                .toList();

        Map<UUID, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            productRepository.findAllByIdWithCategory(productIds)
                    .forEach(p -> productMap.put(p.getId(), p));
        }

        // Preserve ES ordering, exclude soft-deleted products (guards against stale ES index)
        List<Product> products = productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .filter(p -> !p.isDeleted())
                .toList();

        long totalHits = searchHits.getTotalHits();
        return new PageImpl<>(products, pageRequest, totalHits);
    }

    @Override
    public List<Product> getSuggestions(String prefix, int limit) {
        int effectiveLimit = Math.min(Math.max(limit, 1), MAX_SUGGESTIONS);

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field("deleted").value(false)))
                        .must(m -> m.matchPhrasePrefix(mpp -> mpp
                                .field("name")
                                .query(prefix)
                        ))
                ))
                .withPageable(PageRequest.of(0, effectiveLimit, Sort.by("name.keyword").ascending()))
                .build();

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

        List<UUID> productIds = searchHits.getSearchHits().stream()
                .map(hit -> UUID.fromString(hit.getContent().getId()))
                .toList();

        if (productIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Product> productMap = new HashMap<>();
        productRepository.findAllByIdWithCategory(productIds)
                .forEach(p -> productMap.put(p.getId(), p));

        return productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .filter(p -> !p.isDeleted())
                .toList();
    }

    private void validateSearchParameters(Double minPrice, Double maxPrice,
                                          Date createdAfter, Date createdBefore,
                                          int page, int size, String sortBy) throws InvalidSearchParameterException {
        if (page < 0) {
            throw new InvalidSearchParameterException("page cannot be negative");
        }
        if (size < 1) {
            throw new InvalidSearchParameterException("size must be at least 1");
        }
        if (minPrice != null && minPrice < 0) {
            throw new InvalidSearchParameterException("minPrice cannot be negative");
        }
        if (maxPrice != null && maxPrice < 0) {
            throw new InvalidSearchParameterException("maxPrice cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new InvalidSearchParameterException("minPrice cannot be greater than maxPrice");
        }
        if (createdAfter != null && createdBefore != null && createdAfter.after(createdBefore)) {
            throw new InvalidSearchParameterException("createdAfter cannot be after createdBefore");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new InvalidSearchParameterException("Page size cannot exceed " + MAX_PAGE_SIZE);
        }
        if (!isValidSortField(sortBy)) {
            throw new InvalidSearchParameterException(
                    "Invalid sort field: " + sortBy + ". Allowed fields: " + ALLOWED_SORT_FIELDS);
        }
    }

    private String mapSortField(String sortBy) {
        if ("name".equals(sortBy)) {
            return "name.keyword";
        }
        return sortBy;
    }

    private boolean isValidSortField(String sortBy) {
        return sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy);
    }
}
