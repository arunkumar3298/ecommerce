package com.arun.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OrderRequest {

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pincode;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid Indian phone number")
    private String phone;

    public OrderRequest() {}

    public String getStreetAddress() { return streetAddress; }
    public String getCity()          { return city; }
    public String getState()         { return state; }
    public String getPincode()       { return pincode; }
    public String getPhone()         { return phone; }

    public void setStreetAddress(String s) { this.streetAddress = s; }
    public void setCity(String c)          { this.city = c; }
    public void setState(String s)         { this.state = s; }
    public void setPincode(String p)       { this.pincode = p; }
    public void setPhone(String p)         { this.phone = p; }
}
