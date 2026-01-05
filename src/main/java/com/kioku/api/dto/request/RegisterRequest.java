package com.kioku.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration.
 *
 * <p>This request object is used when a new user creates an account.
 * It contains the user's email address and password with validation rules
 * to ensure account security.
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Email: Required, must be a valid email format</li>
 *   <li>Password: Required, minimum 6 characters</li>
 * </ul>
 *
 * <p><strong>Security Note:</strong> Passwords are transmitted over HTTPS and are
 * immediately hashed using BCrypt before being stored in the database. Plain text
 * passwords are never persisted.
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "email": "newuser@example.com",
 *   "password": "SecurePassword123!"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class RegisterRequest {

    /**
     * The user's email address.
     * Required field that must be a valid email format and unique in the system.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * The user's password.
     * Required field with minimum length of 6 characters.
     * Will be hashed with BCrypt before storage.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /**
     * Default constructor for JSON deserialization.
     */
    public RegisterRequest() {}

    /**
     * Constructs a RegisterRequest with specified credentials.
     *
     * @param email the user's email address
     * @param password the user's password (plain text, will be hashed)
     */
    public RegisterRequest(String email, String password) {
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
     * @return the password (plain text, before hashing)
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
}