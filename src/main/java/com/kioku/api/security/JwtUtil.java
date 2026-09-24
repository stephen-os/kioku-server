package com.kioku.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Utility class for JWT token generation and validation.
 *
 * <p>This utility provides methods to:
 * <ul>
 *   <li>Generate JWT tokens for authenticated users</li>
 *   <li>Validate JWT tokens</li>
 *   <li>Extract user information from tokens</li>
 * </ul>
 *
 * <p><strong>Token Structure:</strong>
 * <ul>
 *   <li>Subject: User ID</li>
 *   <li>Claim "email": User's email address</li>
 *   <li>Issued At: Token creation timestamp</li>
 *   <li>Expiration: Configurable expiration time</li>
 * </ul>
 *
 * <p><strong>Configuration:</strong>
 * <ul>
 *   <li>{@code jwt.secret} - Secret key for signing (min 256 bits)</li>
 *   <li>{@code jwt.expiration} - Token expiration time in milliseconds</li>
 * </ul>
 *
 * <p><strong>Security Notes:</strong>
 * <ul>
 *   <li>Uses HMAC-SHA256 for token signing</li>
 *   <li>Secret key must be at least 256 bits (32 characters)</li>
 *   <li>Tokens cannot be forged without the secret key</li>
 *   <li>Expired tokens are automatically rejected</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Gets the secret key for signing JWT tokens.
     *
     * <p>The key is derived from the configured secret string using HMAC-SHA256.
     *
     * @return the secret key for signing
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT token for a user.
     *
     * <p>The generated token includes:
     * <ul>
     *   <li>Subject: User ID</li>
     *   <li>Claim "email": User's email</li>
     *   <li>Issued At: Current timestamp</li>
     *   <li>Expiration: Current time + configured expiration</li>
     * </ul>
     *
     * @param userId the user's database ID
     * @param email the user's email address
     * @return the generated JWT token as a string
     */
    public String generateToken(UUID userId, String email) {
        logger.debug("Generating JWT token for user ID: {}", userId);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        logger.debug("JWT token generated successfully for user ID: {}, expires at: {}",
                userId, expiryDate);

        return token;
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * <p><strong>Note:</strong> This method does NOT validate the token.
     * Call {@link #validateToken(String)} first to ensure the token is valid.
     *
     * @param token the JWT token
     * @return the user ID extracted from the token's subject
     * @throws NumberFormatException if the subject is not a valid Long
     * @throws SignatureException if the signature is invalid
     * @throws MalformedJwtException if the token is malformed
     */
    public UUID getUserIdFromToken(String token) {
        logger.debug("Extracting user ID from JWT token");

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(claims.getSubject());
        logger.debug("Extracted user ID: {}", userId);

        return userId;
    }

    /**
     * Validates a JWT token.
     *
     * <p>This method checks:
     * <ul>
     *   <li>Token signature is valid</li>
     *   <li>Token is not expired</li>
     *   <li>Token is well-formed</li>
     * </ul>
     *
     * @param token the JWT token to validate
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            logger.debug("JWT token validation successful");
            return true;
        } catch (SignatureException e) {
            logger.debug("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.debug("Invalid JWT token format: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.debug("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.debug("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during JWT validation", e);
        }

        return false;
    }
}