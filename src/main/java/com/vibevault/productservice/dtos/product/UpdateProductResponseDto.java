package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Product;
import lombok.Data;

@Data
public class UpdateProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String categoryName;

    public static UpdateProductResponseDto fromProduct(Product product) {
        UpdateProductResponseDto responseDto = new UpdateProductResponseDto();
        responseDto.setId(product.getId());
        responseDto.setName(product.getName());
        responseDto.setDescription(product.getDescription());
        responseDto.setImageUrl(product.getImageUrl());
        responseDto.setPrice(product.getPrice());
        responseDto.setCategoryName(product.getCategory().getName());
        return responseDto;
    }
}
