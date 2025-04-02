package com.vibevault.productservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@Table(name = "categories")
public class Category extends BaseModel{
    @Column(nullable = false, unique = true)
    private String name;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @OneToMany(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Product> featuredProducts;

    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Product> products;

    @Column(name = "product_count")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private long productCount;
}
