package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.search.InvalidSearchParameterException;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.repositories.ProductRepository;
import com.vibevault.productservice.specifications.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service("searchServiceDBImpl")
public class SearchServiceDBImpl implements SearchService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "price", "createdAt", "lastModifiedAt"
    );

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_SUGGESTIONS = 10;

    private final ProductRepository productRepository;

    @Autowired
    public SearchServiceDBImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<Product> searchProducts(String query, Double minPrice, Double maxPrice,
                                         Currency currency, UUID categoryId, String categoryName,
                                         Date createdAfter, Date createdBefore,
                                         int page, int size, String sortBy, String sortDir)
            throws InvalidSearchParameterException {

        validateSearchParameters(minPrice, maxPrice, createdAfter, createdBefore, size, sortBy);

        String mappedSortField = mapSortField(sortBy);
        Sort.Direction direction = sortDir != null && sortDir.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, mappedSortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = Specification.where(ProductSpecification.notDeleted())
                .and(ProductSpecification.withQuery(query))
                .and(ProductSpecification.withMinPrice(minPrice))
                .and(ProductSpecification.withMaxPrice(maxPrice))
                .and(ProductSpecification.withCurrency(currency))
                .and(ProductSpecification.withCategoryId(categoryId))
                .and(ProductSpecification.withCategoryName(categoryName))
                .and(ProductSpecification.withCreatedAfter(createdAfter))
                .and(ProductSpecification.withCreatedBefore(createdBefore));

        return productRepository.findAll(spec, pageable);
    }

    @Override
    public List<Product> getSuggestions(String prefix, int limit) {
        int effectiveLimit = Math.min(Math.max(limit, 1), MAX_SUGGESTIONS);

        Specification<Product> spec = Specification.where(ProductSpecification.notDeleted())
                .and(ProductSpecification.withNamePrefix(prefix));

        Pageable pageable = PageRequest.of(0, effectiveLimit, Sort.by("name").ascending());

        return productRepository.findAll(spec, pageable).getContent();
    }

    private void validateSearchParameters(Double minPrice, Double maxPrice,
                                          Date createdAfter, Date createdBefore,
                                          int size, String sortBy) throws InvalidSearchParameterException {
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
        if ("price".equals(sortBy)) {
            return "price.price";
        }
        return sortBy;
    }

    private boolean isValidSortField(String sortBy) {
        return sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy);
    }
}
