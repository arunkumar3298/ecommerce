package com.arun.ecommerce.auth;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── OTP Email ─────────────────────────────────────────────

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP — E-Commerce App");
        message.setText(
                "Your OTP is: " + otpCode
                        + "\n\nThis OTP is valid for 5 minutes."
                        + "\nDo not share this with anyone.");
        mailSender.send(message);
    }

    // ── Order Confirmation Email ──────────────────────────────
    // Week 3: RabbitMQ consumer will call this method
    // OrderServiceImpl stays untouched — only this impl changes ✅

    @Override
    public void sendOrderConfirmationEmail(String toEmail,
                                           Long orderId,
                                           BigDecimal totalAmount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Confirmed #" + orderId + " — E-Commerce App");
        message.setText(
                "Hi! Your order #" + orderId + " has been placed successfully."
                        + "\n\nTotal Amount: ₹" + totalAmount
                        + "\n\nWe will notify you once it is shipped."
                        + "\n\nThank you for shopping with us!");
        mailSender.send(message);
    }
}
