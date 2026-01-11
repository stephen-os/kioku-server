package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for verifying an email change.
 *
 * <p>Contains the verification token sent to the new email address.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class VerifyEmailChangeRequest {

    @NotBlank(message = "Verification token is required")
    private String token;

    public VerifyEmailChangeRequest() {
    }

    public VerifyEmailChangeRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "VerifyEmailChangeRequest{" +
                "token='[PROTECTED]'" +
                '}';
    }
}
