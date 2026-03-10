package com.smartfinance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private CategoryResponse category;
    private Integer month;
    private Integer year;
    private BigDecimal amountLimit;
    private BigDecimal spentAmount;
    // Percentage calculated at service layer to avoid division-by-zero in frontend
    private BigDecimal percentage;
}
