package com.arun.ecommerce.user.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {

    private Long          id;
    private String        name;
    private String        email;
    private String        role;
    private boolean       isVerified;
    private LocalDateTime createdAt;

    private UserProfileResponse(Builder builder) {
        this.id        = builder.id;
        this.name      = builder.name;
        this.email     = builder.email;
        this.role      = builder.role;
        this.isVerified = builder.isVerified;
        this.createdAt = builder.createdAt;
    }

    public Long          getId()        { return id; }
    public String        getName()      { return name; }
    public String        getEmail()     { return email; }
    public String        getRole()      { return role; }
    public boolean       isVerified()   { return isVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long          id;
        private String        name;
        private String        email;
        private String        role;
        private boolean       isVerified;
        private LocalDateTime createdAt;

        public Builder id(Long id)               { this.id = id;               return this; }
        public Builder name(String name)         { this.name = name;           return this; }
        public Builder email(String email)       { this.email = email;         return this; }
        public Builder role(String role)         { this.role = role;           return this; }
        public Builder isVerified(boolean v)     { this.isVerified = v;        return this; }
        public Builder createdAt(LocalDateTime c){ this.createdAt = c;         return this; }

        public UserProfileResponse build() { return new UserProfileResponse(this); }
    }
}
