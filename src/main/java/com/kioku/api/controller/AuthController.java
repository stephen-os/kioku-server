package com.kioku.api.controller;

import com.kioku.api.dto.request.LoginRequest;
import com.kioku.api.dto.request.RegisterRequest;
import com.kioku.api.dto.response.AuthResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.model.User;
import com.kioku.api.security.JwtUtil;
import com.kioku.api.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST controller for user authentication operations.
 *
 * <p>This controller provides endpoints for user registration and login.
 * It handles JWT token generation and user authentication.
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>POST /api/auth/register - Register a new user account</li>
 *   <li>POST /api/auth/login - Authenticate and obtain JWT token</li>
 * </ul>
 *
 * <p><strong>Authentication Flow:</strong>
 * <ol>
 *   <li>User registers with email and password</li>
 *   <li>Password is hashed and stored securely</li>
 *   <li>User logs in with credentials</li>
 *   <li>Server validates credentials and returns JWT token</li>
 *   <li>Client includes JWT token in subsequent requests</li>
 * </ol>
 *
 * <p><strong>Security:</strong>
 * <ul>
 *   <li>Passwords are hashed using BCrypt</li>
 *   <li>JWT tokens are signed and time-limited</li>
 *   <li>Email addresses must be unique</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong>
 * All exceptions are handled by {@link GlobalExceptionHandler}:
 * <ul>
 *   <li>400 Bad Request - Validation errors, duplicate email, invalid credentials</li>
 *   <li>500 Internal Server Error - Unexpected errors</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Constructs an AuthController with required dependencies.
     *
     * @param userService the user service for user operations
     * @param jwtUtil the JWT utility for token generation
     */
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user account.
     *
     * <p><strong>Endpoint:</strong> POST /api/auth/register
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "email": "user@example.com",
     *   "password": "securePassword123"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Email must be valid format</li>
     *   <li>Email must be unique (not already registered)</li>
     *   <li>Password must be at least 8 characters</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 201 Created with JWT token and user details
     *
     * <p><strong>Response Body:</strong>
     * <pre>
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": 1,
     *   "email": "user@example.com"
     * }
     * </pre>
     *
     * @param request the registration request containing email and password
     * @return ResponseEntity with 201 status and authentication response containing JWT token
     * @throws IllegalArgumentException if email is already registered (handled by GlobalExceptionHandler)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.debug("Registration attempt for email: {}", request.getEmail());

        User user = userService.registerUser(request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        AuthResponse response = new AuthResponse(token, user.getId(), user.getEmail());

        logger.info("User registered successfully: userId={}, email={}", user.getId(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * <p><strong>Endpoint:</strong> POST /api/auth/login
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "email": "user@example.com",
     *   "password": "securePassword123"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Email must exist in the system</li>
     *   <li>Password must match the stored hash</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with JWT token and user details
     *
     * <p><strong>Response Body:</strong>
     * <pre>
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": 1,
     *   "email": "user@example.com"
     * }
     * </pre>
     *
     * @param request the login request containing email and password
     * @return ResponseEntity with 200 status and authentication response containing JWT token
     * @throws IllegalArgumentException if credentials are invalid (handled by GlobalExceptionHandler)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        logger.debug("Login attempt for email: {}", request.getEmail());

        Optional<User> userOptional = userService.authenticateUser(request.getEmail(), request.getPassword());

        if (userOptional.isEmpty()) {
            logger.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
        }

        User user = userOptional.get();
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        AuthResponse response = new AuthResponse(token, user.getId(), user.getEmail());

        logger.info("User logged in successfully: userId={}, email={}", user.getId(), user.getEmail());
        return ResponseEntity.ok(response);
    }
}