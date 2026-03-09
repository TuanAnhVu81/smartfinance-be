package com.smartfinance.service;

import com.smartfinance.dto.request.ChangePasswordRequest;
import com.smartfinance.dto.request.UpdateProfileRequest;
import com.smartfinance.dto.response.UserResponse;

public interface UserService {
    UserResponse getProfile(String username);
    UserResponse updateProfile(String username, UpdateProfileRequest request);
    void changePassword(String username, ChangePasswordRequest request);
}
