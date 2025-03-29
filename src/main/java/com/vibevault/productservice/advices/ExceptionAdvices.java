package com.vibevault.productservice.advices;

import com.vibevault.productservice.dtos.exceptions.ExceptionDto;
import com.vibevault.productservice.exceptions.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
public class ExceptionAdvices {
    /**
     * This method handles ProductNotFoundException and returns a ResponseEntity with a custom ExceptionDto.
     *
     * @param productNotFoundException the exception to handle
     * @return a ResponseEntity containing the ExceptionDto with NOT_FOUND status
     */
    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ExceptionDto> handleProductNotFoundException(ProductNotFoundException productNotFoundException){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.NOT_FOUND,productNotFoundException.getMessage()),HttpStatus.NOT_FOUND);
    }
    /**
     * This method handles ProductNotCreatedException and returns a ResponseEntity with a custom ExceptionDto.
     *
     * @param productNotCreatedException the exception to handle
     * @return a ResponseEntity containing the ExceptionDto with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(ProductNotCreatedException.class)
    ResponseEntity<ExceptionDto> handleProductNotCreatedException(ProductNotCreatedException productNotCreatedException){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR,productNotCreatedException.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(RestClientException.class)
    ResponseEntity<ExceptionDto> handleRestClientException(RestClientException restClientException){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR,restClientException.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
