package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.product.*;
import com.vibevault.productservice.exceptions.products.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.products.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.services.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public CreateProductResponseDto createProduct(@RequestBody CreateProductRequestDto createProductRequestDto) throws ProductNotCreatedException {
        Product product = productService.createProduct(createProductRequestDto.toProduct());
        return CreateProductResponseDto.fromProduct(product);
    }
    @PatchMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public UpdateProductResponseDto updateProduct(@PathVariable("productId") String productId,
                                                  @RequestBody UpdateProductRequestDto updateProductRequestDto) throws ProductNotFoundException {
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
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public DeleteProductResponseDto deleteProduct(@PathVariable("productId") String productId) throws ProductNotFoundException, ProductNotDeletedException {
        Product product=productService.deleteProduct(productId);
        return DeleteProductResponseDto.fromProduct(product);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ReplaceProductResponseDto replaceProduct(@PathVariable("productId") String productId,
                                                    @RequestBody ReplaceProductRequestDto replaceProductRequestDto) throws ProductNotFoundException {
        Product product = productService.replaceProduct(productId, replaceProductRequestDto.toProduct());
        return ReplaceProductResponseDto.fromProduct(product);

    }
}
