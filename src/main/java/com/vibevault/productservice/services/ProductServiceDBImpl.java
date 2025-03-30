package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.ProductNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.repositories.CategoryRepository;
import com.vibevault.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("productServiceDBImpl")
public class ProductServiceDBImpl implements ProductService{
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    @Autowired
    public ProductServiceDBImpl(ProductRepository productRepository,
                                CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    @Override
    public Product createProduct(Product product) throws ProductNotCreatedException {
        Category category = getSavedCategory(product);
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long productId, Product product) throws ProductNotFoundException {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if(optionalProduct.isEmpty()){
            throw new ProductNotFoundException("Product with id " + productId + " not found");
        }
        Product existingProduct = optionalProduct.get();
        if(product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if(product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if(product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }
        if(product.getCategory() != null) {
            Category category = getSavedCategory(product);
            existingProduct.setCategory(category);
        }
        if(product.getImageUrl() != null) {
            existingProduct.setImageUrl(product.getImageUrl());
        }
        return productRepository.save(existingProduct);
    }

    private Category getSavedCategory(Product product) {
        Category category = product.getCategory();
        Optional<Category> categoryOptional = categoryRepository.findByName(product.getCategory().getName());
        if (categoryOptional.isPresent()) {
            category = categoryOptional.get();
        } else {
            category = categoryRepository.save(category);
        }
        return category;
    }

    @Override
    public Product getProductById(Long productId) {
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
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
