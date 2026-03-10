package com.smartfinance.service;

import com.smartfinance.dto.request.TransactionRequest;
import com.smartfinance.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    Page<TransactionResponse> getTransactions(Long userId, Integer month, Integer year, Long categoryId, Pageable pageable);

    TransactionResponse createTransaction(Long userId, TransactionRequest request);

    TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest request);

    void deleteTransaction(Long userId, Long transactionId);
}
