package com.arun.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_user_product",
                columnNames = {"user_id", "product_id"}  // DB-level: one row per user+product
        )
)
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // ── Constructors ────────────────────────────────────────────

    protected CartItem() {}

    private CartItem(Builder builder) {
        this.user     = builder.user;
        this.product  = builder.product;
        this.quantity = builder.quantity;
    }

    // ── Getters ─────────────────────────────────────────────────

    public User    getUser()     { return user; }
    public Product getProduct()  { return product; }
    public Integer getQuantity() { return quantity; }

    // ── Setters ─────────────────────────────────────────────────

    public void setUser(User user)         { this.user = user; }
    public void setProduct(Product product){ this.product = product; }
    public void setQuantity(Integer qty)   { this.quantity = qty; }

    // ── Builder ──────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User    user;
        private Product product;
        private Integer quantity;

        public Builder user(User user)         { this.user = user;         return this; }
        public Builder product(Product product){ this.product = product;   return this; }
        public Builder quantity(Integer qty)   { this.quantity = qty;      return this; }

        public CartItem build() { return new CartItem(this); }
    }
}
