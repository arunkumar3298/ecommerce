package com.arun.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    public ChangePasswordRequest() {}

    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword()     { return newPassword; }

    public void setCurrentPassword(String p) { this.currentPassword = p; }
    public void setNewPassword(String p)     { this.newPassword = p; }
}
