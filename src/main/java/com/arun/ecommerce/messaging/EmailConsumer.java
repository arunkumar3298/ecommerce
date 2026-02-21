package com.arun.ecommerce.messaging;

import com.arun.ecommerce.auth.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer {

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    // ── Listens for OTP emails ────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.OTP_QUEUE)
    public void consumeOtpEmail(EmailMessage message) {
        emailService.sendOtpEmail(
                message.getTo(),
                message.getOtpCode());
    }

    // ── Listens for Order Confirmation emails ─────────────────
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void consumeOrderConfirmationEmail(
            OrderConfirmationMessage message) {
        emailService.sendOrderConfirmationEmail(
                message.getEmail(),
                message.getOrderId(),
                message.getTotalAmount());
    }
}
