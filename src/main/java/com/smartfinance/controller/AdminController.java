package com.smartfinance.controller;

import com.smartfinance.dto.response.AdminSummaryResponse;
import com.smartfinance.dto.response.UserResponse;
import com.smartfinance.exception.ApiResponse;
import com.smartfinance.security.UserPrincipal;
import com.smartfinance.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Module", description = "Endpoints for administration tasks")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get all users with pagination")
    @GetMapping("/users")
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(adminService.getAllUsers(pageable), "Fetched all users successfully");
    }

    @Operation(summary = "Update user status (enable/disable)")
    @PutMapping("/users/{id}/status")
    public ApiResponse<UserResponse> updateUserStatus(
            @PathVariable("id") Long id,
            @RequestParam("enabled") boolean enabled,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        UserResponse response = adminService.updateUserStatus(id, enabled, principal.getUsername());
        
        return ApiResponse.success(response, "Updated user status successfully");
    }

    @Operation(summary = "Get admin summary statistics")
    @GetMapping("/summary")
    public ApiResponse<AdminSummaryResponse> getAdminSummary() {
        return ApiResponse.success(adminService.getAdminSummary(), "Fetched admin summary successfully");
    }
}
