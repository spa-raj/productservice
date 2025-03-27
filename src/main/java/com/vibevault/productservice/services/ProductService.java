package com.vibevault.productservice.services;

import com.vibevault.productservice.models.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);

    Product updateProduct(Long productId, Product product);

    Product getProductById(Long productId);

    List<Product> getAllProducts();

    Product deleteProduct(Long productId);

    Product replaceProduct(Long productId, Product product);
}
