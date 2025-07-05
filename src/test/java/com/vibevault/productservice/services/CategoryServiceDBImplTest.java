package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.categories.CategoryAlreadyExistsException;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceDBImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceDBImpl categoryService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCategories_shouldReturnList_whenCategoriesExist() {
        List<Category> categories = Arrays.asList(getSampleCategory("cat1"), getSampleCategory("cat2"));
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("cat1", result.get(0).getName());
    }

    @Test
    void getAllCategories_shouldThrow_whenNoCategoriesExist() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getAllCategories());
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenFound() {
        UUID uuid = UUID.randomUUID();
        Category category = getSampleCategory("cat1");
        when(categoryRepository.findById(uuid)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(uuid.toString());

        assertEquals("cat1", result.getName());
    }

    @Test
    void getCategoryById_shouldThrow_whenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(categoryRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(uuid.toString()));
    }

    @Test
    void createCategory_shouldSave_whenCategoryDoesNotExist() {
        Category category = getSampleCategory("cat1");
        when(categoryRepository.findByName("cat1")).thenReturn(Optional.empty());
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = categoryService.createCategory(category);

        assertEquals("cat1", result.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_shouldThrow_whenCategoryAlreadyExists() {
        Category category = getSampleCategory("cat1");
        when(categoryRepository.findByName("cat1")).thenReturn(Optional.of(category));

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.createCategory(category));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryByName_shouldReturnCategory_whenFound() {
        Category category = getSampleCategory("cat1");
        when(categoryRepository.findByName("cat1")).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryByName("cat1");

        assertEquals("cat1", result.getName());
    }

    @Test
    void getCategoryByName_shouldThrow_whenNotFound() {
        when(categoryRepository.findByName("cat1")).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryByName("cat1"));
    }

    @Test
    void getProductsList_shouldReturnProducts_whenCategoriesFound() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Category cat1 = getSampleCategory("cat1");
        Category cat2 = getSampleCategory("cat2");
        Product prod1 = getSampleProduct("prod1");
        Product prod2 = getSampleProduct("prod2");
        cat1.setProducts(List.of(prod1));
        cat2.setProducts(List.of(prod2));
        when(categoryRepository.findAllByIdIn(Arrays.asList(uuid1, uuid2))).thenReturn(List.of(cat1, cat2));

        List<Product> result = categoryService.getProductsList(Arrays.asList(uuid1.toString(), uuid2.toString()));

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("prod1")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("prod2")));
    }

    @Test
    void getProductsList_shouldThrow_whenNoCategoriesFound() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        when(categoryRepository.findAllByIdIn(Arrays.asList(uuid1, uuid2))).thenReturn(Collections.emptyList());

        assertThrows(CategoryNotFoundException.class, () ->
                categoryService.getProductsList(Arrays.asList(uuid1.toString(), uuid2.toString())));
    }

    @Test
    void getProductsByCategory_shouldReturnProducts_whenCategoryFound() {
        Category cat = getSampleCategory("cat1");
        Product prod1 = getSampleProduct("prod1");
        Product prod2 = getSampleProduct("prod2");
        cat.setProducts(List.of(prod1, prod2));
        when(categoryRepository.findByName("cat1")).thenReturn(Optional.of(cat));

        List<Product> result = categoryService.getProductsByCategory("cat1");

        assertEquals(2, result.size());
        assertEquals("prod1", result.get(0).getName());
    }

    @Test
    void getProductsByCategory_shouldThrow_whenCategoryNotFound() {
        when(categoryRepository.findByName("cat1")).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getProductsByCategory("cat1"));
    }

    // Helper methods
    private Category getSampleCategory(String name) {
        Category cat = new Category();
        cat.setName(name);
        cat.setProducts(new ArrayList<>());
        return cat;
    }

    private Product getSampleProduct(String name) {
        Product prod = new Product();
        prod.setName(name);
        return prod;
    }
}