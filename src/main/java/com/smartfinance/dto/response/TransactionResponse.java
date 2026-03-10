package com.smartfinance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private CategoryResponse category;
    private BigDecimal amount;
    private String note;
    private LocalDate transactionDate;
    private LocalDate createdAt;
}
