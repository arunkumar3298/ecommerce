package com.arun.ecommerce.auth;

public interface OtpService {
    void    generateAndSendOtp(String email);
    boolean verifyOtp(String email, String otpCode);
}
