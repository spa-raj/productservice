package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories() throws CategoryNotFoundException;
}
