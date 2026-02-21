package com.arun.ecommerce.entity;

import com.arun.ecommerce.entity.enums.OrderStatus;
import com.arun.ecommerce.entity.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // ── Delivery Address ──────────────────────────────────────
    @Column(nullable = false)
    private String streetAddress;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String phone;

    // ── Constructors ──────────────────────────────────────────

    protected Order() {}

    private Order(Builder builder) {
        this.user          = builder.user;
        this.totalAmount   = builder.totalAmount;
        this.status        = builder.status;
        this.paymentStatus = builder.paymentStatus;
        this.orderItems    = builder.orderItems;
        this.streetAddress = builder.streetAddress;
        this.city          = builder.city;
        this.state         = builder.state;
        this.pincode       = builder.pincode;
        this.phone         = builder.phone;
    }

    // ── Getters ───────────────────────────────────────────────

    public User            getUser()          { return user; }
    public BigDecimal      getTotalAmount()   { return totalAmount; }
    public OrderStatus     getStatus()        { return status; }
    public PaymentStatus   getPaymentStatus() { return paymentStatus; }
    public List<OrderItem> getOrderItems()    { return orderItems; }
    public String          getStreetAddress() { return streetAddress; }
    public String          getCity()          { return city; }
    public String          getState()         { return state; }
    public String          getPincode()       { return pincode; }
    public String          getPhone()         { return phone; }

    // ── Setters ───────────────────────────────────────────────

    public void setUser(User user)                   { this.user = user; }
    public void setTotalAmount(BigDecimal t)         { this.totalAmount = t; }
    public void setStatus(OrderStatus status)        { this.status = status; }
    public void setPaymentStatus(PaymentStatus ps)   { this.paymentStatus = ps; }
    public void setOrderItems(List<OrderItem> items) { this.orderItems = items; }
    public void setStreetAddress(String s)           { this.streetAddress = s; }
    public void setCity(String c)                    { this.city = c; }
    public void setState(String s)                   { this.state = s; }
    public void setPincode(String p)                 { this.pincode = p; }
    public void setPhone(String p)                   { this.phone = p; }

    // ── Builder ───────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User            user;
        private BigDecimal      totalAmount;
        private OrderStatus     status        = OrderStatus.PLACED;
        private PaymentStatus   paymentStatus = PaymentStatus.PENDING;
        private List<OrderItem> orderItems    = new ArrayList<>();
        private String          streetAddress;
        private String          city;
        private String          state;
        private String          pincode;
        private String          phone;

        public Builder user(User u)                    { this.user = u;           return this; }
        public Builder totalAmount(BigDecimal t)       { this.totalAmount = t;    return this; }
        public Builder status(OrderStatus s)           { this.status = s;         return this; }
        public Builder paymentStatus(PaymentStatus ps) { this.paymentStatus = ps; return this; }
        public Builder orderItems(List<OrderItem> i)   { this.orderItems = i;     return this; }
        public Builder streetAddress(String s)         { this.streetAddress = s;  return this; }
        public Builder city(String c)                  { this.city = c;           return this; }
        public Builder state(String s)                 { this.state = s;          return this; }
        public Builder pincode(String p)               { this.pincode = p;        return this; }
        public Builder phone(String p)                 { this.phone = p;          return this; }

        public Order build() { return new Order(this); }
    }
}
