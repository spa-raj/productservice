package com.vibevault.productservice.exceptions.categories;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
