package com.vibevault.productservice.exceptions.products;

public class ProductNotDeletedException extends Exception{
    public ProductNotDeletedException(String message) {
        super(message);
    }

    public ProductNotDeletedException(String message, Throwable cause) {
        super(message, cause);
    }
}
