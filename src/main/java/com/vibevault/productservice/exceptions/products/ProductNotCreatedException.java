package com.vibevault.productservice.exceptions.products;

public class ProductNotCreatedException extends Exception{
    public ProductNotCreatedException(String message) {
        super(message);
    }

    public ProductNotCreatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
