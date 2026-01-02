package com.kioku.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>JWT token extraction from headers</li>
 *   <li>Token validation and authentication</li>
 *   <li>Security context population</li>
 *   <li>Error handling for invalid tokens</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilterTest.class);

    // Test data constants
    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";
    private static final Long TEST_USER_ID = 1L;
    private static final String BEARER_PREFIX = "Bearer ";

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up JwtAuthenticationFilter test");
        SecurityContextHolder.clearContext();
    }

    // Valid Token Tests

    /**
     * Tests successful authentication with valid JWT token.
     */
    @Test
    @DisplayName("Should authenticate user with valid JWT token")
    void testAuthenticateWithValidToken() throws Exception {
        logger.debug("Test: Authenticating with valid token");

        when(request.getHeader("Authorization")).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(TEST_USER_ID, authentication.getPrincipal());
        assertTrue(authentication.isAuthenticated());

        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: User authenticated successfully");
    }

    /**
     * Tests that filter chain continues after successful authentication.
     */
    @Test
    @DisplayName("Should continue filter chain after authentication")
    void testFilterChainContinues() throws Exception {
        logger.debug("Test: Verifying filter chain continues");

        when(request.getHeader("Authorization")).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);

        logger.debug("Test passed: Filter chain continued");
    }

    // Invalid Token Tests

    /**
     * Tests that invalid token does not set authentication.
     */
    @Test
    @DisplayName("Should not authenticate with invalid token")
    void testNoAuthenticationWithInvalidToken() throws Exception {
        logger.debug("Test: Attempting authentication with invalid token");

        when(request.getHeader("Authorization")).thenReturn(BEARER_PREFIX + INVALID_TOKEN);
        when(jwtUtil.validateToken(INVALID_TOKEN)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtUtil, never()).getUserIdFromToken(anyString());
        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: No authentication with invalid token");
    }

    /**
     * Tests that missing Authorization header does not set authentication.
     */
    @Test
    @DisplayName("Should not authenticate without Authorization header")
    void testNoAuthenticationWithoutHeader() throws Exception {
        logger.debug("Test: Attempting authentication without header");

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: No authentication without header");
    }

    /**
     * Tests that empty Authorization header does not set authentication.
     */
    @Test
    @DisplayName("Should not authenticate with empty Authorization header")
    void testNoAuthenticationWithEmptyHeader() throws Exception {
        logger.debug("Test: Attempting authentication with empty header");

        when(request.getHeader("Authorization")).thenReturn("");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: No authentication with empty header");
    }

    /**
     * Tests that Authorization header without Bearer prefix does not set authentication.
     */
    @Test
    @DisplayName("Should not authenticate without Bearer prefix")
    void testNoAuthenticationWithoutBearerPrefix() throws Exception {
        logger.debug("Test: Attempting authentication without Bearer prefix");

        when(request.getHeader("Authorization")).thenReturn(VALID_TOKEN); // Missing "Bearer "

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: No authentication without Bearer prefix");
    }

    // Error Handling Tests

    /**
     * Tests that exception during token validation does not break filter chain.
     */
    @Test
    @DisplayName("Should handle token validation exception gracefully")
    void testHandleValidationException() throws Exception {
        logger.debug("Test: Handling token validation exception");

        when(request.getHeader("Authorization")).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenThrow(new RuntimeException("Token validation error"));

        // Should not throw exception
        assertDoesNotThrow(() -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: Exception handled gracefully");
    }

    /**
     * Tests that exception during user ID extraction does not break filter chain.
     */
    @Test
    @DisplayName("Should handle user ID extraction exception gracefully")
    void testHandleUserIdExtractionException() throws Exception {
        logger.debug("Test: Handling user ID extraction exception");

        when(request.getHeader("Authorization")).thenReturn(BEARER_PREFIX + VALID_TOKEN);
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(VALID_TOKEN)).thenThrow(new RuntimeException("User ID extraction error"));

        // Should not throw exception
        assertDoesNotThrow(() -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: Exception handled gracefully");
    }

    // Edge Case Tests

    /**
     * Tests that whitespace-only Authorization header does not authenticate.
     */
    @Test
    @DisplayName("Should not authenticate with whitespace-only header")
    void testNoAuthenticationWithWhitespaceHeader() throws Exception {
        logger.debug("Test: Attempting authentication with whitespace header");

        when(request.getHeader("Authorization")).thenReturn("   ");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: No authentication with whitespace header");
    }

    /**
     * Tests that "Bearer" without token does not authenticate.
     */
    @Test
    @DisplayName("Should not authenticate with Bearer prefix but no token")
    void testNoAuthenticationWithBearerOnly() throws Exception {
        logger.debug("Test: Attempting authentication with Bearer only");

        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: No authentication with Bearer only");
    }

    /**
     * Tests that case-sensitive "bearer" prefix does not work.
     */
    @Test
    @DisplayName("Should not authenticate with lowercase 'bearer' prefix")
    void testNoAuthenticationWithLowercaseBearer() throws Exception {
        logger.debug("Test: Attempting authentication with lowercase bearer");

        when(request.getHeader("Authorization")).thenReturn("bearer " + VALID_TOKEN);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);

        logger.debug("Test passed: Lowercase bearer prefix rejected");
    }
}