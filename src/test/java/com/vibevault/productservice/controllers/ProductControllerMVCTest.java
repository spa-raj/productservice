//package com.vibevault.productservice.controllers;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vibevault.productservice.dtos.product.GetProductResponseDto;
//import com.vibevault.productservice.models.Category;
//import com.vibevault.productservice.models.Currency;
//import com.vibevault.productservice.models.Price;
//import com.vibevault.productservice.models.Product;
//import com.vibevault.productservice.services.ProductService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(ProductController.class)
//public class ProductControllerMVCTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ProductService productService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private List<Product> products;
//    private List<Category> categories;
//
//    @BeforeEach
//    public void setup() {
//        // This method is called before each test
//        // You can initialize common objects or mock behaviors here if needed
//        initializeCategories();
//        initializeProducts();
//    }
//
//    private void initializeProducts() {
//        products = new ArrayList<>();
//
//        Product product = new Product();
//        product.setId(UUID.randomUUID());
//        product.setName("Test Product");
//        product.setDescription("Test Description");
//        product.setImageUrl("http://test.com/image.jpg");
//        product.setPrice(new Price(99.99, Currency.USD));
//        product.setCategory(categories.get(0));
//        products.add(product);
//
//        Product product2 = new Product();
//        product2.setId(UUID.randomUUID());
//        product2.setName("Test Product 2");
//        product2.setDescription("Test Description 2");
//        product2.setImageUrl("http://test.com/image2.jpg");
//        product2.setPrice(new Price(199.99, Currency.EUR));
//        product2.setCategory(categories.get(1));
//        products.add(product2);
//
//        Product product3 = new Product();
//        product3.setId(UUID.randomUUID());
//        product3.setName("Test Product 3");
//        product3.setDescription("Test Description 3");
//        product3.setImageUrl("http://test.com/image3.jpg");
//        product3.setPrice(new Price(299.99, Currency.INR));
//        product3.setCategory(categories.get(0));
//        products.add(product3);
//    }
//
//    private void initializeCategories() {
//        categories = new ArrayList<>();
//        Category category1 = new Category();
//        category1.setId(UUID.randomUUID());
//        category1.setName("Test Category");
//        category1.setDescription("Test Category Description");
//        categories.add(category1);
//
//        Category category2 = new Category();
//        category2.setId(UUID.randomUUID());
//        category2.setName("Test Category 2");
//        category2.setDescription("Test Category Description 2");
//        categories.add(category2);
//    }
//
//    @Test
//    public void test_getAllProducts_success() throws Exception {
//        // Mock the behavior of the productService to return a list of products
//        when(productService.getAllProducts()).thenReturn(products);
//
//        // Convert products to DTOs as the controller would do
//        List<GetProductResponseDto> expectedDtos = GetProductResponseDto.fromProducts(products);
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/products"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(content().json(objectMapper.writeValueAsString(expectedDtos)));
//
//        // Alternative approach using jsonPath
//        // mockMvc.perform(MockMvcRequestBuilders.get("/products"))
//        //        .andExpect(status().isOk())
//        //        .andExpect(jsonPath("$", hasSize(3)))
//        //        .andExpect(jsonPath("$[0].name").value("Test Product"))
//        //        .andExpect(jsonPath("$[1].name").value("Test Product 2"))
//        //        .andExpect(jsonPath("$[2].name").value("Test Product 3"));
//    }
//}
