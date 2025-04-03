package com.vibevault.productservice.dtos.categories;

import com.vibevault.productservice.models.Category;
import lombok.Data;

@Data
public class CreateCategoryRequestDto {
    String name;
    String description;

    public CreateCategoryRequestDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Category toCategory() {
        Category category = new Category();
        category.setName(this.name);
        category.setDescription(this.description);
        return category;
    }
}
