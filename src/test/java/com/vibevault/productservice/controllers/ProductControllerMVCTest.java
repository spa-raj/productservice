package com.vibevault.productservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibevault.productservice.dtos.product.CreateProductRequestDto;
import com.vibevault.productservice.dtos.product.CreateProductResponseDto;
import com.vibevault.productservice.dtos.product.GetProductResponseDto;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ProductControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Product> products;
    private List<Category> categories;
    @BeforeEach
    public void setup() {
        // This method is called before each test
        // You can initialize common objects or mock behaviors here if needed
        initializeCategories();
        initializeProducts();
    }
    private void initializeProducts() {
        products = new ArrayList<>();

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setImageUrl("http://test.com/image.jpg");
        product.setPrice(new Price(99.99, Currency.USD));
        product.setCategory(categories.getFirst());
        products.add(product);

        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setName("Test Product 2");
        product2.setDescription("Test Description 2");
        product2.setImageUrl("http://test.com/image2.jpg");
        product2.setPrice(new Price(199.99, Currency.EUR));
        product2.setCategory(categories.get(1));
        products.add(product2);

        Product product3 = new Product();
        product3.setId(UUID.randomUUID());
        product3.setName("Test Product 3");
        product3.setDescription("Test Description 3");
        product3.setImageUrl("http://test.com/image3.jpg");
        product3.setPrice(new Price(299.99, Currency.INR));
        product3.setCategory(categories.getFirst());
        products.add(product3);
    }
    private void initializeCategories() {
        categories=new ArrayList<>();
        Category category1 = new Category();
        category1.setId(UUID.randomUUID());
        category1.setName("Test Category");
        category1.setDescription("Test Category Description");
        categories.add(category1);

        Category category2 = new Category();
        category2.setId(UUID.randomUUID());
        category2.setName("Test Category 2");
        category2.setDescription("Test Category Description 2");
        categories.add(category2);

    }
    @Test
    public void test_getAllProducts_success() throws Exception {
        // Mock the behavior of the productService to return a list of products
        when(productService.getAllProducts()).thenReturn(products);

        List<GetProductResponseDto> productResponseDtos = GetProductResponseDto.fromProducts(products);
        mockMvc.perform(MockMvcRequestBuilders.get("/products"))
                .andExpect(status().isOk())
                .andExpect( content().string(objectMapper.writeValueAsString(productResponseDtos)));
    }

    @Test
    public void test_createProduct_success() throws Exception {
        // Mock the behavior of the productService to return a created product
        Product toBeCreatedproduct = getToBeCreatedproduct();

        Product createdProduct = products.get(0);
        when(productService.createProduct(toBeCreatedproduct)).thenReturn(createdProduct);

        CreateProductRequestDto requestDto = getCreateProductRequestDto(toBeCreatedproduct);

        CreateProductResponseDto responseDto = CreateProductResponseDto.fromProduct(createdProduct);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
//                .andExpect(jsonPath("$.id").value(createdProduct.getId().toString()));
    }

    private CreateProductRequestDto getCreateProductRequestDto(Product toBeCreatedproduct) {
        CreateProductRequestDto requestDto =new CreateProductRequestDto();
        requestDto.setName(toBeCreatedproduct.getName());
        requestDto.setDescription(toBeCreatedproduct.getDescription());
        requestDto.setImageUrl(toBeCreatedproduct.getImageUrl());
        requestDto.setPrice(toBeCreatedproduct.getPrice().getPrice());
        requestDto.setCurrency(toBeCreatedproduct.getPrice().getCurrency());
        requestDto.setCategoryName(toBeCreatedproduct.getCategory().getName());
        return requestDto;
    }

    private Product getToBeCreatedproduct() {
        Product toBeCreatedproduct =  new Product();
        toBeCreatedproduct.setName(products.get(0).getName());
        toBeCreatedproduct.setDescription(products.get(0).getDescription());
        toBeCreatedproduct.setImageUrl(products.get(0).getImageUrl());
        toBeCreatedproduct.setPrice(products.get(0).getPrice());
        toBeCreatedproduct.setCategory(products.get(0).getCategory());
        return toBeCreatedproduct;
    }
}
