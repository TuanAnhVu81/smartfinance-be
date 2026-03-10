package com.smartfinance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be strictly positive")
        BigDecimal amount,

        String note,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate
) {}
