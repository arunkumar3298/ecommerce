package com.arun.ecommerce.payment.dto;

public class PaymentVerifyResponse {

    private boolean success;
    private String  message;
    private Long    orderId;
    private String  paymentStatus;

    private PaymentVerifyResponse(Builder builder) {
        this.success       = builder.success;
        this.message       = builder.message;
        this.orderId       = builder.orderId;
        this.paymentStatus = builder.paymentStatus;
    }

    public boolean isSuccess()       { return success; }
    public String  getMessage()      { return message; }
    public Long    getOrderId()      { return orderId; }
    public String  getPaymentStatus(){ return paymentStatus; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean success;
        private String  message;
        private Long    orderId;
        private String  paymentStatus;

        public Builder success(boolean s)       { this.success = s;       return this; }
        public Builder message(String m)        { this.message = m;       return this; }
        public Builder orderId(Long o)          { this.orderId = o;       return this; }
        public Builder paymentStatus(String p)  { this.paymentStatus = p; return this; }

        public PaymentVerifyResponse build() { return new PaymentVerifyResponse(this); }
    }
}
