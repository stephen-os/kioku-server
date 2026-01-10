package com.kioku.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user authentication (login).
 *
 * <p>This request object is used when a user attempts to log in to the application.
 * It contains the user's email address and password for authentication.
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Email: Required, must be a valid email format</li>
 *   <li>Password: Required</li>
 * </ul>
 *
 * <p><strong>Security Note:</strong> Passwords are transmitted over HTTPS and are
 * never stored in plain text. They are validated against hashed passwords in the database.
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "email": "user@example.com",
 *   "password": "SecurePassword123!"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class LoginRequest {

    /**
     * The user's email address.
     * Required field that must be a valid email format.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * The user's password.
     * Required field. Sent over HTTPS and validated against hashed password.
     */
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Default constructor for JSON deserialization.
     */
    public LoginRequest() {}

    /**
     * Constructs a LoginRequest with specified credentials.
     *
     * @param email the user's email address
     * @param password the user's password
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Gets the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}