package com.smartfinance.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        String avatarUrl
) {}
