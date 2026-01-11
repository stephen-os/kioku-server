package com.kioku.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for initiating an email change.
 *
 * <p>Requires the user's current password for identity verification
 * and the new email address to change to.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class UpdateEmailRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New email is required")
    @Email(message = "New email must be a valid email address")
    private String newEmail;

    public UpdateEmailRequest() {
    }

    public UpdateEmailRequest(String currentPassword, String newEmail) {
        this.currentPassword = currentPassword;
        this.newEmail = newEmail;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    @Override
    public String toString() {
        return "UpdateEmailRequest{" +
                "currentPassword='[PROTECTED]'" +
                ", newEmail='" + newEmail + '\'' +
                '}';
    }
}
