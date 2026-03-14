package com.vibevault.productservice.events;

import com.vibevault.productservice.models.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductChangedEvent {

    public enum ActionType {
        CREATED, UPDATED, DELETED
    }

    private final Product product;
    private final ActionType actionType;

    public static ProductChangedEvent created(Product product) {
        return new ProductChangedEvent(product, ActionType.CREATED);
    }

    public static ProductChangedEvent updated(Product product) {
        return new ProductChangedEvent(product, ActionType.UPDATED);
    }

    public static ProductChangedEvent deleted(Product product) {
        return new ProductChangedEvent(product, ActionType.DELETED);
    }
}
