package com.arun.ecommerce.auth;

import com.arun.ecommerce.auth.dto.AuthResponse;
import com.arun.ecommerce.auth.dto.LoginRequest;
import com.arun.ecommerce.auth.dto.RegisterRequest;
import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.entity.enums.Role;
import com.arun.ecommerce.exception.ResourceNotFoundException;
import com.arun.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository        userRepository;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private JwtService            jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest    loginRequest;
    private User            savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Arun Kumar");
        registerRequest.setEmail("arun@test.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("arun@test.com");
        loginRequest.setPassword("password123");

        savedUser = User.builder()
                .name("Arun Kumar")
                .email("arun@test.com")
                .password("encoded_password")
                .role(Role.USER)
                .isVerified(true)
                .build();
    }

    // ── register() ────────────────────────────────────────────────

    @Test
    @DisplayName("register: new email should save user successfully")
    void register_newEmail_shouldSaveUser() {
        when(userRepository.existsByEmail("arun@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        authService.register(registerRequest);

        // Verify save() was called exactly once with a User object
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register: should encode password before saving")
    void register_shouldEncodePassword() {
        when(userRepository.existsByEmail("arun@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        authService.register(registerRequest);

        // Raw password must never be saved
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("register: should assign Role.USER by default")
    void register_shouldAssignRoleUser() {
        when(userRepository.existsByEmail("arun@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        // Capture the User passed to save()
        authService.register(registerRequest);

        verify(userRepository).save(argThat(user ->
                user.getRole() == Role.USER
        ));
    }

    @Test
    @DisplayName("register: should set isVerified=false by default")
    void register_shouldSetIsVerifiedFalse() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        authService.register(registerRequest);

        verify(userRepository).save(argThat(user ->
                !user.isVerified()
        ));
    }

    @Test
    @DisplayName("register: duplicate email should throw RuntimeException")
    void register_duplicateEmail_shouldThrowException() {
        when(userRepository.existsByEmail("arun@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertEquals("Email already registered", ex.getMessage());

        // save() must NEVER be called for duplicate email
        verify(userRepository, never()).save(any());
    }

    // ── login() ───────────────────────────────────────────────────

    @Test
    @DisplayName("login: valid credentials should return AuthResponse with token")
    void login_validCredentials_shouldReturnAuthResponse() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("arun@test.com"))
                .thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(savedUser)).thenReturn("mocked.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("arun@test.com",    response.getEmail());
        assertEquals("Arun Kumar",       response.getName());
        assertEquals("USER",             response.getRole());
    }

    @Test
    @DisplayName("login: should call AuthenticationManager with correct credentials")
    void login_shouldCallAuthenticationManager() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(any())).thenReturn("token");

        authService.login(loginRequest);

        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(
                        "arun@test.com", "password123")
        );
    }

    @Test
    @DisplayName("login: wrong password should throw BadCredentialsException")
    void login_wrongPassword_shouldThrowException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));

        // findByEmail must never be called if auth fails
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("login: non-existent user should throw ResourceNotFoundException")
    void login_userNotFound_shouldThrowException() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("arun@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.login(loginRequest));

        // generateToken must never be called
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login: should call generateToken exactly once")
    void login_shouldCallGenerateTokenOnce() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(savedUser)).thenReturn("token");

        authService.login(loginRequest);

        verify(jwtService, times(1)).generateToken(savedUser);
    }
}
