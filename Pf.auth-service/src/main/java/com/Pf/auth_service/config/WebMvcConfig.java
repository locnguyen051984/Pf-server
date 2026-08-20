package com.Pf.auth_service.config;

import org.springframework.context.annotation.Configuration;
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
        // Có thể thay đổi path để phù hợp với router thực tế
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**"); 
    }
}
