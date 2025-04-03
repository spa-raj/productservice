package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.categories.CreateCategoryRequestDto;
import com.vibevault.productservice.dtos.categories.CreateCategoryResponseDto;
import com.vibevault.productservice.dtos.categories.GetCategoryResponseDto;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.services.CategoryService;
import com.vibevault.productservice.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final ProductService productService;
    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }
    @GetMapping("")
    public List<GetCategoryResponseDto> getAllCategories() throws CategoryNotFoundException {
        List<Category> categories = categoryService.getAllCategories();
        List<GetCategoryResponseDto> responseDtoList =GetCategoryResponseDto.fromCategories(categories);
        return responseDtoList;
    }
    @GetMapping("id/{categoryId}")
    public GetCategoryResponseDto getCategoryById(@PathVariable("categoryId") String categoryId) throws CategoryNotFoundException {
        Category category = categoryService.getCategoryById(categoryId);
        return GetCategoryResponseDto.fromCategory(category);
    }
    @GetMapping("name/{categoryName}")
    public GetCategoryResponseDto getCategoryByName(@PathVariable("categoryName") String categoryName) throws CategoryNotFoundException {
        Category category = categoryService.getCategoryByName(categoryName);
        return GetCategoryResponseDto.fromCategory(category);
    }
//    @GetMapping("/products/{categoryId}")
//    public List<GetCategoryResponseDto> getProductsByCategory(@PathVariable("categoryId") Long categoryId) throws CategoryNotFoundException {
//        Category category = categoryService.getCategoryById(categoryId);
//        List<Product> products=
//        return GetCategoryResponseDto.fromCategories(;
//    }
    @PostMapping("")
    public CreateCategoryResponseDto createCategory(@RequestBody CreateCategoryRequestDto createCategoryRequestDto) throws CategoryNotCreatedException {
        Category category = categoryService.createCategory(createCategoryRequestDto.toCategory());
        return CreateCategoryResponseDto.fromCategory(category);
    }
}
