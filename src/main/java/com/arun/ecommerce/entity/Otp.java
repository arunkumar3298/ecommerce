package com.arun.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otps")
public class Otp extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, name = "otp_code")
    private String otpCode;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false, name = "is_used")
    private boolean isUsed = false;

    // ── Constructors ─────────────────────────────────────
    protected Otp() {}

    private Otp(Builder builder) {
        this.email     = builder.email;
        this.otpCode   = builder.otpCode;
        this.expiresAt = builder.expiresAt;
        this.isUsed    = builder.isUsed;
    }

    // ── Getters ──────────────────────────────────────────
    public String        getEmail()     { return email; }
    public String        getOtpCode()   { return otpCode; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean       isUsed()       { return isUsed; }

    // ── Setters ──────────────────────────────────────────
    public void setEmail(String email)               { this.email = email; }
    public void setOtpCode(String otpCode)           { this.otpCode = otpCode; }
    public void setExpiresAt(LocalDateTime expiresAt){ this.expiresAt = expiresAt; }
    public void setUsed(boolean used)                { this.isUsed = used; }

    // ── Builder ───────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String email;
        private String otpCode;
        private LocalDateTime expiresAt;
        private boolean isUsed = false;

        public Builder email(String email)               { this.email = email;         return this; }
        public Builder otpCode(String otpCode)           { this.otpCode = otpCode;     return this; }
        public Builder expiresAt(LocalDateTime expiresAt){ this.expiresAt = expiresAt; return this; }
        public Builder isUsed(boolean isUsed)            { this.isUsed = isUsed;       return this; }

        public Otp build() { return new Otp(this); }
    }
}
