package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.categories.CreateCategoryRequestDto;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.security.RolesClaimConverter;
import com.vibevault.productservice.security.SecurityConfig;
import com.vibevault.productservice.services.CategoryService;
import com.vibevault.productservice.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, RolesClaimConverter.class})
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test-issuer.example.com"
})
class CategoryControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private JsonMapper jsonMapper;

    private Category sampleCategory;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setId(UUID.randomUUID());
        sampleCategory.setName("Electronics");
        sampleCategory.setDescription("Electronic gadgets");

        sampleProduct = new Product();
        sampleProduct.setId(UUID.randomUUID());
        sampleProduct.setName("Smartphone");
        sampleProduct.setDescription("Latest smartphone");
        sampleProduct.setImageUrl("http://example.com/image.jpg");
        sampleProduct.setPrice(new Price());
        sampleProduct.setCategory(sampleCategory);
    }

    // --- getAllCategories ---
    @Test
    void getAllCategories_Success() throws Exception {
        List<Category> categories = Arrays.asList(sampleCategory);
        Mockito.when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleCategory.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(sampleCategory.getName()))
                .andExpect(jsonPath("$[0].description").value(sampleCategory.getDescription()));
    }

    @Test
    void getAllCategories_NotFound() throws Exception {
        Mockito.when(categoryService.getAllCategories()).thenThrow(new CategoryNotFoundException("No categories found"));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isNotFound());
    }

    // --- getCategoryById ---
    @Test
    void getCategoryById_Success() throws Exception {
        Mockito.when(categoryService.getCategoryById(anyString())).thenReturn(sampleCategory);

        mockMvc.perform(get("/categories/id/{categoryId}", sampleCategory.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleCategory.getId().toString()))
                .andExpect(jsonPath("$.name").value(sampleCategory.getName()))
                .andExpect(jsonPath("$.description").value(sampleCategory.getDescription()));
    }

    @Test
    void getCategoryById_NotFound() throws Exception {
        Mockito.when(categoryService.getCategoryById(anyString())).thenThrow(new CategoryNotFoundException("Not found"));

        mockMvc.perform(get("/categories/id/{categoryId}", "nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    // --- getCategoryByName ---
    @Test
    void getCategoryByName_Success() throws Exception {
        Mockito.when(categoryService.getCategoryByName(anyString())).thenReturn(sampleCategory);

        mockMvc.perform(get("/categories/name/{categoryName}", sampleCategory.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleCategory.getId().toString()))
                .andExpect(jsonPath("$.name").value(sampleCategory.getName()))
                .andExpect(jsonPath("$.description").value(sampleCategory.getDescription()));
    }

    @Test
    void getCategoryByName_NotFound() throws Exception {
        Mockito.when(categoryService.getCategoryByName(anyString())).thenThrow(new CategoryNotFoundException("Not found"));

        mockMvc.perform(get("/categories/name/{categoryName}", "Nonexistent"))
                .andExpect(status().isNotFound());
    }

    // --- getProductsListByCategoryUUIDs ---
    @Test
    void getProductsListByCategoryUUIDs_Success() throws Exception {
        List<Product> products = Collections.singletonList(sampleProduct);
        Mockito.when(categoryService.getProductsList(anyList())).thenReturn(products);

        mockMvc.perform(get("/categories/products")
                        .param("categoryUuid", sampleCategory.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleProduct.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(sampleProduct.getName()))
                .andExpect(jsonPath("$[0].categoryName").value(sampleCategory.getName()));
    }

    @Test
    void getProductsListByCategoryUUIDs_NotFound() throws Exception {
        Mockito.when(categoryService.getProductsList(anyList())).thenThrow(new CategoryNotFoundException("Not found"));

        mockMvc.perform(get("/categories/products")
                        .param("categoryUuid", "nonexistent-uuid"))
                .andExpect(status().isNotFound());
    }

    // --- getProductsListByCategoryName ---
    @Test
    void getProductsListByCategoryName_Success() throws Exception {
        List<Product> products = Collections.singletonList(sampleProduct);
        Mockito.when(categoryService.getProductsByCategory(anyString())).thenReturn(products);

        mockMvc.perform(get("/categories/products/{category}", sampleCategory.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleProduct.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(sampleProduct.getName()))
                .andExpect(jsonPath("$[0].categoryName").value(sampleCategory.getName()));
    }

    @Test
    void getProductsListByCategoryName_NotFound() throws Exception {
        Mockito.when(categoryService.getProductsByCategory(anyString())).thenThrow(new CategoryNotFoundException("Not found"));

        mockMvc.perform(get("/categories/products/{category}", "Nonexistent"))
                .andExpect(status().isNotFound());
    }

    // --- createCategory ---
    @Test
    void createCategory_Success_AsAdmin() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Books", "All kinds of books");
        Category createdCategory = new Category();
        createdCategory.setId(UUID.randomUUID());
        createdCategory.setName(requestDto.getName());
        createdCategory.setDescription(requestDto.getDescription());

        Mockito.when(categoryService.createCategory(any(Category.class))).thenReturn(createdCategory);

        mockMvc.perform(post("/categories")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCategory.getId().toString()))
                .andExpect(jsonPath("$.categoryName").value(requestDto.getName()))
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()));
    }

    @Test
    void createCategory_Failure_CategoryNotCreated() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Books", "All kinds of books");
        Mockito.when(categoryService.createCategory(any(Category.class)))
                .thenThrow(new CategoryNotCreatedException("Creation failed"));

        mockMvc.perform(post("/categories")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createCategory_Fails_UnauthorizedRole() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Books", "All kinds of books");

        mockMvc.perform(post("/categories")
                        .with(jwt().authorities(() -> "ROLE_BUYER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_Fails_NoToken() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Books", "All kinds of books");

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }
}