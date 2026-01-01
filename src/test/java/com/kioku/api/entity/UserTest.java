package com.kioku.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the User entity.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>User creation and initialization</li>
 *   <li>Email normalization (lowercase, trimming)</li>
 *   <li>Account locking and unlocking behavior</li>
 *   <li>Email verification flow</li>
 *   <li>Password reset token management</li>
 *   <li>Soft delete and restore functionality</li>
 *   <li>Status transitions</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("User Entity Tests")
class UserTest {

    private static final Logger logger = LoggerFactory.getLogger(UserTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_EMAIL_UPPERCASE = "TEST@EXAMPLE.COM";
    private static final String TEST_EMAIL_WITH_SPACES = "  test@example.com  ";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String PASSWORD_HASH = "hashedPassword123";
    private static final String OTHER_PASSWORD_HASH = "otherHashedPassword456";
    private static final String VERIFICATION_TOKEN = "verification-token-uuid-12345";
    private static final String RESET_TOKEN = "reset-token-uuid-67890";

    private User user;

    /**
     * Set up test fixtures before each test.
     * Creates a fresh User instance with test data.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up test: Creating user with email={}", TEST_EMAIL);
        user = new User(TEST_EMAIL, PASSWORD_HASH);
        user.setId(1L); // Simulate persisted entity
    }

    // Constructor Tests

    /**
     * Tests the parameterized constructor creates a user with email and password hash.
     */
    @Test
    @DisplayName("Should create user with email and password hash")
    void testUserCreation() {
        logger.debug("Test: Creating user with email={}, passwordHash={}", TEST_EMAIL, PASSWORD_HASH);

        User newUser = new User(TEST_EMAIL, PASSWORD_HASH);

        assertEquals(TEST_EMAIL, newUser.getEmail());
        assertEquals(PASSWORD_HASH, newUser.getPasswordHash());
        assertNull(newUser.getId());
        assertNull(newUser.getCreatedAt());
        assertEquals(User.UserStatus.ACTIVE, newUser.getStatus());
        assertFalse(newUser.isEmailVerified());
        assertEquals(0, newUser.getFailedLoginAttempts());

        logger.debug("Test passed: User created successfully");
    }

    /**
     * Tests the no-args constructor creates an empty user.
     */
    @Test
    @DisplayName("Should create empty user with no-args constructor")
    void testNoArgsConstructor() {
        logger.debug("Test: Creating user with no-args constructor");

        User emptyUser = new User();

        assertNotNull(emptyUser);
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getPasswordHash());
        assertNull(emptyUser.getId());
        assertEquals(User.UserStatus.ACTIVE, emptyUser.getStatus());

        logger.debug("Test passed: Empty user created successfully");
    }

    // Email Normalization Tests

    /**
     * Tests that emails are normalized to lowercase.
     */
    @Test
    @DisplayName("Should normalize email to lowercase")
    void testEmailNormalizationToLowercase() {
        logger.debug("Test: Normalizing email {} to lowercase", TEST_EMAIL_UPPERCASE);

        User upperCaseUser = new User(TEST_EMAIL_UPPERCASE, PASSWORD_HASH);

        assertEquals(TEST_EMAIL, upperCaseUser.getEmail());

        logger.debug("Test passed: Email normalized to {}", upperCaseUser.getEmail());
    }

    /**
     * Tests that emails are trimmed of whitespace.
     */
    @Test
    @DisplayName("Should trim whitespace from email")
    void testEmailTrimming() {
        logger.debug("Test: Trimming email '{}'", TEST_EMAIL_WITH_SPACES);

        User spacedUser = new User(TEST_EMAIL_WITH_SPACES, PASSWORD_HASH);

        assertEquals(TEST_EMAIL, spacedUser.getEmail());

        logger.debug("Test passed: Email trimmed to {}", spacedUser.getEmail());
    }

    /**
     * Tests that setting email also normalizes it.
     */
    @Test
    @DisplayName("Should normalize email when using setEmail")
    void testSetEmailNormalization() {
        logger.debug("Test: Setting email to {} via setter", TEST_EMAIL_UPPERCASE);

        user.setEmail(TEST_EMAIL_UPPERCASE);

        assertEquals(TEST_EMAIL, user.getEmail());

        logger.debug("Test passed: Email normalized via setter");
    }

    // Account Locking Tests

    /**
     * Tests that a new user is not locked.
     */
    @Test
    @DisplayName("Should not be locked when newly created")
    void testNewUserIsNotLocked() {
        logger.debug("Test: Checking if new user is locked");

        assertFalse(user.isLocked());
        assertNull(user.getLockedUntil());

        logger.debug("Test passed: New user is not locked");
    }

    /**
     * Tests recording multiple failed login attempts.
     */
    @Test
    @DisplayName("Should increment failed login attempts")
    void testRecordFailedLoginAttempt() {
        logger.debug("Test: Recording failed login attempts");

        assertEquals(0, user.getFailedLoginAttempts());

        user.recordFailedLoginAttempt();
        assertEquals(1, user.getFailedLoginAttempts());

        user.recordFailedLoginAttempt();
        assertEquals(2, user.getFailedLoginAttempts());

        logger.debug("Test passed: Failed login attempts incremented to {}",
                user.getFailedLoginAttempts());
    }

    /**
     * Tests that account is locked after 5 failed login attempts.
     */
    @Test
    @DisplayName("Should lock account after 5 failed login attempts")
    void testAccountLockAfterFiveFailedAttempts() {
        logger.debug("Test: Locking account after 5 failed attempts");

        for (int i = 0; i < 5; i++) {
            user.recordFailedLoginAttempt();
        }

        assertTrue(user.isLocked());
        assertNotNull(user.getLockedUntil());
        assertTrue(user.getLockedUntil().isAfter(LocalDateTime.now()));
        assertEquals(5, user.getFailedLoginAttempts());

        logger.debug("Test passed: Account locked until {}", user.getLockedUntil());
    }

    /**
     * Tests that successful login resets failed attempts and clears lock.
     */
    @Test
    @DisplayName("Should reset failed attempts and clear lock on successful login")
    void testSuccessfulLoginResetsFailedAttempts() {
        logger.debug("Test: Successful login after failed attempts");

        // Lock the account
        for (int i = 0; i < 5; i++) {
            user.recordFailedLoginAttempt();
        }
        assertTrue(user.isLocked());

        // Successful login
        user.recordSuccessfulLogin();

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        assertFalse(user.isLocked());
        assertNotNull(user.getLastLoginAt());

        logger.debug("Test passed: Failed attempts reset, lock cleared, lastLoginAt set");
    }

    /**
     * Tests that a locked account becomes unlocked after the lock period expires.
     */
    @Test
    @DisplayName("Should unlock account after lock period expires")
    void testAccountUnlocksAfterLockPeriod() {
        logger.debug("Test: Account unlock after lock period");

        // Set lock time in the past
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));

        assertFalse(user.isLocked());

        logger.debug("Test passed: Account unlocked after lock period");
    }

    // Account Status Tests

    /**
     * Tests that a new user is active.
     */
    @Test
    @DisplayName("Should be active when newly created")
    void testNewUserIsActive() {
        logger.debug("Test: Checking if new user is active");

        assertTrue(user.isActive());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());

        logger.debug("Test passed: New user is active");
    }

    /**
     * Tests that a locked user is not active.
     */
    @Test
    @DisplayName("Should not be active when locked")
    void testLockedUserIsNotActive() {
        logger.debug("Test: Checking if locked user is active");

        for (int i = 0; i < 5; i++) {
            user.recordFailedLoginAttempt();
        }

        assertFalse(user.isActive());

        logger.debug("Test passed: Locked user is not active");
    }

    /**
     * Tests that a suspended user is not active.
     */
    @Test
    @DisplayName("Should not be active when suspended")
    void testSuspendedUserIsNotActive() {
        logger.debug("Test: Checking if suspended user is active");

        user.setStatus(User.UserStatus.SUSPENDED);

        assertFalse(user.isActive());

        logger.debug("Test passed: Suspended user is not active");
    }

    /**
     * Tests that a deleted user is not active.
     */
    @Test
    @DisplayName("Should not be active when deleted")
    void testDeletedUserIsNotActive() {
        logger.debug("Test: Checking if deleted user is active");

        user.softDelete();

        assertFalse(user.isActive());

        logger.debug("Test passed: Deleted user is not active");
    }

    // Soft Delete Tests

    /**
     * Tests that soft delete sets deletedAt and changes status.
     */
    @Test
    @DisplayName("Should soft delete user and set deletedAt timestamp")
    void testSoftDelete() {
        logger.debug("Test: Soft deleting user id={}", user.getId());

        assertFalse(user.isDeleted());
        assertNull(user.getDeletedAt());

        user.softDelete();

        assertTrue(user.isDeleted());
        assertNotNull(user.getDeletedAt());
        assertEquals(User.UserStatus.DELETED, user.getStatus());

        logger.debug("Test passed: User soft deleted at {}", user.getDeletedAt());
    }

    /**
     * Tests that restore clears deletedAt and sets status to ACTIVE.
     */
    @Test
    @DisplayName("Should restore soft-deleted user")
    void testRestore() {
        logger.debug("Test: Restoring soft-deleted user id={}", user.getId());

        user.softDelete();
        assertTrue(user.isDeleted());

        user.restore();

        assertFalse(user.isDeleted());
        assertNull(user.getDeletedAt());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());

        logger.debug("Test passed: User restored successfully");
    }

    // Email Verification Tests

    /**
     * Tests that new users are not email verified.
     */
    @Test
    @DisplayName("Should not be email verified when newly created")
    void testNewUserEmailNotVerified() {
        logger.debug("Test: Checking email verification status for new user");

        assertFalse(user.isEmailVerified());

        logger.debug("Test passed: New user email not verified");
    }

    /**
     * Tests setting email verification token.
     */
    @Test
    @DisplayName("Should set email verification token and timestamp")
    void testSetEmailVerificationToken() {
        logger.debug("Test: Setting email verification token");

        user.setEmailVerificationToken(VERIFICATION_TOKEN);

        assertEquals(VERIFICATION_TOKEN, user.getEmailVerificationToken());
        assertNotNull(user.getEmailVerificationSentAt());

        logger.debug("Test passed: Email verification token set");
    }

    /**
     * Tests verifying email clears token and sets verified flag.
     */
    @Test
    @DisplayName("Should verify email and clear token")
    void testVerifyEmail() {
        logger.debug("Test: Verifying email for user id={}", user.getId());

        user.setEmailVerificationToken(VERIFICATION_TOKEN);

        user.verifyEmail();

        assertTrue(user.isEmailVerified());
        assertNull(user.getEmailVerificationToken());
        assertNull(user.getEmailVerificationSentAt());

        logger.debug("Test passed: Email verified and token cleared");
    }

    /**
     * Tests that email verification token expires after 24 hours.
     */
    @Test
    @DisplayName("Should detect expired email verification token")
    void testEmailVerificationTokenExpiration() {
        logger.debug("Test: Checking email verification token expiration");

        // Token is expired when not set
        assertTrue(user.isEmailVerificationTokenExpired());

        // Set fresh token
        user.setEmailVerificationToken(VERIFICATION_TOKEN);
        assertFalse(user.isEmailVerificationTokenExpired());

        logger.debug("Test passed: Token expiration detected correctly");
    }

    // Password Reset Tests

    /**
     * Tests setting password reset token.
     */
    @Test
    @DisplayName("Should set password reset token and timestamp")
    void testSetPasswordResetToken() {
        logger.debug("Test: Setting password reset token");

        user.setPasswordResetToken(RESET_TOKEN);

        assertEquals(RESET_TOKEN, user.getPasswordResetToken());
        assertNotNull(user.getPasswordResetSentAt());

        logger.debug("Test passed: Password reset token set");
    }

    /**
     * Tests clearing password reset token.
     */
    @Test
    @DisplayName("Should clear password reset token")
    void testClearPasswordResetToken() {
        logger.debug("Test: Clearing password reset token");

        user.setPasswordResetToken(RESET_TOKEN);
        assertNotNull(user.getPasswordResetToken());

        user.clearPasswordResetToken();

        assertNull(user.getPasswordResetToken());
        assertNull(user.getPasswordResetSentAt());

        logger.debug("Test passed: Password reset token cleared");
    }

    /**
     * Tests that password reset token expires after 24 hours.
     */
    @Test
    @DisplayName("Should detect expired password reset token")
    void testPasswordResetTokenExpiration() {
        logger.debug("Test: Checking password reset token expiration");

        // Token is expired when not set
        assertTrue(user.isPasswordResetTokenExpired());

        // Set fresh token
        user.setPasswordResetToken(RESET_TOKEN);
        assertFalse(user.isPasswordResetTokenExpired());

        logger.debug("Test passed: Token expiration detected correctly");
    }

    // Equality and HashCode Tests

    /**
     * Tests that users with same ID are equal.
     */
    @Test
    @DisplayName("Should be equal when IDs match")
    void testUserEquality() {
        logger.debug("Test: Checking user equality with matching IDs");

        User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
        user1.setId(1L);

        User user2 = new User(OTHER_EMAIL, OTHER_PASSWORD_HASH);
        user2.setId(1L);

        assertEquals(user1, user2);

        logger.debug("Test passed: Users with same ID are equal");
    }

    /**
     * Tests that users with different IDs are not equal.
     */
    @Test
    @DisplayName("Should not be equal when IDs differ")
    void testUserInequality() {
        logger.debug("Test: Checking user inequality with different IDs");

        User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
        user1.setId(1L);

        User user2 = new User(TEST_EMAIL, PASSWORD_HASH);
        user2.setId(2L);

        assertNotEquals(user1, user2);

        logger.debug("Test passed: Users with different IDs are not equal");
    }

    /**
     * Tests that users with same ID have same hash code.
     */
    @Test
    @DisplayName("Should have same hash code when IDs match")
    void testUserHashCode() {
        logger.debug("Test: Checking hash code for users with matching IDs");

        User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
        user1.setId(1L);

        User user2 = new User(OTHER_EMAIL, OTHER_PASSWORD_HASH);
        user2.setId(1L);

        assertEquals(user1.hashCode(), user2.hashCode());

        logger.debug("Test passed: Users with same ID have same hash code");
    }

    /**
     * Tests reflexive property of equals (x.equals(x) is true).
     */
    @Test
    @DisplayName("Should be equal to itself (reflexive)")
    void testEqualityReflexive() {
        logger.debug("Test: Checking reflexive property of equals");

        assertEquals(user, user);

        logger.debug("Test passed: User equals itself");
    }

    /**
     * Tests that user is not equal to null.
     */
    @Test
    @DisplayName("Should not be equal to null")
    void testEqualityWithNull() {
        logger.debug("Test: Checking equality with null");

        assertNotEquals(user, null);

        logger.debug("Test passed: User not equal to null");
    }

    /**
     * Tests that user is not equal to different class.
     */
    @Test
    @DisplayName("Should not be equal to different class")
    void testEqualityWithDifferentClass() {
        logger.debug("Test: Checking equality with different class");

        assertNotEquals(user, "not a user");

        logger.debug("Test passed: User not equal to different class");
    }

    // Security Tests

    /**
     * Tests that toString does not expose password hash.
     */
    @Test
    @DisplayName("Should not expose password hash in toString")
    void testToStringDoesNotExposePassword() {
        logger.debug("Test: Checking toString does not expose password");

        String userString = user.toString();

        assertFalse(userString.contains(PASSWORD_HASH));

        logger.debug("Test passed: Password not in toString");
    }

    /**
     * Tests that toString does not expose email (PII).
     */
    @Test
    @DisplayName("Should not expose email in toString")
    void testToStringDoesNotExposeEmail() {
        logger.debug("Test: Checking toString does not expose email");

        String userString = user.toString();

        assertFalse(userString.contains(TEST_EMAIL));

        logger.debug("Test passed: Email not in toString");
    }

    /**
     * Tests that toString includes non-sensitive information.
     */
    @Test
    @DisplayName("Should include non-sensitive info in toString")
    void testToStringIncludesBasicInfo() {
        logger.debug("Test: Checking toString includes basic info");

        String userString = user.toString();

        assertTrue(userString.contains("User{"));
        assertTrue(userString.contains("id=" + user.getId()));
        assertTrue(userString.contains("status=" + user.getStatus()));

        logger.debug("Test passed: toString includes basic info");
    }

    /**
     * Tests that password hash getter has package-private visibility.
     */
    @Test
    @DisplayName("Should allow package-private access to password hash")
    void testPasswordHashAccess() {
        logger.debug("Test: Accessing password hash (package-private)");

        // This works because test is in same package
        String hash = user.getPasswordHash();

        assertEquals(PASSWORD_HASH, hash);

        logger.debug("Test passed: Password hash accessible within package");
    }

    /**
     * Tests that password hash setter has package-private visibility.
     */
    @Test
    @DisplayName("Should allow package-private setting of password hash")
    void testPasswordHashSetter() {
        logger.debug("Test: Setting password hash (package-private)");

        String newHash = "newHashedPassword789";
        user.setPasswordHash(newHash);

        assertEquals(newHash, user.getPasswordHash());

        logger.debug("Test passed: Password hash set successfully");
    }

    // Edge Case Tests

    /**
     * Tests handling of null email in constructor.
     */
    @Test
    @DisplayName("Should handle null email in constructor")
    void testNullEmailInConstructor() {
        logger.debug("Test: Creating user with null email");

        User nullEmailUser = new User(null, PASSWORD_HASH);

        assertNull(nullEmailUser.getEmail());

        logger.debug("Test passed: Null email handled");
    }

    /**
     * Tests handling of null email in setter.
     */
    @Test
    @DisplayName("Should handle null email in setter")
    void testNullEmailInSetter() {
        logger.debug("Test: Setting email to null");

        user.setEmail(null);

        assertNull(user.getEmail());

        logger.debug("Test passed: Null email handled in setter");
    }

    /**
     * Tests that setting email to empty string normalizes to empty.
     */
    @Test
    @DisplayName("Should normalize empty string email")
    void testEmptyStringEmail() {
        logger.debug("Test: Setting empty string email");

        user.setEmail("");

        assertEquals("", user.getEmail());

        logger.debug("Test passed: Empty string normalized");
    }

    /**
     * Tests all status transitions.
     */
    @Test
    @DisplayName("Should allow all status transitions")
    void testAllStatusTransitions() {
        logger.debug("Test: Testing all status transitions");

        user.setStatus(User.UserStatus.ACTIVE);
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());

        user.setStatus(User.UserStatus.PENDING_VERIFICATION);
        assertEquals(User.UserStatus.PENDING_VERIFICATION, user.getStatus());

        user.setStatus(User.UserStatus.SUSPENDED);
        assertEquals(User.UserStatus.SUSPENDED, user.getStatus());

        user.setStatus(User.UserStatus.DELETED);
        assertEquals(User.UserStatus.DELETED, user.getStatus());

        logger.debug("Test passed: All status transitions work");
    }
}