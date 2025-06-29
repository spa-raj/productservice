package com.vibevault.productservice.controllers;

import com.vibevault.productservice.commons.AuthenticationCommons;
import com.vibevault.productservice.dtos.commons.UserDto;
import com.vibevault.productservice.dtos.exceptions.authentication.InvalidTokenException;
import com.vibevault.productservice.dtos.product.*;
import com.vibevault.productservice.exceptions.products.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.products.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.services.ProductService;
import jakarta.annotation.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductService productService;
    private AuthenticationCommons authenticationCommons;

    public ProductController(ProductService productService,
                             AuthenticationCommons authenticationCommons) {
        this.productService = productService;
        this.authenticationCommons = authenticationCommons;
    }
    @PostMapping("")
    public CreateProductResponseDto createProduct(@RequestBody CreateProductRequestDto createProductRequestDto,
                                                  @Nullable @RequestHeader("Authorization") String token) throws ProductNotCreatedException, InvalidTokenException {
        UserDto userDto = authenticationCommons.validateToken(token);
        if (userDto == null || (!userDto.getRoles().contains("SELLER") && !userDto.getRoles().contains("ADMIN"))) {
            throw new InvalidTokenException("Unauthorized to create product");
        }
        Product product = productService.createProduct(createProductRequestDto.toProduct());
        return CreateProductResponseDto.fromProduct(product);
    }
    @PatchMapping("/{productId}")
    public UpdateProductResponseDto updateProduct(@PathVariable("productId") String productId,
                                                  @RequestBody UpdateProductRequestDto updateProductRequestDto,
                                                  @Nullable @RequestHeader("Authorization") String token) throws ProductNotFoundException, InvalidTokenException {
        UserDto userDto = authenticationCommons.validateToken(token);
        if (userDto == null || (!userDto.getRoles().contains("SELLER") && !userDto.getRoles().contains("ADMIN"))) {
            throw new InvalidTokenException("Unauthorized to update product");
        }
        Product product = productService.updateProduct(productId, updateProductRequestDto.toProduct());
        return UpdateProductResponseDto.fromProduct(product);
    }
    @GetMapping("/{productId}")
    public GetProductResponseDto getProductById(@PathVariable("productId") String productId) throws ProductNotFoundException {
        Product product=productService.getProductById(productId);
        return GetProductResponseDto.fromProduct(product);
    }
    @GetMapping("")
    public List<GetProductResponseDto> getAllProducts() throws ProductNotFoundException {
        List<Product> products = productService.getAllProducts();
        return GetProductResponseDto.fromProducts(products);
    }
    @DeleteMapping("/{productId}")
    public DeleteProductResponseDto deleteProduct(@PathVariable("productId") String productId,
                                                  @Nullable @RequestHeader("Authorization") String token) throws ProductNotFoundException, ProductNotDeletedException {
        UserDto userDto = authenticationCommons.validateToken(token);
        if (userDto == null || (!userDto.getRoles().contains("SELLER") && !userDto.getRoles().contains("ADMIN"))) {
            throw new InvalidTokenException("Unauthorized to delete product");
        }
        Product product=productService.deleteProduct(productId);
        return DeleteProductResponseDto.fromProduct(product);
    }

    @PutMapping("/{productId}")
    public ReplaceProductResponseDto replaceProduct(@PathVariable("productId") String productId,
                                                    @RequestBody ReplaceProductRequestDto replaceProductRequestDto,
                                                    @Nullable @RequestHeader("Authorization") String token) throws ProductNotFoundException {
        UserDto userDto = authenticationCommons.validateToken(token);
        if (userDto == null || (!userDto.getRoles().contains("SELLER") && !userDto.getRoles().contains("ADMIN"))) {
            throw new InvalidTokenException("Unauthorized to replace product");
        }
        Product product = productService.replaceProduct(productId, replaceProductRequestDto.toProduct());
        return ReplaceProductResponseDto.fromProduct(product);

    }
}
