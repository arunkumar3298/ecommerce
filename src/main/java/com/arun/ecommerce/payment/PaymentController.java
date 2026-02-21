package com.arun.ecommerce.payment;

import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.payment.dto.PaymentOrderRequest;
import com.arun.ecommerce.payment.dto.PaymentOrderResponse;
import com.arun.ecommerce.payment.dto.PaymentVerifyRequest;
import com.arun.ecommerce.payment.dto.PaymentVerifyResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Step 1 — Create Razorpay order (user initiates payment)
    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PaymentOrderRequest request) {
        return ResponseEntity.ok(
                paymentService.createPaymentOrder(currentUser, request));
    }

    // Step 2 — Verify payment signature (after Razorpay payment)
    @PostMapping("/verify")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }
}
