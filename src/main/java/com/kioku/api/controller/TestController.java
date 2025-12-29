package com.kioku.api.controller;

import com.kioku.api.dto.ErrorResponse;
import com.kioku.api.entity.User;
import com.kioku.api.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Profile("!prod")
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createTestUser(
            @RequestParam String email,
            @RequestParam(defaultValue = "testPassword123") String password) {
        try {
            User user = userService.createUser(email, password);
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("createdAt", user.getCreatedAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("email", user.getEmail());
                    response.put("createdAt", user.getCreatedAt());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found")));
    }
}