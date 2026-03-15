package com.vibevault.productservice.services;

import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.models.ProductDocument;
import com.vibevault.productservice.repositories.ProductDocumentRepository;
import com.vibevault.productservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexingServiceESImpl implements ProductIndexingService {

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    private static final int BATCH_SIZE = 1000;

    @Override
    public void indexProduct(Product product) {
        ProductDocument document = ProductDocument.fromProduct(product);
        productDocumentRepository.save(document);
        refreshIndex();
        log.debug("Indexed product: {}", product.getId());
    }

    @Override
    public void deleteFromIndex(String productId) {
        productDocumentRepository.deleteById(productId);
        refreshIndex();
        log.debug("Deleted product from index: {}", productId);
    }

    // TODO: Forcing refresh on every write is expensive at scale. This will be replaced
    // by Kafka-based indexing with periodic bulk refresh when Kafka is integrated.
    private void refreshIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        indexOps.refresh();
    }

    @Override
    public long reindexAll() {
        log.info("Starting full reindex of products to Elasticsearch...");
        long startTime = System.currentTimeMillis();

        // Drop and recreate the index instead of _delete_by_query
        // which times out on large datasets with small OpenSearch instances
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        indexOps.putMapping();

        // Use a no-refresh operations instance for bulk reindex performance.
        // OpenSearch's default 1s refresh_interval handles searchability during indexing.
        ElasticsearchOperations bulkOps = elasticsearchOperations.withRefreshPolicy(RefreshPolicy.NONE);

        int page = 0;
        long totalIndexed = 0;

        Page<Product> productPage;
        do {
            productPage = productRepository.findAllWithCategory(PageRequest.of(page, BATCH_SIZE));

            List<IndexQuery> indexQueries = productPage.getContent().stream()
                    .map(ProductDocument::fromProduct)
                    .map(doc -> new IndexQueryBuilder()
                            .withId(doc.getId())
                            .withObject(doc)
                            .build())
                    .toList();

            if (!indexQueries.isEmpty()) {
                bulkOps.bulkIndex(indexQueries, ProductDocument.class);
            }

            totalIndexed += indexQueries.size();

            if (totalIndexed % 100_000 == 0 || !productPage.hasNext()) {
                log.info("Reindex progress: {}/{} products", totalIndexed, productPage.getTotalElements());
            }

            page++;
        } while (productPage.hasNext());

        // Single refresh after all batches are done
        refreshIndex();

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Full reindex completed: {} products in {} seconds", totalIndexed, elapsed);
        return totalIndexed;
    }
}
