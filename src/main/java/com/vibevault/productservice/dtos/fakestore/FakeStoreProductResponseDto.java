package com.vibevault.productservice.dtos.fakestore;

import com.vibevault.productservice.models.Product;
import lombok.Data;

@Data
public class FakeStoreProductResponseDto {
    private int id;
    private String title;
    private String description;
    private String image;
    private Double price;
    private String category;

    public Product toProduct() {
        return new Product((long)id, title, description, image, price, category);
    }
}
