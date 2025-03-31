package com.vibevault.productservice.advices;

import com.vibevault.productservice.constants.ErrorCode;
import com.vibevault.productservice.dtos.exceptions.ExceptionDto;
import com.vibevault.productservice.exceptions.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
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
    ResponseEntity<ExceptionDto> handleProductNotFoundException(ProductNotFoundException productNotFoundException,
                                                                HttpServletRequest request){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.NOT_FOUND,
                productNotFoundException.getMessage(),
                request.getRequestURI(),
                ErrorCode.PRODUCT_NOT_FOUND.toString()),HttpStatus.NOT_FOUND);
    }
    /**
     * This method handles ProductNotCreatedException and returns a ResponseEntity with a custom ExceptionDto.
     *
     * @param productNotCreatedException the exception to handle
     * @return a ResponseEntity containing the ExceptionDto with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(ProductNotCreatedException.class)
    ResponseEntity<ExceptionDto> handleProductNotCreatedException(ProductNotCreatedException productNotCreatedException,
                                                                  HttpServletRequest request){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR,
                productNotCreatedException.getMessage(),
                request.getRequestURI(),
                ErrorCode.PRODUCT_CREATION_FAILED.toString()),HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(RestClientException.class)
    ResponseEntity<ExceptionDto> handleRestClientException(RestClientException restClientException,
                                                           HttpServletRequest request){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR,
                restClientException.getMessage(),
                request.getRequestURI(),
                ErrorCode.EXTERNAL_API_ERROR.toString()),HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ExceptionDto> handleDataAccessException(DataAccessException dataAccessException,
                                                           HttpServletRequest request){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR,
                dataAccessException.getMessage(),
                request.getRequestURI(),
                ErrorCode.DATABASE_ERROR.toString()),HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(ProductNotDeletedException.class)
    ResponseEntity<ExceptionDto> handleProductNotDeletedException(ProductNotDeletedException productNotDeletedException,
                                                                  HttpServletRequest request){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR,
                productNotDeletedException.getMessage(),
                request.getRequestURI(),
                ErrorCode.PRODUCT_DELETION_FAILED.toString()),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
