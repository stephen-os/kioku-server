package com.kioku.api.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Changes how an account presents itself.
 *
 * <p>Both fields are optional: sending one leaves the other alone, so the
 * client can update a name without also restating the avatar.
 */
public class UpdateProfileRequest {

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Size(max = 50, message = "Avatar must not exceed 50 characters")
    private String avatar;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String displayName, String avatar) {
        this.displayName = displayName;
        this.avatar = avatar;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
