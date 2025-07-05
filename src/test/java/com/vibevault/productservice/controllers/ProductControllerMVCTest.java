package com.vibevault.productservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibevault.productservice.commons.AuthenticationCommons;
import com.vibevault.productservice.dtos.commons.UserDto;
import com.vibevault.productservice.dtos.product.*;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private AuthenticationCommons authenticationCommons;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto sellerUser;
    private UserDto adminUser;
    private UserDto buyerUser;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sellerUser = new UserDto("seller@example.com", "Seller", Collections.singletonList("SELLER"));
        adminUser = new UserDto("admin@example.com", "Admin", Collections.singletonList("ADMIN"));
        buyerUser = new UserDto("buyer@example.com", "Buyer", Collections.singletonList("BUYER"));

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

    @Test
    void createProduct_Success_AsSeller() throws Exception {
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Test Product");
        requestDto.setDescription("Test Description");
        requestDto.setImageUrl("http://example.com/image.jpg");
        requestDto.setPrice(100.0);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Electronics");

        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(sellerUser);
        Mockito.when(productService.createProduct(any(Product.class))).thenReturn(sampleProduct);

        mockMvc.perform(post("/products")
                .header("Authorization", "Bearer seller-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
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

        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(adminUser);
        Mockito.when(productService.createProduct(any(Product.class))).thenReturn(sampleProduct);

        mockMvc.perform(post("/products")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void createProduct_Fails_UnauthorizedRole() throws Exception {
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(buyerUser);

        mockMvc.perform(post("/products")
                .header("Authorization", "Bearer buyer-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createProduct_Fails_InvalidToken() throws Exception {
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(null);

        mockMvc.perform(post("/products")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }

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

        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(adminUser);
        Mockito.when(productService.updateProduct(eq(sampleProduct.getId().toString()), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(patch("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateProduct_Fails_UnauthorizedRole() throws Exception {
        UpdateProductRequestDto requestDto = new UpdateProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(buyerUser);

        mockMvc.perform(patch("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer buyer-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateProduct_Fails_InvalidToken() throws Exception {
        UpdateProductRequestDto requestDto = new UpdateProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(null);

        mockMvc.perform(patch("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getProductById_Success() throws Exception {
        Mockito.when(productService.getProductById(sampleProduct.getId().toString())).thenReturn(sampleProduct);

        mockMvc.perform(get("/products/" + sampleProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductById_Fails_NotFound() throws Exception {
        Mockito.when(productService.getProductById(anyString())).thenThrow(new com.vibevault.productservice.exceptions.products.ProductNotFoundException("Not found"));

        mockMvc.perform(get("/products/" + sampleProduct.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllProducts_Success() throws Exception {
        Mockito.when(productService.getAllProducts()).thenReturn(Collections.singletonList(sampleProduct));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void getAllProducts_Fails_NotFound() throws Exception {
        Mockito.when(productService.getAllProducts()).thenThrow(new com.vibevault.productservice.exceptions.products.ProductNotFoundException("Not found"));

        mockMvc.perform(get("/products"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteProduct_Success_AsSeller() throws Exception {
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(sellerUser);
        Mockito.when(productService.deleteProduct(sampleProduct.getId().toString())).thenReturn(sampleProduct);

        mockMvc.perform(delete("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer seller-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void deleteProduct_Fails_UnauthorizedRole() throws Exception {
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(buyerUser);

        mockMvc.perform(delete("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer buyer-token"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteProduct_Fails_InvalidToken() throws Exception {
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(null);

        mockMvc.perform(delete("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteProduct_Fails_NotFound() throws Exception {
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(adminUser);
        Mockito.when(productService.deleteProduct(anyString())).thenThrow(new com.vibevault.productservice.exceptions.products.ProductNotFoundException("Not found"));

        mockMvc.perform(delete("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().is4xxClientError());
    }

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

        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(adminUser);
        Mockito.when(productService.replaceProduct(eq(sampleProduct.getId().toString()), any(Product.class))).thenReturn(replacedProduct);

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Replaced Name"));
    }

    @Test
    void replaceProduct_Fails_UnauthorizedRole() throws Exception {
        ReplaceProductRequestDto requestDto = new ReplaceProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(buyerUser);

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer buyer-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void replaceProduct_Fails_InvalidToken() throws Exception {
        ReplaceProductRequestDto requestDto = new ReplaceProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(null);

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void replaceProduct_Fails_NotFound() throws Exception {
        ReplaceProductRequestDto requestDto = new ReplaceProductRequestDto();
        requestDto.setId(sampleProduct.getId().toString());
        requestDto.setName("Replaced Name");
        requestDto.setDescription("Replaced Description");
        requestDto.setImageUrl("http://example.com/image3.jpg");
        requestDto.setPrice(300.0);
        requestDto.setCurrency("USD"); // This was the null field causing the issue
        requestDto.setCategoryName("Electronics");

        Mockito.when(authenticationCommons.validateToken(anyString())).thenReturn(adminUser);
        Mockito.when(productService.replaceProduct(anyString(), any(Product.class)))
                .thenThrow(new com.vibevault.productservice.exceptions.products.ProductNotFoundException("Not found"));

        mockMvc.perform(put("/products/" + sampleProduct.getId())
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }
}
