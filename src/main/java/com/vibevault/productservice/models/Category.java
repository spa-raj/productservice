package com.vibevault.productservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "Categories")
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseModel{
    private String name;
    @Column(length = 1000)
    private String description;
    @OneToMany
    private List<Product> featuredProducts;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
