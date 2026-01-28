package com.vibevault.productservice.dtos.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vibevault.productservice.models.Product;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSuggestionResponseDto {
    private String id;
    private String name;
    private String categoryName;

    public static ProductSuggestionResponseDto fromProduct(Product product) {
        ProductSuggestionResponseDto dto = new ProductSuggestionResponseDto();
        dto.setId(product.getId().toString());
        dto.setName(product.getName());
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }

    public static List<ProductSuggestionResponseDto> fromProducts(List<Product> products) {
        return products.stream()
                .map(ProductSuggestionResponseDto::fromProduct)
                .toList();
    }
}
