package com.smartfinance.controller;

import com.smartfinance.dto.request.BudgetRequest;
import com.smartfinance.dto.response.BudgetResponse;
import com.smartfinance.entity.User;
import com.smartfinance.exception.ApiResponse;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget", description = "Budget management with real-time spending tracking")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    /**
     * GET /api/budgets?month=5&year=2024
     * Returns all budgets for the authenticated user in the given month/year.
     * Defaults to the current month/year if params are not supplied.
     */
    @GetMapping
    @Operation(summary = "Get all budgets for the current user in a given month/year")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        Long userId = resolveUserId(userDetails);
        // Default to current month/year when not provided by client
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int effectiveYear  = (year  != null) ? year  : LocalDate.now().getYear();

        List<BudgetResponse> budgets = budgetService.getBudgets(userId, effectiveMonth, effectiveYear);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    /**
     * POST /api/budgets
     * Create a new budget. spentAmount is auto-synced from existing transactions (Option A).
     */
    @PostMapping
    @Operation(summary = "Create a new budget with auto-sync of existing transactions")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequest request) {

        Long userId = resolveUserId(userDetails);
        BudgetResponse created = budgetService.createBudget(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * PUT /api/budgets/{id}
     * Update only the spending limit of an existing budget.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update spending limit of an existing budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {

        Long userId = resolveUserId(userDetails);
        BudgetResponse updated = budgetService.updateBudget(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * DELETE /api/budgets/{id}
     * Hard-delete a budget. Does NOT affect any transaction records.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Hard-delete a budget record")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = resolveUserId(userDetails);
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Resolve Long userId from the authenticated UserDetails (username as lookup key)
    private Long resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
