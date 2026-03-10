package com.smartfinance.service;

import com.smartfinance.dto.response.AiInsightResponse;

public interface AiInsightService {

    // Get AI insight for a month (uses cache if fresh, calls Gemini if outdated)
    AiInsightResponse getInsight(Long userId, Integer month, Integer year);

    // Force refresh: bypass cache and call Gemini unconditionally
    AiInsightResponse refreshInsight(Long userId, Integer month, Integer year);

    // Called by TransactionService to mark insight as outdated when data changes
    void markOutdated(Long userId, Integer month, Integer year);
}
