package com.arun.ecommerce.security;

import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // Same secret as application.properties (Base64)
    private static final String SECRET =
            "dGhpc0lzQVN1cGVyU2VjcmV0S2V5Rm9yRWNvbW1lcmNlQXBwQXJ1bg==";
    private static final long EXPIRATION = 86400000L; // 24 hrs

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Inject @Value fields manually (no Spring context needed)
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

        // Build a real User object (no Mockito needed)
        testUser = User.builder()
                .name("Arun Kumar")
                .email("arun@test.com")
                .password("encoded_password")
                .role(Role.USER)
                .isVerified(true)
                .build();
    }

    // ── generateToken ─────────────────────────────────────────────

    @Test
    @DisplayName("generateToken: should return non-null token")
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken(testUser);
        assertNotNull(token);
    }

    @Test
    @DisplayName("generateToken: token should have 3 parts (header.payload.signature)")
    void generateToken_shouldHaveThreeParts() {
        String token = jwtService.generateToken(testUser);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    // ── extractEmail ──────────────────────────────────────────────

    @Test
    @DisplayName("extractEmail: should extract correct email from token")
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateToken(testUser);
        String extractedEmail = jwtService.extractEmail(token);
        assertEquals("arun@test.com", extractedEmail);
    }

    @Test
    @DisplayName("extractEmail: different users should produce different tokens")
    void extractEmail_differentUsers_differentTokens() {
        User anotherUser = User.builder()
                .name("Test Admin")
                .email("admin@test.com")
                .password("pass")
                .role(Role.ADMIN)
                .isVerified(true)
                .build();

        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(anotherUser);

        assertNotEquals(token1, token2);
        assertEquals("arun@test.com",  jwtService.extractEmail(token1));
        assertEquals("admin@test.com", jwtService.extractEmail(token2));
    }

    // ── isTokenValid ──────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid: valid token for correct user should return true")
    void isTokenValid_validToken_shouldReturnTrue() {
        String token = jwtService.generateToken(testUser);
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    @DisplayName("isTokenValid: token from userA should be invalid for userB")
    void isTokenValid_wrongUser_shouldReturnFalse() {
        User otherUser = User.builder()
                .name("Other")
                .email("other@test.com")
                .password("pass")
                .role(Role.USER)
                .isVerified(true)
                .build();

        String tokenForArun = jwtService.generateToken(testUser);

        // Arun's token should NOT be valid for otherUser
        assertFalse(jwtService.isTokenValid(tokenForArun, otherUser));
    }

    @Test
    @DisplayName("isTokenValid: expired token should return false")
    void isTokenValid_expiredToken_shouldReturnFalse() {
        // Create a JwtService with 1ms expiry → token expires immediately
        JwtService shortLivedService = new JwtService();
        ReflectionTestUtils.setField(shortLivedService, "secretKey", SECRET);
        ReflectionTestUtils.setField(shortLivedService, "jwtExpiration", 1L);

        String token = shortLivedService.generateToken(testUser);

        // Wait for token to expire
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        // ExpiredJwtException is expected — token is invalid, so catch and assert false
        boolean result;
        try {
            result = shortLivedService.isTokenValid(token, testUser);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            result = false; // expired = invalid ✅
        }

        assertFalse(result);
    }


    @Test
    @DisplayName("isTokenValid: ADMIN user token should be valid")
    void isTokenValid_adminUser_shouldReturnTrue() {
        User adminUser = User.builder()
                .name("Admin")
                .email("admin@test.com")
                .password("pass")
                .role(Role.ADMIN)
                .isVerified(true)
                .build();

        String token = jwtService.generateToken(adminUser);
        assertTrue(jwtService.isTokenValid(token, adminUser));
    }
}
