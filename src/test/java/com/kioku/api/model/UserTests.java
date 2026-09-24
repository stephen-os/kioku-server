package com.kioku.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Comprehensive unit tests for the User entity.
 *
 * Uses a fruit theme: users are fruit enthusiasts managing their fruit flashcard collections.
 * Deck names reference fruit categories, cards are individual fruits.
 *
 * @author Stephen Watson
 */
@DisplayName("User Entity Tests")
class UserTests {

    private static final String TEST_EMAIL = "fruitlover@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123456789012345678901234567890";
    private static final String VERIFICATION_TOKEN = "verify-token-uuid-12345";
    private static final String RESET_TOKEN = "reset-token-uuid-67890";

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(TEST_EMAIL, PASSWORD_HASH);
        ReflectionTestUtils.setField(user, "id", UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("creates user with email and password hash")
        void createsUserWithEmailAndPasswordHash() {
            User newUser = new User("apple@example.com", PASSWORD_HASH);

            assertEquals("apple@example.com", newUser.getEmail());
            assertEquals(PASSWORD_HASH, newUser.getPasswordHash());
            assertNotNull(newUser.getId()); // UUID assigned at construction, not on insert
            assertEquals(User.UserStatus.ACTIVE, newUser.getStatus());
            assertFalse(newUser.isEmailVerified());
            assertEquals(0, newUser.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("creates user with no-args constructor")
        void createsUserWithNoArgsConstructor() {
            User emptyUser = new User();

            assertNotNull(emptyUser);
            assertNull(emptyUser.getEmail());
            assertNull(emptyUser.getPasswordHash());
            assertEquals(User.UserStatus.ACTIVE, emptyUser.getStatus());
        }

        @Test
        @DisplayName("handles null email in constructor")
        void handlesNullEmailInConstructor() {
            User nullEmailUser = new User(null, PASSWORD_HASH);

            assertNull(nullEmailUser.getEmail());
            assertEquals(PASSWORD_HASH, nullEmailUser.getPasswordHash());
        }

        @Test
        @DisplayName("normalizes email to lowercase in constructor")
        void normalizesEmailToLowercaseInConstructor() {
            User upperUser = new User("BANANA@EXAMPLE.COM", PASSWORD_HASH);
            assertEquals("banana@example.com", upperUser.getEmail());
        }

        @Test
        @DisplayName("trims whitespace from email in constructor")
        void trimsWhitespaceFromEmailInConstructor() {
            User spacedUser = new User("  cherry@example.com  ", PASSWORD_HASH);
            assertEquals("cherry@example.com", spacedUser.getEmail());
        }
    }

    @Nested
    @DisplayName("Email Normalization Tests")
    class EmailNormalizationTests {

        @Test
        @DisplayName("setEmail normalizes to lowercase")
        void setEmailNormalizesToLowercase() {
            user.setEmail("MANGO@EXAMPLE.COM");
            assertEquals("mango@example.com", user.getEmail());
        }

        @Test
        @DisplayName("setEmail trims whitespace")
        void setEmailTrimsWhitespace() {
            user.setEmail("  papaya@example.com  ");
            assertEquals("papaya@example.com", user.getEmail());
        }

        @Test
        @DisplayName("setEmail handles null")
        void setEmailHandlesNull() {
            user.setEmail(null);
            assertNull(user.getEmail());
        }

        @Test
        @DisplayName("setEmail normalizes mixed case with spaces")
        void setEmailNormalizesMixedCaseWithSpaces() {
            user.setEmail("  GrApE@ExAmPlE.cOm  ");
            assertEquals("grape@example.com", user.getEmail());
        }
    }

    @Nested
    @DisplayName("Account Locking Tests")
    class AccountLockingTests {

        @Test
        @DisplayName("new user is not locked")
        void newUserIsNotLocked() {
            assertFalse(user.isLocked());
            assertNull(user.getLockedUntil());
        }

        @Test
        @DisplayName("records failed login attempts")
        void recordsFailedLoginAttempts() {
            assertEquals(0, user.getFailedLoginAttempts());

            user.recordFailedLoginAttempt();
            assertEquals(1, user.getFailedLoginAttempts());

            user.recordFailedLoginAttempt();
            assertEquals(2, user.getFailedLoginAttempts());

            assertFalse(user.isLocked());
        }

        @Test
        @DisplayName("locks account after 5 failed attempts")
        void locksAccountAfterFiveFailedAttempts() {
            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }

            assertTrue(user.isLocked());
            assertNotNull(user.getLockedUntil());
            assertTrue(user.getLockedUntil().isAfter(Instant.now()));
            assertEquals(5, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("lock duration is approximately 15 minutes")
        void lockDurationIsApproximately15Minutes() {
            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }

            Instant expectedUnlock = Instant.now().plus(15, ChronoUnit.MINUTES);
            assertTrue(user.getLockedUntil().isBefore(expectedUnlock.plus(5, ChronoUnit.SECONDS)));
            assertTrue(user.getLockedUntil().isAfter(expectedUnlock.minus(5, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("successful login resets failed attempts and clears lock")
        void successfulLoginResetsFailedAttemptsAndClearsLock() {
            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }
            assertTrue(user.isLocked());

            user.recordSuccessfulLogin();

            assertEquals(0, user.getFailedLoginAttempts());
            assertNull(user.getLockedUntil());
            assertFalse(user.isLocked());
            assertNotNull(user.getLastLoginAt());
        }

        @Test
        @DisplayName("account unlocks after lock period expires")
        void accountUnlocksAfterLockPeriodExpires() {
            user.setLockedUntil(Instant.now().minus(1, ChronoUnit.MINUTES));
            assertFalse(user.isLocked());
        }

        @Test
        @DisplayName("account remains locked if lock time is in future")
        void accountRemainsLockedIfLockTimeInFuture() {
            user.setLockedUntil(Instant.now().plus(10, ChronoUnit.MINUTES));
            assertTrue(user.isLocked());
        }
    }

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("new user is active")
        void newUserIsActive() {
            assertTrue(user.isActive());
            assertEquals(User.UserStatus.ACTIVE, user.getStatus());
        }

        @Test
        @DisplayName("locked user is not active")
        void lockedUserIsNotActive() {
            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }

            assertFalse(user.isActive());
            assertTrue(user.isLocked());
            assertEquals(User.UserStatus.ACTIVE, user.getStatus());
        }

        @Test
        @DisplayName("suspended user is not active")
        void suspendedUserIsNotActive() {
            user.setStatus(User.UserStatus.SUSPENDED);

            assertFalse(user.isActive());
            assertFalse(user.isLocked());
            assertFalse(user.isDeleted());
        }

        @Test
        @DisplayName("deleted user is not active")
        void deletedUserIsNotActive() {
            user.softDelete();

            assertFalse(user.isActive());
            assertTrue(user.isDeleted());
            assertEquals(User.UserStatus.DELETED, user.getStatus());
        }

        @Test
        @DisplayName("pending verification user is not active")
        void pendingVerificationUserIsNotActive() {
            user.setStatus(User.UserStatus.PENDING_VERIFICATION);
            assertFalse(user.isActive());
        }

        @Test
        @DisplayName("supports all status transitions")
        void supportsAllStatusTransitions() {
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
        }
    }

    @Nested
    @DisplayName("Soft Delete and Restore Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("new user is not deleted")
        void newUserIsNotDeleted() {
            assertFalse(user.isDeleted());
            assertNull(user.getDeletedAt());
        }

        @Test
        @DisplayName("softDelete sets deletedAt and status")
        void softDeleteSetsDeletedAtAndStatus() {
            Instant beforeDelete = Instant.now();
            user.softDelete();
            Instant afterDelete = Instant.now();

            assertTrue(user.isDeleted());
            assertNotNull(user.getDeletedAt());
            assertTrue(user.getDeletedAt().isAfter(beforeDelete.minus(1, ChronoUnit.SECONDS)));
            assertTrue(user.getDeletedAt().isBefore(afterDelete.plus(1, ChronoUnit.SECONDS)));
            assertEquals(User.UserStatus.DELETED, user.getStatus());
        }

        @Test
        @DisplayName("restore clears deletedAt and sets status to ACTIVE")
        void restoreClearsDeletedAtAndSetsStatusToActive() {
            user.softDelete();
            assertTrue(user.isDeleted());

            user.restore();

            assertFalse(user.isDeleted());
            assertNull(user.getDeletedAt());
            assertEquals(User.UserStatus.ACTIVE, user.getStatus());
        }

        @Test
        @DisplayName("supports multiple delete/restore cycles")
        void supportsMultipleDeleteRestoreCycles() {
            user.softDelete();
            assertTrue(user.isDeleted());

            user.restore();
            assertFalse(user.isDeleted());

            user.softDelete();
            assertTrue(user.isDeleted());

            user.restore();
            assertFalse(user.isDeleted());
        }

        @Test
        @DisplayName("restore preserves other state")
        void restorePreservesOtherState() {
            user.setEmail("specific@example.com");
            user.recordSuccessfulLogin();
            Instant loginTime = user.getLastLoginAt();

            user.softDelete();
            user.restore();

            assertEquals("specific@example.com", user.getEmail());
            assertEquals(loginTime, user.getLastLoginAt());
        }
    }

    @Nested
    @DisplayName("Email Verification Tests")
    class EmailVerificationTests {

        @Test
        @DisplayName("new user is not email verified")
        void newUserIsNotEmailVerified() {
            assertFalse(user.isEmailVerified());
            assertNull(user.getEmailVerificationToken());
            assertNull(user.getEmailVerificationSentAt());
        }

        @Test
        @DisplayName("setEmailVerificationToken sets token and timestamp")
        void setEmailVerificationTokenSetsTokenAndTimestamp() {
            Instant beforeSet = Instant.now();
            user.setEmailVerificationToken(VERIFICATION_TOKEN);
            Instant afterSet = Instant.now();

            assertEquals(VERIFICATION_TOKEN, user.getEmailVerificationToken());
            assertNotNull(user.getEmailVerificationSentAt());
            assertTrue(user.getEmailVerificationSentAt().isAfter(beforeSet.minus(1, ChronoUnit.SECONDS)));
            assertTrue(user.getEmailVerificationSentAt().isBefore(afterSet.plus(1, ChronoUnit.SECONDS)));
            assertFalse(user.isEmailVerified());
        }

        @Test
        @DisplayName("verifyEmail sets verified and clears token")
        void verifyEmailSetsVerifiedAndClearsToken() {
            user.setEmailVerificationToken(VERIFICATION_TOKEN);
            user.verifyEmail();

            assertTrue(user.isEmailVerified());
            assertNull(user.getEmailVerificationToken());
            assertNull(user.getEmailVerificationSentAt());
        }

        @Test
        @DisplayName("isEmailVerificationTokenExpired returns true when no token")
        void isEmailVerificationTokenExpiredReturnsTrueWhenNoToken() {
            assertTrue(user.isEmailVerificationTokenExpired());
        }

        @Test
        @DisplayName("isEmailVerificationTokenExpired returns false for fresh token")
        void isEmailVerificationTokenExpiredReturnsFalseForFreshToken() {
            user.setEmailVerificationToken(VERIFICATION_TOKEN);
            assertFalse(user.isEmailVerificationTokenExpired());
        }

        @Test
        @DisplayName("allows re-sending verification token")
        void allowsReSendingVerificationToken() throws InterruptedException {
            user.setEmailVerificationToken("first-token");
            Instant firstSentAt = user.getEmailVerificationSentAt();

            Thread.sleep(10);
            user.setEmailVerificationToken("second-token");

            assertEquals("second-token", user.getEmailVerificationToken());
            assertTrue(user.getEmailVerificationSentAt().isAfter(firstSentAt));
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("setPasswordResetToken sets token and timestamp")
        void setPasswordResetTokenSetsTokenAndTimestamp() {
            Instant beforeSet = Instant.now();
            user.setPasswordResetToken(RESET_TOKEN);
            Instant afterSet = Instant.now();

            assertEquals(RESET_TOKEN, user.getPasswordResetToken());
            assertNotNull(user.getPasswordResetSentAt());
            assertTrue(user.getPasswordResetSentAt().isAfter(beforeSet.minus(1, ChronoUnit.SECONDS)));
            assertTrue(user.getPasswordResetSentAt().isBefore(afterSet.plus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("clearPasswordResetToken clears token and timestamp")
        void clearPasswordResetTokenClearsTokenAndTimestamp() {
            user.setPasswordResetToken(RESET_TOKEN);
            user.clearPasswordResetToken();

            assertNull(user.getPasswordResetToken());
            assertNull(user.getPasswordResetSentAt());
        }

        @Test
        @DisplayName("isPasswordResetTokenExpired returns true when no token")
        void isPasswordResetTokenExpiredReturnsTrueWhenNoToken() {
            assertTrue(user.isPasswordResetTokenExpired());
        }

        @Test
        @DisplayName("isPasswordResetTokenExpired returns false for fresh token")
        void isPasswordResetTokenExpiredReturnsFalseForFreshToken() {
            user.setPasswordResetToken(RESET_TOKEN);
            assertFalse(user.isPasswordResetTokenExpired());
        }

        @Test
        @DisplayName("allows replacing reset token")
        void allowsReplacingResetToken() throws InterruptedException {
            user.setPasswordResetToken("first-token");
            Instant firstSentAt = user.getPasswordResetSentAt();

            Thread.sleep(10);
            user.setPasswordResetToken("second-token");

            assertEquals("second-token", user.getPasswordResetToken());
            assertTrue(user.getPasswordResetSentAt().isAfter(firstSentAt));
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("onCreate sets timestamps and normalizes email")
        void onCreateSetsTimestampsAndNormalizesEmail() {
            User newUser = new User("APPLE@EXAMPLE.COM", PASSWORD_HASH);
            newUser.onCreate();

            assertNotNull(newUser.getCreatedAt());
            assertNotNull(newUser.getUpdatedAt());
            assertEquals(newUser.getCreatedAt(), newUser.getUpdatedAt());
            assertEquals("apple@example.com", newUser.getEmail());
        }

        @Test
        @DisplayName("onUpdate updates timestamp and normalizes email")
        void onUpdateUpdatesTimestampAndNormalizesEmail() throws InterruptedException {
            user.onCreate();
            Instant createdAt = user.getCreatedAt();
            Instant initialUpdatedAt = user.getUpdatedAt();

            Thread.sleep(10);
            user.setEmail("BANANA@EXAMPLE.COM");
            user.onUpdate();

            assertEquals(createdAt, user.getCreatedAt());
            assertTrue(user.getUpdatedAt().isAfter(initialUpdatedAt));
            assertEquals("banana@example.com", user.getEmail());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("user equals itself")
        void userEqualsItself() {
            assertEquals(user, user);
        }

        @Test
        @DisplayName("user does not equal null")
        void userDoesNotEqualNull() {
            assertNotEquals(null, user);
        }

        @Test
        @DisplayName("user does not equal different type")
        void userDoesNotEqualDifferentType() {
            assertNotEquals("not a user", user);
        }

        @Test
        @DisplayName("users with same ID are equal")
        void usersWithSameIdAreEqual() {
            User user1 = new User("apple@example.com", PASSWORD_HASH);
            ReflectionTestUtils.setField(user1, "id", UUID.fromString("00000000-0000-0000-0000-000000000001"));

            User user2 = new User("banana@example.com", "differentHash");
            ReflectionTestUtils.setField(user2, "id", UUID.fromString("00000000-0000-0000-0000-000000000001"));

            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());
        }

        @Test
        @DisplayName("users with different IDs are not equal")
        void usersWithDifferentIdsAreNotEqual() {
            User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
            ReflectionTestUtils.setField(user1, "id", UUID.fromString("00000000-0000-0000-0000-000000000001"));

            User user2 = new User(TEST_EMAIL, PASSWORD_HASH);
            ReflectionTestUtils.setField(user2, "id", UUID.fromString("00000000-0000-0000-0000-000000000002"));

            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("users without IDs are only equal to themselves")
        void usersWithoutIdsAreOnlyEqualToThemselves() {
            User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
            User user2 = new User(TEST_EMAIL, PASSWORD_HASH);

            assertEquals(user1, user1);
            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("hashCode is consistent")
        void hashCodeIsConsistent() {
            int hash1 = user.hashCode();
            int hash2 = user.hashCode();
            assertEquals(hash1, hash2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString does not expose email")
        void toStringDoesNotExposeEmail() {
            String userString = user.toString();
            assertFalse(userString.contains(TEST_EMAIL));
        }

        @Test
        @DisplayName("toString does not expose password hash")
        void toStringDoesNotExposePasswordHash() {
            String userString = user.toString();
            assertFalse(userString.contains(PASSWORD_HASH));
        }

        @Test
        @DisplayName("toString does not expose tokens")
        void toStringDoesNotExposeTokens() {
            user.setEmailVerificationToken(VERIFICATION_TOKEN);
            user.setPasswordResetToken(RESET_TOKEN);

            String userString = user.toString();
            assertFalse(userString.contains(VERIFICATION_TOKEN));
            assertFalse(userString.contains(RESET_TOKEN));
        }

        @Test
        @DisplayName("toString includes basic info")
        void toStringIncludesBasicInfo() {
            String userString = user.toString();

            assertTrue(userString.contains("User{"));
            assertTrue(userString.contains("id=" + user.getId()));
            assertTrue(userString.contains("status=" + user.getStatus()));
            assertTrue(userString.contains("emailVerified=" + user.isEmailVerified()));
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("getId is assigned at construction and can be set via reflection")
        void getIdIsAssignedAtConstructionAndCanBeSetViaReflection() {
            User newUser = new User();
            assertNotNull(newUser.getId()); // UUID assigned at construction, not on insert

            ReflectionTestUtils.setField(newUser, "id", UUID.fromString("00000000-0000-0000-0000-000000000042"));
            assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000042"), newUser.getId());
        }

        @Test
        @DisplayName("getPasswordHash and setPasswordHash work correctly")
        void getPasswordHashAndSetPasswordHashWorkCorrectly() {
            String newHash = "newHash123";
            user.setPasswordHash(newHash);
            assertEquals(newHash, user.getPasswordHash());
        }

        @Test
        @DisplayName("getStatus and setStatus work correctly")
        void getStatusAndSetStatusWorkCorrectly() {
            user.setStatus(User.UserStatus.SUSPENDED);
            assertEquals(User.UserStatus.SUSPENDED, user.getStatus());
        }

        @Test
        @DisplayName("getFailedLoginAttempts and setFailedLoginAttempts work correctly")
        void getFailedLoginAttemptsAndSetFailedLoginAttemptsWorkCorrectly() {
            user.setFailedLoginAttempts(3);
            assertEquals(3, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("getLockedUntil and setLockedUntil work correctly")
        void getLockedUntilAndSetLockedUntilWorkCorrectly() {
            Instant lockTime = Instant.now().plus(1, ChronoUnit.HOURS);
            user.setLockedUntil(lockTime);
            assertEquals(lockTime, user.getLockedUntil());
        }

        @Test
        @DisplayName("getLastLoginAt and setLastLoginAt work correctly")
        void getLastLoginAtAndSetLastLoginAtWorkCorrectly() {
            Instant loginTime = Instant.now();
            user.setLastLoginAt(loginTime);
            assertEquals(loginTime, user.getLastLoginAt());
        }

        @Test
        @DisplayName("isEmailVerified and setEmailVerified work correctly")
        void isEmailVerifiedAndSetEmailVerifiedWorkCorrectly() {
            user.setEmailVerified(true);
            assertTrue(user.isEmailVerified());

            user.setEmailVerified(false);
            assertFalse(user.isEmailVerified());
        }

        @Test
        @DisplayName("getVersion returns null before persistence")
        void getVersionReturnsNullBeforePersistence() {
            assertNull(user.getVersion());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("complete user lifecycle")
        void completeUserLifecycle() {
            // Create user
            User fruitFan = new User("fruitfan@example.com", PASSWORD_HASH);
            ReflectionTestUtils.setField(fruitFan, "id", UUID.fromString("00000000-0000-0000-0000-000000000100"));
            assertFalse(fruitFan.isEmailVerified());

            // Email verification
            fruitFan.setEmailVerificationToken(VERIFICATION_TOKEN);
            fruitFan.verifyEmail();
            assertTrue(fruitFan.isEmailVerified());


            // Failed login attempts
            fruitFan.recordFailedLoginAttempt();
            fruitFan.recordFailedLoginAttempt();
            assertEquals(2, fruitFan.getFailedLoginAttempts());

            // Successful login
            fruitFan.recordSuccessfulLogin();
            assertEquals(0, fruitFan.getFailedLoginAttempts());
            assertNotNull(fruitFan.getLastLoginAt());

            // Suspend account
            fruitFan.setStatus(User.UserStatus.SUSPENDED);
            assertFalse(fruitFan.isActive());

            // Reactivate
            fruitFan.setStatus(User.UserStatus.ACTIVE);
            assertTrue(fruitFan.isActive());

            // Soft delete
            fruitFan.softDelete();
            assertTrue(fruitFan.isDeleted());
            assertFalse(fruitFan.isActive());

            // Restore
            fruitFan.restore();
            assertFalse(fruitFan.isDeleted());
            assertTrue(fruitFan.isActive());
        }

        @Test
        @DisplayName("handles simultaneous account issues")
        void handlesSimultaneousAccountIssues() {
            // Lock account
            for (int i = 0; i < 5; i++) {
                user.recordFailedLoginAttempt();
            }

            // Suspend while locked
            user.setStatus(User.UserStatus.SUSPENDED);

            // Delete while locked and suspended
            user.softDelete();

            // All conditions apply
            assertTrue(user.isLocked());
            assertTrue(user.isDeleted());
            assertEquals(User.UserStatus.DELETED, user.getStatus());
            assertFalse(user.isActive());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("handles empty string email")
        void handlesEmptyStringEmail() {
            user.setEmail("");
            assertEquals("", user.getEmail());
        }

        @Test
        @DisplayName("successful login updates lastLoginAt")
        void successfulLoginUpdatesLastLoginAt() {
            assertNull(user.getLastLoginAt());

            user.recordSuccessfulLogin();

            assertNotNull(user.getLastLoginAt());
            assertTrue(user.getLastLoginAt().isBefore(Instant.now().plus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("clearing non-existent password reset token is safe")
        void clearingNonExistentPasswordResetTokenIsSafe() {
            assertNull(user.getPasswordResetToken());
            assertDoesNotThrow(() -> user.clearPasswordResetToken());
        }

        @Test
        @DisplayName("verifying already verified email is safe")
        void verifyingAlreadyVerifiedEmailIsSafe() {
            user.verifyEmail();
            assertTrue(user.isEmailVerified());

            assertDoesNotThrow(() -> user.verifyEmail());
            assertTrue(user.isEmailVerified());
        }
    }
}
