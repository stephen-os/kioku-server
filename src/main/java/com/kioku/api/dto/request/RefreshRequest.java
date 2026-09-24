package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Exchanges a refresh token for a new access token. */
public class RefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public RefreshRequest() {
    }

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
