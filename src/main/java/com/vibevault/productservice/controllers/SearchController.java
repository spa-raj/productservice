package com.vibevault.productservice.controllers;

import com.vibevault.productservice.dtos.search.ProductSearchResponseDto;
import com.vibevault.productservice.dtos.search.ProductSuggestionResponseDto;
import com.vibevault.productservice.exceptions.search.InvalidSearchParameterException;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Product;
import com.vibevault.productservice.services.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/products")
    public ProductSearchResponseDto searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Currency currency,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) throws InvalidSearchParameterException {

        Page<Product> results = searchService.searchProducts(
                query, minPrice, maxPrice, currency, categoryId, categoryName,
                createdAfter, createdBefore, page, size, sortBy, sortDir);

        return ProductSearchResponseDto.fromPage(results);
    }

    @GetMapping("/products/suggest")
    public List<ProductSuggestionResponseDto> getSuggestions(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<Product> suggestions = searchService.getSuggestions(prefix, limit);
        return ProductSuggestionResponseDto.fromProducts(suggestions);
    }
}
