package com.smartfinance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSummaryResponse {
    private long totalUsers;
    private long totalAdmins;
    private long totalLockedUsers;
}
