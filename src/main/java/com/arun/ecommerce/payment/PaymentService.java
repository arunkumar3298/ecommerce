package com.arun.ecommerce.payment;

import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.payment.dto.PaymentOrderRequest;
import com.arun.ecommerce.payment.dto.PaymentOrderResponse;
import com.arun.ecommerce.payment.dto.PaymentVerifyRequest;
import com.arun.ecommerce.payment.dto.PaymentVerifyResponse;

public interface PaymentService {
    PaymentOrderResponse  createPaymentOrder(User user, PaymentOrderRequest request);
    PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request);
}
