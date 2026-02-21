package com.arun.ecommerce.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentVerifyRequest {

    @NotNull(message = "Order ID is required")
    private Long   orderId;

    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Signature is required")
    private String razorpaySignature;

    public PaymentVerifyRequest() {}

    public Long   getOrderId()            { return orderId; }
    public String getRazorpayOrderId()    { return razorpayOrderId; }
    public String getRazorpayPaymentId()  { return razorpayPaymentId; }
    public String getRazorpaySignature()  { return razorpaySignature; }

    public void setOrderId(Long orderId)                      { this.orderId = orderId; }
    public void setRazorpayOrderId(String razorpayOrderId)    { this.razorpayOrderId = razorpayOrderId; }
    public void setRazorpayPaymentId(String razorpayPaymentId){ this.razorpayPaymentId = razorpayPaymentId; }
    public void setRazorpaySignature(String razorpaySignature){ this.razorpaySignature = razorpaySignature; }
}
