package com.arun.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;          // BigDecimal — never double for money

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    // ── Constructors ────────────────────────────────────────────

    protected Product() {}

    private Product(Builder builder) {
        this.name        = builder.name;
        this.description = builder.description;
        this.price       = builder.price;
        this.stock       = builder.stock;
        this.category    = builder.category;
        this.imageUrl    = builder.imageUrl;
    }

    // ── Getters ─────────────────────────────────────────────────

    public String     getName()        { return name; }
    public String     getDescription() { return description; }
    public BigDecimal getPrice()       { return price; }
    public Integer    getStock()       { return stock; }
    public String     getCategory()    { return category; }
    public String     getImageUrl()    { return imageUrl; }

    // ── Setters ─────────────────────────────────────────────────

    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price)         { this.price = price; }
    public void setStock(Integer stock)            { this.stock = stock; }
    public void setCategory(String category)       { this.category = category; }
    public void setImageUrl(String imageUrl)       { this.imageUrl = imageUrl; }

    // ── Builder ──────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String     name;
        private String     description;
        private BigDecimal price;
        private Integer    stock;
        private String     category;
        private String     imageUrl;

        public Builder name(String name)               { this.name = name;               return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(BigDecimal price)         { this.price = price;             return this; }
        public Builder stock(Integer stock)            { this.stock = stock;             return this; }
        public Builder category(String category)       { this.category = category;       return this; }
        public Builder imageUrl(String imageUrl)       { this.imageUrl = imageUrl;       return this; }

        public Product build() { return new Product(this); }
    }
}
