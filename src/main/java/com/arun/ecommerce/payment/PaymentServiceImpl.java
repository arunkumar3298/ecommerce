package com.arun.ecommerce.payment;

import com.arun.ecommerce.entity.Order;
import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.entity.enums.PaymentStatus;
import com.arun.ecommerce.exception.ResourceNotFoundException;
import com.arun.ecommerce.order.OrderService;
import com.arun.ecommerce.payment.dto.PaymentOrderRequest;
import com.arun.ecommerce.payment.dto.PaymentOrderResponse;
import com.arun.ecommerce.payment.dto.PaymentVerifyRequest;
import com.arun.ecommerce.payment.dto.PaymentVerifyResponse;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final OrderService   orderService;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public PaymentServiceImpl(RazorpayClient razorpayClient,
                              OrderService orderService) {
        this.razorpayClient = razorpayClient;
        this.orderService   = orderService;
    }

    @Override
    public PaymentOrderResponse createPaymentOrder(User user,
                                                   PaymentOrderRequest request) {
        Order order = orderService.getOrderEntityById(request.getOrderId());

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Unauthorized: This order does not belong to you");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "Order is already paid");
        }

        try {
            long amountInPaise = order.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",   amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt",  "order_" + order.getId());

            com.razorpay.Order razorpayOrder =
                    razorpayClient.orders.create(orderRequest);

            return PaymentOrderResponse.builder()
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .amount(order.getTotalAmount())
                    .currency("INR")
                    .keyId(keyId)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create Razorpay order: " + e.getMessage());
        }
    }

    @Override
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id",  request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature",  request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (!isValid) {
                return PaymentVerifyResponse.builder()
                        .success(false)
                        .message("Payment verification failed. Invalid signature.")
                        .orderId(request.getOrderId())
                        .paymentStatus("FAILED")
                        .build();
            }

            orderService.markAsPaid(request.getOrderId());

            return PaymentVerifyResponse.builder()
                    .success(true)
                    .message("Payment verified successfully!")
                    .orderId(request.getOrderId())
                    .paymentStatus("PAID")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Payment verification error: " + e.getMessage());
        }
    }
}
