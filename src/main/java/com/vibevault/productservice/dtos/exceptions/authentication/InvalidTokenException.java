package com.vibevault.productservice.dtos.exceptions.authentication;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
      super(message);
    }
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidTokenException(Throwable cause) {
        super(cause);
    }
}
