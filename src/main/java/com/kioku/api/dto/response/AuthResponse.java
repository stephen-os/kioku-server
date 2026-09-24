package com.kioku.api.dto.response;

/**
 * Data Transfer Object for authentication response.
 *
 * <p>This response object is returned after successful user registration or login.
 * It contains a JWT token that the client should include in subsequent requests
 * for authentication, along with user information.
 *
 * <p><strong>Response Fields:</strong>
 * <ul>
 *   <li>token: JWT token for authentication</li>
 *   <li>type: Token type (always "Bearer")</li>
 *   <li>userId: The authenticated user's ID</li>
 *   <li>email: The authenticated user's email</li>
 * </ul>
 *
 * <p><strong>Usage:</strong> Clients should store the token and include it in the
 * Authorization header of subsequent requests as: {@code Authorization: Bearer <token>}
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "type": "Bearer",
 *   "userId": 1,
 *   "email": "user@example.com"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
import java.util.UUID;
public class AuthResponse {

    /**
     * The JWT authentication token.
     * This token should be included in the Authorization header of authenticated requests.
     */
    private String token;

    /**
     * The type of token (always "Bearer").
     * This indicates the token should be used with Bearer authentication scheme.
     */
    private String type = "Bearer";

    /**
     * The authenticated user's unique identifier.
     */
    private UUID userId;

    /**
     * The authenticated user's email address.
     */
    private String email;

    /** Null when the account has never set one; clients fall back to the email. */
    private String displayName;

    /** Identifier of a built-in avatar, resolved to an image by the client. */
    private String avatar;

    /**
     * Long-lived credential for obtaining a new access token.
     *
     * <p>Null on responses that do not establish a session, such as a refresh
     * that only rotates the access token.
     */
    private String refreshToken;

    /**
     * Default constructor for JSON serialization.
     */
    public AuthResponse() {}

    /**
     * Constructs an AuthResponse with the specified values.
     * The token type is automatically set to "Bearer".
     *
     * @param token the JWT authentication token
     * @param userId the user's ID
     * @param email the user's email address
     */
    public AuthResponse(String token, UUID userId, String email) {
        this.token = token;
        this.userId = userId;
        this.email = email;
    }

    /**
     * Full session response, including the refresh token issued alongside.
     */
    public AuthResponse(String token, UUID userId, String email, String refreshToken) {
        this(token, userId, email);
        this.refreshToken = refreshToken;
    }

    /**
     * Full session response including the account's profile.
     *
     * <p>Carried here so a client has a name and avatar to show immediately,
     * rather than signing in and then fetching the account separately.
     */
    public AuthResponse(String token, UUID userId, String email, String refreshToken,
                        String displayName, String avatar) {
        this(token, userId, email, refreshToken);
        this.displayName = displayName;
        this.avatar = avatar;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Gets the JWT authentication token.
     *
     * @return the JWT token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the JWT authentication token.
     *
     * @param token the JWT token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the token type.
     *
     * @return the token type (always "Bearer")
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the token type.
     *
     * @param type the token type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the authenticated user's ID.
     *
     * @return the user ID
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Sets the authenticated user's ID.
     *
     * @param userId the user ID to set
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Gets the authenticated user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the authenticated user's email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='[PROTECTED]'" +
                ", type='" + type + '\'' +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                '}';
    }
}