package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.categories.GetCategoryResponseDto;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.services.CategoryService;
import com.vibevault.productservice.services.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @GetMapping("")
    public List<GetCategoryResponseDto> getAllCategories() throws CategoryNotFoundException {
        List<Category> categories = categoryService.getAllCategories();
        List<GetCategoryResponseDto> responseDtoList =GetCategoryResponseDto.fromCategories(categories);
        return responseDtoList;
    }
}
