package com.vibevault.productservice.dtos.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Price;
import com.vibevault.productservice.models.Product;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateProductRequestDto {
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
        product.setName(this.name);
        product.setDescription(this.description);
        product.setImageUrl(this.imageUrl);

        Price price = new Price();
        price.setPrice(this.price);
        price.setCurrency(this.currency);
        product.setPrice(price);

        product.setCategory(category);
        return product;
    }
}
