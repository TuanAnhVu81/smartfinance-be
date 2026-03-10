package com.smartfinance.dto.response;

import com.smartfinance.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private String icon;
    private String color;
    private Boolean isDefault;
}
