package com.smartfinance.controller;

import com.smartfinance.dto.response.CategoryChartResponse;
import com.smartfinance.dto.response.MonthlyChartResponse;
import com.smartfinance.dto.response.SummaryResponse;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.ApiResponse;
import com.smartfinance.security.UserPrincipal;
import com.smartfinance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregate reporting APIs for UI Charts")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get total income, expense, and balance for a month")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int currentMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int currentYear = (year != null) ? year : LocalDate.now().getYear();

        SummaryResponse response = dashboardService.getSummary(principal.getId(), currentMonth, currentYear);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/chart/category")
    @Operation(summary = "Get category breakdown (Pie Chart) with percentage")
    public ResponseEntity<ApiResponse<List<CategoryChartResponse>>> getCategoryChart(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam CategoryType type,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int currentMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int currentYear = (year != null) ? year : LocalDate.now().getYear();

        List<CategoryChartResponse> response = dashboardService.getCategoryChart(principal.getId(), currentMonth, currentYear, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/chart/monthly")
    @Operation(summary = "Get income vs. expense trend for all 12 months (Bar Chart)")
    public ResponseEntity<ApiResponse<List<MonthlyChartResponse>>> getMonthlyChart(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year) {

        int currentYear = (year != null) ? year : LocalDate.now().getYear();

        List<MonthlyChartResponse> response = dashboardService.getMonthlyChart(principal.getId(), currentYear);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
