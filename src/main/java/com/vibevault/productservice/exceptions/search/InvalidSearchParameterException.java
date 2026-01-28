package com.vibevault.productservice.exceptions.search;

public class InvalidSearchParameterException extends Exception {
    public InvalidSearchParameterException(String message) {
        super(message);
    }

    public InvalidSearchParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
