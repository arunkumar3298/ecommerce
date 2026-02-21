package com.arun.ecommerce.cart.dto;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long       cartItemId;
    private Long       productId;
    private String     productName;
    private String     productCategory;
    private String     productImageUrl;
    private BigDecimal productPrice;
    private Integer    quantity;
    private BigDecimal subtotal;        // productPrice × quantity

    private CartItemResponse(Builder builder) {
        this.cartItemId       = builder.cartItemId;
        this.productId        = builder.productId;
        this.productName      = builder.productName;
        this.productCategory  = builder.productCategory;
        this.productImageUrl  = builder.productImageUrl;
        this.productPrice     = builder.productPrice;
        this.quantity         = builder.quantity;
        this.subtotal         = builder.subtotal;
    }

    public Long       getCartItemId()      { return cartItemId; }
    public Long       getProductId()       { return productId; }
    public String     getProductName()     { return productName; }
    public String     getProductCategory() { return productCategory; }
    public String     getProductImageUrl() { return productImageUrl; }
    public BigDecimal getProductPrice()    { return productPrice; }
    public Integer    getQuantity()        { return quantity; }
    public BigDecimal getSubtotal()        { return subtotal; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long       cartItemId;
        private Long       productId;
        private String     productName;
        private String     productCategory;
        private String     productImageUrl;
        private BigDecimal productPrice;
        private Integer    quantity;
        private BigDecimal subtotal;

        public Builder cartItemId(Long id)            { this.cartItemId = id;             return this; }
        public Builder productId(Long id)             { this.productId = id;              return this; }
        public Builder productName(String n)          { this.productName = n;             return this; }
        public Builder productCategory(String c)      { this.productCategory = c;         return this; }
        public Builder productImageUrl(String u)      { this.productImageUrl = u;         return this; }
        public Builder productPrice(BigDecimal p)     { this.productPrice = p;            return this; }
        public Builder quantity(Integer q)            { this.quantity = q;                return this; }
        public Builder subtotal(BigDecimal s)         { this.subtotal = s;                return this; }

        public CartItemResponse build() { return new CartItemResponse(this); }
    }
}
