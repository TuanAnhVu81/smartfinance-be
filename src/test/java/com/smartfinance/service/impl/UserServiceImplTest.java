package com.smartfinance.service.impl;

import com.smartfinance.dto.request.ChangePasswordRequest;
import com.smartfinance.dto.request.UpdateProfileRequest;
import com.smartfinance.dto.response.UserResponse;
import com.smartfinance.entity.User;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.mapper.UserMapper;
import com.smartfinance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        // Initialize common test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");

        testUserResponse = new UserResponse(1L, "testuser", "test@example.com", "Test User", null, null);
    }

    @Test
    void getProfile_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        UserResponse result = userService.getProfile("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userMapper, times(1)).toResponse(testUser);
    }

    @Test
    void getProfile_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> userService.getProfile("unknown"));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByUsername("unknown");
    }

    @Test
    void updateProfile_Success() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Updated Name", "http://example.com/avatar.png");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse(1L, "testuser", "test@example.com", "Updated Name", "http://example.com/avatar.png", null));

        // Act
        UserResponse result = userService.updateProfile("testuser", request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getFullName());
        assertEquals("http://example.com/avatar.png", result.getAvatarUrl());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void changePassword_Success() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncodedPassword");

        // Act
        userService.changePassword("testuser", request);

        // Assert
        verify(passwordEncoder, times(1)).encode("newPass");
        verify(userRepository, times(1)).save(testUser);
        assertEquals("newEncodedPassword", testUser.getPassword());
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> userService.changePassword("testuser", request));
        assertEquals(ErrorCode.WRONG_PASSWORD, exception.getErrorCode());
        verify(userRepository, times(0)).save(any(User.class));
    }
}
