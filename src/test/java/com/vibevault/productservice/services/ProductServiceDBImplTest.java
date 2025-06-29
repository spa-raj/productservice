package com.vibevault.productservice.services;


import com.vibevault.productservice.exceptions.products.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.products.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.repositories.CategoryRepository;
import com.vibevault.productservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceDBImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceDBImpl productService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = org.mockito.MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProduct_shouldSaveProductWithCategory() throws ProductNotCreatedException {
        Product product = getSampleProduct();
        Category category = getSampleCategory();
        product.setCategory(category);

        when(categoryRepository.findByName(category.getName())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product saved = productService.createProduct(product);

        assertEquals(category, saved.getCategory());
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_shouldSaveProductWithNewCategory() throws ProductNotCreatedException {
        Product product = getSampleProduct();
        Category category = getSampleCategory();
        product.setCategory(category);

        when(categoryRepository.findByName(category.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(category)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product saved = productService.createProduct(product);

        assertEquals(category, saved.getCategory());
        verify(categoryRepository).save(category);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_shouldUpdateAllFields() throws ProductNotFoundException {
        UUID id = UUID.randomUUID();
        Product existing = getSampleProduct();
        existing.setId(id);
        Product update = new Product();
        update.setName("Updated");
        update.setDescription("Updated desc");
        update.setImageUrl("img2");
        update.setPrice(new Price(99.0, existing.getPrice().getCurrency()));
        Category newCategory = new Category();
        newCategory.setName("NewCat");
        update.setCategory(newCategory);

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("NewCat")).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(id.toString(), update);

        assertEquals("Updated", result.getName());
        assertEquals("Updated desc", result.getDescription());
        assertEquals("img2", result.getImageUrl());
        assertEquals(99.0, result.getPrice().getPrice());
        assertEquals(newCategory, result.getCategory());
    }

    @Test
    void updateProduct_shouldUpdatePartialFields() throws ProductNotFoundException {
        UUID id = UUID.randomUUID();
        Product existing = getSampleProduct();
        existing.setId(id);
        Product update = new Product();
        update.setName("Partial");
        // Only name is set

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(id.toString(), update);

        assertEquals("Partial", result.getName());
        assertEquals(existing.getDescription(), result.getDescription());
        assertEquals(existing.getImageUrl(), result.getImageUrl());
    }

    @Test
    void updateProduct_shouldThrowIfNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Product update = new Product();
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(id.toString(), update));
    }

    @Test
    void updateProduct_shouldThrowIfDeleted() {
        UUID id = UUID.randomUUID();
        Product deleted = getSampleProduct();
        deleted.setDeleted(true);
        when(productRepository.findById(id)).thenReturn(Optional.of(deleted));

        Product update = new Product();
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(id.toString(), update));
    }

    @Test
    void getProductById_shouldReturnProduct() throws ProductNotFoundException {
        UUID id = UUID.randomUUID();
        Product product = getSampleProduct();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(id.toString());
        assertEquals(product, result);
    }

    @Test
    void getProductById_shouldThrowIfNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(id.toString()));
    }

    @Test
    void getProductById_shouldThrowIfDeleted() {
        UUID id = UUID.randomUUID();
        Product deleted = getSampleProduct();
        deleted.setDeleted(true);
        when(productRepository.findById(id)).thenReturn(Optional.of(deleted));

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(id.toString()));
    }

    @Test
    void getAllProducts_shouldReturnList() {
        List<Product> products = Arrays.asList(getSampleProduct(), getSampleProduct());
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();
        assertEquals(products, result);
    }

    @Test
    void deleteProduct_shouldSoftDelete() throws ProductNotFoundException, ProductNotDeletedException {
        UUID id = UUID.randomUUID();
        Product product = getSampleProduct();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.deleteProduct(id.toString());
        assertTrue(result.isDeleted());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_shouldThrowIfNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(id.toString()));
    }

    @Test
    void deleteProduct_shouldThrowIfDeleted() {
        UUID id = UUID.randomUUID();
        Product deleted = getSampleProduct();
        deleted.setDeleted(true);
        when(productRepository.findById(id)).thenReturn(Optional.of(deleted));

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(id.toString()));
    }

    @Test
    void deleteProduct_shouldThrowIfDataAccessException() {
        UUID id = UUID.randomUUID();
        Product product = getSampleProduct();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenThrow(new DataAccessException("DB error") {});

        assertThrows(ProductNotDeletedException.class, () -> productService.deleteProduct(id.toString()));
    }

    @Test
    void replaceProduct_shouldReplaceAllFields() throws ProductNotFoundException {
        UUID id = UUID.randomUUID();
        Product existing = getSampleProduct();
        existing.setId(id);
        Product replacement = getSampleProduct();
        replacement.setName("Replaced");
        replacement.setDescription("Replaced desc");
        replacement.setImageUrl("img3");
        replacement.setPrice(new Price(123.0, existing.getPrice().getCurrency()));
        Category newCategory = new Category();
        newCategory.setName("ReplacedCat");
        replacement.setCategory(newCategory);

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("ReplacedCat")).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.replaceProduct(id.toString(), replacement);

        assertEquals("Replaced", result.getName());
        assertEquals("Replaced desc", result.getDescription());
        assertEquals("img3", result.getImageUrl());
        assertEquals(123.0, result.getPrice().getPrice());
        assertEquals(newCategory, result.getCategory());
    }

    @Test
    void replaceProduct_shouldThrowIfNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        Product replacement = getSampleProduct();
        assertThrows(ProductNotFoundException.class, () -> productService.replaceProduct(id.toString(), replacement));
    }

    @Test
    void replaceProduct_shouldThrowIfDeleted() {
        UUID id = UUID.randomUUID();
        Product deleted = getSampleProduct();
        deleted.setDeleted(true);
        when(productRepository.findById(id)).thenReturn(Optional.of(deleted));

        Product replacement = getSampleProduct();
        assertThrows(ProductNotFoundException.class, () -> productService.replaceProduct(id.toString(), replacement));
    }

    @Test
    void getSavedCategory_shouldReturnNullIfCategoryNull() {
        Product product = getSampleProduct();
        product.setCategory(null);

        // Use reflection to call private method
        Category result = invokeGetSavedCategory(productService, product);
        assertNull(result);
    }

    @Test
    void getSavedCategory_shouldReturnNullIfCategoryNameNull() {
        Product product = getSampleProduct();
        Category category = new Category();
        category.setName(null);
        product.setCategory(category);

        Category result = invokeGetSavedCategory(productService, product);
        assertNull(result);
    }

    @Test
    void getSavedCategory_shouldReturnExistingCategory() {
        Product product = getSampleProduct();
        Category category = getSampleCategory();
        product.setCategory(category);

        when(categoryRepository.findByName(category.getName())).thenReturn(Optional.of(category));

        Category result = invokeGetSavedCategory(productService, product);
        assertEquals(category, result);
    }

    @Test
    void getSavedCategory_shouldSaveAndReturnNewCategory() {
        Product product = getSampleProduct();
        Category category = getSampleCategory();
        product.setCategory(category);

        when(categoryRepository.findByName(category.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = invokeGetSavedCategory(productService, product);
        assertEquals(category, result);
        verify(categoryRepository).save(category);
    }

    private Product getSampleProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Sample");
        product.setDescription("Desc");
        product.setImageUrl("img");
        product.setPrice(new Price(10.0, com.vibevault.productservice.models.Currency.USD));
        product.setCategory(getSampleCategory());
        product.setDeleted(false);
        return product;
    }

    private Category getSampleCategory() {
        Category category = new Category();
        category.setName("Cat");
        return category;
    }

    // Helper to invoke private getSavedCategory
    private Category invokeGetSavedCategory(ProductServiceDBImpl service, Product product) {
        try {
            java.lang.reflect.Method method = ProductServiceDBImpl.class.getDeclaredMethod("getSavedCategory", Product.class);
            method.setAccessible(true);
            return (Category) method.invoke(service, product);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
