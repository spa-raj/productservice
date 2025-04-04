package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.product.*;
import com.vibevault.productservice.exceptions.products.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.products.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
public class ProductControllerTest {

    @Autowired
    private ProductController productController;

    @MockitoBean
    private ProductService productService;

    @Captor
    private ArgumentCaptor<String> idCaptor;

    @BeforeEach
    public void setup() {
        // This method is called before each test
        // You can initialize common objects or mock behaviors here if needed
        initiaLizeProducts();
    }
    private void initiaLizeProducts() {
        // Initialize any common objects or mock behaviors here
        // For example, you can set up a mock product object
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setImageUrl("http://test.com/image.jpg");
        product.setPrice(new Price(99.99, Currency.USD));
        Category category = new Category();
        category.setName("Test Category");
        product.setCategory(category);
    }

    @Test
    public void test_create_product_success_WhenValidProductObjectIsPassed_ReturnsSuccesfullyCreatedProduct() throws ProductNotCreatedException {
        // Arrange

        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Test Product");
        requestDto.setDescription("Test Description");
        requestDto.setImageUrl("http://test.com/image.jpg");
        requestDto.setPrice(99.99);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Test Category");

        Product product = requestDto.toProduct();
        UUID id = UUID.randomUUID();
        product.setId(id);

        when(productService.createProduct(any(Product.class))).thenReturn(product);


        // Act
        CreateProductResponseDto responseDto = productController.createProduct(requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals(id.toString(), responseDto.getId());
        assertEquals("Test Product", responseDto.getName());
        assertEquals("Test Description", responseDto.getDescription());
        assertEquals("http://test.com/image.jpg", responseDto.getImageUrl());
        assertEquals("Test Category", responseDto.getCategoryName());
        assertEquals(99.99, responseDto.getPrice().getPrice());
        assertEquals(Currency.USD, responseDto.getPrice().getCurrency());

        verify(productService, times(1)).createProduct(any(Product.class));
    }


    @Test
    public void test_get_product_success_When_Valid_Product_Id_Is_Passed_Returns_Product() throws ProductNotFoundException {
        // Arrange
        String productId = UUID.randomUUID().toString();
        Product product = new Product();
        product.setId(UUID.fromString(productId));
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setImageUrl("http://test.com/image.jpg");
        product.setPrice(new Price(99.99, Currency.USD));
        Category category = new Category();
        category.setName("Test Category");
        product.setCategory(category);

        when(productService.getProductById(anyString())).thenReturn(product);

        // Act
        GetProductResponseDto responseDto = productController.getProductById(productId);

        // Assert
        assertNotNull(responseDto);
        assertEquals(productId, responseDto.getId());
        assertEquals("Test Product", responseDto.getName());
        assertEquals("Test Description", responseDto.getDescription());
        assertEquals("http://test.com/image.jpg", responseDto.getImageUrl());
        assertEquals("Test Category", responseDto.getCategoryName());
        assertEquals(99.99, responseDto.getPrice().getPrice());
        assertEquals(Currency.USD, responseDto.getPrice().getCurrency());

        verify(productService, times(1)).getProductById(idCaptor.capture());
        assertEquals(productId, idCaptor.getValue());
    }

    @Test
    public void test_get_product_throws_ProductNotFoundException_When_Invalid_Product_Id_Is_Passed() throws ProductNotFoundException {
        // Arrange
        String invalidProductId = "invalid-id";

        when(productService.getProductById(anyString()))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // Act & Assert
        ProductNotFoundException exception=assertThrows(ProductNotFoundException.class, () -> {
            productController.getProductById(invalidProductId);
        });
        assertEquals("Product not found", exception.getMessage());

        verify(productService, times(1)).getProductById(idCaptor.capture());
        assertEquals(invalidProductId, idCaptor.getValue());
    }
}
