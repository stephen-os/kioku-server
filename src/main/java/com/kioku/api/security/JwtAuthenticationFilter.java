package com.kioku.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * JWT authentication filter that validates and processes JWT tokens.
 *
 * <p>This filter:
 * <ul>
 *   <li>Extracts JWT tokens from the Authorization header</li>
 *   <li>Validates the token using {@link JwtUtil}</li>
 *   <li>Sets the authenticated user in Spring Security context</li>
 *   <li>Allows the request to proceed if valid, or continues without authentication</li>
 * </ul>
 *
 * <p><strong>Token Format:</strong> {@code Authorization: Bearer <token>}
 *
 * <p><strong>Security Notes:</strong>
 * <ul>
 *   <li>Executes once per request (OncePerRequestFilter)</li>
 *   <li>Does NOT throw exceptions - allows request to continue</li>
 *   <li>Invalid tokens result in unauthenticated request (handled by Spring Security)</li>
 *   <li>User ID is stored as the authentication principal</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 * @see JwtUtil
 * @see CurrentUserArgumentResolver
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    /**
     * Constructs a new JWT authentication filter.
     *
     * @param jwtUtil the JWT utility for token validation and parsing
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Filters each request to validate and process JWT authentication.
     *
     * <p>This method:
     * <ol>
     *   <li>Extracts the JWT token from the Authorization header</li>
     *   <li>Validates the token</li>
     *   <li>Extracts the user ID from the token</li>
     *   <li>Sets the authentication in Spring Security context</li>
     *   <li>Continues the filter chain</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong> All exceptions are caught and logged.
     * The request continues without authentication on error.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                UUID userId = jwtUtil.getUserIdFromToken(jwt);
                logger.debug("Valid JWT token found for user ID: {}", userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("User ID {} authenticated successfully", userId);
            } else {
                logger.debug("No valid JWT token found in request");
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
            // Don't rethrow - allow request to continue unauthenticated
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * <p>Expected header format: {@code Authorization: Bearer <token>}
     *
     * @param request the HTTP request
     * @return the JWT token if present and properly formatted, {@code null} otherwise
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            logger.debug("Extracted JWT token from Authorization header");
            return token;
        }

        logger.debug("No Bearer token found in Authorization header");
        return null;
    }
}