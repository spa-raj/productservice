package com.vibevault.productservice.services;

import com.vibevault.productservice.exceptions.search.InvalidSearchParameterException;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Product;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface SearchService {

    Page<Product> searchProducts(String query, Double minPrice, Double maxPrice,
                                 Currency currency, UUID categoryId, String categoryName,
                                 Date createdAfter, Date createdBefore,
                                 int page, int size, String sortBy, String sortDir)
            throws InvalidSearchParameterException;

    List<Product> getSuggestions(String prefix, int limit);
}
