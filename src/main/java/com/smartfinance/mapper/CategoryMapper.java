package com.smartfinance.mapper;

import com.smartfinance.dto.request.CategoryRequest;
import com.smartfinance.dto.response.CategoryResponse;
import com.smartfinance.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // Entity → Response DTO
    CategoryResponse toResponse(Category category);

    // Request DTO → new Entity (ignore system-managed fields)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Category toEntity(CategoryRequest request);

    // Partial update: overwrite mutable fields only
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateCategoryFromRequest(CategoryRequest request, @MappingTarget Category category);
}
