package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Product;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateProductRequestDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String categoryName;

    public Product toProduct() {
        Category category = new Category();
        category.setName(this.categoryName);
        Product product = new Product();
        product.setId((long) this.id);
        product.setName(this.name);
        product.setDescription(this.description);
        product.setImageUrl(this.imageUrl);
        product.setPrice(this.price);
        product.setPrice(this.price);
        product.setLastModifiedAt(new Date());
        product.setCategory(category);
        return product;
    }
}
