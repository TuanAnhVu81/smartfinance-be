package com.smartfinance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AiChatRequest(
    @NotBlank(message = "Message cannot be blank")
    String message,

    @NotNull(message = "Month is required")
    Integer month,

    @NotNull(message = "Year is required")
    Integer year
) {}
