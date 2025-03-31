package com.vibevault.productservice.dtos.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ExceptionDto {
    private HttpStatus httpStatus;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String path;
    private String errorCode;

    public ExceptionDto(HttpStatus httpStatus, String message,
                        String path, String errorCode) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.path = path;
        this.errorCode = errorCode;
    }
}
