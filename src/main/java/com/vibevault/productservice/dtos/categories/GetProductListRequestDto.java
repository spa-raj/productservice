package com.vibevault.productservice.dtos.categories;

import lombok.Data;

import java.util.List;

@Data
public class GetProductListRequestDto {
    List<String> categoryUuids;
}
