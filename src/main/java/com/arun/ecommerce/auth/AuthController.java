package com.arun.ecommerce.auth;

import com.arun.ecommerce.auth.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService  otpService;

    public AuthController(AuthService authService,
                          OtpService otpService) {
        this.authService = authService;
        this.otpService  = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful. Please verify your email.");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(
            @Valid @RequestBody OtpRequest request) {
        otpService.generateAndSendOtp(request.getEmail()); // ✅ fixed
        return ResponseEntity.ok("OTP sent to " + request.getEmail());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        boolean isValid = otpService.verifyOtp(         // ✅ fixed
                request.getEmail(),
                request.getOtp());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid or expired OTP.");
        }

        return ResponseEntity.ok("Email verified. You can now login.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
