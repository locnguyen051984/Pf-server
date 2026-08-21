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
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
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
        Optional<User> userOptional = userRepository.findByPhone(request.getPhone());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPasswordHash() != null && passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                
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
        }
        throw new RuntimeException("Sai số điện thoại hoặc mật khẩu");
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken != null) {
                Payload payload = idToken.getPayload();
                String userId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                Optional<User> userOptional = userRepository.findByGoogleAccountId(userId);
                User user;

                if (userOptional.isPresent()) {
                    user = userOptional.get();
                } else {
                    // Kiểm tra xem email đã tồn tại do đăng ký trước đó hay không
                    Optional<User> emailUserOptional = email != null ? userRepository.findByEmail(email) : Optional.empty();
                    if (emailUserOptional.isPresent()) {
                        user = emailUserOptional.get();
                        user.setGoogleAccountId(userId);
                        userRepository.save(user);
                    } else {
                        // Tạo user mới nếu chưa tồn tại
                        user = User.builder()
                                .googleAccountId(userId)
                                .email(email)
                                .username("google_" + userId) // Tự sinh username
                                .fullName(name)
                                .role(Role.CUSTOMER)
                                .active(true)
                                .build();
                        userRepository.save(user);
                    }
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

            } else {
                throw new RuntimeException("ID Token của Google không hợp lệ.");
            }
        } catch (Exception e) {
            log.error("Google login failed", e);
            throw new RuntimeException("Đăng nhập bằng Google thất bại: " + e.getMessage());
        }
    }

    @Override
    public List<com.Pf.auth_service.dto.UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToUserDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void banUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
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
