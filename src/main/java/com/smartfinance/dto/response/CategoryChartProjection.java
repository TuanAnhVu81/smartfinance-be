package com.smartfinance.dto.response;

import java.math.BigDecimal;

// Projection interface used by JPA to map GROUP BY results directly
public interface CategoryChartProjection {
    Long getCategoryId();
    String getCategoryName();
    String getIcon();
    String getColor();
    BigDecimal getTotalAmount();
}
