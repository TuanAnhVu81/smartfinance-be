package com.smartfinance.service;

import com.smartfinance.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse updateUserStatus(Long userId, boolean enabled, String currentUsername);
}
