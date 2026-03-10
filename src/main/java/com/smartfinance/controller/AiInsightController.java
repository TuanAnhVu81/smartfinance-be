package com.smartfinance.controller;

import com.smartfinance.dto.response.AiInsightResponse;
import com.smartfinance.entity.User;
import com.smartfinance.exception.ApiResponse;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.AiInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ai-insights")
@RequiredArgsConstructor
@Tag(name = "AI Insight", description = "Gemini-powered personal finance advisor with smart caching")
@SecurityRequirement(name = "bearerAuth")
public class AiInsightController {

    private final AiInsightService aiInsightService;
    private final UserRepository userRepository;

    /**
     * GET /api/ai-insights?month=5&year=2026
     * Returns cached AI insight or generates a new one if stale/missing.
     */
    @GetMapping
    @Operation(summary = "Get AI financial insight (uses smart cache)")
    public ResponseEntity<ApiResponse<AiInsightResponse>> getInsight(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        Long userId = resolveUserId(userDetails);
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int effectiveYear  = (year  != null) ? year  : LocalDate.now().getYear();

        AiInsightResponse response = aiInsightService.getInsight(userId, effectiveMonth, effectiveYear);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/ai-insights/refresh?month=5&year=2026
     * Force re-calls Gemini and refreshes the cached insight regardless of freshness flag.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Force refresh AI insight (bypasses cache)")
    public ResponseEntity<ApiResponse<AiInsightResponse>> refreshInsight(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        Long userId = resolveUserId(userDetails);
        int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int effectiveYear  = (year  != null) ? year  : LocalDate.now().getYear();

        AiInsightResponse response = aiInsightService.refreshInsight(userId, effectiveMonth, effectiveYear);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Long resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
