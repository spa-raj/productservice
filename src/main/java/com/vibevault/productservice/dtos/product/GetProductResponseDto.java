package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Product;
import lombok.Data;

import java.util.List;

@Data
public class GetProductResponseDto {
    private  Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String categoryName;
    public static GetProductResponseDto fromProduct(Product product) {
        GetProductResponseDto responseDto = new GetProductResponseDto();
        responseDto.setId(product.getId());
        responseDto.setName(product.getName());
        responseDto.setDescription(product.getDescription());
        responseDto.setImageUrl(product.getImageUrl());
        responseDto.setPrice(product.getPrice());
        responseDto.setCategoryName(product.getCategoryName());
        return responseDto;
    }


    public static List<GetProductResponseDto> fromProducts(List<Product> products) {
        return products.stream()
                .map(GetProductResponseDto::fromProduct)
                .toList();
    }
}
