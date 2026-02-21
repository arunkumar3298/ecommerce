package com.arun.ecommerce.auth;

import com.arun.ecommerce.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    // Gets latest unused OTP for this email — Spring Data JPA
    // auto-implements this just from the method name
    Optional<Otp> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);
}
