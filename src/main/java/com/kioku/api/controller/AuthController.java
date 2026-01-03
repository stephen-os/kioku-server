package com.kioku.api.controller;

import com.kioku.api.dto.response.AuthResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.dto.request.LoginRequest;
import com.kioku.api.dto.request.RegisterRequest;
import com.kioku.api.entity.UserEntity;
import com.kioku.api.security.JwtUtil;
import com.kioku.api.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for authentication endpoints.
 *
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>User registration</li>
 *   <li>User login</li>
 * </ul>
 *
 * <p><strong>Base Path:</strong> {@code /api/auth}
 *
 * <p><strong>Security:</strong> All endpoints in this controller are publicly accessible
 * and do not require authentication.
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
     * @param userService the user service
     * @param jwtUtil the JWT utility
     */
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user account.
     *
     * <p><strong>Endpoint:</strong> {@code POST /api/auth/register}
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "email": "user@example.com",
     *   "password": "SecurePassword123!"
     * }
     * </pre>
     *
     * <p><strong>Success Response (201 CREATED):</strong>
     * <pre>
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": 1,
     *   "email": "user@example.com"
     * }
     * </pre>
     *
     * <p><strong>Error Response (400 BAD REQUEST):</strong>
     * <pre>
     * {
     *   "error": "Email already in use"
     * }
     * </pre>
     *
     * @param request the registration request containing email and password
     * @return ResponseEntity with auth token on success, error message on failure
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        try {
            UserEntity user = userService.registerUser(request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            logger.info("User registered successfully with id={}", user.getId());

            AuthResponse response = new AuthResponse(token, user.getId(), user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed for email {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * <p><strong>Endpoint:</strong> {@code POST /api/auth/login}
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "email": "user@example.com",
     *   "password": "SecurePassword123!"
     * }
     * </pre>
     *
     * <p><strong>Success Response (200 OK):</strong>
     * <pre>
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": 1,
     *   "email": "user@example.com"
     * }
     * </pre>
     *
     * <p><strong>Error Response (401 UNAUTHORIZED):</strong>
     * <pre>
     * {
     *   "error": "Invalid email or password"
     * }
     * </pre>
     *
     * @param request the login request containing email and password
     * @return ResponseEntity with auth token on success, error message on failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        Optional<UserEntity> userOpt = userService.authenticateUser(request.getEmail(), request.getPassword());

        if (userOpt.isEmpty()) {
            logger.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
        }

        UserEntity user = userOpt.get();
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        logger.info("User logged in successfully with id={}", user.getId());

        AuthResponse response = new AuthResponse(token, user.getId(), user.getEmail());
        return ResponseEntity.ok(response);
    }
}