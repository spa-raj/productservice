package com.vibevault.productservice.services;

import com.vibevault.productservice.dtos.fakestore.FakeStoreProductRequestDto;
import com.vibevault.productservice.dtos.fakestore.FakeStoreProductResponseDto;
import com.vibevault.productservice.exceptions.products.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.products.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServiceFakeStoreImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceFakeStoreImpl productService;

    @BeforeEach
    void setUp() throws Exception {
        try (AutoCloseable closeable = MockitoAnnotations.openMocks(this)) {
            // Initialization logic
        }
    }

    @Test
    void createProduct_success() throws ProductNotCreatedException {
        Product product = getSampleProduct();
        FakeStoreProductResponseDto responseDto = getSampleResponseDto();
        when(restTemplate.postForObject(anyString(), any(FakeStoreProductRequestDto.class), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseDto);

        Product result = productService.createProduct(product);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
    }

    @Test
    void createProduct_nullResponse_throwsException() {
        Product product = getSampleProduct();
        when(restTemplate.postForObject(anyString(), any(FakeStoreProductRequestDto.class), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(null);

        assertThrows(ProductNotCreatedException.class, () -> productService.createProduct(product));
    }

    @Test
    void updateProduct_success() throws ProductNotFoundException {
        String productId = "1";
        Product product = getSampleProduct();
        FakeStoreProductResponseDto responseDto = getSampleResponseDto();
        when(restTemplate.patchForObject(anyString(), any(FakeStoreProductRequestDto.class), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseDto);

        Product result = productService.updateProduct(productId, product);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
    }

    @Test
    void updateProduct_nullResponse_throwsException() {
        String productId = "1";
        Product product = getSampleProduct();
        when(restTemplate.patchForObject(anyString(), any(FakeStoreProductRequestDto.class), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(null);

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(productId, product));
    }

    @Test
    void getProductById_success() throws ProductNotFoundException {
        String productId = "1";
        FakeStoreProductResponseDto responseDto = getSampleResponseDto();
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(restTemplate.getForEntity(contains(productId), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        Product result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(responseDto.getTitle(), result.getName());
    }

    @Test
    void getProductById_nullBody_throwsException() {
        String productId = "1";
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity(contains(productId), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void getProductById_non2xxStatus_throwsException() {
        String productId = "1";
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        when(restTemplate.getForEntity(contains(productId), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void getAllProducts_success() throws ProductNotFoundException {
        FakeStoreProductResponseDto[] responseDtos = new FakeStoreProductResponseDto[] {
                getSampleResponseDto(), getSampleResponseDto()
        };
        ResponseEntity<FakeStoreProductResponseDto[]> responseEntity =
                new ResponseEntity<>(responseDtos, HttpStatus.OK);

        when(restTemplate.getForEntity(contains("products"), eq(FakeStoreProductResponseDto[].class)))
                .thenReturn(responseEntity);

        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllProducts_emptyOrNull_throwsException() {
        ResponseEntity<FakeStoreProductResponseDto[]> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity(contains("products"), eq(FakeStoreProductResponseDto[].class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotFoundException.class, () -> productService.getAllProducts());

        // Test with empty array
        responseEntity = new ResponseEntity<>(new FakeStoreProductResponseDto[0], HttpStatus.OK);
        when(restTemplate.getForEntity(contains("products"), eq(FakeStoreProductResponseDto[].class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotFoundException.class, () -> productService.getAllProducts());
    }

    @Test
    void getAllProducts_non2xxStatus_throwsException() {
        ResponseEntity<FakeStoreProductResponseDto[]> responseEntity =
                new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.getForEntity(contains("products"), eq(FakeStoreProductResponseDto[].class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotFoundException.class, () -> productService.getAllProducts());
    }

    @Test
    void deleteProduct_success() throws ProductNotFoundException, ProductNotDeletedException {
        String productId = "1";
        FakeStoreProductResponseDto responseDto = getSampleResponseDto();
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(restTemplate.exchange(contains(productId), eq(HttpMethod.DELETE), isNull(), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        Product result = productService.deleteProduct(productId);

        assertNotNull(result);
        assertEquals(responseDto.getTitle(), result.getName());
    }

    @Test
    void deleteProduct_nullBody_throwsProductNotFound() {
        String productId = "1";
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(contains(productId), eq(HttpMethod.DELETE), isNull(), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void deleteProduct_non2xxStatus_throwsProductNotDeleted() {
        String productId = "1";
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(contains(productId), eq(HttpMethod.DELETE), isNull(), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotDeletedException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void replaceProduct_success() throws ProductNotFoundException {
        String productId = "1";
        Product product = getSampleProduct();
        FakeStoreProductResponseDto responseDto = getSampleResponseDto();
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(restTemplate.exchange(contains(productId), eq(HttpMethod.PUT), any(HttpEntity.class), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        Product result = productService.replaceProduct(productId, product);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
    }

    @Test
    void replaceProduct_nullBody_throwsException() {
        String productId = "1";
        Product product = getSampleProduct();
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(contains(productId), eq(HttpMethod.PUT), any(HttpEntity.class), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotFoundException.class, () -> productService.replaceProduct(productId, product));
    }

    @Test
    void replaceProduct_non2xxStatus_throwsException() {
        String productId = "1";
        Product product = getSampleProduct();
        ResponseEntity<FakeStoreProductResponseDto> responseEntity =
                new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(contains(productId), eq(HttpMethod.PUT), any(HttpEntity.class), eq(FakeStoreProductResponseDto.class)))
                .thenReturn(responseEntity);

        assertThrows(ProductNotFoundException.class, () -> productService.replaceProduct(productId, product));
    }

    // Helper methods
    private Product getSampleProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Sample Product");
        product.setDescription("Sample Description");
        product.setImageUrl("sample.jpg");
        product.setPrice(new Price(99.99, Currency.USD));
        Category category = new Category();
        category.setName("electronics");
        product.setCategory(category);
        return product;
    }

    private FakeStoreProductResponseDto getSampleResponseDto() {
        FakeStoreProductResponseDto dto = new FakeStoreProductResponseDto();
        dto.setId(UUID.randomUUID().toString());
        dto.setTitle("Sample Product");
        dto.setDescription("Sample Description");
        dto.setImage("sample.jpg");
        dto.setPrice(new Price(99.99, Currency.USD));
        dto.setCategory("electronics");
        return dto;
    }
}