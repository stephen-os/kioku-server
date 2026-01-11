package com.kioku.api.dto.response;

import com.kioku.api.model.User;

import java.time.LocalDateTime;

/**
 * Response DTO for user account profile information.
 *
 * <p>Returns user profile data suitable for display in the frontend,
 * excluding sensitive information like passwords and tokens.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class AccountResponse {

    private Long id;
    private String email;
    private boolean emailVerified;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public AccountResponse() {
    }

    public AccountResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.emailVerified = user.isEmailVerified();
        this.status = user.getStatus().name();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    @Override
    public String toString() {
        return "AccountResponse{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
}
