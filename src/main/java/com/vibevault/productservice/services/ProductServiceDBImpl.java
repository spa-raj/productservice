package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.ProductNotCreatedException;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("productServiceDBImpl")
public class ProductServiceDBImpl implements ProductService{
    private ProductRepository productRepository;
    @Autowired
    public ProductServiceDBImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    @Override
    public Product createProduct(Product product) throws ProductNotCreatedException {
        Product savedProduct;
        try{
            savedProduct = productRepository.save(product);
        }
        catch (Exception e){
            throw new ProductNotCreatedException("Product not created",e);
        }
        return savedProduct;
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
