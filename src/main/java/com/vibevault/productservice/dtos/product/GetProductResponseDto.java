package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import lombok.Data;

import java.util.List;

@Data
public class GetProductResponseDto {
    private  String id;
    private String name;
    private String description;
    private String imageUrl;
    private Price price;
    private String categoryName;
    public static GetProductResponseDto fromProduct(Product product) {
        GetProductResponseDto responseDto = new GetProductResponseDto();
        responseDto.setId(String.valueOf(product.getId()));
        responseDto.setName(product.getName());
        responseDto.setDescription(product.getDescription());
        responseDto.setImageUrl(product.getImageUrl());
        responseDto.setPrice(product.getPrice());
        responseDto.setCategoryName(product.getCategory().getName());
        return responseDto;
    }


    public static List<GetProductResponseDto> fromProducts(List<Product> products) {
        return products.stream()
                .map(GetProductResponseDto::fromProduct)
                .toList();
    }
}
