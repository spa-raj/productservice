package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.ProductNotFoundException;
import com.vibevault.productservice.models.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product) throws ProductNotCreatedException;

    Product updateProduct(Long productId, Product product) throws ProductNotFoundException;

    Product getProductById(Long productId) throws ProductNotFoundException;

    List<Product> getAllProducts() throws ProductNotFoundException;

    Product deleteProduct(Long productId) throws ProductNotFoundException, ProductNotDeletedException;

    Product replaceProduct(Long productId, Product product) throws ProductNotFoundException;
}
