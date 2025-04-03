package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.categories.CategoryAlreadyExistsException;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
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
}
