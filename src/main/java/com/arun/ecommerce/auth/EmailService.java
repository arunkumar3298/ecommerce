package com.arun.ecommerce.auth;

import java.math.BigDecimal;

public interface EmailService {

    // ── Auth ──────────────────────────────────────────────────
    void sendOtpEmail(String toEmail, String otpCode);

    // ── Order ─────────────────────────────────────────────────
    // Week 3: this gets replaced by RabbitMQ internally
    // — interface stays the same, only impl changes (OCP ✅)
    void sendOrderConfirmationEmail(String toEmail, Long orderId,
                                    BigDecimal totalAmount);
}
