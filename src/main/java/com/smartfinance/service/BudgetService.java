package com.smartfinance.service;

import com.smartfinance.dto.request.BudgetRequest;
import com.smartfinance.dto.response.BudgetResponse;

import java.util.List;

public interface BudgetService {

    // Get all budgets for a user in a specific month/year (with computed percentage)
    List<BudgetResponse> getBudgets(Long userId, Integer month, Integer year);

    // Create a new budget with auto-sync of spentAmount from existing transactions (Option A)
    BudgetResponse createBudget(Long userId, BudgetRequest request);

    // Update only the spending limit of an existing budget
    BudgetResponse updateBudget(Long userId, Long budgetId, BudgetRequest request);

    // Hard delete a budget record
    void deleteBudget(Long userId, Long budgetId);
}
