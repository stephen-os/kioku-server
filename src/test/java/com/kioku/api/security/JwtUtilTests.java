package com.kioku.api.security;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

/**
 * Unit tests for JwtUtil.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>JWT token generation</li>
 *   <li>Token validation</li>
 *   <li>User ID extraction from tokens</li>
 *   <li>Handling of invalid/expired tokens</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTests {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtilTests.class);

    // Test data constants
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SECRET = "ThisIsAVerySecureSecretKeyForTestingPurposesOnly123456789";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hour
    private static final Long SHORT_EXPIRATION = 1L; // 1 millisecond (for expiration tests)

    private JwtUtil jwtUtil;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up JwtUtil test");
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    // Token Generation Tests

    /**
     * Tests successful token generation.
     */
    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken() {
        logger.debug("Test: Generating JWT token for user ID={}", TEST_USER_ID);

        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature

        logger.debug("Test passed: Token generated successfully");
    }

    /**
     * Tests that each generated token is unique.
     */
    @Test
    @DisplayName("Should generate unique tokens for same user")
    void testGenerateUniqueTokens() throws InterruptedException {
        logger.debug("Test: Generating multiple tokens for same user");

        String token1 = jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);

        await().pollDelay(1000, MILLISECONDS).until(() -> true);

        String token2 = jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2, "Tokens should be unique even for same user");

        logger.debug("Test passed: Tokens are unique");
    }

    /**
     * Tests token generation with different user IDs.
     */
    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateTokensForDifferentUsers() {
        logger.debug("Test: Generating tokens for different users");

        String token1 = jwtUtil.generateToken(UUID.randomUUID(), "user1@example.com");
        String token2 = jwtUtil.generateToken(UUID.randomUUID(), "user2@example.com");

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);

        logger.debug("Test passed: Different tokens for different users");
    }

    // Token Validation Tests

    /**
     * Tests validation of a valid token.
     */
    @Test
    @DisplayName("Should validate valid token")
    void testValidateValidToken() {
        logger.debug("Test: Validating valid token");

        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);
        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);

        logger.debug("Test passed: Valid token validated successfully");
    }

    /**
     * Tests validation fails for null token.
     */
    @Test
    @DisplayName("Should reject null token")
    void testValidateNullToken() {
        logger.debug("Test: Validating null token");

        boolean isValid = jwtUtil.validateToken(null);

        assertFalse(isValid);

        logger.debug("Test passed: Null token rejected");
    }

    /**
     * Tests validation fails for empty token.
     */
    @Test
    @DisplayName("Should reject empty token")
    void testValidateEmptyToken() {
        logger.debug("Test: Validating empty token");

        boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid);

        logger.debug("Test passed: Empty token rejected");
    }

    /**
     * Tests validation fails for malformed token.
     */
    @Test
    @DisplayName("Should reject malformed token")
    void testValidateMalformedToken() {
        logger.debug("Test: Validating malformed token");

        String malformedToken = "this.is.not.a.valid.jwt.token";
        boolean isValid = jwtUtil.validateToken(malformedToken);

        assertFalse(isValid);

        logger.debug("Test passed: Malformed token rejected");
    }

    /**
     * Tests validation fails for token with invalid signature.
     */
    @Test
    @DisplayName("Should reject token with invalid signature")
    void testValidateTokenWithInvalidSignature() {
        logger.debug("Test: Validating token with invalid signature");

        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);
        // Tamper with the token by changing last character
        String tamperedToken = token.substring(0, token.length() - 1) + "X";

        boolean isValid = jwtUtil.validateToken(tamperedToken);

        assertFalse(isValid);

        logger.debug("Test passed: Tampered token rejected");
    }

    /**
     * Tests validation fails for expired token.
     */
    @Test
    @DisplayName("Should reject expired token")
    void testValidateExpiredToken() throws InterruptedException {
        logger.debug("Test: Validating expired token");

        // Create JwtUtil with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", SHORT_EXPIRATION);

        String token = shortExpirationJwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);

        // Wait for token to expire
        Thread.sleep(10);

        boolean isValid = shortExpirationJwtUtil.validateToken(token);

        assertFalse(isValid);

        logger.debug("Test passed: Expired token rejected");
    }

    /**
     * Tests validation fails for token signed with different secret.
     */
    @Test
    @DisplayName("Should reject token signed with different secret")
    void testValidateTokenWithDifferentSecret() {
        logger.debug("Test: Validating token signed with different secret");

        // Generate token with one secret
        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);

        // Try to validate with different secret
        JwtUtil differentSecretJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentSecretJwtUtil, "secret", "DifferentSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(differentSecretJwtUtil, "expiration", TEST_EXPIRATION);

        boolean isValid = differentSecretJwtUtil.validateToken(token);

        assertFalse(isValid);

        logger.debug("Test passed: Token with different secret rejected");
    }

    // User ID Extraction Tests

    /**
     * Tests successful user ID extraction from valid token.
     */
    @Test
    @DisplayName("Should extract user ID from valid token")
    void testGetUserIdFromValidToken() {
        logger.debug("Test: Extracting user ID from token");

        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);
        UUID extractedUserId = jwtUtil.getUserIdFromToken(token);

        assertNotNull(extractedUserId);
        assertEquals(TEST_USER_ID, extractedUserId);

        logger.debug("Test passed: User ID extracted successfully");
    }

    /**
     * Tests user ID extraction with different user IDs.
     */
    @Test
    @DisplayName("Should extract correct user ID for different users")
    void testGetUserIdFromDifferentTokens() {
        logger.debug("Test: Extracting different user IDs");

        UUID userId1 = UUID.fromString("00000000-0000-0000-0000-000000000123");
        UUID userId2 = UUID.fromString("00000000-0000-0000-0000-000000000456");

        String token1 = jwtUtil.generateToken(userId1, "user1@example.com");
        String token2 = jwtUtil.generateToken(userId2, "user2@example.com");

        assertEquals(userId1, jwtUtil.getUserIdFromToken(token1));
        assertEquals(userId2, jwtUtil.getUserIdFromToken(token2));

        logger.debug("Test passed: Correct user IDs extracted");
    }

    /**
     * Tests that extracting user ID from invalid token throws exception.
     */
    @Test
    @DisplayName("Should throw exception when extracting user ID from invalid token")
    void testGetUserIdFromInvalidToken() {
        logger.debug("Test: Extracting user ID from invalid token");

        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> {
            jwtUtil.getUserIdFromToken(invalidToken);
        });

        logger.debug("Test passed: Exception thrown for invalid token");
    }

    /**
     * Tests that extracting user ID from tampered token throws exception.
     */
    @Test
    @DisplayName("Should throw exception when extracting user ID from tampered token")
    void testGetUserIdFromTamperedToken() {
        logger.debug("Test: Extracting user ID from tampered token");

        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL);
        String tamperedToken = token.substring(0, token.length() - 1) + "X";

        assertThrows(Exception.class, () -> {
            jwtUtil.getUserIdFromToken(tamperedToken);
        });

        logger.debug("Test passed: Exception thrown for tampered token");
    }

    // Edge Case Tests

    /**
     * Tests token generation with null user ID throws exception.
     */
    @Test
    @DisplayName("Should throw exception when generating token with null user ID")
    void testGenerateTokenWithNullUserId() {
        logger.debug("Test: Generating token with null user ID");

        assertThrows(Exception.class, () -> {
            jwtUtil.generateToken(null, TEST_EMAIL);
        });

        logger.debug("Test passed: Exception thrown for null user ID");
    }

    /**
     * Tests token generation with null email.
     */
    @Test
    @DisplayName("Should generate token with null email (stored as 'null' string)")
    void testGenerateTokenWithNullEmail() {
        logger.debug("Test: Generating token with null email");

        String token = jwtUtil.generateToken(TEST_USER_ID, null);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(TEST_USER_ID, jwtUtil.getUserIdFromToken(token));

        logger.debug("Test passed: Token generated with null email");
    }

    /**
     * Tests token generation with very large user ID.
     */
    @Test
    @DisplayName("Should handle very large user ID")
    void testGenerateTokenWithLargeUserId() {
        logger.debug("Test: Generating token with large user ID");

        UUID largeUserId = new UUID(-1L, -1L);
        String token = jwtUtil.generateToken(largeUserId, TEST_EMAIL);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(largeUserId, jwtUtil.getUserIdFromToken(token));

        logger.debug("Test passed: Large user ID handled correctly");
    }
}