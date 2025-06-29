//package com.vibevault.productservice.controllers;
//
//import com.vibevault.productservice.config.TestConfig;
//import com.vibevault.productservice.dtos.product.*;
//import com.vibevault.productservice.exceptions.products.ProductNotCreatedException;
//import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
//import com.vibevault.productservice.models.Category;
//import com.vibevault.productservice.models.Currency;
//import com.vibevault.productservice.models.Price;
//import com.vibevault.productservice.models.Product;
//import com.vibevault.productservice.services.ProductService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayNameGeneration;
//import org.junit.jupiter.api.DisplayNameGenerator;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
//@SpringBootTest(classes = {ProductController.class, TestConfig.class})
//public class ProductControllerTest {
//
//    @Autowired
//    private ProductController productController;
//
//    @MockitoBean
//    private ProductService productService;
//
//    @Captor
//    private ArgumentCaptor<String> idCaptor;
//
//    private List<Product> products;
//    private List<Category> categories;
//    @BeforeEach
//    public void setup() {
//        // This method is called before each test
//        // You can initialize common objects or mock behaviors here if needed
//        initializeCategories();
//        initializeProducts();
//    }
//    private void initializeProducts() {
//        // Initialize any common objects or mock behaviors here
//        // For example, you can set up a mock product object
//        Product product = new Product();
//        product.setId(UUID.randomUUID());
//        product.setName("Test Product");
//        product.setDescription("Test Description");
//        product.setImageUrl("http://test.com/image.jpg");
//        product.setPrice(new Price(99.99, Currency.USD));
//        product.setCategory(categories.get(0));
//
//        products = List.of(product);
//    }
//    private void initializeCategories() {
//        Category category = new Category();
//        category.setId(UUID.randomUUID());
//        category.setName("Test Category");
//        category.setDescription("Test Category Description");
//
//        categories = List.of(category);
//    }
//
//    @Test
//    public void test_create_product_success_WhenValidProductObjectIsPassed_ReturnsSuccesfullyCreatedProduct() throws ProductNotCreatedException {
//        // Arrange
//
//        CreateProductRequestDto requestDto = new CreateProductRequestDto();
//        requestDto.setName("Test Product");
//        requestDto.setDescription("Test Description");
//        requestDto.setImageUrl("http://test.com/image.jpg");
//        requestDto.setPrice(99.99);
//        requestDto.setCurrency(Currency.USD);
//        requestDto.setCategoryName("Test Category");
//
//        Product product = products.getFirst();
//        when(productService.createProduct(any(Product.class))).thenReturn(product);
//
//
//        // Act
//        CreateProductResponseDto responseDto = productController.createProduct(requestDto);
//
//        // Assert
//        assertNotNull(responseDto);
//        assertEquals(product.getId().toString(), responseDto.getId());
//        assertEquals(product.getName(), responseDto.getName());
//        assertEquals(product.getDescription(), responseDto.getDescription());
//        assertEquals(product.getImageUrl(), responseDto.getImageUrl());
//        assertEquals(product.getCategory().getName(), responseDto.getCategoryName());
//        assertEquals(product.getPrice().getPrice(), responseDto.getPrice().getPrice());
//        assertEquals(product.getPrice().getCurrency(), responseDto.getPrice().getCurrency());
//
//        verify(productService, times(1)).createProduct(any(Product.class));
//    }
//
//
//    @Test
//    public void test_get_product_success_When_Valid_Product_Id_Is_Passed_Returns_Product() throws ProductNotFoundException {
//        // Arrange
//        Product product = products.getFirst();
//
//        when(productService.getProductById(anyString())).thenReturn(product);
//
//        // Act
//        GetProductResponseDto responseDto = productController.getProductById(product.getId().toString());
//
//        // Assert
//        assertNotNull(responseDto);
//        assertEquals(product.getName(), responseDto.getName());
//        assertEquals(product.getDescription(), responseDto.getDescription());
//        assertEquals(product.getImageUrl(), responseDto.getImageUrl());
//        assertEquals(product.getCategory().getName(), responseDto.getCategoryName());
//        assertEquals(product.getPrice().getPrice(), responseDto.getPrice().getPrice());
//        assertEquals(product.getPrice().getCurrency(), responseDto.getPrice().getCurrency());
//
//        verify(productService, times(1)).getProductById(idCaptor.capture());
//        assertEquals(product.getId().toString(), idCaptor.getValue());
//    }
//
//    @Test
//    public void test_get_product_throws_ProductNotFoundException_When_Invalid_Product_Id_Is_Passed() throws ProductNotFoundException {
//        // Arrange
//        String invalidProductId = "invalid-id";
//
//        when(productService.getProductById(anyString()))
//                .thenThrow(new ProductNotFoundException("Product not found"));
//
//        // Act & Assert
//        ProductNotFoundException exception=assertThrows(ProductNotFoundException.class, () -> {
//            productController.getProductById(invalidProductId);
//        });
//        assertEquals("Product not found", exception.getMessage());
//
//        verify(productService, times(1)).getProductById(idCaptor.capture());
//        assertEquals(invalidProductId, idCaptor.getValue());
//    }
//}
