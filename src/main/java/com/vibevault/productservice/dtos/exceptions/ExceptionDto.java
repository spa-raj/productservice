package com.vibevault.productservice.dtos.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ExceptionDto {
    HttpStatus httpStatus;
    String message;
    public ExceptionDto(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
