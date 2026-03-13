package com.smartfinance.controller;

import com.smartfinance.dto.request.BudgetRequest;
import com.smartfinance.dto.response.BudgetResponse;
import com.smartfinance.exception.ApiResponse;
import com.smartfinance.security.UserPrincipal;
import com.smartfinance.service.BudgetService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget", description = "Budget management with real-time spending tracking")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * GET /api/budgets?month=5&year=2024
     * Returns all budgets for the authenticated user in the given month/year.
     * Defaults to the current month/year if params are not supplied.
     */
    @GetMapping
    @Operation(summary = "Get all budgets for the current user in a given month/year")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        // Default to current month/year when not provided by client
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int effectiveYear  = (year  != null) ? year  : LocalDate.now().getYear();

        List<BudgetResponse> budgets = budgetService.getBudgets(principal.getId(), effectiveMonth, effectiveYear);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    /**
     * POST /api/budgets
     * Create a new budget. spentAmount is auto-synced from existing transactions (Option A).
     */
    @PostMapping
    @Operation(summary = "Create a new budget with auto-sync of existing transactions")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BudgetRequest request) {

        BudgetResponse created = budgetService.createBudget(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * PUT /api/budgets/{id}
     * Update only the spending limit of an existing budget.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update spending limit of an existing budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {

        BudgetResponse updated = budgetService.updateBudget(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * DELETE /api/budgets/{id}
     * Hard-delete a budget. Does NOT affect any transaction records.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Hard-delete a budget record")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        budgetService.deleteBudget(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
