package com.Pf.auth_service.service.impl;

import com.Pf.auth_service.dto.AuthResponse;
import com.Pf.auth_service.dto.GoogleLoginRequest;
import com.Pf.auth_service.dto.LoginRequest;
import com.Pf.auth_service.dto.RegisterRequest;
import com.Pf.auth_service.dto.RegisterResponse;
import com.Pf.auth_service.entity.AuditLog;
import com.Pf.auth_service.entity.Role;
import com.Pf.auth_service.entity.User;
import com.Pf.auth_service.repository.AuditLogRepository;
import com.Pf.auth_service.repository.UserRepository;
import com.Pf.auth_service.service.AuthService;
import com.Pf.auth_service.exception.InvalidCredentialsException;
import com.Pf.auth_service.exception.InvalidTokenException;
import com.Pf.auth_service.exception.PhoneAlreadyExistsException;
import com.Pf.auth_service.exception.UserAlreadyExistsException;
import com.Pf.auth_service.exception.UserNotFoundException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id:YOUR_GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username đã tồn tại");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException("Số điện thoại đã được sử dụng");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
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
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new InvalidCredentialsException("Sai số điện thoại hoặc mật khẩu"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Sai số điện thoại hoặc mật khẩu");
        }

        AuditLog logAction = AuditLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .action("LOGIN")
                .details("Người dùng đăng nhập thành công bằng Số điện thoại")
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(logAction);

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdToken idToken;
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            idToken = verifier.verify(request.getIdToken());
        } catch (Exception e) {
            log.error("Google login failed", e);
            throw new InvalidTokenException("Đăng nhập bằng Google thất bại: " + e.getMessage(), e);
        }

        if (idToken == null) {
            throw new InvalidTokenException("ID Token của Google không hợp lệ.");
        }

        Payload payload = idToken.getPayload();
        String userId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        User user = userRepository.findByGoogleAccountId(userId).orElse(null);

        if (user == null && email != null) {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setGoogleAccountId(userId);
                userRepository.save(user);
            }
        }

        if (user == null) {
            user = User.builder()
                    .googleAccountId(userId)
                    .email(email)
                    .username("google_" + userId)
                    .fullName(name)
                    .role(Role.CUSTOMER)
                    .active(true)
                    .build();
            userRepository.save(user);
        }

        AuditLog logAction = AuditLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .action("GOOGLE_LOGIN")
                .details("Người dùng đăng nhập bằng Google")
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(logAction);

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public List<com.Pf.auth_service.dto.UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToUserDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void banUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + id));
        user.setActive(false);
        userRepository.save(user);
        
        AuditLog audit = AuditLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .action("BAN_USER")
                .details("Tài khoản đã bị khóa (ban)")
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(audit);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        banUser(id);
    }

    private com.Pf.auth_service.dto.UserDTO mapToUserDTO(User user) {
        return com.Pf.auth_service.dto.UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
