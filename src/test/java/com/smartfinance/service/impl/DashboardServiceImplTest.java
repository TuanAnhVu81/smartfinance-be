package com.smartfinance.service.impl;

import com.smartfinance.dto.response.CategoryChartProjection;
import com.smartfinance.dto.response.CategoryChartResponse;
import com.smartfinance.dto.response.MonthlyChartResponse;
import com.smartfinance.dto.response.MonthlyTrendProjection;
import com.smartfinance.dto.response.SummaryResponse;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getSummary_Success() {
        // Arrange
        Long userId = 1L;
        Integer month = 10;
        Integer year = 2023;
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);

        when(transactionRepository.calculateTotalAmountByTypeAndPeriod(userId, CategoryType.INCOME, startDate, endDate))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.calculateTotalAmountByTypeAndPeriod(userId, CategoryType.EXPENSE, startDate, endDate))
                .thenReturn(new BigDecimal("2000.00"));

        // Act
        SummaryResponse response = dashboardService.getSummary(userId, month, year);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome());
        assertEquals(new BigDecimal("2000.00"), response.getTotalExpense());
        assertEquals(new BigDecimal("3000.00"), response.getBalance());
    }

    @Test
    void getCategoryChart_Success() {
        // Arrange
        Long userId = 1L;
        Integer month = 10;
        Integer year = 2023;
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);

        when(transactionRepository.calculateTotalAmountByTypeAndPeriod(userId, CategoryType.EXPENSE, startDate, endDate))
                .thenReturn(new BigDecimal("1000.00"));

        CategoryChartProjection proj = new CategoryChartProjection() {
            @Override public Long getCategoryId() { return 1L; }
            @Override public String getCategoryName() { return "Food"; }
            @Override public String getIcon() { return "icon"; }
            @Override public String getColor() { return "color"; }
            @Override public BigDecimal getTotalAmount() { return new BigDecimal("500.00"); }
        };

        when(transactionRepository.getCategoryBreakdownByPeriod(userId, CategoryType.EXPENSE, startDate, endDate))
                .thenReturn(List.of(proj));

        // Act
        List<CategoryChartResponse> responses = dashboardService.getCategoryChart(userId, month, year, CategoryType.EXPENSE);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Food", responses.get(0).getCategoryName());
        assertEquals(new BigDecimal("500.00"), responses.get(0).getTotalAmount());
        assertEquals(new BigDecimal("50.00"), responses.get(0).getPercentage());
    }

    @Test
    void getMonthlyChart_Success() {
        // Arrange
        Long userId = 1L;
        Integer year = 2023;
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        MonthlyTrendProjection proj = new MonthlyTrendProjection() {
            @Override public Integer getMonth() { return 10; }
            @Override public CategoryType getType() { return CategoryType.EXPENSE; }
            @Override public BigDecimal getTotalAmount() { return new BigDecimal("1500.00"); }
        };

        when(transactionRepository.getMonthlyTrendByPeriod(userId, startDate, endDate)).thenReturn(List.of(proj));

        // Act
        List<MonthlyChartResponse> responses = dashboardService.getMonthlyChart(userId, year);

        // Assert
        assertNotNull(responses);
        assertEquals(12, responses.size());
        
        MonthlyChartResponse octResponse = responses.get(9); // 0-indexed, so 9 is October
        assertEquals(10, octResponse.getMonth());
        assertEquals(new BigDecimal("1500.00"), octResponse.getTotalExpense());
        assertEquals(BigDecimal.ZERO, octResponse.getTotalIncome());
    }
}
