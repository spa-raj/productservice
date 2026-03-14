package com.vibevault.productservice.services;

import com.vibevault.productservice.models.Product;

public interface ProductIndexingService {

    void indexProduct(Product product);

    void deleteFromIndex(String productId);

    long reindexAll();
}
