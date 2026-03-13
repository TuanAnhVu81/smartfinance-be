package com.smartfinance.controller;

import com.smartfinance.dto.request.CategoryRequest;
import com.smartfinance.dto.response.CategoryResponse;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.ApiResponse;
import com.smartfinance.security.UserPrincipal;
import com.smartfinance.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management for income and expense classification")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all visible categories (default + own)", description = "Can optionally filter by type: INCOME or EXPENSE")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) CategoryType type) {

        List<CategoryResponse> result = (type != null)
                ? categoryService.getAllByType(principal.getId(), type)
                : categoryService.getAll(principal.getId());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @Operation(summary = "Create a new custom category")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse response = categoryService.create(principal.getId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user-owned category")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse response = categoryService.update(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a user-owned category")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        categoryService.delete(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
