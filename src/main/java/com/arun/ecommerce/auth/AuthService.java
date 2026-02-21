package com.arun.ecommerce.auth;

import com.arun.ecommerce.auth.dto.AuthResponse;
import com.arun.ecommerce.auth.dto.LoginRequest;
import com.arun.ecommerce.auth.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
