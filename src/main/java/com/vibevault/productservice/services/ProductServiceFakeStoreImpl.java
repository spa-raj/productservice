package com.vibevault.productservice.services;

import com.vibevault.productservice.dtos.fakestore.FakeStoreProductRequestDto;
import com.vibevault.productservice.dtos.fakestore.FakeStoreProductResponseDto;
import com.vibevault.productservice.exceptions.products.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.products.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
import com.vibevault.productservice.models.Product;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Stream;

@Service("productServiceFakeStoreImpl")
public class ProductServiceFakeStoreImpl implements ProductService{
    private RestTemplate restTemplate;

    public ProductServiceFakeStoreImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Override
    public Product createProduct(Product product) throws ProductNotCreatedException {
        String url = "https://fakestoreapi.com/products";
        FakeStoreProductRequestDto productRequestDto = new FakeStoreProductRequestDto();
        productRequestDto.fromProduct(product);

        FakeStoreProductResponseDto response = restTemplate.postForObject(url, productRequestDto, FakeStoreProductResponseDto.class);
        if (response == null) {
            throw new ProductNotCreatedException("Product not created.");
        }
        return response.toProduct();
    }

    @Override
    public Product updateProduct(@PathVariable Long productId, Product product) throws ProductNotFoundException {
        String url = "https://fakestoreapi.com/products/" + productId;
        FakeStoreProductRequestDto productRequestDto = new FakeStoreProductRequestDto();

        productRequestDto.fromProduct(product);


        FakeStoreProductResponseDto fakeStoreProductResponseDto= restTemplate.patchForObject(url, productRequestDto, FakeStoreProductResponseDto.class);
        if (fakeStoreProductResponseDto == null) {
            throw new ProductNotFoundException("Product with id: " + productId + " not found.");
        }
        return fakeStoreProductResponseDto.toProduct();
    }

    @Override
    public Product getProductById(Long productId) throws ProductNotFoundException {
        String url = "https://fakestoreapi.com/products/" + productId;
        ResponseEntity<FakeStoreProductResponseDto> response = restTemplate.getForEntity(url, FakeStoreProductResponseDto.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            FakeStoreProductResponseDto productResponseDto = response.getBody();
            if (productResponseDto == null) {
                throw new ProductNotFoundException("Product with id: " + productId + " not found.");
            }
            return productResponseDto.toProduct();
        } else {
            // Handle the case when the product is not found
            throw new ProductNotFoundException("Product with id: " + productId + " not found.");
        }
    }

    @Override
    public List<Product> getAllProducts() throws ProductNotFoundException {
        String url = "https://fakestoreapi.com/products";
        ResponseEntity<FakeStoreProductResponseDto[]> response = restTemplate.getForEntity(url, FakeStoreProductResponseDto[].class);

        if (response.getStatusCode().is2xxSuccessful()) {
            FakeStoreProductResponseDto[] productResponseDtos = response.getBody();
            if (productResponseDtos == null || productResponseDtos.length == 0) {
                throw new ProductNotFoundException("No products found.");
            }
            return Stream.of(productResponseDtos)
                    .map(FakeStoreProductResponseDto::toProduct)
                    .toList();
        } else {
            // Handle the case when the products are not found
            throw new ProductNotFoundException("No products found.");
        }
    }

    @Override
    public Product deleteProduct(Long productId) throws ProductNotFoundException, ProductNotDeletedException {
        String url = "https://fakestoreapi.com/products/" + productId;
        ResponseEntity<FakeStoreProductResponseDto> response = restTemplate.exchange(url, HttpMethod.DELETE, null, FakeStoreProductResponseDto.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            FakeStoreProductResponseDto productResponseDto = response.getBody();
            if (productResponseDto == null) {
                throw new ProductNotFoundException("Product with id: " + productId + " not found.");
            }
            return productResponseDto.toProduct();
        } else {
            // Handle the case when the product is not found
            throw new ProductNotDeletedException("Product with id: " + productId + " not deleted.");
        }
    }

    @Override
    public Product replaceProduct(Long productId, Product product) throws ProductNotFoundException {
        String url = "https://fakestoreapi.com/products/" + productId;
        FakeStoreProductRequestDto productRequestDto = new FakeStoreProductRequestDto();
        productRequestDto.fromProduct(product);
        ResponseEntity<FakeStoreProductResponseDto> response;
        try{
            response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(productRequestDto), FakeStoreProductResponseDto.class);
        }
        // RestTemplateException
        catch(RestClientException e){
            throw new RestClientException("Error occurred while replacing the product: " + e.getMessage());
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            FakeStoreProductResponseDto productResponseDto = response.getBody();
            if (productResponseDto == null) {
                throw new ProductNotFoundException("Product with id: " + productId + " not found.");
            }
            return productResponseDto.toProduct();
        } else {
            // Handle the case when the product is not found
            throw new ProductNotFoundException("Product with id: " + productId + " not found.");
        }
    }

}
