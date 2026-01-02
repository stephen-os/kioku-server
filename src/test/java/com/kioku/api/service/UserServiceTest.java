package com.kioku.api.service;

import com.kioku.api.entity.UserEntity;
import com.kioku.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>User registration and authentication</li>
 *   <li>Email verification workflow</li>
 *   <li>Password reset workflow</li>
 *   <li>Account locking and unlocking</li>
 *   <li>Password updates</li>
 *   <li>User status management</li>
 *   <li>Soft delete and restore</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String PLAIN_PASSWORD = "Password123!";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String OTHER_PASSWORD_HASH = "$2a$10$otherHashedPassword456";
    private static final Long USER_ID = 1L;
    private static final String VERIFICATION_TOKEN = "verification-token-uuid-12345";
    private static final String RESET_TOKEN = "reset-token-uuid-67890";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserEntity testUserEntity;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up test: Creating test user with email={}", TEST_EMAIL);
        testUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        testUserEntity.setId(USER_ID);
    }

    // User Lookup Tests

    /**
     * Tests finding a user by ID.
     */
    @Test
    @DisplayName("Should find user by ID")
    void testFindById() {
        logger.debug("Test: Finding user by id={}", USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));

        Optional<UserEntity> found = userService.findById(USER_ID);

        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());
        verify(userRepository).findById(USER_ID);

        logger.debug("Test passed: User found by ID");
    }

    /**
     * Tests finding user by ID returns empty when not found.
     */
    @Test
    @DisplayName("Should return empty when user ID not found")
    void testFindByIdNotFound() {
        logger.debug("Test: Finding non-existent user by ID");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Optional<UserEntity> found = userService.findById(USER_ID);

        assertFalse(found.isPresent());
        verify(userRepository).findById(USER_ID);

        logger.debug("Test passed: Empty result for non-existent ID");
    }

    /**
     * Tests finding a user by email.
     */
    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        logger.debug("Test: Finding user by email={}", TEST_EMAIL);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUserEntity));

        Optional<UserEntity> found = userService.findByEmail(TEST_EMAIL);

        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());
        verify(userRepository).findByEmail(TEST_EMAIL);

        logger.debug("Test passed: User found by email");
    }

    /**
     * Tests checking if email exists.
     */
    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        logger.debug("Test: Checking if email exists");

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        boolean exists = userService.existsByEmail(TEST_EMAIL);

        assertTrue(exists);
        verify(userRepository).existsByEmail(TEST_EMAIL);

        logger.debug("Test passed: Email existence check successful");
    }

    /**
     * Tests finding users by status.
     */
    @Test
    @DisplayName("Should find users by status")
    void testFindByStatus() {
        logger.debug("Test: Finding users by status=ACTIVE");

        when(userRepository.findByStatus(UserEntity.UserStatus.ACTIVE))
                .thenReturn(List.of(testUserEntity));

        List<UserEntity> userEntities = userService.findByStatus(UserEntity.UserStatus.ACTIVE);

        assertEquals(1, userEntities.size());
        assertEquals(TEST_EMAIL, userEntities.get(0).getEmail());
        verify(userRepository).findByStatus(UserEntity.UserStatus.ACTIVE);

        logger.debug("Test passed: Found {} users with status ACTIVE", userEntities.size());
    }

    // User Registration Tests

    /**
     * Tests successful user registration.
     */
    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterUser() {
        logger.debug("Test: Registering new user with email={}", TEST_EMAIL);

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PLAIN_PASSWORD)).thenReturn(PASSWORD_HASH);
        // ✅ FIX: Use Answer to capture and return the actual user being saved
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity userEntity = invocation.getArgument(0);
            userEntity.setId(USER_ID); // Simulate ID being set by database
            return userEntity;
        });

        UserEntity registered = userService.registerUser(TEST_EMAIL, PLAIN_PASSWORD);

        assertNotNull(registered);
        assertEquals(TEST_EMAIL, registered.getEmail());
        assertEquals(UserEntity.UserStatus.PENDING_VERIFICATION, registered.getStatus());
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(PLAIN_PASSWORD);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: User registered successfully");
    }

    /**
     * Tests registration fails when email already exists.
     */
    @Test
    @DisplayName("Should throw exception when registering duplicate email")
    void testRegisterUserDuplicateEmail() {
        logger.debug("Test: Attempting to register duplicate email");

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(TEST_EMAIL, PLAIN_PASSWORD);
        });

        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Duplicate email registration prevented");
    }

    // Authentication Tests

    /**
     * Tests successful authentication.
     */
    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void testAuthenticateUserSuccess() {
        logger.debug("Test: Authenticating user with valid credentials");

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches(PLAIN_PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        Optional<UserEntity> authenticated = userService.authenticateUser(TEST_EMAIL, PLAIN_PASSWORD);

        assertTrue(authenticated.isPresent());
        assertEquals(TEST_EMAIL, authenticated.get().getEmail());
        assertEquals(0, authenticated.get().getFailedLoginAttempts());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(PLAIN_PASSWORD, PASSWORD_HASH);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: User authenticated successfully");
    }

    /**
     * Tests authentication fails with invalid password.
     */
    @Test
    @DisplayName("Should fail authentication with invalid password")
    void testAuthenticateUserInvalidPassword() {
        logger.debug("Test: Authenticating user with invalid password");

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches(PLAIN_PASSWORD, PASSWORD_HASH)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        Optional<UserEntity> authenticated = userService.authenticateUser(TEST_EMAIL, PLAIN_PASSWORD);

        assertFalse(authenticated.isPresent());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(PLAIN_PASSWORD, PASSWORD_HASH);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: Authentication failed with invalid password");
    }

    /**
     * Tests authentication fails for non-existent user.
     */
    @Test
    @DisplayName("Should fail authentication when user not found")
    void testAuthenticateUserNotFound() {
        logger.debug("Test: Authenticating non-existent user");

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        Optional<UserEntity> authenticated = userService.authenticateUser(TEST_EMAIL, PLAIN_PASSWORD);

        assertFalse(authenticated.isPresent());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Authentication failed for non-existent user");
    }

    /**
     * Tests authentication fails for locked account.
     */
    @Test
    @DisplayName("Should fail authentication for locked account")
    void testAuthenticateUserLocked() {
        logger.debug("Test: Authenticating locked user");

        testUserEntity.setLockedUntil(LocalDateTime.now().plusHours(1));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        Optional<UserEntity> authenticated = userService.authenticateUser(TEST_EMAIL, PLAIN_PASSWORD);

        assertFalse(authenticated.isPresent());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: Authentication failed for locked account");
    }

    /**
     * Tests authentication fails for deleted account.
     */
    @Test
    @DisplayName("Should fail authentication for deleted account")
    void testAuthenticateUserDeleted() {
        logger.debug("Test: Authenticating deleted user");

        testUserEntity.softDelete();
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUserEntity));

        Optional<UserEntity> authenticated = userService.authenticateUser(TEST_EMAIL, PLAIN_PASSWORD);

        assertFalse(authenticated.isPresent());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Authentication failed for deleted account");
    }

    // Email Verification Tests

    /**
     * Tests initiating email verification.
     */
    @Test
    @DisplayName("Should initiate email verification")
    void testInitiateEmailVerification() {
        logger.debug("Test: Initiating email verification for user id={}", USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        String token = userService.initiateEmailVerification(USER_ID);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: Email verification initiated with token");
    }

    /**
     * Tests initiating email verification fails for non-existent user.
     */
    @Test
    @DisplayName("Should throw exception when initiating verification for non-existent user")
    void testInitiateEmailVerificationUserNotFound() {
        logger.debug("Test: Initiating verification for non-existent user");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.initiateEmailVerification(USER_ID);
        });

        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Exception thrown for non-existent user");
    }

    /**
     * Tests successful email verification.
     */
    @Test
    @DisplayName("Should verify email with valid token")
    void testVerifyEmailSuccess() {
        logger.debug("Test: Verifying email with valid token");

        testUserEntity.setEmailVerificationToken(VERIFICATION_TOKEN);
        when(userRepository.findByEmailVerificationToken(VERIFICATION_TOKEN))
                .thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        boolean verified = userService.verifyEmail(VERIFICATION_TOKEN);

        assertTrue(verified);
        verify(userRepository).findByEmailVerificationToken(VERIFICATION_TOKEN);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: Email verified successfully");
    }

    /**
     * Tests email verification fails with invalid token.
     */
    @Test
    @DisplayName("Should fail email verification with invalid token")
    void testVerifyEmailInvalidToken() {
        logger.debug("Test: Verifying email with invalid token");

        when(userRepository.findByEmailVerificationToken(VERIFICATION_TOKEN))
                .thenReturn(Optional.empty());

        boolean verified = userService.verifyEmail(VERIFICATION_TOKEN);

        assertFalse(verified);
        verify(userRepository).findByEmailVerificationToken(VERIFICATION_TOKEN);
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Email verification failed with invalid token");
    }

    // Password Reset Tests

    /**
     * Tests initiating password reset.
     */
    @Test
    @DisplayName("Should initiate password reset")
    void testInitiatePasswordReset() {
        logger.debug("Test: Initiating password reset for email={}", TEST_EMAIL);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        Optional<String> token = userService.initiatePasswordReset(TEST_EMAIL);

        assertTrue(token.isPresent());
        assertFalse(token.get().isEmpty());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: Password reset initiated with token");
    }

    /**
     * Tests initiating password reset returns empty for non-existent user.
     */
    @Test
    @DisplayName("Should return empty when initiating reset for non-existent user")
    void testInitiatePasswordResetUserNotFound() {
        logger.debug("Test: Initiating reset for non-existent user");

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        Optional<String> token = userService.initiatePasswordReset(TEST_EMAIL);

        assertFalse(token.isPresent());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Empty result for non-existent user");
    }

    /**
     * Tests successful password reset.
     */
    @Test
    @DisplayName("Should reset password with valid token")
    void testResetPasswordSuccess() {
        logger.debug("Test: Resetting password with valid token");

        testUserEntity.setPasswordResetToken(RESET_TOKEN);
        when(userRepository.findByPasswordResetToken(RESET_TOKEN))
                .thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.encode(PLAIN_PASSWORD)).thenReturn(OTHER_PASSWORD_HASH);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        boolean reset = userService.resetPassword(RESET_TOKEN, PLAIN_PASSWORD);

        assertTrue(reset);
        verify(userRepository).findByPasswordResetToken(RESET_TOKEN);
        verify(passwordEncoder).encode(PLAIN_PASSWORD);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: Password reset successfully");
    }

    /**
     * Tests password reset fails with invalid token.
     */
    @Test
    @DisplayName("Should fail password reset with invalid token")
    void testResetPasswordInvalidToken() {
        logger.debug("Test: Resetting password with invalid token");

        when(userRepository.findByPasswordResetToken(RESET_TOKEN))
                .thenReturn(Optional.empty());

        boolean reset = userService.resetPassword(RESET_TOKEN, PLAIN_PASSWORD);

        assertFalse(reset);
        verify(userRepository).findByPasswordResetToken(RESET_TOKEN);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Password reset failed with invalid token");
    }

    // Password Update Tests

    /**
     * Tests successful password update.
     */
    @Test
    @DisplayName("Should update password with correct current password")
    void testUpdatePasswordSuccess() {
        logger.debug("Test: Updating password for user id={}", USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches(PLAIN_PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn(OTHER_PASSWORD_HASH);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        boolean updated = userService.updatePassword(USER_ID, PLAIN_PASSWORD, "NewPassword123!");

        assertTrue(updated);
        verify(userRepository).findById(USER_ID);
        verify(passwordEncoder).matches(PLAIN_PASSWORD, PASSWORD_HASH);
        verify(passwordEncoder).encode("NewPassword123!");
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: Password updated successfully");
    }

    /**
     * Tests password update fails with incorrect current password.
     */
    @Test
    @DisplayName("Should fail password update with incorrect current password")
    void testUpdatePasswordWrongCurrent() {
        logger.debug("Test: Updating password with incorrect current password");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches(PLAIN_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        boolean updated = userService.updatePassword(USER_ID, PLAIN_PASSWORD, "NewPassword123!");

        assertFalse(updated);
        verify(userRepository).findById(USER_ID);
        verify(passwordEncoder).matches(PLAIN_PASSWORD, PASSWORD_HASH);
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Password update failed with incorrect current password");
    }

    /**
     * Tests password update throws exception for non-existent user.
     */
    @Test
    @DisplayName("Should throw exception when updating password for non-existent user")
    void testUpdatePasswordUserNotFound() {
        logger.debug("Test: Updating password for non-existent user");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePassword(USER_ID, PLAIN_PASSWORD, "NewPassword123!");
        });

        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Exception thrown for non-existent user");
    }

    // Account Management Tests

    /**
     * Tests soft deleting a user.
     */
    @Test
    @DisplayName("Should soft delete user")
    void testDeleteUser() {
        logger.debug("Test: Soft deleting user id={}", USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        userService.deleteUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: User soft deleted");
    }

    /**
     * Tests restoring a user.
     */
    @Test
    @DisplayName("Should restore soft-deleted user")
    void testRestoreUser() {
        logger.debug("Test: Restoring user id={}", USER_ID);

        testUserEntity.softDelete();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        userService.restoreUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: User restored");
    }

    /**
     * Tests suspending a user.
     */
    @Test
    @DisplayName("Should suspend user")
    void testSuspendUser() {
        logger.debug("Test: Suspending user id={}", USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        userService.suspendUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: User suspended");
    }

    /**
     * Tests activating a user.
     */
    @Test
    @DisplayName("Should activate user")
    void testActivateUser() {
        logger.debug("Test: Activating user id={}", USER_ID);

        testUserEntity.setStatus(UserEntity.UserStatus.SUSPENDED);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        userService.activateUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: User activated");
    }

    /**
     * Tests unlocking a user.
     */
    @Test
    @DisplayName("Should unlock user account")
    void testUnlockUser() {
        logger.debug("Test: Unlocking user id={}", USER_ID);

        testUserEntity.setLockedUntil(LocalDateTime.now().plusHours(1));
        testUserEntity.setFailedLoginAttempts(5);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        userService.unlockUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(any(UserEntity.class));

        logger.debug("Test passed: User unlocked");
    }

    // Utility Method Tests

    /**
     * Tests creating user with pre-hashed password.
     */
    @Test
    @DisplayName("Should create user with pre-hashed password")
    void testCreateUser() {
        logger.debug("Test: Creating user with pre-hashed password");

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        UserEntity created = userService.createUser(TEST_EMAIL, PASSWORD_HASH);

        assertNotNull(created);
        assertEquals(TEST_EMAIL, created.getEmail());
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(userRepository).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode(anyString());

        logger.debug("Test passed: User created with pre-hashed password");
    }

    /**
     * Tests createUser throws exception for duplicate email.
     */
    @Test
    @DisplayName("Should throw exception when creating user with duplicate email")
    void testCreateUserDuplicateEmail() {
        logger.debug("Test: Creating user with duplicate email");

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(TEST_EMAIL, PASSWORD_HASH);
        });

        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(UserEntity.class));

        logger.debug("Test passed: Exception thrown for duplicate email");
    }
}