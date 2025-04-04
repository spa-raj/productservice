package com.vibevault.productservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;


@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class Price{
    @Column(nullable = false)
    @NumberFormat(pattern = "#,###,###,###.##", style = NumberFormat.Style.CURRENCY)
    private Double price;

    @Enumerated
    @Column(nullable = false)
    private Currency currency;

    @Override
    public String toString() {
        return currency + " " + price;
    }
}

