package com.smartfinance.service.impl;

import com.smartfinance.dto.request.LoginRequest;
import com.smartfinance.dto.request.RegisterRequest;
import com.smartfinance.dto.response.AuthResponse;
import com.smartfinance.entity.Role;
import com.smartfinance.entity.User;
import com.smartfinance.enums.RoleName;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.RoleRepository;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.security.JwtTokenProvider;
import com.smartfinance.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // Validate uniqueness of username and email before creating user
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR));

        User newUser = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .roles(Set.of(userRole))
                .build();

        userRepository.save(newUser);
        log.info("New user registered: username={}", request.username());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        // Validate password match using BCrypt
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Check if account is active
        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        log.info("User logged in: username={}", user.getUsername());
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        // Validate the incoming refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Verify user still exists and account is active
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(username);
        log.info("Access token refreshed for username={}", username);
        return new AuthResponse(newAccessToken, refreshToken);
    }
}
