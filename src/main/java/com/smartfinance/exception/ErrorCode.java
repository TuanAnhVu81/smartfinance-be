package com.smartfinance.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // --- Common ---
    INTERNAL_SERVER_ERROR(5000, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

    // --- Auth & User ---
    USER_NOT_FOUND(4001, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_EXISTED(4002, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_EXISTED(4003, "Email already exists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(4004, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(4005, "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(4006, "Account has been locked", HttpStatus.FORBIDDEN),
    WRONG_PASSWORD(4007, "Current password is incorrect", HttpStatus.BAD_REQUEST),

    // --- Category ---
    CATEGORY_NOT_FOUND(4010, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_OWNED(4011, "You do not have permission to modify this category", HttpStatus.FORBIDDEN),
    CATEGORY_NAME_DUPLICATED(4012, "Category name already exists", HttpStatus.CONFLICT),
    CATEGORY_HAS_TRANSACTIONS(4013, "Cannot delete category with existing transactions", HttpStatus.CONFLICT),
    SYSTEM_CATEGORY_IMMUTABLE(4014, "System categories cannot be modified or deleted", HttpStatus.FORBIDDEN),

    // --- Transaction ---
    TRANSACTION_NOT_FOUND(4020, "Transaction not found", HttpStatus.NOT_FOUND),
    TRANSACTION_NOT_OWNED(4021, "You do not have permission to modify this transaction", HttpStatus.FORBIDDEN),

    // --- Budget ---
    BUDGET_NOT_FOUND(4030, "Budget not found", HttpStatus.NOT_FOUND),
    BUDGET_NOT_OWNED(4031, "You do not have permission to modify this budget", HttpStatus.FORBIDDEN),
    BUDGET_DUPLICATED(4032, "A budget for this category in the specified month already exists", HttpStatus.CONFLICT),
    BUDGET_INCOME_NOT_ALLOWED(4033, "Budgets can only be created for EXPENSE categories", HttpStatus.BAD_REQUEST),

    // --- AI Insight ---
    AI_SERVICE_UNAVAILABLE(5010, "AI service is currently unavailable, please try again later", HttpStatus.SERVICE_UNAVAILABLE),
    AI_INSIGHT_NOT_FOUND(4040, "No AI Insight found for this period", HttpStatus.NOT_FOUND),

    // --- Export ---
    NO_DATA_TO_EXPORT(4050, "No data available to export for this period", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
