package com.arun.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OtpVerifyRequest {

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;

    public OtpVerifyRequest() {}

    public String getEmail() { return email; }
    public String getOtp()   { return otp; }

    public void setEmail(String email) { this.email = email; }
    public void setOtp(String otp)     { this.otp = otp; }
}
