package com.vibevault.productservice.dtos.fakestore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vibevault.productservice.models.Product;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FakeStoreProductRequestDto {
    @JsonProperty(required = false)
    private String title;
    @JsonProperty(required = false)
    private String description;
    @JsonProperty(required = false)
    private String image;
    @JsonProperty(required = false)
    private Double price;
    @JsonProperty(required = false)
    private String category;

    public FakeStoreProductRequestDto fromProduct(Product product) {
        this.title = product.getName();
        this.description = product.getDescription();
        this.image = product.getImageUrl();
        this.price = product.getPrice();
        this.category = product.getCategory().getName();
        return this;
    }
}
