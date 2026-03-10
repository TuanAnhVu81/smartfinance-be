package com.smartfinance.service.impl;

import com.smartfinance.dto.response.CategoryChartProjection;
import com.smartfinance.dto.response.CategoryChartResponse;
import com.smartfinance.dto.response.MonthlyChartResponse;
import com.smartfinance.dto.response.MonthlyTrendProjection;
import com.smartfinance.dto.response.SummaryResponse;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.repository.TransactionRepository;
import com.smartfinance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    @Override
    public SummaryResponse getSummary(Long userId, Integer month, Integer year) {
        LocalDate startDate = getStartDate(month, year);
        LocalDate endDate = getEndDate(month, year);

        BigDecimal totalIncome = transactionRepository.calculateTotalAmountByTypeAndPeriod(
                userId, CategoryType.INCOME, startDate, endDate);
        
        BigDecimal totalExpense = transactionRepository.calculateTotalAmountByTypeAndPeriod(
                userId, CategoryType.EXPENSE, startDate, endDate);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }

    @Override
    public List<CategoryChartResponse> getCategoryChart(Long userId, Integer month, Integer year, CategoryType type) {
        LocalDate startDate = getStartDate(month, year);
        LocalDate endDate = getEndDate(month, year);

        // Fetch sum total first to calculate percentages
        BigDecimal totalCategoryAmount = transactionRepository.calculateTotalAmountByTypeAndPeriod(
                userId, type, startDate, endDate);

        List<CategoryChartProjection> projections = transactionRepository.getCategoryBreakdownByPeriod(
                userId, type, startDate, endDate);

        return projections.stream().map(proj -> {
            BigDecimal percentage = BigDecimal.ZERO;
            if (totalCategoryAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = proj.getTotalAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalCategoryAmount, 2, RoundingMode.HALF_UP);
            }

            return CategoryChartResponse.builder()
                    .categoryId(proj.getCategoryId())
                    .categoryName(proj.getCategoryName())
                    .icon(proj.getIcon())
                    .color(proj.getColor())
                    .totalAmount(proj.getTotalAmount())
                    .percentage(percentage)
                    .build();
        }).toList();
    }

    @Override
    public List<MonthlyChartResponse> getMonthlyChart(Long userId, Integer year) {
        // Full year range: Jan 1st to Dec 31st
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<MonthlyTrendProjection> rawProjections = transactionRepository.getMonthlyTrendByPeriod(
                userId, startDate, endDate);

        // Build a map for quick lookup: [Month] -> [Map of Type -> Amount]
        Map<Integer, Map<CategoryType, BigDecimal>> groupedData = rawProjections.stream()
                .collect(Collectors.groupingBy(
                        MonthlyTrendProjection::getMonth,
                        Collectors.toMap(
                                MonthlyTrendProjection::getType,
                                MonthlyTrendProjection::getTotalAmount
                        )
                ));

        // Generate full 12 months guarantee
        List<MonthlyChartResponse> responses = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<CategoryType, BigDecimal> monthData = groupedData.getOrDefault(m, Map.of());
            
            BigDecimal totalIncome = monthData.getOrDefault(CategoryType.INCOME, BigDecimal.ZERO);
            BigDecimal totalExpense = monthData.getOrDefault(CategoryType.EXPENSE, BigDecimal.ZERO);

            responses.add(MonthlyChartResponse.builder()
                    .month(m)
                    .totalIncome(totalIncome)
                    .totalExpense(totalExpense)
                    .build());
        }

        return responses;
    }

    // --- Helpers for Date Ranges ---
    private LocalDate getStartDate(int month, int year) {
        return LocalDate.of(year, month, 1);
    }

    private LocalDate getEndDate(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return yearMonth.atEndOfMonth();
    }
}
