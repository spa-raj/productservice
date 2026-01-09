package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.product.*;
import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
import com.vibevault.productservice.security.SecurityConfig;
import com.vibevault.productservice.security.RolesClaimConverter;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, RolesClaimConverter.class})
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test-issuer.example.com"
})
class ProductControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private JsonMapper jsonMapper;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Electronics");
        Price price = new Price();
        price.setPrice(100.0);
        price.setCurrency(Currency.USD);
        sampleProduct = new Product();
        sampleProduct.setId(UUID.randomUUID());
        sampleProduct.setName("Test Product");
        sampleProduct.setDescription("Test Description");
        sampleProduct.setImageUrl("http://example.com/image.jpg");
        sampleProduct.setPrice(price);
        sampleProduct.setCategory(category);
    }

    // ==================== CREATE PRODUCT TESTS ====================

    @Test
    void createProduct_Success_AsSeller() throws Exception {
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Test Product");
        requestDto.setDescription("Test Description");
        requestDto.setImageUrl("http://example.com/image.jpg");
        requestDto.setPrice(100.0);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Electronics");

        Mockito.when(productService.createProduct(any(Product.class))).thenReturn(sampleProduct);

        mockMvc.perform(post("/products")
                        .with(jwt().authorities(() -> "ROLE_SELLER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void createProduct_Success_AsAdmin() throws Exception {
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Test Product");
        requestDto.setDescription("Test Description");
        requestDto.setImageUrl("http://example.com/image.jpg");
        requestDto.setPrice(100.0);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Electronics");

        Mockito.when(productService.createProduct(any(Product.class))).thenReturn(sampleProduct);

        mockMvc.perform(post("/products")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void createProduct_Fails_UnauthorizedRole() throws Exception {
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Test Product");

        mockMvc.perform(post("/products")
                        .with(jwt().authorities(() -> "ROLE_BUYER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_Fails_NoToken() throws Exception {
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Test Product");

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== UPDATE PRODUCT TESTS ====================

    @Test
    void updateProduct_Success_AsAdmin() throws Exception {
        UpdateProductRequestDto requestDto = new UpdateProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        requestDto.setName("Updated Name");
        requestDto.setDescription("Updated Description");
        requestDto.setImageUrl("http://example.com/image2.jpg");
        requestDto.setPrice(200.0);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Electronics");

        Product updatedProduct = new Product();
        updatedProduct.setId(sampleProduct.getId());
        updatedProduct.setName("Updated Name");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setImageUrl("http://example.com/image2.jpg");
        Price updatedPrice = new Price();
        updatedPrice.setPrice(200.0);
        updatedPrice.setCurrency(Currency.USD);
        updatedProduct.setPrice(updatedPrice);
        updatedProduct.setCategory(sampleProduct.getCategory());

        Mockito.when(productService.updateProduct(eq(sampleProduct.getId().toString()), any(Product.class)))
                .thenReturn(updatedProduct);

        mockMvc.perform(patch("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateProduct_Success_AsSeller() throws Exception {
        UpdateProductRequestDto requestDto = new UpdateProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        requestDto.setName("Updated Name");

        Mockito.when(productService.updateProduct(eq(sampleProduct.getId().toString()), any(Product.class)))
                .thenReturn(sampleProduct);

        mockMvc.perform(patch("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_SELLER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProduct_Fails_UnauthorizedRole() throws Exception {
        UpdateProductRequestDto requestDto = new UpdateProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());

        mockMvc.perform(patch("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_BUYER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProduct_Fails_NoToken() throws Exception {
        UpdateProductRequestDto requestDto = new UpdateProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());

        mockMvc.perform(patch("/products/" + sampleProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET PRODUCT TESTS (Public) ====================

    @Test
    void getProductById_Success() throws Exception {
        Mockito.when(productService.getProductById(sampleProduct.getId().toString())).thenReturn(sampleProduct);

        mockMvc.perform(get("/products/" + sampleProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductById_Success_WithoutAuth() throws Exception {
        // GET endpoints are public - no authentication required
        Mockito.when(productService.getProductById(sampleProduct.getId().toString())).thenReturn(sampleProduct);

        mockMvc.perform(get("/products/" + sampleProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductById_Fails_NotFound() throws Exception {
        Mockito.when(productService.getProductById(anyString()))
                .thenThrow(new ProductNotFoundException("Not found"));

        mockMvc.perform(get("/products/" + sampleProduct.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_Success() throws Exception {
        Mockito.when(productService.getAllProducts()).thenReturn(Collections.singletonList(sampleProduct));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void getAllProducts_Success_WithoutAuth() throws Exception {
        // GET endpoints are public - no authentication required
        Mockito.when(productService.getAllProducts()).thenReturn(Collections.singletonList(sampleProduct));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllProducts_ReturnsEmptyList() throws Exception {
        Mockito.when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== DELETE PRODUCT TESTS ====================

    @Test
    void deleteProduct_Success_AsSeller() throws Exception {
        Mockito.when(productService.deleteProduct(sampleProduct.getId().toString())).thenReturn(sampleProduct);

        mockMvc.perform(delete("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_SELLER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void deleteProduct_Success_AsAdmin() throws Exception {
        Mockito.when(productService.deleteProduct(sampleProduct.getId().toString())).thenReturn(sampleProduct);

        mockMvc.perform(delete("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void deleteProduct_Fails_UnauthorizedRole() throws Exception {
        mockMvc.perform(delete("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProduct_Fails_NoToken() throws Exception {
        mockMvc.perform(delete("/products/" + sampleProduct.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteProduct_Fails_NotFound() throws Exception {
        Mockito.when(productService.deleteProduct(anyString()))
                .thenThrow(new ProductNotFoundException("Not found"));

        mockMvc.perform(delete("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isNotFound());
    }

    // ==================== REPLACE PRODUCT TESTS ====================

    @Test
    void replaceProduct_Success_AsAdmin() throws Exception {
        ReplaceProductRequestDto requestDto = new ReplaceProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        requestDto.setName("Replaced Name");
        requestDto.setDescription("Replaced Description");
        requestDto.setImageUrl("http://example.com/image3.jpg");
        requestDto.setPrice(300.0);
        requestDto.setCurrency("USD");
        requestDto.setCategoryName("Electronics");

        Product replacedProduct = new Product();
        replacedProduct.setId(sampleProduct.getId());
        replacedProduct.setName("Replaced Name");
        replacedProduct.setDescription("Replaced Description");
        replacedProduct.setImageUrl("http://example.com/image3.jpg");
        Price replacedPrice = new Price();
        replacedPrice.setPrice(300.0);
        replacedPrice.setCurrency(Currency.USD);
        replacedProduct.setPrice(replacedPrice);
        replacedProduct.setCategory(sampleProduct.getCategory());

        Mockito.when(productService.replaceProduct(eq(sampleProduct.getId().toString()), any(Product.class)))
                .thenReturn(replacedProduct);

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Replaced Name"));
    }

    @Test
    void replaceProduct_Success_AsSeller() throws Exception {
        ReplaceProductRequestDto requestDto = new ReplaceProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        requestDto.setName("Replaced Name");
        requestDto.setDescription("Replaced Description");
        requestDto.setImageUrl("http://example.com/image3.jpg");
        requestDto.setPrice(300.0);
        requestDto.setCurrency("USD");
        requestDto.setCategoryName("Electronics");

        Mockito.when(productService.replaceProduct(eq(sampleProduct.getId().toString()), any(Product.class)))
                .thenReturn(sampleProduct);

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_SELLER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void replaceProduct_Fails_UnauthorizedRole() throws Exception {
        ReplaceProductRequestDto requestDto = new ReplaceProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_BUYER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void replaceProduct_Fails_NoToken() throws Exception {
        ReplaceProductRequestDto requestDto = new ReplaceProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void replaceProduct_Fails_NotFound() throws Exception {
        ReplaceProductRequestDto requestDto = new ReplaceProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        requestDto.setName("Replaced Name");
        requestDto.setDescription("Replaced Description");
        requestDto.setImageUrl("http://example.com/image3.jpg");
        requestDto.setPrice(300.0);
        requestDto.setCurrency("USD");
        requestDto.setCategoryName("Electronics");

        Mockito.when(productService.replaceProduct(anyString(), any(Product.class)))
                .thenThrow(new ProductNotFoundException("Not found"));

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }
}
