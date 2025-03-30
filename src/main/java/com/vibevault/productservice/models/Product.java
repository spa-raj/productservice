package com.vibevault.productservice.models;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Entity
@Table(name = "Products")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Product extends BaseModel{
    private String name;
    @Column(length = 1000)
    private String description;
    private String imageUrl;
    private Double price;
    @ManyToOne
    private Category category;
}
