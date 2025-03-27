package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Product;
import lombok.Data;

@Data
public class CreateProductRequestDto {
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String categoryName;

    public Product toProduct() {
        return new Product(null, name, description, imageUrl, price, categoryName);
    }
}
