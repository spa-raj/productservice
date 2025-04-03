package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.categories.CategoryAlreadyExistsException;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceDBImpl implements CategoryService{
    private CategoryRepository categoryRepository;

    public CategoryServiceDBImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Override
    public List<Category> getAllCategories() throws CategoryNotFoundException {
        List<Category> categories = categoryRepository.findAll();
        if(categories.isEmpty()){
            throw new CategoryNotFoundException("No categories found");
        }
        return categories;
    }

    @Override
    public Category getCategoryById(String categoryId) throws CategoryNotFoundException {
        Optional<Category> categoryOptional = categoryRepository.findById(UUID.fromString(categoryId));
        if (categoryOptional.isPresent()) {
            return categoryOptional.get();
        } else {
            throw new CategoryNotFoundException("Category with ID " + categoryId + " not found");
        }
    }

    @Override
    public Category createCategory(Category category) throws CategoryNotCreatedException, CategoryAlreadyExistsException {
        Optional<Category> existingCategory = categoryRepository.findByName(category.getName());
        if(existingCategory.isPresent()){
            throw new CategoryAlreadyExistsException("Category with name " + category.getName() + " already exists");
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category getCategoryByName(String categoryName) throws CategoryNotFoundException {
        Optional<Category> categoryOptional = categoryRepository.findByName(categoryName);
        if (categoryOptional.isPresent()) {
            return categoryOptional.get();
        } else {
            throw new CategoryNotFoundException("Category with name " + categoryName + " not found");
        }
    }

    @Override
    public List<Product> getProductsList(List<String> categoryUuids) throws CategoryNotFoundException {
        List<UUID> uuids = categoryUuids.stream().map(UUID::fromString).toList();
        List<Category> categories = categoryRepository.findAllByIdIn(uuids);
        if (categories.isEmpty()) {
            throw new CategoryNotFoundException("No categories found for the provided UUIDs");
        }
        return categories.stream()
                .flatMap(category -> category.getProducts().stream())
                .toList();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        Optional<Category> categoryOptional = categoryRepository.findByName(category);
        if (categoryOptional.isPresent()) {
            return categoryOptional.get().getProducts();
        } else {
            throw new CategoryNotFoundException("Category with name " + category + " not found");
        }
    }
}
