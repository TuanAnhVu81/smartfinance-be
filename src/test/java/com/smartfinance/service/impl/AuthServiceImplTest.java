package com.smartfinance.service.impl;

import com.smartfinance.dto.request.LoginRequest;
import com.smartfinance.dto.request.RegisterRequest;
import com.smartfinance.dto.response.AuthResponse;
import com.smartfinance.entity.Role;
import com.smartfinance.entity.User;
import com.smartfinance.enums.RoleName;
import com.smartfinance.exception.AppException;
import com.smartfinance.dto.response.UserResponse;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.mapper.UserMapper;
import com.smartfinance.repository.RoleRepository;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Setup common objects
        testRole = new Role(1L, RoleName.ROLE_USER);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(testRole));
    }

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest("newuser", "test@example.com", "password", "New User");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        authService.register(request);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_UsernameExisted_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existinguser", "test@example.com", "password", "New User");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.register(request));
        assertEquals(ErrorCode.USERNAME_EXISTED, exception.getErrorCode());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("refreshToken");
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.login(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    void login_AccountLocked_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password");
        testUser.setIsActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.login(request));
        assertEquals(ErrorCode.ACCOUNT_LOCKED, exception.getErrorCode());
    }

    @Test
    void refresh_Success() {
        // Arrange
        when(jwtTokenProvider.validateToken("validRefresh")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("validRefresh")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn("newAccessToken");
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        // Act
        AuthResponse response = authService.refresh("validRefresh");

        // Assert
        assertNotNull(response);
        assertEquals("newAccessToken", response.accessToken());
        assertEquals("validRefresh", response.refreshToken());
    }
}
