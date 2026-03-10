package com.smartfinance.service;

import com.smartfinance.dto.request.CategoryRequest;
import com.smartfinance.dto.response.CategoryResponse;
import com.smartfinance.enums.CategoryType;

import java.util.List;

public interface CategoryService {

    // Get all visible categories for the current user (default + own, non-deleted)
    List<CategoryResponse> getAll(Long userId);

    // Get categories filtered by INCOME or EXPENSE type
    List<CategoryResponse> getAllByType(Long userId, CategoryType type);

    // Create a new custom category for the current user
    CategoryResponse create(Long userId, CategoryRequest request);

    // Update an existing user-owned category (system categories are immutable)
    CategoryResponse update(Long userId, Long categoryId, CategoryRequest request);

    // Soft-delete a user-owned category (blocked if it has active transactions)
    void delete(Long userId, Long categoryId);
}
