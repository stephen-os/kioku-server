package com.kioku.api.service;

import com.kioku.api.entity.UserEntity;
import com.kioku.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for User entity business logic.
 *
 * <p>This service handles:
 * <ul>
 *   <li>User registration and authentication</li>
 *   <li>Email verification workflow</li>
 *   <li>Password reset workflow</li>
 *   <li>Account locking and unlocking</li>
 *   <li>User status management</li>
 *   <li>Password encoding and verification</li>
 * </ul>
 *
 * <p><strong>Security:</strong> This service ensures passwords are properly
 * encoded using BCrypt and never stored or logged in plain text.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a UserService with required dependencies.
     *
     * @param userRepository the user repository for database operations
     * @param passwordEncoder the password encoder for hashing passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        logger.debug("UserService initialized");
    }

    // User Lookup Methods

    /**
     * Finds a user by their unique identifier.
     *
     * @param id the user ID
     * @return an Optional containing the user if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> findById(Long id) {
        logger.debug("Finding user by id={}", id);
        Optional<UserEntity> user = userRepository.findById(id);
        logger.debug("User found: {}", user.isPresent());
        return user;
    }

    /**
     * Finds a user by their email address.
     *
     * <p>Email lookup is case-insensitive.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByEmail(String email) {
        logger.debug("Finding user by email");
        Optional<UserEntity> user = userRepository.findByEmail(email);
        logger.debug("User found: {}", user.isPresent());
        return user;
    }

    /**
     * Checks if a user with the given email exists.
     *
     * <p>Email lookup is case-insensitive.
     *
     * @param email the email address to check
     * @return {@code true} if a user exists, {@code false} otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        logger.debug("Checking if email exists");
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("Email exists: {}", exists);
        return exists;
    }

    /**
     * Finds all users with a specific status.
     *
     * @param status the user status to filter by
     * @return a list of users with the given status
     */
    @Transactional(readOnly = true)
    public List<UserEntity> findByStatus(UserEntity.UserStatus status) {
        logger.debug("Finding users by status={}", status);
        List<UserEntity> userEntities = userRepository.findByStatus(status);
        logger.debug("Found {} users with status={}", userEntities.size(), status);
        return userEntities;
    }

    /**
     * Finds all currently active accounts.
     *
     * @return a list of active users
     */
    @Transactional(readOnly = true)
    public List<UserEntity> findActiveAccounts() {
        logger.debug("Finding all active accounts");
        List<UserEntity> userEntities = userRepository.findActiveAccounts(LocalDateTime.now());
        logger.debug("Found {} active accounts", userEntities.size());
        return userEntities;
    }

    /**
     * Finds all currently locked accounts.
     *
     * @return a list of locked users
     */
    @Transactional(readOnly = true)
    public List<UserEntity> findLockedAccounts() {
        logger.debug("Finding all locked accounts");
        List<UserEntity> userEntities = userRepository.findLockedAccounts(LocalDateTime.now());
        logger.debug("Found {} locked accounts", userEntities.size());
        return userEntities;
    }

    // User Registration and Authentication

    /**
     * Registers a new user with email and password.
     *
     * <p>This method:
     * <ul>
     *   <li>Validates email doesn't already exist</li>
     *   <li>Hashes the password using BCrypt</li>
     *   <li>Creates and saves the user</li>
     *   <li>Sets initial status to PENDING_VERIFICATION</li>
     * </ul>
     *
     * @param email the user's email address
     * @param password the user's plain text password (will be hashed)
     * @return the newly created user
     * @throws IllegalArgumentException if email already exists
     */
    public UserEntity registerUser(String email, String password) {
        logger.debug("Registering new user");

        if (existsByEmail(email)) {
            logger.warn("Registration failed: email already exists");
            throw new IllegalArgumentException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        UserEntity userEntity = new UserEntity(email, hashedPassword);
        userEntity.setStatus(UserEntity.UserStatus.PENDING_VERIFICATION);

        UserEntity savedUserEntity = userRepository.save(userEntity);
        logger.debug("User registered successfully with id={}", savedUserEntity.getId());

        return savedUserEntity;
    }

    /**
     * Authenticates a user with email and password.
     *
     * <p>This method:
     * <ul>
     *   <li>Finds the user by email</li>
     *   <li>Checks if account is active</li>
     *   <li>Verifies the password</li>
     *   <li>Records successful or failed login attempts</li>
     * </ul>
     *
     * @param email the user's email address
     * @param password the user's plain text password
     * @return an Optional containing the authenticated user, or empty if authentication fails
     */
    public Optional<UserEntity> authenticateUser(String email, String password) {
        logger.debug("Authenticating user");

        Optional<UserEntity> userOpt = findByEmail(email);

        if (userOpt.isEmpty()) {
            logger.debug("Authentication failed: user not found");
            return Optional.empty();
        }

        UserEntity userEntity = userOpt.get();

        // Check if account is locked
        if (userEntity.isLocked()) {
            logger.warn("Authentication failed: user id={} account is locked until {}",
                    userEntity.getId(), userEntity.getLockedUntil());
            userEntity.recordFailedLoginAttempt();
            userRepository.save(userEntity);
            return Optional.empty();
        }

        // Check if account is deleted
        if (userEntity.isDeleted()) {
            logger.warn("Authentication failed: user id={} account is deleted", userEntity.getId());
            return Optional.empty();
        }

        // Verify password
        if (passwordEncoder.matches(password, userEntity.getPasswordHash())) {
            logger.debug("Authentication successful for user id={}", userEntity.getId());
            userEntity.recordSuccessfulLogin();
            userRepository.save(userEntity);
            return Optional.of(userEntity);
        } else {
            logger.debug("Authentication failed: invalid password for user id={}", userEntity.getId());
            userEntity.recordFailedLoginAttempt();
            userRepository.save(userEntity);
            return Optional.empty();
        }
    }

    // Email Verification

    /**
     * Initiates email verification by generating and setting a verification token.
     *
     * <p>The token expires after 24 hours.
     *
     * @param userId the ID of the user to verify
     * @return the generated verification token
     * @throws IllegalArgumentException if user not found
     */
    public String initiateEmailVerification(Long userId) {
        logger.debug("Initiating email verification for user id={}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = UUID.randomUUID().toString();
        userEntity.setEmailVerificationToken(token);
        userRepository.save(userEntity);

        logger.debug("Email verification token generated for user id={}", userId);
        return token;
    }

    /**
     * Verifies a user's email using the verification token.
     *
     * @param token the verification token
     * @return {@code true} if verification successful, {@code false} otherwise
     */
    public boolean verifyEmail(String token) {
        logger.debug("Verifying email with token");

        Optional<UserEntity> userOpt = userRepository.findByEmailVerificationToken(token);

        if (userOpt.isEmpty()) {
            logger.debug("Email verification failed: invalid token");
            return false;
        }

        UserEntity userEntity = userOpt.get();

        if (userEntity.isEmailVerificationTokenExpired()) {
            logger.debug("Email verification failed: token expired for user id={}", userEntity.getId());
            return false;
        }

        userEntity.verifyEmail();
        userEntity.setStatus(UserEntity.UserStatus.ACTIVE);
        userRepository.save(userEntity);

        logger.debug("Email verified successfully for user id={}", userEntity.getId());
        return true;
    }

    // Password Reset

    /**
     * Initiates password reset by generating and setting a reset token.
     *
     * <p>The token expires after 24 hours.
     *
     * @param email the email of the user requesting password reset
     * @return an Optional containing the reset token if user found, empty otherwise
     */
    public Optional<String> initiatePasswordReset(String email) {
        logger.debug("Initiating password reset");

        Optional<UserEntity> userOpt = findByEmail(email);

        if (userOpt.isEmpty()) {
            logger.debug("Password reset failed: user not found");
            return Optional.empty();
        }

        UserEntity userEntity = userOpt.get();
        String token = UUID.randomUUID().toString();
        userEntity.setPasswordResetToken(token);
        userRepository.save(userEntity);

        logger.debug("Password reset token generated for user id={}", userEntity.getId());
        return Optional.of(token);
    }

    /**
     * Resets a user's password using a reset token.
     *
     * @param token the password reset token
     * @param newPassword the new plain text password (will be hashed)
     * @return {@code true} if reset successful, {@code false} otherwise
     */
    public boolean resetPassword(String token, String newPassword) {
        logger.debug("Resetting password with token");

        Optional<UserEntity> userOpt = userRepository.findByPasswordResetToken(token);

        if (userOpt.isEmpty()) {
            logger.debug("Password reset failed: invalid token");
            return false;
        }

        UserEntity userEntity = userOpt.get();

        if (userEntity.isPasswordResetTokenExpired()) {
            logger.debug("Password reset failed: token expired for user id={}", userEntity.getId());
            return false;
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        userEntity.setPasswordHash(hashedPassword);
        userEntity.clearPasswordResetToken();
        userRepository.save(userEntity);

        logger.debug("Password reset successfully for user id={}", userEntity.getId());
        return true;
    }

    // Account Management

    /**
     * Updates a user's password.
     *
     * @param userId the user ID
     * @param currentPassword the current password for verification
     * @param newPassword the new password
     * @return {@code true} if password updated, {@code false} if current password is incorrect
     * @throws IllegalArgumentException if user not found
     */
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        logger.debug("Updating password for user id={}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, userEntity.getPasswordHash())) {
            logger.debug("Password update failed: current password incorrect for user id={}", userId);
            return false;
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        userEntity.setPasswordHash(hashedPassword);
        userRepository.save(userEntity);

        logger.debug("Password updated successfully for user id={}", userId);
        return true;
    }

    /**
     * Soft deletes a user account.
     *
     * @param userId the user ID to delete
     * @throws IllegalArgumentException if user not found
     */
    public void deleteUser(Long userId) {
        logger.debug("Soft deleting user id={}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userEntity.softDelete();
        userRepository.save(userEntity);

        logger.debug("User id={} soft deleted successfully", userId);
    }

    /**
     * Restores a soft-deleted user account.
     *
     * @param userId the user ID to restore
     * @throws IllegalArgumentException if user not found
     */
    public void restoreUser(Long userId) {
        logger.debug("Restoring user id={}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userEntity.restore();
        userRepository.save(userEntity);

        logger.debug("User id={} restored successfully", userId);
    }

    /**
     * Suspends a user account.
     *
     * @param userId the user ID to suspend
     * @throws IllegalArgumentException if user not found
     */
    public void suspendUser(Long userId) {
        logger.debug("Suspending user id={}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userEntity.setStatus(UserEntity.UserStatus.SUSPENDED);
        userRepository.save(userEntity);

        logger.debug("User id={} suspended successfully", userId);
    }

    /**
     * Activates a suspended user account.
     *
     * @param userId the user ID to activate
     * @throws IllegalArgumentException if user not found
     */
    public void activateUser(Long userId) {
        logger.debug("Activating user id={}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userEntity.setStatus(UserEntity.UserStatus.ACTIVE);
        userRepository.save(userEntity);

        logger.debug("User id={} activated successfully", userId);
    }

    /**
     * Manually unlocks a locked user account.
     *
     * @param userId the user ID to unlock
     * @throws IllegalArgumentException if user not found
     */
    public void unlockUser(Long userId) {
        logger.debug("Unlocking user id={}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userEntity.setLockedUntil(null);
        userEntity.setFailedLoginAttempts(0);
        userRepository.save(userEntity);

        logger.debug("User id={} unlocked successfully", userId);
    }

    // Utility Methods

    /**
     * Creates a new user with pre-hashed password (for testing purposes).
     *
     * <p><strong>Warning:</strong> This method bypasses password encoding.
     * Use {@link #registerUser(String, String)} for normal user creation.
     *
     * @param email the user's email address
     * @param passwordHash the pre-hashed password
     * @return the created user
     * @throws IllegalArgumentException if email already exists
     */
    public UserEntity createUser(String email, String passwordHash) {
        logger.debug("Creating user with pre-hashed password (testing mode)");

        if (existsByEmail(email)) {
            logger.warn("User creation failed: email already exists");
            throw new IllegalArgumentException("Email already exists");
        }

        UserEntity userEntity = new UserEntity(email, passwordHash);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        logger.debug("User created with id={}", savedUserEntity.getId());
        return savedUserEntity;
    }
}