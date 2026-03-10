package com.smartfinance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BudgetRequest(

        @NotNull(message = "categoryId is required")
        Long categoryId,

        // Spending limit set by user for this category/month
        @NotNull(message = "amountLimit is required")
        @Positive(message = "amountLimit must be greater than 0")
        BigDecimal amountLimit,

        @NotNull(message = "month is required")
        Integer month,

        @NotNull(message = "year is required")
        Integer year
) {}
