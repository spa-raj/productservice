package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.categories.*;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Product;
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
    @GetMapping("/products")
    public List<GetProductListResponseDto> getProductsList(@RequestBody GetProductListRequestDto getProductListRequestDto) throws CategoryNotFoundException {
        List<Product> products= categoryService.getProductsList(getProductListRequestDto.getCategoryUuids());
        return GetProductListResponseDto.fromProducts(products);
    }
    @GetMapping("/products/{category}")
    public List<GetProductListResponseDto> getProductsListByCategory(@PathVariable("category") String category) throws CategoryNotFoundException {
        List<Product> products= categoryService.getProductsByCategory(category);
        return GetProductListResponseDto.fromProducts(products);
    }
    @PostMapping("")
    public CreateCategoryResponseDto createCategory(@RequestBody CreateCategoryRequestDto createCategoryRequestDto) throws CategoryNotCreatedException {
        Category category = categoryService.createCategory(createCategoryRequestDto.toCategory());
        return CreateCategoryResponseDto.fromCategory(category);
    }
}
