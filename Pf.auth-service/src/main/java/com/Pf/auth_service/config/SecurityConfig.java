package com.Pf.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Bật tính năng kiểm tra quyền qua annotation @PreAuthorize
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Kích hoạt cấu hình CORS
            .csrf(AbstractHttpConfigurer::disable) // Tạm tắt CSRF vì chưa dùng token
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll() // Cho phép truy cập tự do
                .anyRequest().authenticated()
            );
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép các domain nào được gọi API (thay * bằng domain cụ thể trên production, ví dụ: "http://localhost:3000")
        configuration.setAllowedOriginPatterns(List.of("*")); 
        
        // Cho phép các HTTP method
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Cho phép các Header mang theo (quan trọng khi gửi Token)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        
        // Có cho phép gửi Cookie/Credential không? (nếu AllowedOrigins là * thì set này phải false hoặc dùng AllowedOriginPatterns)
        configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Áp dụng CORS cho tất cả API
        return source;
    }
}
