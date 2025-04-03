package com.vibevault.productservice.dtos.product;

import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class UpdateProductRequestDto {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private Currency currency;
    private String categoryName;

    public Product toProduct() {
        Category category = new Category();
        category.setName(this.categoryName);

        Product product = new Product();
        product.setId(UUID.fromString(this.id));
        product.setName(this.name);
        product.setDescription(this.description);
        product.setImageUrl(this.imageUrl);

        Price price = new Price();
        price.setPrice(this.price);
        price.setCurrency(this.currency);
        product.setPrice(price);
        product.setLastModifiedAt(new Date());
        product.setCategory(category);
        return product;
    }
}
