package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.categories.CreateCategoryRequestDto;
import com.vibevault.productservice.dtos.categories.CreateCategoryResponseDto;
import com.vibevault.productservice.dtos.categories.GetCategoryResponseDto;
import com.vibevault.productservice.dtos.categories.GetProductListResponseDto;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.services.CategoryService;
import com.vibevault.productservice.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CategoryControllerTest {

    private CategoryService categoryService;
    private ProductService productService;
    private CategoryController categoryController;

    @BeforeEach
    public void setUp() {
        categoryService = mock(CategoryService.class);
        productService = mock(ProductService.class);
        categoryController = new CategoryController(categoryService, productService);
    }

    @Test
    public void testGetAllCategoriesReturnsList() throws Exception {
        Category category1 = new Category();
        category1.setName("Electronics");
        category1.setDescription("Electronic items");

        Category category2 = new Category();
        category2.setName("Books");
        category2.setDescription("Books and Magazines");

        List<Category> categories = List.of(category1, category2);

        when(categoryService.getAllCategories()).thenReturn(categories);

        List<GetCategoryResponseDto> response = categoryController.getAllCategories();

        assertEquals(2, response.size());
        assertEquals("Electronics", response.get(0).getName());
        assertEquals("Books", response.get(1).getName());
    }

    @Test
    public void testGetAllCategoriesThrowsWhenNotFound() {
        when(categoryService.getAllCategories()).thenThrow(new CategoryNotFoundException("No categories"));
        assertThrows(CategoryNotFoundException.class, () -> categoryController.getAllCategories());
    }

    @Test
    public void testGetCategoryByIdReturnsCategory() throws Exception {
        String categoryId = "123";
        Category category = new Category();
        category.setName("Toys");
        category.setDescription("Toys for kids");

        when(categoryService.getCategoryById(categoryId)).thenReturn(category);

        GetCategoryResponseDto response = categoryController.getCategoryById(categoryId);

        assertEquals("Toys", response.getName());
        assertEquals("Toys for kids", response.getDescription());
    }

    @Test
    public void testGetCategoryByIdThrowsWhenNotFound() {
        String categoryId = "not-exist";
        when(categoryService.getCategoryById(categoryId)).thenThrow(new CategoryNotFoundException("Not found"));

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryController.getCategoryById(categoryId);
        });
    }

    @Test
    public void testGetCategoryByNameReturnsCategory() throws Exception {
        String categoryName = "Gadgets";
        Category category = new Category();
        category.setName("Gadgets");
        category.setDescription("All gadgets");

        when(categoryService.getCategoryByName(categoryName)).thenReturn(category);

        GetCategoryResponseDto response = categoryController.getCategoryByName(categoryName);

        assertEquals("Gadgets", response.getName());
        assertEquals("All gadgets", response.getDescription());
    }

    @Test
    public void testGetCategoryByNameThrowsWhenNotFound() {
        String categoryName = "NonExistent";
        when(categoryService.getCategoryByName(categoryName)).thenThrow(new CategoryNotFoundException("Not found"));

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryController.getCategoryByName(categoryName);
        });
    }

    @Test
    public void testGetProductsListByCategoryUUIDsReturnsProducts() throws Exception {
        List<String> uuids = Arrays.asList("uuid1", "uuid2");
        Product product1 = new Product();
        product1.setName("Product1");
        product1.setDescription("Desc1");
        product1.setImageUrl("img1");
        product1.setPrice(new Price());
        Category cat1 = new Category();
        cat1.setName("Cat1");
        product1.setCategory(cat1);

        Product product2 = new Product();
        product2.setName("Product2");
        product2.setDescription("Desc2");
        product2.setImageUrl("img2");
        product2.setPrice(new Price());
        Category cat2 = new Category();
        cat2.setName("Cat2");
        product2.setCategory(cat2);

        List<Product> products = Arrays.asList(product1, product2);

        when(categoryService.getProductsList(uuids)).thenReturn(products);

        List<GetProductListResponseDto> response = categoryController.getProductsListByCategoryUUIDs(uuids);

        assertEquals(2, response.size());
        assertEquals("Product1", response.get(0).getName());
        assertEquals("Product2", response.get(1).getName());
    }

    @Test
    public void testGetProductsListByCategoryUUIDsThrowsWhenNotFound() {
        List<String> uuids = Arrays.asList("uuid1", "uuid2");
        when(categoryService.getProductsList(uuids)).thenThrow(new CategoryNotFoundException("Not found"));

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryController.getProductsListByCategoryUUIDs(uuids);
        });
    }

    @Test
    public void testGetProductsListByCategoryReturnsProducts() throws Exception {
        String categoryName = "Electronics";
        Product product = new Product();
        product.setName("Phone");
        product.setDescription("Smartphone");
        product.setImageUrl("img");
        product.setPrice(new Price());
        Category cat = new Category();
        cat.setName(categoryName);
        product.setCategory(cat);

        List<Product> products = Collections.singletonList(product);

        when(categoryService.getProductsByCategory(categoryName)).thenReturn(products);

        List<GetProductListResponseDto> response = categoryController.getProductsListByCategoryName(categoryName);

        assertEquals(1, response.size());
        assertEquals("Phone", response.get(0).getName());
        assertEquals("Electronics", response.get(0).getCategoryName());
    }

    @Test
    public void testGetProductsListByCategoryThrowsWhenNotFound() {
        String categoryName = "NonExistent";
        when(categoryService.getProductsByCategory(categoryName)).thenThrow(new CategoryNotFoundException("Not found"));

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryController.getProductsListByCategoryName(categoryName);
        });
    }

    @Test
    public void testCreateCategoryWithValidData() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Fashion", "Clothing and accessories");
        Category category = new Category();
        category.setName("Fashion");
        category.setDescription("Clothing and accessories");
        UUID categoryId = UUID.randomUUID();
        category.setId(categoryId);

        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        CreateCategoryResponseDto response = categoryController.createCategory(requestDto);

        assertEquals("Fashion", response.getCategoryName());
        assertEquals("Clothing and accessories", response.getDescription());
        assertEquals(categoryId.toString(), response.getId());
    }

    @Test
    public void testCreateCategoryThrowsOnInvalidInput() {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("", "");
        when(categoryService.createCategory(any(Category.class)))
                .thenThrow(new CategoryNotCreatedException("Invalid data"));

        assertThrows(CategoryNotCreatedException.class, () -> {
            categoryController.createCategory(requestDto);
        });
    }

    @Test
    public void testGetProductsListByCategoryReturnsEmptyList() throws Exception {
        String categoryName = "EmptyCategory";
        when(categoryService.getProductsByCategory(categoryName)).thenReturn(Collections.emptyList());

        List<GetProductListResponseDto> response = categoryController.getProductsListByCategoryName(categoryName);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    public void testGetProductsListByCategoryUUIDsReturnsEmptyList() throws Exception {
        List<String> uuids = Arrays.asList("uuid1", "uuid2");
        when(categoryService.getProductsList(uuids)).thenReturn(Collections.emptyList());

        List<GetProductListResponseDto> response = categoryController.getProductsListByCategoryUUIDs(uuids);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }
}