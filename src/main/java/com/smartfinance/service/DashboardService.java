package com.smartfinance.service;

import com.smartfinance.dto.response.CategoryChartResponse;
import com.smartfinance.dto.response.MonthlyChartResponse;
import com.smartfinance.dto.response.SummaryResponse;
import com.smartfinance.enums.CategoryType;

import java.util.List;

public interface DashboardService {

    // 1. Get summary (income, expense, balance) for a specific month and year
    SummaryResponse getSummary(Long userId, Integer month, Integer year);

    // 2. Get category breakdown (pie chart) for a specific month/year and category type
    List<CategoryChartResponse> getCategoryChart(Long userId, Integer month, Integer year, CategoryType type);

    // 3. Get monthly trend (bar chart) for all 12 months of a specific year
    List<MonthlyChartResponse> getMonthlyChart(Long userId, Integer year);
}
