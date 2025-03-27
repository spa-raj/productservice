package com.vibevault.productservice.services;

import com.vibevault.productservice.models.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("productServiceDBImpl")
public class ProductServiceDBImpl implements ProductService{
    @Override
    public Product createProduct(Product product) {
        return null;
    }

    @Override
    public Product updateProduct(Long productId, Product product) {
        return null;
    }

    @Override
    public Product getProductById(Long productId) {
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        return List.of();
    }

    @Override
    public Product deleteProduct(Long productId) {
        return null;
    }

    @Override
    public Product replaceProduct(Long productId, Product product) {
        return null;
    }
}
