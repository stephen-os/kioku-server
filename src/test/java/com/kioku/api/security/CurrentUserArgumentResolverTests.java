package com.kioku.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import java.util.UUID;

/**
 * Unit tests for CurrentUserArgumentResolver.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Parameter support detection</li>
 *   <li>User ID resolution from security context</li>
 *   <li>Handling of unauthenticated requests</li>
 *   <li>Handling of different parameter types</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CurrentUserArgumentResolver Unit Tests")
class CurrentUserArgumentResolverTests {

    private static final Logger logger = LoggerFactory.getLogger(CurrentUserArgumentResolverTests.class);

    // Test data constants
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private MethodParameter mockParameter;

    @Mock
    private CurrentUser mockCurrentUserAnnotation;

    private CurrentUserArgumentResolver resolver;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up CurrentUserArgumentResolver test");
        resolver = new CurrentUserArgumentResolver();
        SecurityContextHolder.clearContext();
    }

    // Parameter Support Tests

    /**
     * Tests that resolver supports parameter with @CurrentUser and UUID type.
     */
    @Test
    @DisplayName("Should support parameter with @CurrentUser annotation and UUID type")
    void testSupportsParameterWithAnnotationAndUuidType() {
        logger.debug("Test: Checking parameter support with annotation and UUID type");

        when(mockParameter.getParameterAnnotation(CurrentUser.class)).thenReturn(mockCurrentUserAnnotation);
        when(mockParameter.getParameterType()).thenReturn((Class) UUID.class);

        boolean supports = resolver.supportsParameter(mockParameter);

        assertTrue(supports);

        logger.debug("Test passed: Parameter supported");
    }

    /**
     * Tests that resolver does not support parameter without @CurrentUser annotation.
     */
    @Test
    @DisplayName("Should not support parameter without @CurrentUser annotation")
    void testDoesNotSupportParameterWithoutAnnotation() {
        logger.debug("Test: Checking parameter support without annotation");

        when(mockParameter.getParameterAnnotation(CurrentUser.class)).thenReturn(null);
        when(mockParameter.getParameterType()).thenReturn((Class) UUID.class);

        boolean supports = resolver.supportsParameter(mockParameter);

        assertFalse(supports);

        logger.debug("Test passed: Parameter not supported without annotation");
    }

    /**
     * Tests that resolver does not support parameter with wrong type.
     */
    @Test
    @DisplayName("Should not support parameter with @CurrentUser but non-Long type")
    void testDoesNotSupportParameterWithWrongType() {
        logger.debug("Test: Checking parameter support with wrong type");

        when(mockParameter.getParameterAnnotation(CurrentUser.class)).thenReturn(mockCurrentUserAnnotation);
        when(mockParameter.getParameterType()).thenReturn((Class) String.class);

        boolean supports = resolver.supportsParameter(mockParameter);

        assertFalse(supports);

        logger.debug("Test passed: Parameter not supported with wrong type");
    }

    // Argument Resolution Tests

    /**
     * Tests successful user ID resolution from security context.
     */
    @Test
    @DisplayName("Should resolve user ID from security context")
    void testResolveUserIdFromSecurityContext() {
        logger.debug("Test: Resolving user ID from security context");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Object result = resolver.resolveArgument(mockParameter, null, null, null);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result);

        logger.debug("Test passed: User ID resolved successfully");
    }

    /**
     * Tests that null is returned when no authentication is present.
     */
    @Test
    @DisplayName("Should return null when no authentication present")
    void testResolveWithNoAuthentication() {
        logger.debug("Test: Resolving with no authentication");

        SecurityContextHolder.clearContext();

        Object result = resolver.resolveArgument(mockParameter, null, null, null);

        assertNull(result);

        logger.debug("Test passed: Null returned for no authentication");
    }

    /**
     * Tests that null is returned when principal is not a Long.
     */
    @Test
    @DisplayName("Should return null when principal is not Long")
    void testResolveWithNonLongPrincipal() {
        logger.debug("Test: Resolving with non-Long principal");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "stringPrincipal", null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Object result = resolver.resolveArgument(mockParameter, null, null, null);

        assertNull(result);

        logger.debug("Test passed: Null returned for non-Long principal");
    }

    /**
     * Tests resolution with different user IDs.
     */
    @Test
    @DisplayName("Should resolve different user IDs correctly")
    void testResolveDifferentUserIds() {
        logger.debug("Test: Resolving different user IDs");

        UUID userId1 = UUID.fromString("00000000-0000-0000-0000-000000000123");
        UUID userId2 = UUID.fromString("00000000-0000-0000-0000-000000000456");

        // Test first user ID
        Authentication auth1 = new UsernamePasswordAuthenticationToken(userId1, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth1);
        Object result1 = resolver.resolveArgument(mockParameter, null, null, null);
        assertEquals(userId1, result1);

        // Test second user ID
        Authentication auth2 = new UsernamePasswordAuthenticationToken(userId2, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth2);
        Object result2 = resolver.resolveArgument(mockParameter, null, null, null);
        assertEquals(userId2, result2);

        logger.debug("Test passed: Different user IDs resolved correctly");
    }

    // Edge Case Tests

    /**
     * Tests resolution with null authentication principal.
     */
    @Test
    @DisplayName("Should return null when authentication principal is null")
    void testResolveWithNullPrincipal() {
        logger.debug("Test: Resolving with null principal");

        Authentication authentication = new UsernamePasswordAuthenticationToken(null, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Object result = resolver.resolveArgument(mockParameter, null, null, null);

        assertNull(result);

        logger.debug("Test passed: Null returned for null principal");
    }

    /**
     * Tests resolution with very large user ID.
     */
    @Test
    @DisplayName("Should handle very large user ID")
    void testResolveWithLargeUserId() {
        logger.debug("Test: Resolving with large user ID");

        UUID largeUserId = new UUID(-1L, -1L);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                largeUserId, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Object result = resolver.resolveArgument(mockParameter, null, null, null);

        assertNotNull(result);
        assertEquals(largeUserId, result);

        logger.debug("Test passed: Large user ID handled correctly");
    }
}