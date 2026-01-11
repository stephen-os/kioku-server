package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for deleting a user account.
 *
 * <p>Requires the user's current password for identity verification
 * before allowing account deletion.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class DeleteAccountRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    public DeleteAccountRequest() {
    }

    public DeleteAccountRequest(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    @Override
    public String toString() {
        return "DeleteAccountRequest{" +
                "currentPassword='[PROTECTED]'" +
                '}';
    }
}
