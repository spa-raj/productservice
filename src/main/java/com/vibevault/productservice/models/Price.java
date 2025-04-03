package com.vibevault.productservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;


@Getter
@Setter
@Embeddable
public class Price{
    @Column(nullable = false)
    @NumberFormat(pattern = "#,###,###,###.##", style = NumberFormat.Style.CURRENCY)
    private Double price;

    @Enumerated(EnumType.ORDINAL)
    private Currency currency;

    @Override
    public String toString() {
        return currency + " " + price;
    }
}
