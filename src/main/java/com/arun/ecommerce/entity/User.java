package com.arun.ecommerce.entity;

import com.arun.ecommerce.entity.enums.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean isVerified = false;

    // ── Constructors ────────────────────────────────────────────

    protected User() {}   // JPA only

    private User(Builder builder) {
        this.name       = builder.name;
        this.email      = builder.email;
        this.password   = builder.password;
        this.role       = builder.role;
        this.isVerified = builder.isVerified;
    }

    // ── Getters ─────────────────────────────────────────────────

    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public Role getRole()       { return role; }
    public boolean isVerified() { return isVerified; }

    // ── Setters ─────────────────────────────────────────────────

    public void setName(String name)         { this.name = name; }
    public void setEmail(String email)       { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role)           { this.role = role; }
    public void setVerified(boolean verified){ this.isVerified = verified; }

    // ── UserDetails Contract ─────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return password; }  // serves as getter too

    @Override
    public String getUsername() { return email; }     // email is login ID, not name

    @Override
    public boolean isAccountNonExpired()     { return true; }

    @Override
    public boolean isAccountNonLocked()      { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isVerified; } // blocks unverified users

    // ── Builder ──────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String  name;
        private String  email;
        private String  password;
        private Role    role;
        private boolean isVerified = false;  // safe default

        public Builder name(String name)         { this.name = name;           return this; }
        public Builder email(String email)       { this.email = email;         return this; }
        public Builder password(String password) { this.password = password;   return this; }
        public Builder role(Role role)           { this.role = role;           return this; }
        public Builder isVerified(boolean v)     { this.isVerified = v;        return this; }

        public User build() { return new User(this); }
    }
}
