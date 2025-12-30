package com.kioku.api.service;

import com.kioku.api.entity.User;
import com.kioku.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Register a new user
     */
    public User registerUser(String email, String password) {
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword);
        return userRepository.save(user);
    }

    /**
     * Authenticate user
     */
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * Create a new user (for testing - real registration uses registerUser)
     */
    public User createUser(String email, String passwordHash) {
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = new User(email, passwordHash);
        return userRepository.save(user);
    }
}