package com.arun.ecommerce.messaging;

import java.io.Serializable;

public class EmailMessage implements Serializable {

    private String to;
    private String otpCode;

    // No-arg constructor — required for Jackson deserialization
    public EmailMessage() {}

    public EmailMessage(String to, String otpCode) {
        this.to      = to;
        this.otpCode = otpCode;
    }

    public String getTo()      { return to; }
    public String getOtpCode() { return otpCode; }

    public void setTo(String to)           { this.to = to; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
