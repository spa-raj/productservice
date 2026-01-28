package com.vibevault.productservice.advices;

import com.vibevault.productservice.constants.ErrorCode;
import com.vibevault.productservice.dtos.exceptions.ExceptionDto;
import com.vibevault.productservice.dtos.exceptions.authentication.InvalidTokenException;
import com.vibevault.productservice.exceptions.categories.CategoryAlreadyExistsException;
import com.vibevault.productservice.exceptions.categories.CategoryNotCreatedException;
import com.vibevault.productservice.exceptions.categories.CategoryNotFoundException;
import com.vibevault.productservice.exceptions.products.ProductNotCreatedException;
import com.vibevault.productservice.exceptions.products.ProductNotDeletedException;
import com.vibevault.productservice.exceptions.products.ProductNotFoundException;
import com.vibevault.productservice.exceptions.search.InvalidSearchParameterException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

import org.springframework.security.access.AccessDeniedException;

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
    @ExceptionHandler(CategoryNotFoundException.class)
    ResponseEntity<ExceptionDto> handleCategoryNotFoundException(CategoryNotFoundException categoryNotFoundException,
                                                                 HttpServletRequest request){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.NOT_FOUND,
                categoryNotFoundException.getMessage(),
                request.getRequestURI(),
                ErrorCode.CATEGORY_NOT_FOUND.toString()),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoryNotCreatedException.class)
    ResponseEntity<ExceptionDto> handleCategoryNotCreatedException(CategoryNotCreatedException categoryNotCreatedException,
                                                                    HttpServletRequest request){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.INTERNAL_SERVER_ERROR,
                categoryNotCreatedException.getMessage(),
                request.getRequestURI(),
                ErrorCode.CATEGORY_CREATION_FAILED.toString()),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    ResponseEntity<ExceptionDto> handleCategoryAlreadyExistsException(CategoryAlreadyExistsException categoryAlreadyExistsException,
                                                                 HttpServletRequest request){
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.CONFLICT,
                categoryAlreadyExistsException.getMessage(),
                request.getRequestURI(),
                ErrorCode.CATEGORY_ALREADY_EXISTS.toString()),HttpStatus.CONFLICT);
    }
    @ExceptionHandler(InvalidTokenException.class)
    ResponseEntity<ExceptionDto> handleInvalidTokenException(InvalidTokenException invalidTokenException,
                                                              HttpServletRequest request) {
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.UNAUTHORIZED,
                invalidTokenException.getMessage(),
                request.getRequestURI(),
                ErrorCode.INVALID_TOKEN.toString()), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ExceptionDto> handleAccessDeniedException(AccessDeniedException accessDeniedException,
                                                             HttpServletRequest request) {
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.FORBIDDEN,
                accessDeniedException.getMessage(),
                request.getRequestURI(),
                ErrorCode.ACCESS_DENIED.toString()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidSearchParameterException.class)
    ResponseEntity<ExceptionDto> handleInvalidSearchParameterException(InvalidSearchParameterException invalidSearchParameterException,
                                                                       HttpServletRequest request) {
        return new ResponseEntity<>(new ExceptionDto(HttpStatus.BAD_REQUEST,
                invalidSearchParameterException.getMessage(),
                request.getRequestURI(),
                ErrorCode.INVALID_SEARCH_PARAMETER.toString()), HttpStatus.BAD_REQUEST);
    }
}
