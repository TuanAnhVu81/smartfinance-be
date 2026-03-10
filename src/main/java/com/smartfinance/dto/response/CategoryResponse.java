package com.smartfinance.dto.response;

import com.smartfinance.entity.Category;
import com.smartfinance.enums.CategoryType;

// Category response – exposes only safe, UI-facing fields
public record CategoryResponse(
        Long id,
        String name,
        CategoryType type,
        String icon,
        String color,
        Boolean isDefault
) {
    // Static factory: map Category entity → CategoryResponse DTO
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.getIsDefault()
        );
    }
}
