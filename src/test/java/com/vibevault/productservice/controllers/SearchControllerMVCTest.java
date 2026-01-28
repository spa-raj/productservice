package com.vibevault.productservice.controllers;

import com.vibevault.productservice.exceptions.search.InvalidSearchParameterException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.security.RolesClaimConverter;
import com.vibevault.productservice.security.SecurityConfig;
import com.vibevault.productservice.services.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
@Import({SecurityConfig.class, RolesClaimConverter.class})
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test-issuer.example.com"
})
class SearchControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Product sampleProduct;
    private List<Product> sampleProducts;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");

        Price price = new Price();
        price.setPrice(100.0);
        price.setCurrency(Currency.USD);

        sampleProduct = new Product();
        sampleProduct.setId(UUID.randomUUID());
        sampleProduct.setName("iPhone 14");
        sampleProduct.setDescription("Latest Apple smartphone");
        sampleProduct.setImageUrl("http://example.com/iphone.jpg");
        sampleProduct.setPrice(price);
        sampleProduct.setCategory(category);

        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setName("Samsung Galaxy");
        product2.setDescription("Latest Samsung smartphone");
        product2.setImageUrl("http://example.com/samsung.jpg");
        product2.setPrice(new Price(200.0, Currency.USD));
        product2.setCategory(category);

        sampleProducts = Arrays.asList(sampleProduct, product2);
    }

    // ==================== SEARCH PRODUCTS - BASIC TESTS ====================

    @Test
    void searchProducts_Success_NoFilters() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void searchProducts_Success_WithQuery() throws Exception {
        Page<Product> page = new PageImpl<>(Collections.singletonList(sampleProduct), PageRequest.of(0, 10), 1);
        Mockito.when(searchService.searchProducts(
                eq("iphone"), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("query", "iphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].name").value("iPhone 14"));
    }

    @Test
    void searchProducts_Success_WithPriceRange() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), eq(50.0), eq(300.0), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("minPrice", "50")
                        .param("maxPrice", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    @Test
    void searchProducts_Success_WithCurrency() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), eq(Currency.USD), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    @Test
    void searchProducts_Success_WithCategoryId() throws Exception {
        UUID categoryId = UUID.randomUUID();
        Page<Product> page = new PageImpl<>(Collections.singletonList(sampleProduct), PageRequest.of(0, 10), 1);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), eq(categoryId), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("categoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1));
    }

    @Test
    void searchProducts_Success_WithCategoryName() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), eq("Electronics"), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("categoryName", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    @Test
    void searchProducts_Success_WithDateRange() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(Date.class), any(Date.class),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("createdAfter", "2024-01-01")
                        .param("createdBefore", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    // ==================== SEARCH PRODUCTS - PAGINATION TESTS ====================

    @Test
    void searchProducts_Success_WithPagination() throws Exception {
        Page<Product> page = new PageImpl<>(Collections.singletonList(sampleProduct), PageRequest.of(1, 5), 10);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    @Test
    void searchProducts_Success_FirstPage() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 20);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    // ==================== SEARCH PRODUCTS - SORTING TESTS ====================

    @Test
    void searchProducts_Success_SortByName() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    @Test
    void searchProducts_Success_SortByPrice() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("sortBy", "price")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    @Test
    void searchProducts_Success_SortByCreatedAt() throws Exception {
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    void searchProducts_Fails_InvalidSortField() throws Exception {
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), eq("invalidField"), anyString()))
                .thenThrow(new InvalidSearchParameterException("Invalid sort field: invalidField"));

        mockMvc.perform(get("/search/products")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest());
    }

    // ==================== SEARCH PRODUCTS - COMBINED FILTERS ====================

    @Test
    void searchProducts_Success_AllFiltersCombined() throws Exception {
        Page<Product> page = new PageImpl<>(Collections.singletonList(sampleProduct), PageRequest.of(0, 10), 1);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products")
                        .param("query", "iphone")
                        .param("minPrice", "100")
                        .param("maxPrice", "500")
                        .param("currency", "USD")
                        .param("categoryName", "Electronics")
                        .param("createdAfter", "2024-01-01")
                        .param("createdBefore", "2024-12-31")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "price")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1));
    }

    // ==================== SEARCH PRODUCTS - ERROR HANDLING ====================

    @Test
    void searchProducts_Fails_ServiceThrowsException() throws Exception {
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new InvalidSearchParameterException("minPrice cannot be negative"));

        mockMvc.perform(get("/search/products")
                        .param("minPrice", "-10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchProducts_ReturnsEmptyResults() throws Exception {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/search/products")
                        .param("query", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ==================== SEARCH PRODUCTS - SECURITY TESTS ====================

    @Test
    void searchProducts_Success_WithoutAuthentication() throws Exception {
        // Search endpoints should be public
        Page<Product> page = new PageImpl<>(sampleProducts, PageRequest.of(0, 10), 2);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products"))
                .andExpect(status().isOk());
    }

    // ==================== SUGGESTIONS TESTS ====================

    @Test
    void getSuggestions_Success() throws Exception {
        List<Product> suggestions = Arrays.asList(
                createProductWithName("iPhone 14"),
                createProductWithName("iPhone 13"),
                createProductWithName("iPhone SE")
        );
        Mockito.when(searchService.getSuggestions("iph", 5)).thenReturn(suggestions);

        mockMvc.perform(get("/search/products/suggest")
                        .param("prefix", "iph")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("iPhone 14"))
                .andExpect(jsonPath("$[0].categoryName").value("Electronics"));
    }

    @Test
    void getSuggestions_Success_DefaultLimit() throws Exception {
        List<Product> suggestions = Collections.singletonList(createProductWithName("iPhone 14"));
        Mockito.when(searchService.getSuggestions("iph", 5)).thenReturn(suggestions);

        mockMvc.perform(get("/search/products/suggest")
                        .param("prefix", "iph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSuggestions_ReturnsEmptyList() throws Exception {
        Mockito.when(searchService.getSuggestions("xyz", 5)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/search/products/suggest")
                        .param("prefix", "xyz")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getSuggestions_Success_WithoutAuthentication() throws Exception {
        // Suggestions endpoint should be public
        Mockito.when(searchService.getSuggestions(any(), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/search/products/suggest")
                        .param("prefix", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void getSuggestions_Fails_MissingPrefix() throws Exception {
        mockMvc.perform(get("/search/products/suggest"))
                .andExpect(status().isBadRequest());
    }

    // ==================== RESPONSE FORMAT TESTS ====================

    @Test
    void searchProducts_ResponseContainsAllFields() throws Exception {
        Page<Product> page = new PageImpl<>(Collections.singletonList(sampleProduct), PageRequest.of(0, 10), 1);
        Mockito.when(searchService.searchProducts(
                any(), any(), any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/search/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].id").exists())
                .andExpect(jsonPath("$.products[0].name").value("iPhone 14"))
                .andExpect(jsonPath("$.products[0].description").value("Latest Apple smartphone"))
                .andExpect(jsonPath("$.products[0].imageUrl").exists())
                .andExpect(jsonPath("$.products[0].price").exists())
                .andExpect(jsonPath("$.products[0].price.price").value(100.0))
                .andExpect(jsonPath("$.products[0].price.currency").value("USD"))
                .andExpect(jsonPath("$.products[0].categoryName").value("Electronics"));
    }

    @Test
    void getSuggestions_ResponseContainsAllFields() throws Exception {
        List<Product> suggestions = Collections.singletonList(sampleProduct);
        Mockito.when(searchService.getSuggestions("iph", 5)).thenReturn(suggestions);

        mockMvc.perform(get("/search/products/suggest")
                        .param("prefix", "iph")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("iPhone 14"))
                .andExpect(jsonPath("$[0].categoryName").value("Electronics"));
    }

    // ==================== HELPER METHODS ====================

    private Product createProductWithName(String name) {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName(name);
        product.setDescription("Description for " + name);
        product.setImageUrl("http://example.com/" + name.toLowerCase().replace(" ", "-") + ".jpg");
        product.setPrice(new Price(100.0, Currency.USD));
        product.setCategory(category);
        return product;
    }
}
