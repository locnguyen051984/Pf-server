package com.Pf.auth_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Áp dụng Rate Limiting cho tất cả các endpoint thuộc /api/**
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**"); 
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Cho phép tất cả các origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Các method cho phép
                .allowedHeaders("*") // Cho phép tất cả các header
                .allowCredentials(true) // Cho phép mang theo cookie/credential
                .maxAge(3600);
    }
}
