package com.smartfinance.dto.response;

import com.smartfinance.enums.CategoryType;
import java.math.BigDecimal;

// Projection interface for Monthly Trend GROUP BY query
public interface MonthlyTrendProjection {
    Integer getMonth();
    CategoryType getType();
    BigDecimal getTotalAmount();
}
