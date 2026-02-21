package com.arun.ecommerce.auth.dto;

public class AuthResponse {

    private String token;
    private String email;
    private String name;
    private String role;

    private AuthResponse(Builder builder) {
        this.token = builder.token;
        this.email = builder.email;
        this.name  = builder.name;
        this.role  = builder.role;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getName()  { return name; }
    public String getRole()  { return role; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String email;
        private String name;
        private String role;

        public Builder token(String token) { this.token = token; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder name(String name)   { this.name = name;   return this; }
        public Builder role(String role)   { this.role = role;   return this; }

        public AuthResponse build() { return new AuthResponse(this); }
    }
}
