package com.smartfinance.dto.request;

import com.smartfinance.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 50, message = "Category name must not exceed 50 characters")
        String name,

        @NotNull(message = "Category type is required")
        CategoryType type,

        @Size(max = 10, message = "Icon must not exceed 10 characters")
        String icon,

        @Size(max = 7, message = "Color must be a valid hex color code (e.g. #FF5733)")
        String color
) {}
