package com.smartfinance.dto.response;

import com.smartfinance.entity.User;

// User profile response – never expose the password or internal fields
public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String avatarUrl,
        Boolean isActive
) {
    // Static factory: map User entity → UserResponse DTO
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getIsActive()
        );
    }
}
