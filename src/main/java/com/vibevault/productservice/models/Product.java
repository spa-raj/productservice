package com.vibevault.productservice.models;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

@Entity
@Table(name = "products")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
public class Product extends BaseModel{
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private String name;

    @Lob
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private String description;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Column(length = 1000)
    private String imageUrl;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Column(nullable = false)
    @Embedded
    private Price price;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    private Category category;
}
