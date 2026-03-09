package com.smartfinance.service;

import com.smartfinance.dto.request.LoginRequest;
import com.smartfinance.dto.request.RegisterRequest;
import com.smartfinance.dto.response.AuthResponse;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
}
