package com.vibevault.productservice.services;

import com.vibevault.productservice.dtos.fakestore.FakeStoreProductRequestDto;
import com.vibevault.productservice.dtos.fakestore.FakeStoreProductResponseDto;
import com.vibevault.productservice.models.Product;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.parser.Entity;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service("productServiceFakeStoreImpl")
public class ProductServiceFakeStoreImpl implements ProductService{
    private RestTemplate restTemplate;

    public ProductServiceFakeStoreImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Override
    public Product createProduct(Product product) {
        String url = "https://fakestoreapi.com/products";
        FakeStoreProductRequestDto productRequestDto = new FakeStoreProductRequestDto();
        productRequestDto.setTitle(product.getName());
        productRequestDto.setPrice(product.getPrice());
        productRequestDto.setDescription(product.getDescription());
        productRequestDto.setImage(product.getImageUrl());
        productRequestDto.setCategory(product.getCategoryName());

        FakeStoreProductResponseDto response = restTemplate.postForObject(url, productRequestDto, FakeStoreProductResponseDto.class);

        return new Product(
                (long)response.getId(),
                response.getTitle(),
                response.getDescription(),
                response.getImage(),
                response.getPrice(),
                response.getCategory()
        );
    }

    @Override
    public Product updateProduct(@PathVariable Long productId, Product product) {
        String url = "https://fakestoreapi.com/products/" + productId;
        FakeStoreProductRequestDto productRequestDto = new FakeStoreProductRequestDto();
        // If the product is not found, the API will return a 404 error
        // We can handle this by checking the response status code
        // and throwing an exception if the product is not found

        // Set the product details if they are provided
        if (product.getName() != null) {
            productRequestDto.setTitle(product.getName());
        }
        if (product.getDescription() != null) {
            productRequestDto.setDescription(product.getDescription());
        }
        if (product.getImageUrl() != null) {
            productRequestDto.setImage(product.getImageUrl());
        }
        if (product.getPrice() != null) {
            productRequestDto.setPrice(product.getPrice());
        }
        if (product.getCategoryName() != null) {
            productRequestDto.setCategory(product.getCategoryName());
        }


        ResponseEntity<FakeStoreProductResponseDto> fakeStoreProductResponseEntity= restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(productRequestDto),
                FakeStoreProductResponseDto.class);
        FakeStoreProductResponseDto fakeStoreProductResponseDto = fakeStoreProductResponseEntity.getBody();

        return new Product(
                (long)fakeStoreProductResponseDto.getId(),
                fakeStoreProductResponseDto.getTitle(),
                fakeStoreProductResponseDto.getDescription(),
                fakeStoreProductResponseDto.getImage(),
                fakeStoreProductResponseDto.getPrice(),
                fakeStoreProductResponseDto.getCategory()
        );
    }

    @Override
    public Product getProductById(Long productId) {
        String url = "https://fakestoreapi.com/products/" + productId;
        ResponseEntity<FakeStoreProductResponseDto> response = restTemplate.getForEntity(url, FakeStoreProductResponseDto.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            FakeStoreProductResponseDto productResponseDto = response.getBody();
            return new Product(
                    (long)productResponseDto.getId(),
                    productResponseDto.getTitle(),
                    productResponseDto.getDescription(),
                    productResponseDto.getImage(),
                    productResponseDto.getPrice(),
                    productResponseDto.getCategory()
            );
        } else {
            // Handle the case when the product is not found
            return null;
        }
    }

    @Override
    public List<Product> getAllProducts() {
        String url = "https://fakestoreapi.com/products";
        ResponseEntity<FakeStoreProductResponseDto[]> response = restTemplate.getForEntity(url, FakeStoreProductResponseDto[].class);

        if (response.getStatusCode().is2xxSuccessful()) {
            FakeStoreProductResponseDto[] productResponseDtos = response.getBody();
            return Stream.of(Objects.requireNonNull(productResponseDtos))
                    .map(productResponseDto -> new Product(
                            (long)productResponseDto.getId(),
                            productResponseDto.getTitle(),
                            productResponseDto.getDescription(),
                            productResponseDto.getImage(),
                            productResponseDto.getPrice(),
                            productResponseDto.getCategory()
                    )).toList();
        } else {
            // Handle the case when the products are not found
            return List.of();
        }
    }

    @Override
    public Product deleteProduct(Long productId) {
        String url = "https://fakestoreapi.com/products/" + productId;
        ResponseEntity<FakeStoreProductResponseDto> response = restTemplate.exchange(url, HttpMethod.DELETE, null, FakeStoreProductResponseDto.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            FakeStoreProductResponseDto productResponseDto = response.getBody();
            return new Product(
                    (long) Objects.requireNonNull(productResponseDto).getId(),
                    productResponseDto.getTitle(),
                    productResponseDto.getDescription(),
                    productResponseDto.getImage(),
                    productResponseDto.getPrice(),
                    productResponseDto.getCategory()
            );
        } else {
            // Handle the case when the product is not found
            return null;
        }
    }

    @Override
    public Product replaceProduct(Long productId, Product product) {
        String url = "https://fakestoreapi.com/products/" + productId;
        FakeStoreProductRequestDto productRequestDto = new FakeStoreProductRequestDto();
        productRequestDto.setTitle(product.getName());
        productRequestDto.setPrice(product.getPrice());
        productRequestDto.setDescription(product.getDescription());
        productRequestDto.setImage(product.getImageUrl());
        productRequestDto.setCategory(product.getCategoryName());

        ResponseEntity<FakeStoreProductResponseDto> response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(productRequestDto), FakeStoreProductResponseDto.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            FakeStoreProductResponseDto productResponseDto = response.getBody();
            return new Product(
                    (long) Objects.requireNonNull(productResponseDto).getId(),
                    productResponseDto.getTitle(),
                    productResponseDto.getDescription(),
                    productResponseDto.getImage(),
                    productResponseDto.getPrice(),
                    productResponseDto.getCategory()
            );
        } else {
            // Handle the case when the product is not found
            return null;
        }
    }
}
