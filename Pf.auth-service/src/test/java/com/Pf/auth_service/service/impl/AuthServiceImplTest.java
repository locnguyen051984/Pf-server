package com.Pf.auth_service.service.impl;

import com.Pf.auth_service.dto.AuthResponse;
import com.Pf.auth_service.dto.LoginRequest;
import com.Pf.auth_service.dto.RegisterRequest;
import com.Pf.auth_service.dto.RegisterResponse;
import com.Pf.auth_service.entity.AuditLog;
import com.Pf.auth_service.entity.PasswordResetOtp;
import com.Pf.auth_service.entity.Role;
import com.Pf.auth_service.entity.User;
import com.Pf.auth_service.repository.AuditLogRepository;
import com.Pf.auth_service.repository.PasswordResetOtpRepository;
import com.Pf.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .passwordHash("hashedPassword123")
                .email("test@example.com")
                .fullName("Test User")
                .role(Role.CUSTOMER)
                .active(true)
                .build();
    }

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .email("new@example.com")
                .fullName("New User")
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("Đăng ký thành công!", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_Fail_UsernameExists() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        assertEquals("Username đã tồn tại", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void login_Fail_WrongPassword() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Sai tài khoản hoặc mật khẩu", exception.getMessage());
    }

    @Test
    void login_Fail_UserNotFound() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("unknownuser")
                .password("password123")
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Sai tài khoản hoặc mật khẩu", exception.getMessage());
    }

    @Test
    void forgotPassword_Success() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordResetOtpRepository.save(any(PasswordResetOtp.class))).thenReturn(new PasswordResetOtp());

        // Act
        assertDoesNotThrow(() -> authService.forgotPassword(email));

        // Assert
        verify(passwordResetOtpRepository, times(1)).deleteByUser(testUser);
        verify(passwordResetOtpRepository, times(1)).save(any(PasswordResetOtp.class));
    }

    @Test
    void forgotPassword_Fail_EmailNotFound() {
        // Arrange
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.forgotPassword(email);
        });

        assertEquals("Không tìm thấy người dùng với email này", exception.getMessage());
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        String otp = "123456";
        String newPassword = "newPassword123";
        PasswordResetOtp otpEntity = PasswordResetOtp.builder()
                .otp(otp)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        when(passwordResetOtpRepository.findByOtp(otp)).thenReturn(Optional.of(otpEntity));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // Act
        assertDoesNotThrow(() -> authService.resetPassword(otp, newPassword));

        // Assert
        verify(userRepository, times(1)).save(testUser);
        verify(passwordResetOtpRepository, times(1)).delete(otpEntity);
        assertEquals("encodedNewPassword", testUser.getPasswordHash());
    }

    @Test
    void resetPassword_Fail_OtpExpired() {
        // Arrange
        String otp = "123456";
        String newPassword = "newPassword123";
        PasswordResetOtp otpEntity = PasswordResetOtp.builder()
                .otp(otp)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusMinutes(5)) // Đã hết hạn
                .build();

        when(passwordResetOtpRepository.findByOtp(otp)).thenReturn(Optional.of(otpEntity));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.resetPassword(otp, newPassword);
        });

        assertEquals("Mã xác nhận đã hết hạn", exception.getMessage());
        verify(passwordResetOtpRepository, times(1)).delete(otpEntity);
        verify(userRepository, never()).save(any());
    }
}
