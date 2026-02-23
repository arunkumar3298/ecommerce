package com.arun.ecommerce.auth;

import com.arun.ecommerce.messaging.EmailPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private EmailPublisher                emailPublisher;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpServiceImpl otpService;

    private static final String EMAIL      = "arun@test.com";
    private static final String OTP_PREFIX = "OTP:";

    @BeforeEach
    void setUp() {
        // RedisTemplate.opsForValue() must return our mock ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ── generateAndSendOtp() ──────────────────────────────────────

    @Test
    @DisplayName("generateAndSendOtp: should store OTP in Redis with correct key")
    void generateAndSendOtp_shouldStoreInRedisWithCorrectKey() {
        otpService.generateAndSendOtp(EMAIL);

        // Verify Redis set() called with key "OTP:arun@test.com"
        verify(valueOperations, times(1)).set(
                eq(OTP_PREFIX + EMAIL),
                anyString(),
                eq(Duration.ofMinutes(5))
        );
    }

    @Test
    @DisplayName("generateAndSendOtp: OTP stored should be exactly 6 digits")
    void generateAndSendOtp_otpShouldBeSixDigits() {
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);

        otpService.generateAndSendOtp(EMAIL);

        // Capture the OTP value passed to Redis
        verify(valueOperations).set(
                eq(OTP_PREFIX + EMAIL),
                otpCaptor.capture(),
                any(Duration.class)
        );

        String capturedOtp = otpCaptor.getValue();
        assertNotNull(capturedOtp);
        assertEquals(6, capturedOtp.length());
        assertTrue(capturedOtp.matches("\\d{6}"),
                "OTP must be exactly 6 numeric digits");
    }

    @Test
    @DisplayName("generateAndSendOtp: should publish OTP email via RabbitMQ")
    void generateAndSendOtp_shouldPublishEmailEvent() {
        otpService.generateAndSendOtp(EMAIL);

        verify(emailPublisher, times(1))
                .publishOtpEmail(eq(EMAIL), anyString());
    }

    @Test
    @DisplayName("generateAndSendOtp: OTP published to RabbitMQ should match OTP stored in Redis")
    void generateAndSendOtp_otpInRedisAndRabbitMQShouldMatch() {
        ArgumentCaptor<String> redisOtpCaptor   = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> rabbitOtpCaptor  = ArgumentCaptor.forClass(String.class);

        otpService.generateAndSendOtp(EMAIL);

        verify(valueOperations).set(
                eq(OTP_PREFIX + EMAIL),
                redisOtpCaptor.capture(),
                any(Duration.class)
        );
        verify(emailPublisher).publishOtpEmail(
                eq(EMAIL),
                rabbitOtpCaptor.capture()
        );

        // Both must be the SAME OTP value
        assertEquals(redisOtpCaptor.getValue(), rabbitOtpCaptor.getValue(),
                "OTP in Redis and RabbitMQ message must be identical");
    }

    @Test
    @DisplayName("generateAndSendOtp: TTL should be 5 minutes")
    void generateAndSendOtp_ttlShouldBeFiveMinutes() {
        ArgumentCaptor<Duration> durationCaptor =
                ArgumentCaptor.forClass(Duration.class);

        otpService.generateAndSendOtp(EMAIL);

        verify(valueOperations).set(
                anyString(),
                anyString(),
                durationCaptor.capture()
        );

        assertEquals(Duration.ofMinutes(5), durationCaptor.getValue());
    }

    // ── verifyOtp() ───────────────────────────────────────────────

    @Test
    @DisplayName("verifyOtp: correct OTP should return true")
    void verifyOtp_correctOtp_shouldReturnTrue() {
        when(valueOperations.get(OTP_PREFIX + EMAIL)).thenReturn("123456");

        boolean result = otpService.verifyOtp(EMAIL, "123456");

        assertTrue(result);
    }

    @Test
    @DisplayName("verifyOtp: correct OTP should delete it from Redis (one-time use)")
    void verifyOtp_correctOtp_shouldDeleteFromRedis() {
        when(valueOperations.get(OTP_PREFIX + EMAIL)).thenReturn("123456");

        otpService.verifyOtp(EMAIL, "123456");

        verify(redisTemplate, times(1)).delete(OTP_PREFIX + EMAIL);
    }

    @Test
    @DisplayName("verifyOtp: wrong OTP should return false")
    void verifyOtp_wrongOtp_shouldReturnFalse() {
        when(valueOperations.get(OTP_PREFIX + EMAIL)).thenReturn("123456");

        boolean result = otpService.verifyOtp(EMAIL, "999999");

        assertFalse(result);
    }

    @Test
    @DisplayName("verifyOtp: wrong OTP should NOT delete from Redis")
    void verifyOtp_wrongOtp_shouldNotDeleteFromRedis() {
        when(valueOperations.get(OTP_PREFIX + EMAIL)).thenReturn("123456");

        otpService.verifyOtp(EMAIL, "999999");

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("verifyOtp: expired OTP (null in Redis) should return false")
    void verifyOtp_expiredOtp_shouldReturnFalse() {
        // Redis returns null → OTP expired or never generated
        when(valueOperations.get(OTP_PREFIX + EMAIL)).thenReturn(null);

        boolean result = otpService.verifyOtp(EMAIL, "123456");

        assertFalse(result);
    }

    @Test
    @DisplayName("verifyOtp: expired OTP should NOT delete from Redis")
    void verifyOtp_expiredOtp_shouldNotDeleteFromRedis() {
        when(valueOperations.get(OTP_PREFIX + EMAIL)).thenReturn(null);

        otpService.verifyOtp(EMAIL, "123456");

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("verifyOtp: two consecutive correct verifications — second should fail")
    void verifyOtp_usedOtp_secondAttemptShouldFail() {
        // First call returns OTP, second call returns null (deleted)
        when(valueOperations.get(OTP_PREFIX + EMAIL))
                .thenReturn("123456")
                .thenReturn(null);

        boolean first  = otpService.verifyOtp(EMAIL, "123456");
        boolean second = otpService.verifyOtp(EMAIL, "123456");

        assertTrue(first,   "First verification must succeed");
        assertFalse(second, "Second verification must fail — OTP is one-time use");
    }
}
