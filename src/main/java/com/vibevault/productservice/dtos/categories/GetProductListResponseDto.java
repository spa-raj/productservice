package com.vibevault.productservice.dtos.categories;

import com.vibevault.productservice.models.Price;
import lombok.Data;

import java.util.List;

@Data
public class GetProductListResponseDto {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private Price price;
    private String categoryName;

    public static GetProductListResponseDto fromProduct(com.vibevault.productservice.models.Product product) {
        GetProductListResponseDto responseDto = new GetProductListResponseDto();
        responseDto.setId(String.valueOf(product.getId()));
        responseDto.setName(product.getName());
        responseDto.setDescription(product.getDescription());
        responseDto.setImageUrl(product.getImageUrl());
        responseDto.setPrice(product.getPrice());
        responseDto.setCategoryName(product.getCategory().getName());
        return responseDto;
    }
    public static List<GetProductListResponseDto> fromProducts(List<com.vibevault.productservice.models.Product> products) {
        return products.stream()
                .map(GetProductListResponseDto::fromProduct)
                .toList();
    }
}
