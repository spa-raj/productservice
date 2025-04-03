package com.vibevault.productservice.exceptions.categories;

public class CategoryNotCreatedException extends RuntimeException{
    public CategoryNotCreatedException(String message) {
        super(message);
    }

    public CategoryNotCreatedException(String message, Throwable cause) {
        super(message, cause);
    }

}
