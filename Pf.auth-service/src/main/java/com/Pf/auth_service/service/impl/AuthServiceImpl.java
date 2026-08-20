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
import com.Pf.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.CUSTOMER) // Mặc định role là CUSTOMER
                .active(true)
                .build();

        userRepository.save(user);
        return RegisterResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .message("Đăng ký thành công!")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                
                AuditLog log = AuditLog.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .action("LOGIN")
                        .details("Người dùng đăng nhập thành công")
                        .createdAt(LocalDateTime.now())
                        .build();
                auditLogRepository.save(log);

                return AuthResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build();
            }
        }
        throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email này"));

        passwordResetOtpRepository.deleteByUser(user);

        String otp = String.format("%06d", new Random().nextInt(999999));

        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .otp(otp)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();
        passwordResetOtpRepository.save(resetOtp);

        log.info("=====================================================");
        log.info("📧 GIẢ LẬP GỬI EMAIL");
        log.info("Đến: {}", email);
        log.info("Nội dung: Mã xác nhận đổi mật khẩu của bạn là: {}", otp);
        log.info("=====================================================");
    }

    @Override
    @Transactional
    public void resetPassword(String otp, String newPassword) {
        PasswordResetOtp resetOtp = passwordResetOtpRepository.findByOtp(otp)
                .orElseThrow(() -> new RuntimeException("Mã xác nhận không tồn tại hoặc không chính xác"));

        if (resetOtp.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetOtpRepository.delete(resetOtp);
            throw new RuntimeException("Mã xác nhận đã hết hạn");
        }

        User user = resetOtp.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetOtpRepository.delete(resetOtp);
        log.info("Người dùng {} đã đổi mật khẩu thành công", user.getUsername());
    }
}
