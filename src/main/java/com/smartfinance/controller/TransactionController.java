package com.smartfinance.controller;

import com.smartfinance.dto.request.TransactionRequest;
import com.smartfinance.dto.response.TransactionResponse;
import com.smartfinance.entity.User;
import com.smartfinance.exception.ApiResponse;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Transaction management for tracking incomes and expenses")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get list of user transactions with pagination and optional filters")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long categoryId,
            @ParameterObject @PageableDefault(sort = {"transactionDate", "createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = resolveUserId(userDetails);
        Page<TransactionResponse> page = transactionService.getTransactions(userId, month, year, categoryId, pageable);

        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PostMapping
    @Operation(summary = "Create a new transaction and adjust budget if applicable")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest request) {

        Long userId = resolveUserId(userDetails);
        TransactionResponse response = transactionService.createTransaction(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transaction created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transaction and readjust budget delta")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        Long userId = resolveUserId(userDetails);
        TransactionResponse response = transactionService.updateTransaction(userId, id, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Transaction updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a transaction and rollback budget spending")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = resolveUserId(userDetails);
        transactionService.deleteTransaction(userId, id);

        return ResponseEntity.ok(ApiResponse.success(null, "Transaction deleted successfully"));
    }

    // Helper: resolve Long userId from the authenticated UserDetails (username)
    private Long resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
