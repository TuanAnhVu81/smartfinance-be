package com.smartfinance.service.impl;

import com.smartfinance.dto.response.UserResponse;
import com.smartfinance.entity.User;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.mapper.UserMapper;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean enabled, String currentUsername) {
        log.info("Updating status for user ID: {} to {}", userId, enabled);

        // 1. Find the target user first (fail-fast if not found)
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Resolve the current admin for self-protection
        User currentAdmin = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Prevent self-blocking
        if (currentAdmin.getId().equals(userId)) {
            log.error("Admin user ID {} attempted to block themselves", currentAdmin.getId());
            throw new AppException(ErrorCode.CANNOT_BLOCK_SELF);
        }

        userToUpdate.setIsActive(enabled);
        User savedUser = userRepository.save(userToUpdate);

        log.info("User ID {} status updated to {} by admin '{}'", userId, enabled, currentUsername);
        return userMapper.toResponse(savedUser);
    }
}
