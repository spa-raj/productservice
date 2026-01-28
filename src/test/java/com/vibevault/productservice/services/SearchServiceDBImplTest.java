package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.search.InvalidSearchParameterException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchServiceDBImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SearchServiceDBImpl searchService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ==================== SEARCH PRODUCTS TESTS ====================

    @Test
    void searchProducts_shouldReturnPagedResults() throws InvalidSearchParameterException {
        List<Product> products = Arrays.asList(getSampleProduct(), getSampleProduct());
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 2);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Product> result = searchService.searchProducts(
                "test", null, null, null, null, null, null, null,
                0, 10, "createdAt", "desc");

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchProducts_shouldReturnEmptyPage_whenNoMatches() throws InvalidSearchParameterException {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        Page<Product> result = searchService.searchProducts(
                "nonexistent", null, null, null, null, null, null, null,
                0, 10, "createdAt", "desc");

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void searchProducts_shouldWorkWithAllFilters() throws InvalidSearchParameterException {
        List<Product> products = Collections.singletonList(getSampleProduct());
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        UUID categoryId = UUID.randomUUID();
        Date createdAfter = new Date(System.currentTimeMillis() - 86400000);
        Date createdBefore = new Date();

        Page<Product> result = searchService.searchProducts(
                "iphone", 100.0, 500.0, Currency.USD, categoryId, "Electronics",
                createdAfter, createdBefore, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchProducts_shouldWorkWithNullQuery() throws InvalidSearchParameterException {
        List<Product> products = Arrays.asList(getSampleProduct(), getSampleProduct());
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 2);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Product> result = searchService.searchProducts(
                null, null, null, null, null, null, null, null,
                0, 10, "createdAt", "desc");

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void searchProducts_shouldWorkWithOnlyPriceFilter() throws InvalidSearchParameterException {
        List<Product> products = Collections.singletonList(getSampleProduct());
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Product> result = searchService.searchProducts(
                null, 50.0, 200.0, null, null, null, null, null,
                0, 10, "price", "asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchProducts_shouldWorkWithOnlyCategoryFilter() throws InvalidSearchParameterException {
        List<Product> products = Collections.singletonList(getSampleProduct());
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Product> result = searchService.searchProducts(
                null, null, null, null, null, "Electronics", null, null,
                0, 10, "createdAt", "desc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    void searchProducts_shouldThrowException_whenMinPriceNegative() {
        InvalidSearchParameterException exception = assertThrows(
                InvalidSearchParameterException.class,
                () -> searchService.searchProducts(null, -10.0, null, null, null, null, null, null,
                        0, 10, "createdAt", "desc")
        );

        assertEquals("minPrice cannot be negative", exception.getMessage());
    }

    @Test
    void searchProducts_shouldThrowException_whenMaxPriceNegative() {
        InvalidSearchParameterException exception = assertThrows(
                InvalidSearchParameterException.class,
                () -> searchService.searchProducts(null, null, -10.0, null, null, null, null, null,
                        0, 10, "createdAt", "desc")
        );

        assertEquals("maxPrice cannot be negative", exception.getMessage());
    }

    @Test
    void searchProducts_shouldThrowException_whenMinPriceGreaterThanMaxPrice() {
        InvalidSearchParameterException exception = assertThrows(
                InvalidSearchParameterException.class,
                () -> searchService.searchProducts(null, 500.0, 100.0, null, null, null, null, null,
                        0, 10, "createdAt", "desc")
        );

        assertEquals("minPrice cannot be greater than maxPrice", exception.getMessage());
    }

    @Test
    void searchProducts_shouldThrowException_whenCreatedAfterIsAfterCreatedBefore() {
        Date futureDate = new Date(System.currentTimeMillis() + 86400000);
        Date pastDate = new Date(System.currentTimeMillis() - 86400000);

        InvalidSearchParameterException exception = assertThrows(
                InvalidSearchParameterException.class,
                () -> searchService.searchProducts(null, null, null, null, null, null, futureDate, pastDate,
                        0, 10, "createdAt", "desc")
        );

        assertEquals("createdAfter cannot be after createdBefore", exception.getMessage());
    }

    @Test
    void searchProducts_shouldThrowException_whenPageSizeExceedsMax() {
        InvalidSearchParameterException exception = assertThrows(
                InvalidSearchParameterException.class,
                () -> searchService.searchProducts(null, null, null, null, null, null, null, null,
                        0, 150, "createdAt", "desc")
        );

        assertEquals("Page size cannot exceed 100", exception.getMessage());
    }

    @Test
    void searchProducts_shouldAllowValidPriceRange() throws InvalidSearchParameterException {
        Page<Product> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Should not throw - min equals max is valid
        assertDoesNotThrow(() -> searchService.searchProducts(
                null, 100.0, 100.0, null, null, null, null, null,
                0, 10, "createdAt", "desc"));
    }

    @Test
    void searchProducts_shouldAllowZeroPrices() throws InvalidSearchParameterException {
        Page<Product> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Zero is a valid price
        assertDoesNotThrow(() -> searchService.searchProducts(
                null, 0.0, 100.0, null, null, null, null, null,
                0, 10, "createdAt", "desc"));
    }

    // ==================== GET SUGGESTIONS TESTS ====================

    @Test
    void getSuggestions_shouldReturnMatchingProducts() {
        List<Product> products = Arrays.asList(
                createProductWithName("iPhone 14"),
                createProductWithName("iPhone 13"),
                createProductWithName("iPhone SE")
        );
        Page<Product> page = new PageImpl<>(products);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<Product> result = searchService.getSuggestions("iph", 5);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getSuggestions_shouldReturnEmptyList_whenNoMatches() {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        List<Product> result = searchService.getSuggestions("xyz", 5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSuggestions_shouldRespectLimit() {
        List<Product> products = Arrays.asList(
                createProductWithName("iPhone 14"),
                createProductWithName("iPhone 13")
        );
        Page<Product> page = new PageImpl<>(products);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        searchService.getSuggestions("iph", 2);

        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(2, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getSuggestions_shouldEnforceMaxLimit() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Request 100 but max is 20
        searchService.getSuggestions("test", 100);

        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(20, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getSuggestions_shouldEnforceMinLimit() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Request 0 but min is 1
        searchService.getSuggestions("test", 0);

        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(1, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getSuggestions_shouldSortByNameAscending() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        searchService.getSuggestions("test", 5);

        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        assertTrue(sort.getOrderFor("name").isAscending());
    }

    // ==================== SORT FIELD VALIDATION TESTS ====================

    @Test
    void searchProducts_shouldThrowException_whenSortFieldInvalid() {
        InvalidSearchParameterException exception = assertThrows(
                InvalidSearchParameterException.class,
                () -> searchService.searchProducts(null, null, null, null, null, null, null, null,
                        0, 10, "invalidField", "desc")
        );

        assertTrue(exception.getMessage().contains("Invalid sort field"));
    }

    @Test
    void searchProducts_shouldThrowException_whenSortFieldNull() {
        InvalidSearchParameterException exception = assertThrows(
                InvalidSearchParameterException.class,
                () -> searchService.searchProducts(null, null, null, null, null, null, null, null,
                        0, 10, null, "desc")
        );

        assertTrue(exception.getMessage().contains("Invalid sort field"));
    }

    @Test
    void searchProducts_shouldAcceptValidSortFields() throws InvalidSearchParameterException {
        Page<Product> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // All valid sort fields should work
        assertDoesNotThrow(() -> searchService.searchProducts(
                null, null, null, null, null, null, null, null,
                0, 10, "name", "asc"));
        assertDoesNotThrow(() -> searchService.searchProducts(
                null, null, null, null, null, null, null, null,
                0, 10, "price", "desc"));
        assertDoesNotThrow(() -> searchService.searchProducts(
                null, null, null, null, null, null, null, null,
                0, 10, "createdAt", "asc"));
        assertDoesNotThrow(() -> searchService.searchProducts(
                null, null, null, null, null, null, null, null,
                0, 10, "lastModifiedAt", "desc"));
    }

    // ==================== HELPER METHODS ====================

    private Product getSampleProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Sample Product");
        product.setDescription("Sample Description");
        product.setImageUrl("http://example.com/image.jpg");
        product.setPrice(new Price(100.0, Currency.USD));
        product.setCategory(getSampleCategory());
        product.setDeleted(false);
        return product;
    }

    private Product createProductWithName(String name) {
        Product product = getSampleProduct();
        product.setName(name);
        return product;
    }

    private Category getSampleCategory() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");
        return category;
    }
}
