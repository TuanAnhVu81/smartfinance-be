package com.smartfinance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsightResponse {
    private Long id;
    private Integer month;
    private Integer year;
    private String aiResponse;
    private Boolean isOutdated;
    private LocalDateTime updatedAt; // When the AI last refreshed this insight
}
