package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Product;
import lombok.Data;

@Data
public class CreateProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String categoryName;

    public static CreateProductResponseDto fromProduct(Product product) {
        CreateProductResponseDto responseDto = new CreateProductResponseDto();
        responseDto.setId(product.getId());
        responseDto.setName(product.getName());
        responseDto.setDescription(product.getDescription());
        responseDto.setImageUrl(product.getImageUrl());
        responseDto.setPrice(product.getPrice());
        responseDto.setCategoryName(product.getCategory().getName());
        return responseDto;
    }
}
