package com.kioku.api.controller;

import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.entity.UserEntity;
import com.kioku.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for manual testing and verification during development.
 *
 * <p>This controller provides endpoints for creating and retrieving test users
 * without going through the full authentication flow. It's useful for:
 * <ul>
 *   <li>Manual testing with Postman/curl during development</li>
 *   <li>Quick smoke tests in dev/staging environments</li>
 *   <li>Database connectivity verification</li>
 *   <li>Development environment setup and debugging</li>
 * </ul>
 *
 * <p><strong>⚠️ SECURITY WARNING:</strong>
 * This controller is DISABLED in production via {@code @Profile("!prod")}.
 * It bypasses authentication and should NEVER be enabled in production.
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>POST /api/test/users - Create a test user</li>
 *   <li>GET /api/test/users?email={email} - Get user by email</li>
 * </ul>
 *
 * <p><strong>Profile Configuration:</strong>
 * <ul>
 *   <li>Enabled: development, test, staging (any profile except "prod")</li>
 *   <li>Disabled: production (profile "prod")</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong>
 * All exceptions are handled by {@link GlobalExceptionHandler}.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 * @see UserService
 * @see GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/test")
@Profile("!prod")  // Disabled in production
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private final UserService userService;

    /**
     * Constructs a TestController with required dependencies.
     *
     * @param userService the user service for user operations
     */
    public TestController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a test user without authentication.
     *
     * <p><strong>Endpoint:</strong> POST /api/test/users
     *
     * <p><strong>Query Parameters:</strong>
     * <ul>
     *   <li>{@code email} - User email (required)</li>
     *   <li>{@code password} - User password (optional, defaults to "testPassword123")</li>
     * </ul>
     *
     * <p><strong>Example Request:</strong>
     * <pre>
     * POST /api/test/users?email=test@example.com&password=myPassword123
     * </pre>
     *
     * <p><strong>Response:</strong> 201 Created with user details
     *
     * <p><strong>Response Body:</strong>
     * <pre>
     * {
     *   "id": 1,
     *   "email": "test@example.com",
     *   "createdAt": "2026-01-03T15:30:00"
     * }
     * </pre>
     *
     * <p><strong>⚠️ WARNING:</strong>
     * This endpoint bypasses normal authentication flow and should only
     * be used in development/testing environments.
     *
     * @param email the email for the test user
     * @param password the password for the test user (defaults to "testPassword123")
     * @return ResponseEntity containing the created user details
     * @throws IllegalArgumentException if email is already in use (handled by GlobalExceptionHandler)
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createTestUser(
            @RequestParam String email,
            @RequestParam(defaultValue = "testPassword123") String password) {

        logger.debug("Creating test user with email: {}", email);

        UserEntity user = userService.createUser(email, password);

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("createdAt", user.getCreatedAt());

        logger.info("Test user created successfully: userId={}, email={}", user.getId(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a user by email without authentication.
     *
     * <p><strong>Endpoint:</strong> GET /api/test/users?email={email}
     *
     * <p><strong>Query Parameters:</strong>
     * <ul>
     *   <li>{@code email} - User email to search for (required)</li>
     * </ul>
     *
     * <p><strong>Example Request:</strong>
     * <pre>
     * GET /api/test/users?email=test@example.com
     * </pre>
     *
     * <p><strong>Response:</strong> 200 OK with user details, or 404 Not Found
     *
     * <p><strong>Response Body (Success):</strong>
     * <pre>
     * {
     *   "id": 1,
     *   "email": "test@example.com",
     *   "createdAt": "2026-01-03T15:30:00"
     * }
     * </pre>
     *
     * <p><strong>Response Body (Not Found):</strong>
     * <pre>
     * {
     *   "message": "User not found"
     * }
     * </pre>
     *
     * <p><strong>⚠️ WARNING:</strong>
     * This endpoint exposes user data without authentication and should only
     * be used in development/testing environments.
     *
     * @param email the email of the user to retrieve
     * @return ResponseEntity containing the user details or error message
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {

        logger.debug("Retrieving test user by email: {}", email);

        return userService.findByEmail(email)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("email", user.getEmail());
                    response.put("createdAt", user.getCreatedAt());

                    logger.debug("Test user found: userId={}", user.getId());
                    return ResponseEntity.ok((Object) response);
                })
                .orElseGet(() -> {
                    logger.warn("Test user not found with email: {}", email);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse("User not found"));
                });
    }

    /**
     * Health check endpoint to verify the test controller is active.
     *
     * <p><strong>Endpoint:</strong> GET /api/test/health
     *
     * <p>This endpoint confirms that:
     * <ul>
     *   <li>The application is running</li>
     *   <li>Test endpoints are enabled (not in production)</li>
     *   <li>The test controller is loaded</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with status message
     *
     * <p><strong>Response Body:</strong>
     * <pre>
     * {
     *   "status": "ok",
     *   "message": "Test endpoints are active",
     *   "warning": "These endpoints are disabled in production"
     * }
     * </pre>
     *
     * @return ResponseEntity with health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        logger.debug("Test controller health check");

        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Test endpoints are active");
        response.put("warning", "These endpoints are disabled in production");

        return ResponseEntity.ok(response);
    }
}