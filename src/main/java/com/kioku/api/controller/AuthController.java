package com.kioku.api.controller;

import com.kioku.api.dto.AuthResponse;
import com.kioku.api.dto.ErrorResponse;
import com.kioku.api.dto.LoginRequest;
import com.kioku.api.dto.RegisterRequest;
import com.kioku.api.entity.User;
import com.kioku.api.security.JwtUtil;
import com.kioku.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            AuthResponse response = new AuthResponse(token, user.getId(), user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.authenticateUser(request.getEmail(), request.getPassword());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        AuthResponse response = new AuthResponse(token, user.getId(), user.getEmail());
        return ResponseEntity.ok(response);
    }
}