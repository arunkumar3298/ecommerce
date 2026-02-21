package com.arun.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

    private Long          id;
    private String        name;
    private String        description;
    private BigDecimal    price;
    private Integer       stock;
    private String        category;
    private String        imageUrl;
    private LocalDateTime createdAt;

    // ← Required for Jackson to deserialize from Redis
    public ProductResponse() {}

    private ProductResponse(Builder builder) {
        this.id          = builder.id;
        this.name        = builder.name;
        this.description = builder.description;
        this.price       = builder.price;
        this.stock       = builder.stock;
        this.category    = builder.category;
        this.imageUrl    = builder.imageUrl;
        this.createdAt   = builder.createdAt;
    }

    public Long          getId()          { return id; }
    public String        getName()        { return name; }
    public String        getDescription() { return description; }
    public BigDecimal    getPrice()       { return price; }
    public Integer       getStock()       { return stock; }
    public String        getCategory()    { return category; }
    public String        getImageUrl()    { return imageUrl; }
    public LocalDateTime getCreatedAt()   { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long          id;
        private String        name;
        private String        description;
        private BigDecimal    price;
        private Integer       stock;
        private String        category;
        private String        imageUrl;
        private LocalDateTime createdAt;

        public Builder id(Long id)                { this.id = id;             return this; }
        public Builder name(String name)          { this.name = name;         return this; }
        public Builder description(String desc)   { this.description = desc;  return this; }
        public Builder price(BigDecimal price)    { this.price = price;       return this; }
        public Builder stock(Integer stock)       { this.stock = stock;       return this; }
        public Builder category(String category)  { this.category = category; return this; }
        public Builder imageUrl(String imageUrl)  { this.imageUrl = imageUrl; return this; }
        public Builder createdAt(LocalDateTime c) { this.createdAt = c;       return this; }

        public ProductResponse build() { return new ProductResponse(this); }
    }
}
