package com.vibevault.productservice.dtos.categories;

import com.vibevault.productservice.models.Category;
import lombok.Data;

@Data
public class CreateCategoryResponseDto {
    private String id;
    private String categoryName;
    private String description;

    public static CreateCategoryResponseDto fromCategory(Category category) {
        CreateCategoryResponseDto responseDto = new CreateCategoryResponseDto();
        responseDto.setId(category.getId().toString());
        responseDto.setCategoryName(category.getName());
        responseDto.setDescription(category.getDescription());
        return responseDto;
    }
}
