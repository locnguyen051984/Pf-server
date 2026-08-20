package com.Pf.auth_service.service;

import com.Pf.auth_service.dto.AuthResponse;
import com.Pf.auth_service.dto.LoginRequest;
import com.Pf.auth_service.dto.RegisterRequest;
import com.Pf.auth_service.dto.RegisterResponse;
import com.Pf.auth_service.dto.UserDTO;
import java.util.List;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    
    void forgotPassword(String email);
    void resetPassword(String otp, String newPassword);
    
    List<UserDTO> getAllUsers();
    void banUser(Long id);
    void deleteUser(Long id);
}
