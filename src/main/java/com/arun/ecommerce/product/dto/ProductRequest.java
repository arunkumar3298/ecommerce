package com.arun.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.1", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Category is required")
    private String category;

    private String imageUrl;

    public ProductRequest() {}

    public String     getName()        { return name; }
    public String     getDescription() { return description; }
    public BigDecimal getPrice()       { return price; }
    public Integer    getStock()       { return stock; }
    public String     getCategory()    { return category; }
    public String     getImageUrl()    { return imageUrl; }

    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price)         { this.price = price; }
    public void setStock(Integer stock)            { this.stock = stock; }
    public void setCategory(String category)       { this.category = category; }
    public void setImageUrl(String imageUrl)       { this.imageUrl = imageUrl; }
}
