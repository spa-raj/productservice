package com.vibevault.productservice.controllers;

import com.vibevault.productservice.config.TestConfig;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest(classes = {ProductController.class, TestConfig.class})
public class ProductControllerTest {

    @Autowired
    private ProductController productController;

    @MockitoBean
    private ProductService productService;

    private List<Product> products;
    private List<Category> categories;

    @BeforeEach
    public void setup() {
        initializeCategories();
        initializeProducts();
    }

    private void initializeProducts() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setImageUrl("http://test.com/image.jpg");
        product.setPrice(new Price(99.99, Currency.USD));
        product.setCategory(categories.get(0));

        products = List.of(product);
    }

    private void initializeCategories() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Test Category");
        category.setDescription("Test Category Description");

        categories = List.of(category);
    }

    // ==================== CREATE PRODUCT TESTS ====================

    @Test
    @WithMockUser(roles = "SELLER")
    public void test_create_product_success_WhenSellerRole_CreatesProduct() throws Exception {
        // Arrange
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Smartphone");
        requestDto.setDescription("Latest model smartphone");
        requestDto.setImageUrl("http://test.com/smartphone.jpg");
        requestDto.setPrice(499.99);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Electronics");

        Product createdProduct = new Product();
        createdProduct.setId(UUID.randomUUID());
        createdProduct.setName("Smartphone");
        createdProduct.setDescription("Latest model smartphone");
        createdProduct.setImageUrl("http://test.com/smartphone.jpg");
        createdProduct.setPrice(new Price(499.99, Currency.USD));
        createdProduct.setCategory(categories.get(0));

        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        // Act
        CreateProductResponseDto responseDto = productController.createProduct(requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals(createdProduct.getId().toString(), responseDto.getId());
        assertEquals(createdProduct.getName(), responseDto.getName());
        assertEquals(createdProduct.getDescription(), responseDto.getDescription());
        assertEquals(createdProduct.getImageUrl(), responseDto.getImageUrl());
        assertEquals(createdProduct.getPrice().getPrice(), responseDto.getPrice().getPrice());
        assertEquals(createdProduct.getPrice().getCurrency(), responseDto.getPrice().getCurrency());
        assertEquals(createdProduct.getCategory().getName(), responseDto.getCategoryName());

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void test_create_product_success_WhenAdminRole_CreatesProduct() throws Exception {
        // Arrange
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Laptop");
        requestDto.setDescription("High-end laptop");
        requestDto.setImageUrl("http://test.com/laptop.jpg");
        requestDto.setPrice(1299.99);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Electronics");

        Product createdProduct = new Product();
        createdProduct.setId(UUID.randomUUID());
        createdProduct.setName("Laptop");
        createdProduct.setDescription("High-end laptop");
        createdProduct.setImageUrl("http://test.com/laptop.jpg");
        createdProduct.setPrice(new Price(1299.99, Currency.USD));
        createdProduct.setCategory(categories.get(0));

        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        // Act
        CreateProductResponseDto responseDto = productController.createProduct(requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals(createdProduct.getName(), responseDto.getName());
        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void test_create_product_throws_AccessDeniedException_WhenCustomerRole() throws ProductNotCreatedException {
        // Arrange
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Unauthorized Product");
        requestDto.setDescription("Should not be created");
        requestDto.setImageUrl("http://test.com/unauth.jpg");
        requestDto.setPrice(10.0);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Test Category");

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            productController.createProduct(requestDto);
        });

        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    public void test_create_product_throws_ProductNotCreatedException_WhenServiceFails() throws Exception {
        // Arrange
        CreateProductRequestDto requestDto = new CreateProductRequestDto();
        requestDto.setName("Fail Product");
        requestDto.setDescription("Should fail");
        requestDto.setImageUrl("http://test.com/fail.jpg");
        requestDto.setPrice(10.0);
        requestDto.setCurrency(Currency.USD);
        requestDto.setCategoryName("Test Category");

        when(productService.createProduct(any(Product.class)))
                .thenThrow(new ProductNotCreatedException("Creation failed"));

        // Act & Assert
        ProductNotCreatedException ex = assertThrows(ProductNotCreatedException.class, () -> {
            productController.createProduct(requestDto);
        });
        assertEquals("Creation failed", ex.getMessage());
        verify(productService, times(1)).createProduct(any(Product.class));
    }

    // ==================== UPDATE PRODUCT TESTS ====================

    @Test
    @WithMockUser(roles = "SELLER")
    public void test_update_product_success_WhenSellerRole_UpdatesProduct() throws Exception {
        // Arrange
        String productId = UUID.randomUUID().toString();
        UpdateProductRequestDto updateDto = new UpdateProductRequestDto();
        updateDto.setId(productId);
        updateDto.setName("Updated Product");
        updateDto.setDescription("Updated Description");
        updateDto.setImageUrl("http://test.com/updated.jpg");
        updateDto.setPrice(199.99);
        updateDto.setCurrency(Currency.USD);
        updateDto.setCategoryName("Test Category");

        Product updatedProduct = products.get(0);

        when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(updatedProduct);

        // Act
        UpdateProductResponseDto responseDto = productController.updateProduct(productId, updateDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals(updatedProduct.getId().toString(), responseDto.getId());
        assertEquals(updatedProduct.getName(), responseDto.getName());
        assertEquals(updatedProduct.getDescription(), responseDto.getDescription());
        assertEquals(updatedProduct.getImageUrl(), responseDto.getImageUrl());
        assertEquals(updatedProduct.getCategory().getName(), responseDto.getCategoryName());
        assertEquals(updatedProduct.getPrice().getPrice(), responseDto.getPrice().getPrice());
        assertEquals(updatedProduct.getPrice().getCurrency(), responseDto.getPrice().getCurrency());

        verify(productService, times(1)).updateProduct(eq(productId), any(Product.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void test_update_product_throws_AccessDeniedException_WhenCustomerRole() throws ProductNotFoundException {
        // Arrange
        String productId = UUID.randomUUID().toString();
        UpdateProductRequestDto updateDto = new UpdateProductRequestDto();
        updateDto.setId(productId);
        updateDto.setName("Unauthorized Update");

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            productController.updateProduct(productId, updateDto);
        });

        verify(productService, never()).updateProduct(anyString(), any(Product.class));
    }

    // ==================== DELETE PRODUCT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void test_delete_product_success_WhenAdminRole_DeletesProduct() throws Exception {
        // Arrange
        String productId = UUID.randomUUID().toString();
        Product deletedProduct = products.get(0);

        when(productService.deleteProduct(eq(productId))).thenReturn(deletedProduct);

        // Act
        DeleteProductResponseDto responseDto = productController.deleteProduct(productId);

        // Assert
        assertNotNull(responseDto);
        assertEquals(deletedProduct.getId().toString(), responseDto.getId());
        assertEquals(deletedProduct.getName(), responseDto.getName());
        assertEquals(deletedProduct.getDescription(), responseDto.getDescription());
        assertEquals(deletedProduct.getImageUrl(), responseDto.getImageUrl());
        assertEquals(deletedProduct.getCategory().getName(), responseDto.getCategoryName());
        assertEquals(deletedProduct.getPrice().getPrice(), responseDto.getPrice().getPrice());
        assertEquals(deletedProduct.getPrice().getCurrency(), responseDto.getPrice().getCurrency());

        verify(productService, times(1)).deleteProduct(eq(productId));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    public void test_delete_product_success_WhenSellerRole_DeletesProduct() throws Exception {
        // Arrange
        String productId = UUID.randomUUID().toString();
        Product deletedProduct = products.get(0);

        when(productService.deleteProduct(eq(productId))).thenReturn(deletedProduct);

        // Act
        DeleteProductResponseDto responseDto = productController.deleteProduct(productId);

        // Assert
        assertNotNull(responseDto);
        verify(productService, times(1)).deleteProduct(eq(productId));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void test_delete_product_throws_AccessDeniedException_WhenCustomerRole() throws ProductNotFoundException, ProductNotDeletedException {
        // Arrange
        String productId = UUID.randomUUID().toString();

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            productController.deleteProduct(productId);
        });

        verify(productService, never()).deleteProduct(anyString());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    public void test_delete_product_throws_ProductNotDeletedException_WhenDeletionFails() throws Exception {
        // Arrange
        String productId = UUID.randomUUID().toString();
        when(productService.deleteProduct(eq(productId)))
                .thenThrow(new ProductNotDeletedException("Could not delete product"));

        // Act & Assert
        ProductNotDeletedException ex = assertThrows(ProductNotDeletedException.class, () -> {
            productController.deleteProduct(productId);
        });
        assertEquals("Could not delete product", ex.getMessage());
        verify(productService, times(1)).deleteProduct(eq(productId));
    }

    // ==================== REPLACE PRODUCT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void test_replace_product_success_WhenAdminRole_ReplacesProduct() throws Exception {
        // Arrange
        String productId = UUID.randomUUID().toString();
        ReplaceProductRequestDto replaceDto = new ReplaceProductRequestDto();
        replaceDto.setId(productId);
        replaceDto.setName("Replaced Product");
        replaceDto.setDescription("Replaced Description");
        replaceDto.setImageUrl("http://test.com/replaced.jpg");
        replaceDto.setPrice(299.99);
        replaceDto.setCurrency("USD");
        replaceDto.setCategoryName("Test Category");

        Product replacedProduct = new Product();
        replacedProduct.setId(UUID.fromString(productId));
        replacedProduct.setName("Replaced Product");
        replacedProduct.setDescription("Replaced Description");
        replacedProduct.setImageUrl("http://test.com/replaced.jpg");
        replacedProduct.setPrice(new Price(299.99, Currency.USD));
        replacedProduct.setCategory(categories.get(0));

        when(productService.replaceProduct(eq(productId), any(Product.class))).thenReturn(replacedProduct);

        // Act
        ReplaceProductResponseDto responseDto = productController.replaceProduct(productId, replaceDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals(replacedProduct.getId().toString(), responseDto.getId());
        assertEquals(replacedProduct.getName(), responseDto.getName());
        assertEquals(replacedProduct.getDescription(), responseDto.getDescription());
        assertEquals(replacedProduct.getImageUrl(), responseDto.getImageUrl());
        assertEquals(replacedProduct.getCategory().getName(), responseDto.getCategoryName());
        assertEquals(replacedProduct.getPrice().getPrice(), responseDto.getPrice().getPrice());
        assertEquals(replacedProduct.getPrice().getCurrency(), responseDto.getPrice().getCurrency());
        verify(productService, times(1)).replaceProduct(eq(productId), any(Product.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void test_replace_product_throws_AccessDeniedException_WhenCustomerRole() throws ProductNotFoundException {
        // Arrange
        String productId = UUID.randomUUID().toString();
        ReplaceProductRequestDto replaceDto = new ReplaceProductRequestDto();
        replaceDto.setId(productId);
        replaceDto.setName("Unauthorized Replace");

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            productController.replaceProduct(productId, replaceDto);
        });

        verify(productService, never()).replaceProduct(anyString(), any(Product.class));
    }

    // ==================== GET PRODUCT TESTS (No Auth Required) ====================

    @Test
    public void test_get_product_by_id_success_WhenProductExists_ReturnsProduct() throws Exception {
        // Arrange
        String productId = products.get(0).getId().toString();
        when(productService.getProductById(productId)).thenReturn(products.get(0));

        // Act
        GetProductResponseDto responseDto = productController.getProductById(productId);

        // Assert
        assertNotNull(responseDto);
        assertEquals(products.get(0).getId().toString(), responseDto.getId());
        assertEquals(products.get(0).getName(), responseDto.getName());
        assertEquals(products.get(0).getDescription(), responseDto.getDescription());
        assertEquals(products.get(0).getImageUrl(), responseDto.getImageUrl());
        assertEquals(products.get(0).getCategory().getName(), responseDto.getCategoryName());
        assertEquals(products.get(0).getPrice().getPrice(), responseDto.getPrice().getPrice());
        assertEquals(products.get(0).getPrice().getCurrency(), responseDto.getPrice().getCurrency());
        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    public void test_get_product_by_id_throws_ProductNotFoundException_WhenProductDoesNotExist() throws Exception {
        // Arrange
        String nonExistentProductId = UUID.randomUUID().toString();
        when(productService.getProductById(nonExistentProductId))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // Act & Assert
        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class, () -> {
            productController.getProductById(nonExistentProductId);
        });
        assertEquals("Product not found", ex.getMessage());
        verify(productService, times(1)).getProductById(nonExistentProductId);
    }

    @Test
    public void test_get_all_products_success_WhenProductsExist_ReturnsProductList() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(products);

        // Act
        List<GetProductResponseDto> responseList = productController.getAllProducts();

        // Assert
        assertNotNull(responseList);
        assertEquals(products.size(), responseList.size());
        assertEquals(products.get(0).getName(), responseList.get(0).getName());
        assertEquals(products.get(0).getDescription(), responseList.get(0).getDescription());
        assertEquals(products.get(0).getImageUrl(), responseList.get(0).getImageUrl());
        assertEquals(products.get(0).getCategory().getName(), responseList.get(0).getCategoryName());
        assertEquals(products.get(0).getPrice().getPrice(), responseList.get(0).getPrice().getPrice());
        assertEquals(products.get(0).getPrice().getCurrency(), responseList.get(0).getPrice().getCurrency());

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    public void test_get_all_products_success_WhenNoProductsExist_ReturnsEmptyList() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        // Act
        List<GetProductResponseDto> responseList = productController.getAllProducts();

        // Assert
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
        verify(productService, times(1)).getAllProducts();
    }
}
