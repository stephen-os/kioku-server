package com.kioku.api.service;

import com.kioku.api.model.User;
import com.kioku.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    public Optional<User> findById(UUID id) {
        logger.debug("Finding user by id={}", id);
        Optional<User> user = userRepository.findById(id);
        logger.debug("User found: {}", user.isPresent());
        return user;
    }

    /**
     * Saves a user to the database.
     *
     * <p>This method should be used when updating user relationships
     * (e.g., adding decks) since those are unidirectional from User.
     *
     * @param user the user to save
     * @return the saved user
     */
    public User save(User user) {
        logger.debug("Saving user id={}", user.getId());
        return userRepository.save(user);
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
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email");
        Optional<User> user = userRepository.findByEmail(email);
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
    public List<User> findByStatus(User.UserStatus status) {
        logger.debug("Finding users by status={}", status);
        List<User> userEntities = userRepository.findByStatus(status);
        logger.debug("Found {} users with status={}", userEntities.size(), status);
        return userEntities;
    }

    /**
     * Finds all currently active accounts.
     *
     * @return a list of active users
     */
    @Transactional(readOnly = true)
    public List<User> findActiveAccounts() {
        logger.debug("Finding all active accounts");
        List<User> userEntities = userRepository.findActiveAccounts(Instant.now());
        logger.debug("Found {} active accounts", userEntities.size());
        return userEntities;
    }

    /**
     * Finds all currently locked accounts.
     *
     * @return a list of locked users
     */
    @Transactional(readOnly = true)
    public List<User> findLockedAccounts() {
        logger.debug("Finding all locked accounts");
        List<User> userEntities = userRepository.findLockedAccounts(Instant.now());
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
    public User registerUser(String email, String password) {
        logger.debug("Registering new user");

        if (existsByEmail(email)) {
            logger.warn("Registration failed: email already exists");
            throw new IllegalArgumentException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword);
        user.setStatus(User.UserStatus.PENDING_VERIFICATION);

        User savedUser = userRepository.save(user);
        logger.debug("User registered successfully with id={}", savedUser.getId());

        return savedUser;
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
    public Optional<User> authenticateUser(String email, String password) {
        logger.debug("Authenticating user");

        Optional<User> userOpt = findByEmail(email);

        if (userOpt.isEmpty()) {
            logger.debug("Authentication failed: user not found");
            return Optional.empty();
        }

        User user = userOpt.get();

        // Check if account is locked
        if (user.isLocked()) {
            logger.warn("Authentication failed: user id={} account is locked until {}",
                    user.getId(), user.getLockedUntil());
            user.recordFailedLoginAttempt();
            userRepository.save(user);
            return Optional.empty();
        }

        // Check if account is deleted
        if (user.isDeleted()) {
            logger.warn("Authentication failed: user id={} account is deleted", user.getId());
            return Optional.empty();
        }

        // Verify password
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.debug("Authentication successful for user id={}", user.getId());
            user.recordSuccessfulLogin();
            userRepository.save(user);
            return Optional.of(user);
        } else {
            logger.debug("Authentication failed: invalid password for user id={}", user.getId());
            user.recordFailedLoginAttempt();
            userRepository.save(user);
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
    public String initiateEmailVerification(UUID userId) {
        logger.debug("Initiating email verification for user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        userRepository.save(user);

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

        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);

        if (userOpt.isEmpty()) {
            logger.debug("Email verification failed: invalid token");
            return false;
        }

        User user = userOpt.get();

        if (user.isEmailVerificationTokenExpired()) {
            logger.debug("Email verification failed: token expired for user id={}", user.getId());
            return false;
        }

        user.verifyEmail();
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        logger.debug("Email verified successfully for user id={}", user.getId());
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

        Optional<User> userOpt = findByEmail(email);

        if (userOpt.isEmpty()) {
            logger.debug("Password reset failed: user not found");
            return Optional.empty();
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        userRepository.save(user);

        logger.debug("Password reset token generated for user id={}", user.getId());
        return Optional.of(token);
    }

    /**
     * Resets a user's password using a reset token.
     *
     * @param token the password reset token
     * @param newPassword the new plain text password (will be hashed)
     * @return {@code true} if reset successful, {@code false} otherwise
     */
    /**
     * Finds the account a reset token belongs to.
     *
     * <p>Callers that need the account after a reset must look it up first:
     * {@link #resetPassword} clears the token, so afterwards there is nothing
     * left to search by.
     *
     * @param token the reset token
     * @return the account, or empty if no account holds that token
     */
    @Transactional(readOnly = true)
    public Optional<User> findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    public boolean resetPassword(String token, String newPassword) {
        logger.debug("Resetting password with token");

        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);

        if (userOpt.isEmpty()) {
            logger.debug("Password reset failed: invalid token");
            return false;
        }

        User user = userOpt.get();

        if (user.isPasswordResetTokenExpired()) {
            logger.debug("Password reset failed: token expired for user id={}", user.getId());
            return false;
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        user.clearPasswordResetToken();
        userRepository.save(user);

        logger.debug("Password reset successfully for user id={}", user.getId());
        return true;
    }

    // Account Management

    /**
     * Updates how an account presents itself.
     *
     * <p>A null field is left unchanged rather than cleared, so a client can
     * change one without restating the other.
     *
     * @param userId      the account
     * @param displayName new display name, or null to leave it
     * @param avatar      new avatar id, or null to leave it
     * @return the updated account
     */
    @Transactional
    public User updateProfile(UUID userId, String displayName, String avatar) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        logger.debug("Updated profile for user id={}", userId);
        return userRepository.save(user);
    }

    /**
     * Gets a user's account profile information.
     *
     * @param userId the user ID
     * @return the user
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public User getAccountProfile(UUID userId) {
        logger.debug("Getting account profile for user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Account profile failed: user not found id={}", userId);
                    return new IllegalArgumentException("User not found");
                });

        logger.debug("Account profile retrieved for user id={}", userId);
        return user;
    }

    /**
     * Initiates an email change by generating a verification token.
     *
     * <p>This method:
     * <ul>
     *   <li>Verifies the user's current password</li>
     *   <li>Checks the new email isn't already registered</li>
     *   <li>Stores the pending email with a verification token</li>
     * </ul>
     *
     * <p>The token expires after 24 hours.
     *
     * @param userId the user ID
     * @param currentPassword the current password for verification
     * @param newEmail the new email address to change to
     * @return the verification token
     * @throws IllegalArgumentException if user not found, password incorrect, or email already registered
     */
    public String initiateEmailChange(UUID userId, String currentPassword, String newEmail) {
        logger.debug("Initiating email change for user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Email change failed: user not found id={}", userId);
                    return new IllegalArgumentException("User not found");
                });

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            logger.warn("Email change failed: current password incorrect for user id={}", userId);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Check if new email is already registered (case-insensitive)
        String normalizedEmail = newEmail.toLowerCase().trim();
        if (existsByEmail(normalizedEmail)) {
            logger.warn("Email change failed: email already registered: {}", normalizedEmail);
            throw new IllegalArgumentException("Email is already registered");
        }

        // Generate token and store pending email
        String token = UUID.randomUUID().toString();
        user.setPendingEmailChange(normalizedEmail, token);
        userRepository.save(user);

        logger.debug("Email change token generated for user id={}, pending email={}",
                userId, normalizedEmail);
        return token;
    }

    /**
     * Confirms an email change using the verification token.
     *
     * @param token the verification token
     * @return {@code true} if email change successful, {@code false} otherwise
     */
    public boolean confirmEmailChange(String token) {
        logger.debug("Confirming email change with token");

        Optional<User> userOpt = userRepository.findByPendingEmailToken(token);

        if (userOpt.isEmpty()) {
            logger.debug("Email change confirmation failed: invalid token");
            return false;
        }

        User user = userOpt.get();

        if (user.isPendingEmailTokenExpired()) {
            logger.debug("Email change confirmation failed: token expired for user id={}", user.getId());
            user.clearPendingEmailChange();
            userRepository.save(user);
            return false;
        }

        // Verify email still not taken (race condition check)
        if (existsByEmail(user.getPendingEmail())) {
            logger.warn("Email change confirmation failed: email now registered by another user");
            user.clearPendingEmailChange();
            userRepository.save(user);
            return false;
        }

        String newEmail = user.confirmPendingEmailChange();
        userRepository.save(user);

        logger.debug("Email changed successfully for user id={} to {}", user.getId(), newEmail);
        return true;
    }

    /**
     * Soft deletes a user account after verifying the password.
     *
     * @param userId the user ID
     * @param currentPassword the current password for verification
     * @throws IllegalArgumentException if user not found or password incorrect
     */
    public void softDeleteAccount(UUID userId, String currentPassword) {
        logger.debug("Soft deleting account for user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Account deletion failed: user not found id={}", userId);
                    return new IllegalArgumentException("User not found");
                });

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            logger.warn("Account deletion failed: current password incorrect for user id={}", userId);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.softDelete();
        userRepository.save(user);

        logger.info("Account soft deleted for user id={}", userId);
    }

    /**
     * Updates a user's password.
     *
     * @param userId the user ID
     * @param currentPassword the current password for verification
     * @param newPassword the new password
     * @return {@code true} if password updated, {@code false} if current password is incorrect
     * @throws IllegalArgumentException if user not found
     */
    public boolean updatePassword(UUID userId, String currentPassword, String newPassword) {
        logger.debug("Updating password for user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            logger.debug("Password update failed: current password incorrect for user id={}", userId);
            return false;
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);

        logger.debug("Password updated successfully for user id={}", userId);
        return true;
    }

    /**
     * Soft deletes a user account.
     *
     * @param userId the user ID to delete
     * @throws IllegalArgumentException if user not found
     */
    public void deleteUser(UUID userId) {
        logger.debug("Soft deleting user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.softDelete();
        userRepository.save(user);

        logger.debug("User id={} soft deleted successfully", userId);
    }

    /**
     * Restores a soft-deleted user account.
     *
     * @param userId the user ID to restore
     * @throws IllegalArgumentException if user not found
     */
    public void restoreUser(UUID userId) {
        logger.debug("Restoring user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.restore();
        userRepository.save(user);

        logger.debug("User id={} restored successfully", userId);
    }

    /**
     * Suspends a user account.
     *
     * @param userId the user ID to suspend
     * @throws IllegalArgumentException if user not found
     */
    public void suspendUser(UUID userId) {
        logger.debug("Suspending user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setStatus(User.UserStatus.SUSPENDED);
        userRepository.save(user);

        logger.debug("User id={} suspended successfully", userId);
    }

    /**
     * Activates a suspended user account.
     *
     * @param userId the user ID to activate
     * @throws IllegalArgumentException if user not found
     */
    public void activateUser(UUID userId) {
        logger.debug("Activating user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        logger.debug("User id={} activated successfully", userId);
    }

    /**
     * Manually unlocks a locked user account.
     *
     * @param userId the user ID to unlock
     * @throws IllegalArgumentException if user not found
     */
    public void unlockUser(UUID userId) {
        logger.debug("Unlocking user id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

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
    public User createUser(String email, String passwordHash) {
        logger.debug("Creating user with pre-hashed password (testing mode)");

        if (existsByEmail(email)) {
            logger.warn("User creation failed: email already exists");
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(email, passwordHash);
        User savedUser = userRepository.save(user);

        logger.debug("User created with id={}", savedUser.getId());
        return savedUser;
    }
}