package com.Pf.auth_service.repository;

import com.Pf.auth_service.entity.PasswordResetOtp;
import com.Pf.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findByOtp(String otp);
    void deleteByUser(User user);
}
