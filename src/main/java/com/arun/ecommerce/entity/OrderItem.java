package com.arun.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;   // PRICE SNAPSHOT — decoupled from product.price

    // ── Constructors ────────────────────────────────────────────

    protected OrderItem() {}

    private OrderItem(Builder builder) {
        this.order           = builder.order;
        this.product         = builder.product;
        this.quantity        = builder.quantity;
        this.priceAtPurchase = builder.priceAtPurchase;
    }

    // ── Getters ─────────────────────────────────────────────────

    public Order      getOrder()           { return order; }
    public Product    getProduct()         { return product; }
    public Integer    getQuantity()        { return quantity; }
    public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }

    // ── Setters ─────────────────────────────────────────────────

    public void setOrder(Order order)                        { this.order = order; }
    public void setProduct(Product product)                  { this.product = product; }
    public void setQuantity(Integer quantity)                { this.quantity = quantity; }
    public void setPriceAtPurchase(BigDecimal price)         { this.priceAtPurchase = price; }

    // ── Builder ──────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Order      order;
        private Product    product;
        private Integer    quantity;
        private BigDecimal priceAtPurchase;

        public Builder order(Order order)                    { this.order = order;                   return this; }
        public Builder product(Product product)              { this.product = product;               return this; }
        public Builder quantity(Integer quantity)            { this.quantity = quantity;             return this; }
        public Builder priceAtPurchase(BigDecimal price)     { this.priceAtPurchase = price;         return this; }

        public OrderItem build() { return new OrderItem(this); }
    }
}
