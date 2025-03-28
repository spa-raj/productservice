package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.product.*;
import com.vibevault.productservice.exceptions.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.ProductNotFoundException;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.services.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping("")
    public CreateProductResponseDto createProduct(@RequestBody CreateProductRequestDto createProductRequestDto) throws ProductNotCreatedException {
        Product product = productService.createProduct(createProductRequestDto.toProduct());
        return CreateProductResponseDto.fromProduct(product);
    }
    @PatchMapping("/{productId}")
    public UpdateProductResponseDto updateProduct(@PathVariable("productId") Long productId, @RequestBody UpdateProductRequestDto updateProductRequestDto) throws ProductNotFoundException {
        Product product = productService.updateProduct(productId, updateProductRequestDto.toProduct());
        return UpdateProductResponseDto.fromProduct(product);
    }
    @GetMapping("/{productId}")
    public GetProductResponseDto getSingleProduct(@PathVariable("productId") Long productId) throws ProductNotFoundException {
        Product product=productService.getProductById(productId);
        return GetProductResponseDto.fromProduct(product);
    }
    @GetMapping("")
    public List<GetProductResponseDto> getAllProducts() throws ProductNotFoundException {
        List<Product> products = productService.getAllProducts();
        return GetProductResponseDto.fromProducts(products);
    }
    @DeleteMapping("/{productId}")
    public DeleteProductResponseDto deleteProduct(@PathVariable("productId") Long productId) throws ProductNotFoundException, ProductNotDeletedException {
        Product product=productService.deleteProduct(productId);
        return DeleteProductResponseDto.fromProduct(product);
    }

    @PutMapping("/{productId}")
    public ReplaceProductResponseDto replaceProduct(@PathVariable("productId") Long productId, @RequestBody ReplaceProductRequestDto replaceProductRequestDto) throws ProductNotFoundException {
        Product product = productService.replaceProduct(productId, replaceProductRequestDto.toProduct());
        return ReplaceProductResponseDto.fromProduct(product);

    }
}
