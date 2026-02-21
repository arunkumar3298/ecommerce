package com.arun.ecommerce.messaging;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderConfirmationMessage implements Serializable {

    private String     email;
    private Long       orderId;
    private BigDecimal totalAmount;

    // Required for Jackson deserialization
    public OrderConfirmationMessage() {}

    public OrderConfirmationMessage(String email,
                                    Long orderId,
                                    BigDecimal totalAmount) {
        this.email       = email;
        this.orderId     = orderId;
        this.totalAmount = totalAmount;
    }

    public String     getEmail()       { return email; }
    public Long       getOrderId()     { return orderId; }
    public BigDecimal getTotalAmount() { return totalAmount; }

    public void setEmail(String email)             { this.email = email; }
    public void setOrderId(Long orderId)           { this.orderId = orderId; }
    public void setTotalAmount(BigDecimal t)       { this.totalAmount = t; }
}
