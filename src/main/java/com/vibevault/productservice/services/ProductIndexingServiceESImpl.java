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
import org.springframework.stereotype.Service;

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

    private void refreshIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        indexOps.refresh();
    }

    @Override
    public long reindexAll() {
        log.info("Starting full reindex of products to Elasticsearch...");
        long startTime = System.currentTimeMillis();

        productDocumentRepository.deleteAll();

        int page = 0;
        long totalIndexed = 0;

        Page<Product> productPage;
        do {
            productPage = productRepository.findAll(PageRequest.of(page, BATCH_SIZE));
            var documents = productPage.getContent().stream()
                    .map(ProductDocument::fromProduct)
                    .toList();

            productDocumentRepository.saveAll(documents);
            totalIndexed += documents.size();

            if (totalIndexed % 10_000 == 0 || !productPage.hasNext()) {
                log.info("Reindex progress: {}/{} products", totalIndexed, productPage.getTotalElements());
            }

            page++;
        } while (productPage.hasNext());

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Full reindex completed: {} products in {} seconds", totalIndexed, elapsed);
        return totalIndexed;
    }
}
