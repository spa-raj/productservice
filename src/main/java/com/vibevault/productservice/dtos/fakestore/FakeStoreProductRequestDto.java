package com.vibevault.productservice.dtos.fakestore;

import lombok.Data;

@Data
public class FakeStoreProductRequestDto {
    private String title;
    private String description;
    private String image;
    private Double price;
    private String category;
}
