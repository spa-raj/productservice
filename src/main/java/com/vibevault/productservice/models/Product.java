package com.vibevault.productservice.models;

import lombok.Data;

@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String categoryName;

    public Product(Long id, String name, String description, String imageUrl, Double price, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.categoryName = categoryName;
    }
}
