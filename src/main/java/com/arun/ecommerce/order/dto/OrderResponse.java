package com.arun.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long                    id;
    private String                  userEmail;
    private BigDecimal              totalAmount;
    private String                  status;
    private String                  paymentStatus;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime           createdAt;
    private String                  streetAddress;
    private String                  city;
    private String                  state;
    private String                  pincode;
    private String                  phone;

    private OrderResponse(Builder builder) {
        this.id            = builder.id;
        this.userEmail     = builder.userEmail;
        this.totalAmount   = builder.totalAmount;
        this.status        = builder.status;
        this.paymentStatus = builder.paymentStatus;
        this.orderItems    = builder.orderItems;
        this.createdAt     = builder.createdAt;
        this.streetAddress = builder.streetAddress;
        this.city          = builder.city;
        this.state         = builder.state;
        this.pincode       = builder.pincode;
        this.phone         = builder.phone;
    }

    public Long                    getId()            { return id; }
    public String                  getUserEmail()     { return userEmail; }
    public BigDecimal              getTotalAmount()   { return totalAmount; }
    public String                  getStatus()        { return status; }
    public String                  getPaymentStatus() { return paymentStatus; }
    public List<OrderItemResponse> getOrderItems()    { return orderItems; }
    public LocalDateTime           getCreatedAt()     { return createdAt; }
    public String                  getStreetAddress() { return streetAddress; }
    public String                  getCity()          { return city; }
    public String                  getState()         { return state; }
    public String                  getPincode()       { return pincode; }
    public String                  getPhone()         { return phone; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long                    id;
        private String                  userEmail;
        private BigDecimal              totalAmount;
        private String                  status;
        private String                  paymentStatus;
        private List<OrderItemResponse> orderItems;
        private LocalDateTime           createdAt;
        private String                  streetAddress;
        private String                  city;
        private String                  state;
        private String                  pincode;
        private String                  phone;

        public Builder id(Long id)                           { this.id = id;             return this; }
        public Builder userEmail(String e)                   { this.userEmail = e;       return this; }
        public Builder totalAmount(BigDecimal t)             { this.totalAmount = t;     return this; }
        public Builder status(String s)                      { this.status = s;          return this; }
        public Builder paymentStatus(String p)               { this.paymentStatus = p;   return this; }
        public Builder orderItems(List<OrderItemResponse> i) { this.orderItems = i;      return this; }
        public Builder createdAt(LocalDateTime c)            { this.createdAt = c;       return this; }
        public Builder streetAddress(String s)               { this.streetAddress = s;   return this; }
        public Builder city(String c)                        { this.city = c;            return this; }
        public Builder state(String s)                       { this.state = s;           return this; }
        public Builder pincode(String p)                     { this.pincode = p;         return this; }
        public Builder phone(String p)                       { this.phone = p;           return this; }

        public OrderResponse build() { return new OrderResponse(this); }
    }
}
