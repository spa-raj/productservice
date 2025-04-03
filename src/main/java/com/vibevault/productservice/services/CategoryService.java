package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.categories.CategoryAlreadyExistsException;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Product;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories() throws CategoryNotFoundException;

    Category getCategoryById(String categoryId) throws CategoryNotFoundException;

    Category createCategory(Category category) throws CategoryNotCreatedException, CategoryAlreadyExistsException;

    Category getCategoryByName(String categoryName) throws CategoryNotFoundException;

    List<Product> getProductsList(List<String> categoryUuids) throws CategoryNotFoundException;

    List<Product> getProductsByCategory(String category);
}
