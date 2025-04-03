package com.vibevault.productservice.dtos.fakestore;

import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class FakeStoreProductResponseDto {
    private String id;
    private String title;
    private String description;
    private String image;
    private Price price;
    private String category;

    public Product toProduct() {
        Category category = new Category();
        category.setName(this.category);
        Product product = new Product();
        product.setId(UUID.fromString(this.id));
        product.setName(this.title);
        product.setDescription(this.description);
        product.setImageUrl(this.image);

        product.setPrice(this.price);
        product.setLastModifiedAt(new Date());
        product.setCategory(category);
        return product;
    }
}
