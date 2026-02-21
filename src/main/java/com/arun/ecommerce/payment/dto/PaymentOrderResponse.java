package com.arun.ecommerce.payment.dto;

import java.math.BigDecimal;

public class PaymentOrderResponse {

    private String     razorpayOrderId;
    private BigDecimal amount;
    private String     currency;
    private String     keyId;          // sent to frontend to init Razorpay checkout

    private PaymentOrderResponse(Builder builder) {
        this.razorpayOrderId = builder.razorpayOrderId;
        this.amount          = builder.amount;
        this.currency        = builder.currency;
        this.keyId           = builder.keyId;
    }

    public String     getRazorpayOrderId() { return razorpayOrderId; }
    public BigDecimal getAmount()          { return amount; }
    public String     getCurrency()        { return currency; }
    public String     getKeyId()           { return keyId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String     razorpayOrderId;
        private BigDecimal amount;
        private String     currency;
        private String     keyId;

        public Builder razorpayOrderId(String r) { this.razorpayOrderId = r; return this; }
        public Builder amount(BigDecimal a)       { this.amount = a;          return this; }
        public Builder currency(String c)         { this.currency = c;        return this; }
        public Builder keyId(String k)            { this.keyId = k;           return this; }

        public PaymentOrderResponse build() { return new PaymentOrderResponse(this); }
    }
}
