package com.arun.ecommerce.messaging;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailPublisher {

    private final AmqpTemplate amqpTemplate;

    public EmailPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    // ── OTP Email ─────────────────────────────────────────────
    public void publishOtpEmail(String email, String otpCode) {
        EmailMessage message = new EmailMessage(email, otpCode);
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.OTP_ROUTING_KEY,
                message);
    }

    // ── Order Confirmation Email ──────────────────────────────
    public void publishOrderConfirmationEmail(String email,
                                              Long orderId,
                                              BigDecimal totalAmount) {
        OrderConfirmationMessage message =
                new OrderConfirmationMessage(email, orderId, totalAmount);
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                message);
    }
}
