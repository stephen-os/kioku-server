package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating the user's password.
 *
 * <p>Requires the current password for identity verification
 * and the new password to set.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class UpdatePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    public UpdatePasswordRequest() {
    }

    public UpdatePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "UpdatePasswordRequest{" +
                "currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                '}';
    }
}
