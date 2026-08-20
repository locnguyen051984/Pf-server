package com.Pf.auth_service.controller;

import com.Pf.auth_service.dto.LoginRequest;
import com.Pf.auth_service.dto.RegisterRequest;
import com.Pf.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<com.Pf.auth_service.dto.RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            com.Pf.auth_service.dto.RegisterResponse result = authService.register(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<com.Pf.auth_service.dto.AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            com.Pf.auth_service.dto.AuthResponse result = authService.login(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==========================================
    // TEST ROLE (Sẽ cần user có token hợp lệ để test)
    // ==========================================

    @org.springframework.web.bind.annotation.GetMapping("/admin-test")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testAdminRole() {
        return ResponseEntity.ok("Xin chào ADMIN, bạn đã được cấp quyền truy cập API này!");
    }

    @org.springframework.web.bind.annotation.GetMapping("/user-test")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> testUserRole() {
        return ResponseEntity.ok("Xin chào CUSTOMER hoặc ADMIN, API này dành cho bạn!");
    }
}
