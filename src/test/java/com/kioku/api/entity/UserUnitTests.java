package com.kioku.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the User entity.
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
 *   <li>Deck management (add/remove)</li>
 *   <li>Bidirectional relationship consistency</li>
 *   <li>Cascade operations (deck deletion cascades to cards and tags)</li>
 *   <li>OrphanRemoval behavior</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 2.0
 * @since 1.0
 */
@DisplayName("User Entity Comprehensive Tests")
class UserUnitTests {

    private static final Logger logger = LoggerFactory.getLogger(UserUnitTests.class);

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


    /**
     * Tests for the User constructor and initialization.
     */
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        /**
         * Tests the User constructor with valid email and password hash.
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
            assertNotNull(newUser.getDecks(), "Decks collection should be initialized");
            assertTrue(newUser.getDecks().isEmpty(), "Decks collection should be empty");

            logger.debug("Test passed: User created successfully");
        }

        /**
         * Tests the no-args constructor for User.
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
            assertNotNull(emptyUser.getDecks(), "Decks collection should be initialized");
            assertTrue(emptyUser.getDecks().isEmpty(), "Decks collection should be empty");

            logger.debug("Test passed: Empty user created successfully");
        }

        /**
         * Tests the User constructor with null email.
         */
        @Test
        @DisplayName("Should handle null email in constructor")
        void testNullEmailInConstructor() {
            logger.debug("Test: Creating user with null email");

            User nullEmailUser = new User(null, PASSWORD_HASH);

            assertNull(nullEmailUser.getEmail());
            assertEquals(PASSWORD_HASH, nullEmailUser.getPasswordHash());

            logger.debug("Test passed: Null email handled");
        }
    }

    /**
     * Tests for email normalization behavior.
     */
    @Nested
    @DisplayName("Email Normalization Tests")
    class EmailNormalizationTests {

        /**
         * Tests email normalization to lowercase.
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
         * Tests email trimming.
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
         * Tests email normalization when using setEmail.
         */
        @Test
        @DisplayName("Should normalize email when using setEmail")
        void testSetEmailNormalization() {
            logger.debug("Test: Setting email to {} via setter", TEST_EMAIL_UPPERCASE);

            user.setEmail(TEST_EMAIL_UPPERCASE);

            assertEquals(TEST_EMAIL, user.getEmail());

            logger.debug("Test passed: Email normalized via setter");
        }

        /**
         * Tests handling null email in setEmail.
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
         * Tests handling empty string email in setEmail.
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
         * Tests complex email normalization.
         */
        @Test
        @DisplayName("Should trim and normalize mixed case with spaces")
        void testComplexEmailNormalization() {
            logger.debug("Test: Complex email normalization");

            user.setEmail("  TeSt@ExAmPlE.CoM  ");

            assertEquals("test@example.com", user.getEmail());

            logger.debug("Test passed: Complex normalization successful");
        }
    }

    /**
     * Tests account locking behavior.
     */
    @Nested
    @DisplayName("Account Locking Tests")
    class AccountLockingTests {

        /**
         * Tests that a newly created user is not locked.
         */
        @Test
        @DisplayName("Should not be locked when newly created")
        void testNewUserIsNotLocked() {
            logger.debug("Test: Checking if new user is locked");

            assertFalse(user.isLocked());
            assertNull(user.getLockedUntil());

            logger.debug("Test passed: New user is not locked");
        }

        @Test
        @DisplayName("Should increment failed login attempts")
        void testRecordFailedLoginAttempt() {
            logger.debug("Test: Recording failed login attempts");

            assertEquals(0, user.getFailedLoginAttempts());

            user.recordFailedLoginAttempt();
            assertEquals(1, user.getFailedLoginAttempts());

            user.recordFailedLoginAttempt();
            assertEquals(2, user.getFailedLoginAttempts());

            user.recordFailedLoginAttempt();
            assertEquals(3, user.getFailedLoginAttempts());

            user.recordFailedLoginAttempt();
            assertEquals(4, user.getFailedLoginAttempts());

            assertNull(user.getLockedUntil(), "Account should not be locked yet");

            logger.debug("Test passed: Failed login attempts incremented to {}",
                    user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should lock account after 5 failed login attempts")
        void testAccountLockAfterFiveFailedAttempts() {
            logger.debug("Test: Locking account after 5 failed attempts");

            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }

            assertTrue(user.isLocked(), "Account should be locked after 5 attempts");
            assertNotNull(user.getLockedUntil(), "LockedUntil should be set");
            assertTrue(user.getLockedUntil().isAfter(LocalDateTime.now()),
                    "Lock time should be in the future");
            assertEquals(5, user.getFailedLoginAttempts());

            // Verify lock duration is approximately 15 minutes
            LocalDateTime expectedUnlock = LocalDateTime.now().plusMinutes(15);
            assertTrue(user.getLockedUntil().isBefore(expectedUnlock.plusSeconds(5)));
            assertTrue(user.getLockedUntil().isAfter(expectedUnlock.minusSeconds(5)));

            logger.debug("Test passed: Account locked until {}", user.getLockedUntil());
        }

        @Test
        @DisplayName("Should remain locked with continued failed attempts")
        void testContinuedFailedAttemptsAfterLock() {
            logger.debug("Test: Continued failed attempts after lock");

            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }
            assertTrue(user.isLocked());
            LocalDateTime firstLockTime = user.getLockedUntil();

            // Additional failed attempts should update the lock time
            user.recordFailedLoginAttempt();
            assertEquals(6, user.getFailedLoginAttempts());
            assertTrue(user.isLocked());
            assertNotNull(user.getLockedUntil());
            assertTrue(user.getLockedUntil().isAfter(firstLockTime) ||
                    user.getLockedUntil().equals(firstLockTime));

            logger.debug("Test passed: Lock maintained with continued attempts");
        }

        @Test
        @DisplayName("Should reset failed attempts and clear lock on successful login")
        void testSuccessfulLoginResetsFailedAttempts() {
            logger.debug("Test: Successful login after failed attempts");

            // Lock the account
            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }
            assertTrue(user.isLocked());
            assertNull(user.getLastLoginAt());

            // Successful login
            user.recordSuccessfulLogin();

            assertEquals(0, user.getFailedLoginAttempts());
            assertNull(user.getLockedUntil());
            assertFalse(user.isLocked());
            assertNotNull(user.getLastLoginAt());
            assertTrue(user.getLastLoginAt().isBefore(LocalDateTime.now().plusSeconds(1)));

            logger.debug("Test passed: Failed attempts reset, lock cleared, lastLoginAt set");
        }

        @Test
        @DisplayName("Should unlock account after lock period expires")
        void testAccountUnlocksAfterLockPeriod() {
            logger.debug("Test: Account unlock after lock period");

            // Set lock time in the past
            user.setLockedUntil(LocalDateTime.now().minusMinutes(1));

            assertFalse(user.isLocked(), "Account should be unlocked");

            logger.debug("Test passed: Account unlocked after lock period");
        }

        @Test
        @DisplayName("Should remain locked if lock time is in future")
        void testAccountRemainsLockedIfTimeInFuture() {
            logger.debug("Test: Account remains locked if time in future");

            user.setLockedUntil(LocalDateTime.now().plusMinutes(10));

            assertTrue(user.isLocked(), "Account should still be locked");

            logger.debug("Test passed: Account remains locked");
        }

        @Test
        @DisplayName("Should handle manual lock setting")
        void testManualLockSetting() {
            logger.debug("Test: Manual lock setting");

            LocalDateTime lockTime = LocalDateTime.now().plusHours(1);
            user.setLockedUntil(lockTime);

            assertTrue(user.isLocked());
            assertEquals(lockTime, user.getLockedUntil());

            logger.debug("Test passed: Manual lock set successfully");
        }

        @Test
        @DisplayName("Should handle manual unlock")
        void testManualUnlock() {
            logger.debug("Test: Manual unlock");

            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            user.setFailedLoginAttempts(5);
            assertTrue(user.isLocked());

            // Manual unlock
            user.setLockedUntil(null);

            assertFalse(user.isLocked());
            assertEquals(5, user.getFailedLoginAttempts(),
                    "Failed attempts should remain unless reset");

            logger.debug("Test passed: Manual unlock successful");
        }
    }

    // ========================================
    // Account Status Tests
    // ========================================

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Should be active when newly created")
        void testNewUserIsActive() {
            logger.debug("Test: Checking if new user is active");

            assertTrue(user.isActive());
            assertEquals(User.UserStatus.ACTIVE, user.getStatus());

            logger.debug("Test passed: New user is active");
        }

        @Test
        @DisplayName("Should not be active when locked")
        void testLockedUserIsNotActive() {
            logger.debug("Test: Checking if locked user is active");

            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }

            assertFalse(user.isActive(), "Locked user should not be active");
            assertTrue(user.isLocked());
            assertEquals(User.UserStatus.ACTIVE, user.getStatus(),
                    "Status remains ACTIVE even when locked");

            logger.debug("Test passed: Locked user is not active");
        }

        @Test
        @DisplayName("Should not be active when suspended")
        void testSuspendedUserIsNotActive() {
            logger.debug("Test: Checking if suspended user is active");

            user.setStatus(User.UserStatus.SUSPENDED);

            assertFalse(user.isActive());
            assertFalse(user.isLocked());
            assertFalse(user.isDeleted());

            logger.debug("Test passed: Suspended user is not active");
        }

        @Test
        @DisplayName("Should not be active when deleted")
        void testDeletedUserIsNotActive() {
            logger.debug("Test: Checking if deleted user is active");

            user.softDelete();

            assertFalse(user.isActive());
            assertTrue(user.isDeleted());
            assertEquals(User.UserStatus.DELETED, user.getStatus());

            logger.debug("Test passed: Deleted user is not active");
        }

        @Test
        @DisplayName("Should not be active when pending verification")
        void testPendingVerificationUserIsNotActive() {
            logger.debug("Test: Checking if pending verification user is active");

            user.setStatus(User.UserStatus.PENDING_VERIFICATION);

            assertFalse(user.isActive());

            logger.debug("Test passed: Pending verification user is not active");
        }

        @Test
        @DisplayName("Should support all status transitions")
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

            user.setStatus(User.UserStatus.ACTIVE);
            assertEquals(User.UserStatus.ACTIVE, user.getStatus());

            logger.debug("Test passed: All status transitions work");
        }

        @Test
        @DisplayName("Should handle multiple status flags simultaneously")
        void testMultipleStatusFlags() {
            logger.debug("Test: Multiple status flags");

            // User can be suspended AND locked
            user.setStatus(User.UserStatus.SUSPENDED);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));

            assertFalse(user.isActive());
            assertTrue(user.isLocked());
            assertEquals(User.UserStatus.SUSPENDED, user.getStatus());

            logger.debug("Test passed: Multiple status flags handled");
        }
    }

    // ========================================
    // Soft Delete and Restore Tests
    // ========================================

    @Nested
    @DisplayName("Soft Delete and Restore Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should not be deleted when newly created")
        void testNewUserIsNotDeleted() {
            logger.debug("Test: Checking if new user is deleted");

            assertFalse(user.isDeleted());
            assertNull(user.getDeletedAt());

            logger.debug("Test passed: New user is not deleted");
        }

        @Test
        @DisplayName("Should soft delete user and set deletedAt timestamp")
        void testSoftDelete() {
            logger.debug("Test: Soft deleting user id={}", user.getId());

            assertFalse(user.isDeleted());
            assertNull(user.getDeletedAt());
            assertEquals(User.UserStatus.ACTIVE, user.getStatus());

            LocalDateTime beforeDelete = LocalDateTime.now();
            user.softDelete();
            LocalDateTime afterDelete = LocalDateTime.now();

            assertTrue(user.isDeleted());
            assertNotNull(user.getDeletedAt());
            assertTrue(user.getDeletedAt().isAfter(beforeDelete.minusSeconds(1)));
            assertTrue(user.getDeletedAt().isBefore(afterDelete.plusSeconds(1)));
            assertEquals(User.UserStatus.DELETED, user.getStatus());

            logger.debug("Test passed: User soft deleted at {}", user.getDeletedAt());
        }

        @Test
        @DisplayName("Should restore soft-deleted user")
        void testRestore() {
            logger.debug("Test: Restoring soft-deleted user id={}", user.getId());

            user.softDelete();
            assertTrue(user.isDeleted());
            assertEquals(User.UserStatus.DELETED, user.getStatus());

            user.restore();

            assertFalse(user.isDeleted());
            assertNull(user.getDeletedAt());
            assertEquals(User.UserStatus.ACTIVE, user.getStatus());

            logger.debug("Test passed: User restored successfully");
        }

        @Test
        @DisplayName("Should allow multiple delete/restore cycles")
        void testMultipleDeleteRestoreCycles() {
            logger.debug("Test: Multiple delete/restore cycles");

            // First cycle
            user.softDelete();
            assertTrue(user.isDeleted());
            user.restore();
            assertFalse(user.isDeleted());

            // Second cycle
            user.softDelete();
            assertTrue(user.isDeleted());
            user.restore();
            assertFalse(user.isDeleted());

            // Third cycle
            user.softDelete();
            assertTrue(user.isDeleted());

            logger.debug("Test passed: Multiple cycles handled correctly");
        }

        @Test
        @DisplayName("Should restore without affecting other user state")
        void testRestorePreservesOtherState() {
            logger.debug("Test: Restore preserves other state");

            user.setEmail("specific@example.com");
            user.recordSuccessfulLogin();
            LocalDateTime loginTime = user.getLastLoginAt();

            user.softDelete();
            user.restore();

            assertEquals("specific@example.com", user.getEmail());
            assertEquals(loginTime, user.getLastLoginAt());
            assertFalse(user.isEmailVerified());

            logger.debug("Test passed: Restore preserves other state");
        }

        @Test
        @DisplayName("Should not restore email verification or locks")
        void testRestoreDoesNotRestoreVerificationOrLocks() {
            logger.debug("Test: Restore doesn't restore verification or locks");

            user.verifyEmail();
            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));

            user.softDelete();
            user.restore();

            assertTrue(user.isEmailVerified(), "Email verification should remain");
            assertNotNull(user.getLockedUntil(), "Lock should remain");
            assertTrue(user.isLocked(), "Should still be locked");

            logger.debug("Test passed: Verification and lock state preserved");
        }
    }

    // ========================================
    // Email Verification Tests
    // ========================================

    @Nested
    @DisplayName("Email Verification Tests")
    class EmailVerificationTests {

        @Test
        @DisplayName("Should not be email verified when newly created")
        void testNewUserEmailNotVerified() {
            logger.debug("Test: Checking email verification status for new user");

            assertFalse(user.isEmailVerified());
            assertNull(user.getEmailVerificationToken());
            assertNull(user.getEmailVerificationSentAt());

            logger.debug("Test passed: New user email not verified");
        }

        @Test
        @DisplayName("Should set email verification token and timestamp")
        void testSetEmailVerificationToken() {
            logger.debug("Test: Setting email verification token");

            LocalDateTime beforeSet = LocalDateTime.now();
            user.setEmailVerificationToken(VERIFICATION_TOKEN);
            LocalDateTime afterSet = LocalDateTime.now();

            assertEquals(VERIFICATION_TOKEN, user.getEmailVerificationToken());
            assertNotNull(user.getEmailVerificationSentAt());
            assertTrue(user.getEmailVerificationSentAt().isAfter(beforeSet.minusSeconds(1)));
            assertTrue(user.getEmailVerificationSentAt().isBefore(afterSet.plusSeconds(1)));
            assertFalse(user.isEmailVerified());

            logger.debug("Test passed: Email verification token set at {}",
                    user.getEmailVerificationSentAt());
        }

        @Test
        @DisplayName("Should verify email and clear token")
        void testVerifyEmail() {
            logger.debug("Test: Verifying email for user id={}", user.getId());

            user.setEmailVerificationToken(VERIFICATION_TOKEN);
            assertFalse(user.isEmailVerified());
            assertNotNull(user.getEmailVerificationToken());

            user.verifyEmail();

            assertTrue(user.isEmailVerified());
            assertNull(user.getEmailVerificationToken());
            assertNull(user.getEmailVerificationSentAt());

            logger.debug("Test passed: Email verified and token cleared");
        }

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

        @Test
        @DisplayName("Should allow re-sending verification token")
        void testResendVerificationToken() {
            logger.debug("Test: Re-sending verification token");

            user.setEmailVerificationToken("first-token");
            LocalDateTime firstSentAt = user.getEmailVerificationSentAt();

            // Wait a moment
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            user.setEmailVerificationToken("second-token");
            LocalDateTime secondSentAt = user.getEmailVerificationSentAt();

            assertEquals("second-token", user.getEmailVerificationToken());
            assertTrue(secondSentAt.isAfter(firstSentAt));

            logger.debug("Test passed: Token resent successfully");
        }

        @Test
        @DisplayName("Should handle verification of already verified email")
        void testVerifyAlreadyVerifiedEmail() {
            logger.debug("Test: Verifying already verified email");

            user.verifyEmail();
            assertTrue(user.isEmailVerified());

            user.verifyEmail(); // Verify again

            assertTrue(user.isEmailVerified());
            assertNull(user.getEmailVerificationToken());

            logger.debug("Test passed: Double verification handled");
        }

        @Test
        @DisplayName("Should allow setting verification token after verification")
        void testSetTokenAfterVerification() {
            logger.debug("Test: Setting token after verification");

            user.verifyEmail();
            assertTrue(user.isEmailVerified());

            user.setEmailVerificationToken(VERIFICATION_TOKEN);

            assertTrue(user.isEmailVerified(),
                    "Should remain verified even with new token");
            assertEquals(VERIFICATION_TOKEN, user.getEmailVerificationToken());

            logger.debug("Test passed: Token set after verification");
        }
    }

    // ========================================
    // Password Reset Tests
    // ========================================

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("Should set password reset token and timestamp")
        void testSetPasswordResetToken() {
            logger.debug("Test: Setting password reset token");

            LocalDateTime beforeSet = LocalDateTime.now();
            user.setPasswordResetToken(RESET_TOKEN);
            LocalDateTime afterSet = LocalDateTime.now();

            assertEquals(RESET_TOKEN, user.getPasswordResetToken());
            assertNotNull(user.getPasswordResetSentAt());
            assertTrue(user.getPasswordResetSentAt().isAfter(beforeSet.minusSeconds(1)));
            assertTrue(user.getPasswordResetSentAt().isBefore(afterSet.plusSeconds(1)));

            logger.debug("Test passed: Password reset token set at {}",
                    user.getPasswordResetSentAt());
        }

        @Test
        @DisplayName("Should clear password reset token")
        void testClearPasswordResetToken() {
            logger.debug("Test: Clearing password reset token");

            user.setPasswordResetToken(RESET_TOKEN);
            assertNotNull(user.getPasswordResetToken());
            assertNotNull(user.getPasswordResetSentAt());

            user.clearPasswordResetToken();

            assertNull(user.getPasswordResetToken());
            assertNull(user.getPasswordResetSentAt());

            logger.debug("Test passed: Password reset token cleared");
        }

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

        @Test
        @DisplayName("Should allow replacing reset token")
        void testReplaceResetToken() {
            logger.debug("Test: Replacing reset token");

            user.setPasswordResetToken("first-token");
            LocalDateTime firstSentAt = user.getPasswordResetSentAt();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            user.setPasswordResetToken("second-token");
            LocalDateTime secondSentAt = user.getPasswordResetSentAt();

            assertEquals("second-token", user.getPasswordResetToken());
            assertTrue(secondSentAt.isAfter(firstSentAt));

            logger.debug("Test passed: Token replaced successfully");
        }

        @Test
        @DisplayName("Should handle clearing non-existent token")
        void testClearNonExistentToken() {
            logger.debug("Test: Clearing non-existent token");

            assertNull(user.getPasswordResetToken());

            user.clearPasswordResetToken();

            assertNull(user.getPasswordResetToken());
            assertNull(user.getPasswordResetSentAt());

            logger.debug("Test passed: Clearing non-existent token handled");
        }

        @Test
        @DisplayName("Should handle setting null token")
        void testSetNullResetToken() {
            logger.debug("Test: Setting null reset token");

            user.setPasswordResetToken(RESET_TOKEN);
            assertNotNull(user.getPasswordResetToken());

            user.setPasswordResetToken(null);

            assertNull(user.getPasswordResetToken());
            assertNotNull(user.getPasswordResetSentAt(),
                    "Timestamp should still be set even with null token");

            logger.debug("Test passed: Null token handled");
        }
    }

    // ========================================
    // Deck Management Tests (NEW)
    // ========================================

    @Nested
    @DisplayName("Deck Management Tests")
    class DeckManagementTests {

        @Test
        @DisplayName("Should have empty deck collection when created")
        void testNewUserHasEmptyDeckCollection() {
            logger.debug("Test: New user has empty deck collection");

            assertNotNull(user.getDecks());
            assertTrue(user.getDecks().isEmpty());
            assertEquals(0, user.getDecks().size());

            logger.debug("Test passed: Empty deck collection initialized");
        }

        @Test
        @DisplayName("Should add deck to user")
        void testAddDeck() {
            logger.debug("Test: Adding deck to user");

            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);

            user.addDeck(deck);

            assertTrue(user.getDecks().contains(deck));
            assertEquals(1, user.getDecks().size());
            assertEquals(user, deck.getUser(), "Bidirectional relationship should be maintained");

            logger.debug("Test passed: Deck added successfully");
        }

        @Test
        @DisplayName("Should throw exception when adding null deck")
        void testAddNullDeck() {
            logger.debug("Test: Adding null deck");

            assertThrows(IllegalArgumentException.class, () -> {
                user.addDeck(null);
            });

            logger.debug("Test passed: Exception thrown for null deck");
        }

        @Test
        @DisplayName("Should add multiple decks to user")
        void testAddMultipleDecks() {
            logger.debug("Test: Adding multiple decks");

            Deck deck1 = new Deck(user, "Japanese", "Japanese vocabulary");
            deck1.setId(1L);
            Deck deck2 = new Deck(user, "Spanish", "Spanish vocabulary");
            deck2.setId(2L);
            Deck deck3 = new Deck(user, "French", "French vocabulary");
            deck3.setId(3L);

            user.addDeck(deck1);
            user.addDeck(deck2);
            user.addDeck(deck3);

            assertEquals(3, user.getDecks().size());
            assertTrue(user.getDecks().contains(deck1));
            assertTrue(user.getDecks().contains(deck2));
            assertTrue(user.getDecks().contains(deck3));

            logger.debug("Test passed: Multiple decks added");
        }

        @Test
        @DisplayName("Should maintain bidirectional relationship when adding deck")
        void testAddDeckMaintainsBidirectionalRelationship() {
            logger.debug("Test: Bidirectional relationship when adding deck");

            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);

            // Initially deck has the user set from constructor
            assertEquals(user, deck.getUser());

            user.addDeck(deck);

            // After adding, relationship should still be maintained
            assertTrue(user.getDecks().contains(deck));
            assertEquals(user, deck.getUser());

            logger.debug("Test passed: Bidirectional relationship maintained");
        }

        @Test
        @DisplayName("Should remove deck from user")
        void testRemoveDeck() {
            logger.debug("Test: Removing deck from user");

            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);

            user.addDeck(deck);
            assertTrue(user.getDecks().contains(deck));

            user.removeDeck(deck);

            assertFalse(user.getDecks().contains(deck));
            assertEquals(0, user.getDecks().size());

            logger.debug("Test passed: Deck removed successfully");
        }

        @Test
        @DisplayName("Should throw exception when removing null deck")
        void testRemoveNullDeck() {
            logger.debug("Test: Removing null deck");

            assertThrows(IllegalArgumentException.class, () -> {
                user.removeDeck(null);
            });

            logger.debug("Test passed: Exception thrown for null deck");
        }

        @Test
        @DisplayName("Should handle removing non-existent deck gracefully")
        void testRemoveNonExistentDeck() {
            logger.debug("Test: Removing non-existent deck");

            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);

            // Deck was never added
            user.removeDeck(deck);

            assertTrue(user.getDecks().isEmpty());

            logger.debug("Test passed: Removing non-existent deck handled");
        }

        @Test
        @DisplayName("Should remove specific deck from multiple decks")
        void testRemoveSpecificDeck() {
            logger.debug("Test: Removing specific deck from multiple");

            Deck deck1 = new Deck(user, "Japanese", "Japanese vocabulary");
            deck1.setId(1L);
            Deck deck2 = new Deck(user, "Spanish", "Spanish vocabulary");
            deck2.setId(2L);
            Deck deck3 = new Deck(user, "French", "French vocabulary");
            deck3.setId(3L);

            user.addDeck(deck1);
            user.addDeck(deck2);
            user.addDeck(deck3);

            user.removeDeck(deck2);

            assertEquals(2, user.getDecks().size());
            assertTrue(user.getDecks().contains(deck1));
            assertFalse(user.getDecks().contains(deck2));
            assertTrue(user.getDecks().contains(deck3));

            logger.debug("Test passed: Specific deck removed");
        }

        @Test
        @DisplayName("Should handle adding same deck twice")
        void testAddSameDeckTwice() {
            logger.debug("Test: Adding same deck twice");

            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);

            user.addDeck(deck);
            user.addDeck(deck); // Add again

            // Set doesn't allow duplicates
            assertEquals(1, user.getDecks().size());
            assertTrue(user.getDecks().contains(deck));

            logger.debug("Test passed: Duplicate deck not added");
        }

        @Test
        @DisplayName("Should correctly update deck owner when reassigning")
        void testReassignDeckOwner() {
            logger.debug("Test: Reassigning deck owner");

            User otherUser = new User(OTHER_EMAIL, OTHER_PASSWORD_HASH);
            otherUser.setId(2L);

            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);

            user.addDeck(deck);
            assertEquals(user, deck.getUser());

            user.removeDeck(deck);

            deck.setUser(otherUser);
            otherUser.getDecks().add(deck);

            assertFalse(user.getDecks().contains(deck));
            assertTrue(otherUser.getDecks().contains(deck));
            assertEquals(otherUser, deck.getUser());

            logger.debug("Test passed: Deck owner reassigned");
        }
    }

    // ========================================
    // Cascade and OrphanRemoval Tests (NEW)
    // ========================================

    @Nested
    @DisplayName("Cascade and OrphanRemoval Tests")
    class CascadeTests {

        @Test
        @DisplayName("Should demonstrate cascade ALL behavior")
        void testCascadeAll() {
            logger.debug("Test: Cascade ALL behavior");

            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);
            user.addDeck(deck);

            // In real JPA, deleting user would cascade to deck
            // We can verify the relationship is set up correctly
            assertTrue(user.getDecks().contains(deck));
            assertEquals(user, deck.getUser());

            logger.debug("Test passed: Cascade relationship verified");
        }

        @Test
        @DisplayName("Should demonstrate orphanRemoval behavior")
        void testOrphanRemoval() {
            logger.debug("Test: OrphanRemoval behavior");

            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);
            user.addDeck(deck);

            // Removing from collection would trigger orphanRemoval in JPA
            user.removeDeck(deck);

            assertFalse(user.getDecks().contains(deck));
            // In real JPA, deck would be deleted from database

            logger.debug("Test passed: OrphanRemoval relationship verified");
        }

        @Test
        @DisplayName("Should verify cascade chain: User -> Deck -> Card -> Tags")
        void testCascadeChain() {
            logger.debug("Test: Cascade chain verification");

            // Create user with deck
            Deck deck = new Deck(user, "Japanese", "Japanese vocabulary");
            deck.setId(1L);
            user.addDeck(deck);

            // Add cards to deck
            Card card1 = new Card(deck, "食べる", "to eat");
            card1.setId(1L);
            Card card2 = new Card(deck, "飲む", "to drink");
            card2.setId(2L);
            deck.getCards().add(card1);
            deck.getCards().add(card2);

            // Add tags to deck
            Tag tag1 = new Tag(deck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(deck, "N5");
            tag2.setId(2L);
            deck.getTags().add(tag1);
            deck.getTags().add(tag2);

            // Associate tags with cards
            card1.addTag(tag1);
            card2.addTag(tag1);
            card2.addTag(tag2);

            // Verify entire structure
            assertEquals(1, user.getDecks().size());
            assertEquals(2, deck.getCards().size());
            assertEquals(2, deck.getTags().size());
            assertEquals(1, card1.getTags().size());
            assertEquals(2, card2.getTags().size());

            // In real JPA, removing user would cascade delete all of this
            user.removeDeck(deck);
            assertTrue(user.getDecks().isEmpty());

            logger.debug("Test passed: Cascade chain verified");
        }

        @Test
        @DisplayName("Should verify multiple decks each have independent cascades")
        void testMultipleDecksCascadeIndependently() {
            logger.debug("Test: Multiple independent deck cascades");

            // Create two decks
            Deck japaneseDeck = new Deck(user, "Japanese", "Japanese vocabulary");
            japaneseDeck.setId(1L);
            Deck spanishDeck = new Deck(user, "Spanish", "Spanish vocabulary");
            spanishDeck.setId(2L);

            user.addDeck(japaneseDeck);
            user.addDeck(spanishDeck);

            // Add cards to each deck
            Card japaneseCard = new Card(japaneseDeck, "食べる", "to eat");
            japaneseCard.setId(1L);
            japaneseDeck.getCards().add(japaneseCard);

            Card spanishCard = new Card(spanishDeck, "comer", "to eat");
            spanishCard.setId(2L);
            spanishDeck.getCards().add(spanishCard);

            // Remove one deck
            user.removeDeck(japaneseDeck);

            // Other deck should remain
            assertEquals(1, user.getDecks().size());
            assertTrue(user.getDecks().contains(spanishDeck));
            assertFalse(user.getDecks().contains(japaneseDeck));

            logger.debug("Test passed: Independent cascades verified");
        }
    }

    // ========================================
    // Equality and HashCode Tests
    // ========================================

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when IDs match")
        void testUserEquality() {
            logger.debug("Test: Checking user equality with matching IDs");

            User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
            user1.setId(1L);

            User user2 = new User(OTHER_EMAIL, OTHER_PASSWORD_HASH);
            user2.setId(1L);

            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());

            logger.debug("Test passed: Users with same ID are equal");
        }

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

        @Test
        @DisplayName("Should be equal to itself (reflexive)")
        void testEqualityReflexive() {
            logger.debug("Test: Checking reflexive property of equals");

            assertEquals(user, user);

            logger.debug("Test passed: User equals itself");
        }

        @Test
        @DisplayName("Should not be equal to null")
        void testEqualityWithNull() {
            logger.debug("Test: Checking equality with null");

            assertNotEquals(user, null);

            logger.debug("Test passed: User not equal to null");
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void testEqualityWithDifferentClass() {
            logger.debug("Test: Checking equality with different class");

            assertNotEquals(user, "not a user");

            logger.debug("Test passed: User not equal to different class");
        }

        @Test
        @DisplayName("Should be symmetric")
        void testEqualitySymmetric() {
            logger.debug("Test: Checking symmetric property");

            User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
            user1.setId(1L);

            User user2 = new User(OTHER_EMAIL, OTHER_PASSWORD_HASH);
            user2.setId(1L);

            assertEquals(user1, user2);
            assertEquals(user2, user1);

            logger.debug("Test passed: Equality is symmetric");
        }

        @Test
        @DisplayName("Should be transitive")
        void testEqualityTransitive() {
            logger.debug("Test: Checking transitive property");

            User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
            user1.setId(1L);

            User user2 = new User(OTHER_EMAIL, OTHER_PASSWORD_HASH);
            user2.setId(1L);

            User user3 = new User("third@example.com", "thirdPassword");
            user3.setId(1L);

            assertEquals(user1, user2);
            assertEquals(user2, user3);
            assertEquals(user1, user3);

            logger.debug("Test passed: Equality is transitive");
        }

        @Test
        @DisplayName("Should handle users without IDs")
        void testEqualityWithoutIds() {
            logger.debug("Test: Equality for users without IDs");

            User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
            User user2 = new User(TEST_EMAIL, PASSWORD_HASH);

            // Without IDs, only same instance is equal to itself
            assertEquals(user1, user1);
            assertNotEquals(user1, user2);

            logger.debug("Test passed: Users without IDs handled");
        }
    }

    // ========================================
    // Security Tests
    // ========================================

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should not expose password hash in toString")
        void testToStringDoesNotExposePassword() {
            logger.debug("Test: Checking toString does not expose password");

            String userString = user.toString();

            assertFalse(userString.contains(PASSWORD_HASH));
            assertFalse(userString.toLowerCase().contains("password"));

            logger.debug("Test passed: Password not in toString");
        }

        @Test
        @DisplayName("Should not expose email in toString")
        void testToStringDoesNotExposeEmail() {
            logger.debug("Test: Checking toString does not expose email");

            String userString = user.toString();

            assertFalse(userString.contains(TEST_EMAIL));

            logger.debug("Test passed: Email not in toString");
        }

        @Test
        @DisplayName("Should include non-sensitive info in toString")
        void testToStringIncludesBasicInfo() {
            logger.debug("Test: Checking toString includes basic info");

            String userString = user.toString();

            assertTrue(userString.contains("User{"));
            assertTrue(userString.contains("id=" + user.getId()));
            assertTrue(userString.contains("status=" + user.getStatus()));
            assertTrue(userString.contains("emailVerified=" + user.isEmailVerified()));

            logger.debug("Test passed: toString includes basic info");
        }

        @Test
        @DisplayName("Should not expose tokens in toString")
        void testToStringDoesNotExposeTokens() {
            logger.debug("Test: Checking toString doesn't expose tokens");

            user.setEmailVerificationToken(VERIFICATION_TOKEN);
            user.setPasswordResetToken(RESET_TOKEN);

            String userString = user.toString();

            assertFalse(userString.contains(VERIFICATION_TOKEN));
            assertFalse(userString.contains(RESET_TOKEN));

            logger.debug("Test passed: Tokens not in toString");
        }

        @Test
        @DisplayName("Should allow package-private access to password hash")
        void testPasswordHashAccess() {
            logger.debug("Test: Accessing password hash (package-private)");

            String hash = user.getPasswordHash();

            assertEquals(PASSWORD_HASH, hash);

            logger.debug("Test passed: Password hash accessible within package");
        }

        @Test
        @DisplayName("Should allow package-private setting of password hash")
        void testPasswordHashSetter() {
            logger.debug("Test: Setting password hash (package-private)");

            String newHash = "newHashedPassword789";
            user.setPasswordHash(newHash);

            assertEquals(newHash, user.getPasswordHash());

            logger.debug("Test passed: Password hash set successfully");
        }
    }

    // ========================================
    // Integration Tests (Complex Scenarios)
    // ========================================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete user lifecycle")
        void testCompleteUserLifecycle() {
            logger.debug("Test: Complete user lifecycle");

            // Create user
            User newUser = new User("lifecycle@example.com", PASSWORD_HASH);
            newUser.setId(100L);
            assertFalse(newUser.isEmailVerified());

            // Email verification
            newUser.setEmailVerificationToken(VERIFICATION_TOKEN);
            newUser.verifyEmail();
            assertTrue(newUser.isEmailVerified());

            // Add decks
            Deck deck1 = new Deck(newUser, "Deck 1", "Description");
            deck1.setId(1L);
            newUser.addDeck(deck1);
            assertEquals(1, newUser.getDecks().size());

            // Failed login attempts
            newUser.recordFailedLoginAttempt();
            newUser.recordFailedLoginAttempt();
            assertEquals(2, newUser.getFailedLoginAttempts());

            // Successful login
            newUser.recordSuccessfulLogin();
            assertEquals(0, newUser.getFailedLoginAttempts());
            assertNotNull(newUser.getLastLoginAt());

            // Suspend account
            newUser.setStatus(User.UserStatus.SUSPENDED);
            assertFalse(newUser.isActive());

            // Reactivate
            newUser.setStatus(User.UserStatus.ACTIVE);
            assertTrue(newUser.isActive());

            // Soft delete
            newUser.softDelete();
            assertTrue(newUser.isDeleted());
            assertFalse(newUser.isActive());

            // Restore
            newUser.restore();
            assertFalse(newUser.isDeleted());
            assertTrue(newUser.isActive());

            logger.debug("Test passed: Complete lifecycle handled");
        }

        @Test
        @DisplayName("Should handle user with multiple decks and complex relationships")
        void testComplexUserWithMultipleDecks() {
            logger.debug("Test: User with multiple decks and complex relationships");

            // Create decks
            Deck japDeck = new Deck(user, "Japanese", "Japanese vocab");
            japDeck.setId(1L);
            Deck spaDeck = new Deck(user, "Spanish", "Spanish vocab");
            spaDeck.setId(2L);

            user.addDeck(japDeck);
            user.addDeck(spaDeck);

            // Add cards to Japanese deck
            Card japCard1 = new Card(japDeck, "食べる", "to eat");
            japCard1.setId(1L);
            Card japCard2 = new Card(japDeck, "飲む", "to drink");
            japCard2.setId(2L);
            japDeck.getCards().add(japCard1);
            japDeck.getCards().add(japCard2);

            // Add cards to Spanish deck
            Card spaCard1 = new Card(spaDeck, "comer", "to eat");
            spaCard1.setId(3L);
            spaDeck.getCards().add(spaCard1);

            // Add tags to Japanese deck
            Tag japTag = new Tag(japDeck, "verbs");
            japTag.setId(1L);
            japDeck.getTags().add(japTag);
            japCard1.addTag(japTag);
            japCard2.addTag(japTag);

            // Add tags to Spanish deck
            Tag spaTag = new Tag(spaDeck, "verbs");
            spaTag.setId(2L);
            spaDeck.getTags().add(spaTag);
            spaCard1.addTag(spaTag);

            // Verify structure
            assertEquals(2, user.getDecks().size());
            assertEquals(2, japDeck.getCards().size());
            assertEquals(1, spaDeck.getCards().size());
            assertEquals(1, japDeck.getTags().size());
            assertEquals(1, spaDeck.getTags().size());
            assertEquals(1, japCard1.getTags().size());
            assertEquals(1, japCard2.getTags().size());
            assertEquals(1, spaCard1.getTags().size());

            // Remove one deck
            user.removeDeck(japDeck);
            assertEquals(1, user.getDecks().size());
            assertTrue(user.getDecks().contains(spaDeck));

            logger.debug("Test passed: Complex relationships handled");
        }

        @Test
        @DisplayName("Should handle simultaneous account issues")
        void testSimultaneousAccountIssues() {
            logger.debug("Test: Simultaneous account issues");

            // Lock account
            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }

            // Suspend while locked
            user.setStatus(User.UserStatus.SUSPENDED);

            // Try to delete while locked and suspended
            user.softDelete();

            // All conditions should apply
            assertTrue(user.isLocked());
            assertTrue(user.isDeleted());
            assertEquals(User.UserStatus.DELETED, user.getStatus());
            assertFalse(user.isActive());

            logger.debug("Test passed: Multiple issues handled");
        }
    }
}