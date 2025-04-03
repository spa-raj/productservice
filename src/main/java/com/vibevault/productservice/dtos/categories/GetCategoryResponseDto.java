package com.vibevault.productservice.dtos.categories;

import com.vibevault.productservice.models.Category;
import lombok.Data;

import java.util.List;

@Data
public class GetCategoryResponseDto {
    private String id;
    private String name;
    private String description;

    public static GetCategoryResponseDto fromCategory(Category category) {
        GetCategoryResponseDto responseDto = new GetCategoryResponseDto();
        responseDto.setId(String.valueOf(category.getId()));
        responseDto.setName(category.getName());
        responseDto.setDescription(category.getDescription());
        return responseDto;
    }
    public static List<GetCategoryResponseDto> fromCategories(List<Category> categories) {
        return categories.stream()
                .map(GetCategoryResponseDto::fromCategory)
                .toList();
    }
}
