package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import lombok.Data;

@Data
public class DeleteProductResponseDto {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private Price price;
    private String categoryName;

    public static DeleteProductResponseDto fromProduct(Product product) {
        DeleteProductResponseDto responseDto = new DeleteProductResponseDto();
        responseDto.setId(String.valueOf(product.getId()));
        responseDto.setName(product.getName());
        responseDto.setDescription(product.getDescription());
        responseDto.setImageUrl(product.getImageUrl());
        responseDto.setPrice(product.getPrice());
        responseDto.setCategoryName(product.getCategory().getName());
        return responseDto;
    }
}
