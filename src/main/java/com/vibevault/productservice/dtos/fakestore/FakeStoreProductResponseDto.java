package com.vibevault.productservice.dtos.fakestore;

import lombok.Data;

@Data
public class FakeStoreProductResponseDto {
    private int id;
    private String title;
    private String description;
    private String image;
    private Double price;
    private String category;
}
