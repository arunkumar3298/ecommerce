package com.arun.ecommerce.order.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long       orderItemId;
    private Long       productId;
    private String     productName;
    private String     productCategory;
    private Integer    quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;

    private OrderItemResponse(Builder builder) {
        this.orderItemId     = builder.orderItemId;
        this.productId       = builder.productId;
        this.productName     = builder.productName;
        this.productCategory = builder.productCategory;
        this.quantity        = builder.quantity;
        this.priceAtPurchase = builder.priceAtPurchase;
        this.subtotal        = builder.subtotal;
    }

    public Long       getOrderItemId()     { return orderItemId; }
    public Long       getProductId()       { return productId; }
    public String     getProductName()     { return productName; }
    public String     getProductCategory() { return productCategory; }
    public Integer    getQuantity()        { return quantity; }
    public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
    public BigDecimal getSubtotal()        { return subtotal; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long       orderItemId;
        private Long       productId;
        private String     productName;
        private String     productCategory;
        private Integer    quantity;
        private BigDecimal priceAtPurchase;
        private BigDecimal subtotal;

        public Builder orderItemId(Long id)          { this.orderItemId = id;         return this; }
        public Builder productId(Long id)            { this.productId = id;           return this; }
        public Builder productName(String n)         { this.productName = n;          return this; }
        public Builder productCategory(String c)     { this.productCategory = c;      return this; }
        public Builder quantity(Integer q)           { this.quantity = q;             return this; }
        public Builder priceAtPurchase(BigDecimal p) { this.priceAtPurchase = p;      return this; }
        public Builder subtotal(BigDecimal s)        { this.subtotal = s;             return this; }

        public OrderItemResponse build() { return new OrderItemResponse(this); }
    }
}
