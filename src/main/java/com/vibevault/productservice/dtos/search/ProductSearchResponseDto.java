package com.vibevault.productservice.dtos.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vibevault.productservice.dtos.product.GetProductResponseDto;
import com.vibevault.productservice.models.Product;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSearchResponseDto {
    private List<GetProductResponseDto> products;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean first;
    private boolean last;

    public static ProductSearchResponseDto fromPage(Page<Product> page) {
        ProductSearchResponseDto dto = new ProductSearchResponseDto();
        dto.setProducts(GetProductResponseDto.fromProducts(page.getContent()));
        dto.setCurrentPage(page.getNumber());
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements(page.getTotalElements());
        dto.setPageSize(page.getSize());
        dto.setHasNext(page.hasNext());
        dto.setHasPrevious(page.hasPrevious());
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        return dto;
    }
}
