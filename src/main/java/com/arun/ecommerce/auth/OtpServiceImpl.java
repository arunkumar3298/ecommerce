package com.arun.ecommerce.auth;

import com.arun.ecommerce.messaging.EmailPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpServiceImpl implements OtpService {

    private static final int    OTP_LENGTH     = 6;
    private static final long   OTP_TTL_MINUTES = 5L;
    private static final String OTP_PREFIX     = "OTP:";

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailPublisher                emailPublisher;

    public OtpServiceImpl(RedisTemplate<String, String> redisTemplate,
                          EmailPublisher emailPublisher) {
        this.redisTemplate  = redisTemplate;
        this.emailPublisher = emailPublisher;
    }

    @Override
    public void generateAndSendOtp(String email) {
        String otpCode = generateOtp();

        // Store in Redis with 5-minute TTL
        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otpCode,
                Duration.ofMinutes(OTP_TTL_MINUTES));

        // Publish to RabbitMQ → async email ✅
        emailPublisher.publishOtpEmail(email, otpCode);
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {
        String storedOtp = redisTemplate.opsForValue()
                .get(OTP_PREFIX + email);

        if (storedOtp == null) {
            return false; // OTP expired or never generated
        }

        if (!storedOtp.equals(otpCode)) {
            return false; // Wrong OTP
        }

        // OTP verified — delete immediately (one-time use)
        redisTemplate.delete(OTP_PREFIX + email);
        return true;
    }

    // ── Private Helper ────────────────────────────────────────

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }
}
