package com.smartfinance.service.impl;

import com.smartfinance.dto.request.BudgetRequest;
import com.smartfinance.dto.response.BudgetResponse;
import com.smartfinance.entity.Budget;
import com.smartfinance.entity.Category;
import com.smartfinance.entity.User;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.mapper.BudgetMapper;
import com.smartfinance.repository.BudgetRepository;
import com.smartfinance.repository.CategoryRepository;
import com.smartfinance.repository.TransactionRepository;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;

    @Override
    public List<BudgetResponse> getBudgets(Long userId, Integer month, Integer year) {
        return budgetRepository.findAllByUserIdAndMonthAndYear(userId, month, year)
                .stream()
                .map(this::toResponseWithPercentage)
                .toList();
    }

    @Override
    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        Category category = findAndValidateExpenseCategory(userId, request.categoryId());

        // Enforce UNIQUE (user, category, month, year) constraint at service layer
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                userId, request.categoryId(), request.month(), request.year())) {
            throw new AppException(ErrorCode.BUDGET_DUPLICATED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Option A: Auto-Sync – query SUM of existing transactions to initialize spentAmount
        // This ensures the budget is accurate even when created mid-month
        BigDecimal initialSpent = transactionRepository.sumExpenseAmountByCategoryAndPeriod(
                userId, category.getId(), request.month(), request.year()
        );

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .amountLimit(request.amountLimit())
                .spentAmount(initialSpent != null ? initialSpent : BigDecimal.ZERO)
                .month(request.month())
                .year(request.year())
                .build();

        Budget saved = budgetRepository.save(budget);
        log.info("Budget created: id={}, categoryId={}, userId={}, initialSpent={}",
                saved.getId(), category.getId(), userId, initialSpent);
        return toResponseWithPercentage(saved);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long userId, Long budgetId, BudgetRequest request) {
        Budget budget = findBudgetByIdAndUser(budgetId, userId);

        // Only the spending limit is updatable; month/year/category are immutable after creation
        budget.setAmountLimit(request.amountLimit());
        Budget saved = budgetRepository.save(budget);

        log.info("Budget updated: id={}, newLimit={}, userId={}", saved.getId(), request.amountLimit(), userId);
        return toResponseWithPercentage(saved);
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = findBudgetByIdAndUser(budgetId, userId);
        budgetRepository.delete(budget);
        log.info("Budget hard-deleted: id={}, userId={}", budgetId, userId);
    }

    // --- Private Helpers ---

    /**
     * Map Budget → BudgetResponse and compute percentage.
     * Guard against division-by-zero: if amountLimit is 0, percentage is 0.
     */
    private BudgetResponse toResponseWithPercentage(Budget budget) {
        BudgetResponse response = budgetMapper.toResponse(budget);

        BigDecimal percentage = BigDecimal.ZERO;
        if (budget.getAmountLimit().compareTo(BigDecimal.ZERO) > 0) {
            percentage = budget.getSpentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP);
        }
        response.setPercentage(percentage);
        return response;
    }

    /**
     * Validate that the category:
     *  1. Exists and is not soft-deleted.
     *  2. Is of type EXPENSE (no budget for income categories).
     *  3. Is either a system category (user IS NULL) or belongs to the authenticated user.
     */
    private Category findAndValidateExpenseCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Budgets are only meaningful for EXPENSE categories
        if (category.getType() != CategoryType.EXPENSE) {
            throw new AppException(ErrorCode.BUDGET_INCOME_NOT_ALLOWED);
        }

        // Verify ownership: must be system category OR owned by the requesting user
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_OWNED);
        }

        return category;
    }

    private Budget findBudgetByIdAndUser(Long budgetId, Long userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));

        // Prevent accessing another user's budget
        if (!budget.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.BUDGET_NOT_OWNED);
        }
        return budget;
    }
}
