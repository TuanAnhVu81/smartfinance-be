package com.smartfinance.service.impl;

import com.smartfinance.dto.request.ChangePasswordRequest;
import com.smartfinance.dto.request.UpdateProfileRequest;
import com.smartfinance.dto.response.UserResponse;
import com.smartfinance.entity.User;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getProfile(String username) {
        User user = findUserByUsername(username);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findUserByUsername(username);

        // Update only non-null fields from request
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }

        userRepository.save(user);
        log.info("Profile updated for username={}", username);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findUserByUsername(username);

        // Verify the current password before allowing change
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for username={}", username);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
