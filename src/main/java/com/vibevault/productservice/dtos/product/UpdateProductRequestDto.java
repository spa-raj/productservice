package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Product;
import lombok.Data;

@Data
public class UpdateProductRequestDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String categoryName;

    public Product toProduct() {
        return new Product(id, name, description, imageUrl, price, categoryName);
    }
}
