package com.kioku.api.repository;

import com.kioku.api.BaseIntegrationTest;
import com.kioku.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>User CRUD operations</li>
 *   <li>Finding users by email, tokens, and status</li>
 *   <li>Querying locked, deleted, and active accounts</li>
 *   <li>Email existence checks</li>
 *   <li>Custom query methods</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("UserRepository Integration Tests")
@Transactional
class UserRepositoryTest extends BaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String THIRD_EMAIL = "third@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String VERIFICATION_TOKEN = "verification-token-uuid-12345";
    private static final String RESET_TOKEN = "reset-token-uuid-67890";

    @Autowired
    private UserRepository userRepository;

    /**
     * Cleans up the database before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up test: Cleaning database");
        userRepository.deleteAll();
    }

    // Basic CRUD Tests

    /**
     * Tests saving a new user to the database.
     */
    @Test
    @DisplayName("Should save a new user")
    void testSaveUser() {
        logger.debug("Test: Saving user with email={}", TEST_EMAIL);

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals(TEST_EMAIL, savedUser.getEmail());
        assertEquals(PASSWORD_HASH, savedUser.getPasswordHash());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        logger.debug("Test passed: User saved with id={}", savedUser.getId());
    }

    /**
     * Tests finding a user by ID.
     */
    @Test
    @DisplayName("Should find user by ID")
    void testFindById() {
        logger.debug("Test: Finding user by ID");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        User savedUser = userRepository.save(user);

        Optional<User> found = userRepository.findById(savedUser.getId());

        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());

        logger.debug("Test passed: User found by id={}", savedUser.getId());
    }

    /**
     * Tests that findById returns empty for non-existent ID.
     */
    @Test
    @DisplayName("Should return empty when user ID not found")
    void testFindByIdNotFound() {
        logger.debug("Test: Finding non-existent user by ID");

        Optional<User> found = userRepository.findById(999L);

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty result for non-existent ID");
    }

    /**
     * Tests deleting a user from the database.
     */
    @Test
    @DisplayName("Should delete a user")
    void testDeleteUser() {
        logger.debug("Test: Deleting user");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        userRepository.delete(savedUser);

        assertFalse(userRepository.existsById(userId));

        logger.debug("Test passed: User deleted with id={}", userId);
    }

    // Email Query Tests

    /**
     * Tests finding a user by email address.
     */
    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        logger.debug("Test: Finding user by email={}", TEST_EMAIL);

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail(TEST_EMAIL);

        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());

        logger.debug("Test passed: User found by email");
    }

    /**
     * Tests that email search is case-insensitive.
     */
    @Test
    @DisplayName("Should find user by email (case-insensitive)")
    void testFindByEmailCaseInsensitive() {
        logger.debug("Test: Finding user by email (case-insensitive)");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("TEST@EXAMPLE.COM");

        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());

        logger.debug("Test passed: Case-insensitive email search works");
    }

    /**
     * Tests that findByEmail returns empty for non-existent email.
     */
    @Test
    @DisplayName("Should return empty when email not found")
    void testFindByEmailNotFound() {
        logger.debug("Test: Finding non-existent email");

        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty result for non-existent email");
    }

    /**
     * Tests checking if an email exists.
     */
    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        logger.debug("Test: Checking email existence");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail(TEST_EMAIL));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));

        logger.debug("Test passed: Email existence check works");
    }

    /**
     * Tests that existsByEmail is case-insensitive.
     */
    @Test
    @DisplayName("Should check email existence (case-insensitive)")
    void testExistsByEmailCaseInsensitive() {
        logger.debug("Test: Checking email existence (case-insensitive)");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("TEST@EXAMPLE.COM"));

        logger.debug("Test passed: Case-insensitive email existence check works");
    }

    // Token Query Tests

    /**
     * Tests finding a user by email verification token.
     */
    @Test
    @DisplayName("Should find user by email verification token")
    void testFindByEmailVerificationToken() {
        logger.debug("Test: Finding user by email verification token");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        user.setEmailVerificationToken(VERIFICATION_TOKEN);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmailVerificationToken(VERIFICATION_TOKEN);

        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());
        assertEquals(VERIFICATION_TOKEN, found.get().getEmailVerificationToken());

        logger.debug("Test passed: User found by verification token");
    }

    /**
     * Tests that findByEmailVerificationToken returns empty for invalid token.
     */
    @Test
    @DisplayName("Should return empty for invalid verification token")
    void testFindByEmailVerificationTokenNotFound() {
        logger.debug("Test: Finding user with invalid verification token");

        Optional<User> found = userRepository.findByEmailVerificationToken("invalid-token");

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty result for invalid verification token");
    }

    /**
     * Tests finding a user by password reset token.
     */
    @Test
    @DisplayName("Should find user by password reset token")
    void testFindByPasswordResetToken() {
        logger.debug("Test: Finding user by password reset token");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        user.setPasswordResetToken(RESET_TOKEN);
        userRepository.save(user);

        Optional<User> found = userRepository.findByPasswordResetToken(RESET_TOKEN);

        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());
        assertEquals(RESET_TOKEN, found.get().getPasswordResetToken());

        logger.debug("Test passed: User found by reset token");
    }

    /**
     * Tests that findByPasswordResetToken returns empty for invalid token.
     */
    @Test
    @DisplayName("Should return empty for invalid reset token")
    void testFindByPasswordResetTokenNotFound() {
        logger.debug("Test: Finding user with invalid reset token");

        Optional<User> found = userRepository.findByPasswordResetToken("invalid-token");

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty result for invalid reset token");
    }

    // Status Query Tests

    /**
     * Tests finding users by status.
     */
    @Test
    @DisplayName("Should find users by status")
    void testFindByStatus() {
        logger.debug("Test: Finding users by status");

        User activeUser = new User(TEST_EMAIL, PASSWORD_HASH);
        activeUser.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(activeUser);

        User suspendedUser = new User(OTHER_EMAIL, PASSWORD_HASH);
        suspendedUser.setStatus(User.UserStatus.SUSPENDED);
        userRepository.save(suspendedUser);

        List<User> activeUsers = userRepository.findByStatus(User.UserStatus.ACTIVE);
        List<User> suspendedUsers = userRepository.findByStatus(User.UserStatus.SUSPENDED);

        assertEquals(1, activeUsers.size());
        assertEquals(TEST_EMAIL, activeUsers.get(0).getEmail());

        assertEquals(1, suspendedUsers.size());
        assertEquals(OTHER_EMAIL, suspendedUsers.get(0).getEmail());

        logger.debug("Test passed: Users found by status");
    }

    /**
     * Tests finding users by email verification status.
     */
    @Test
    @DisplayName("Should find users by email verified status")
    void testFindByEmailVerified() {
        logger.debug("Test: Finding users by email verification status");

        User verifiedUser = new User(TEST_EMAIL, PASSWORD_HASH);
        verifiedUser.setEmailVerified(true);
        userRepository.save(verifiedUser);

        User unverifiedUser = new User(OTHER_EMAIL, PASSWORD_HASH);
        unverifiedUser.setEmailVerified(false);
        userRepository.save(unverifiedUser);

        List<User> verified = userRepository.findByEmailVerified(true);
        List<User> unverified = userRepository.findByEmailVerified(false);

        assertEquals(1, verified.size());
        assertEquals(TEST_EMAIL, verified.get(0).getEmail());

        assertEquals(1, unverified.size());
        assertEquals(OTHER_EMAIL, unverified.get(0).getEmail());

        logger.debug("Test passed: Users found by email verification status");
    }

    // Account State Query Tests

    /**
     * Tests finding locked accounts.
     */
    @Test
    @DisplayName("Should find locked accounts")
    void testFindLockedAccounts() {
        logger.debug("Test: Finding locked accounts");

        User lockedUser = new User(TEST_EMAIL, PASSWORD_HASH);
        lockedUser.setLockedUntil(LocalDateTime.now().plusHours(1));
        userRepository.save(lockedUser);

        User unlockedUser = new User(OTHER_EMAIL, PASSWORD_HASH);
        userRepository.save(unlockedUser);

        User expiredLockUser = new User(THIRD_EMAIL, PASSWORD_HASH);
        expiredLockUser.setLockedUntil(LocalDateTime.now().minusHours(1));
        userRepository.save(expiredLockUser);

        List<User> lockedAccounts = userRepository.findLockedAccounts(LocalDateTime.now());

        assertEquals(1, lockedAccounts.size());
        assertEquals(TEST_EMAIL, lockedAccounts.get(0).getEmail());

        logger.debug("Test passed: Found {} locked accounts", lockedAccounts.size());
    }

    /**
     * Tests finding soft-deleted accounts.
     */
    @Test
    @DisplayName("Should find deleted accounts")
    void testFindDeletedAccounts() {
        logger.debug("Test: Finding deleted accounts");

        User deletedUser = new User(TEST_EMAIL, PASSWORD_HASH);
        deletedUser.softDelete();
        userRepository.save(deletedUser);

        User activeUser = new User(OTHER_EMAIL, PASSWORD_HASH);
        userRepository.save(activeUser);

        List<User> deletedAccounts = userRepository.findDeletedAccounts();

        assertEquals(1, deletedAccounts.size());
        assertEquals(TEST_EMAIL, deletedAccounts.get(0).getEmail());

        logger.debug("Test passed: Found {} deleted accounts", deletedAccounts.size());
    }

    /**
     * Tests finding active accounts.
     */
    @Test
    @DisplayName("Should find active accounts")
    void testFindActiveAccounts() {
        logger.debug("Test: Finding active accounts");

        User activeUser = new User(TEST_EMAIL, PASSWORD_HASH);
        activeUser.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(activeUser);

        User suspendedUser = new User(OTHER_EMAIL, PASSWORD_HASH);
        suspendedUser.setStatus(User.UserStatus.SUSPENDED);
        userRepository.save(suspendedUser);

        User lockedUser = new User(THIRD_EMAIL, PASSWORD_HASH);
        lockedUser.setStatus(User.UserStatus.ACTIVE);
        lockedUser.setLockedUntil(LocalDateTime.now().plusHours(1));
        userRepository.save(lockedUser);

        List<User> activeAccounts = userRepository.findActiveAccounts(LocalDateTime.now());

        assertEquals(1, activeAccounts.size());
        assertEquals(TEST_EMAIL, activeAccounts.get(0).getEmail());

        logger.debug("Test passed: Found {} active accounts", activeAccounts.size());
    }

    // Analytics Query Tests

    /**
     * Tests counting users created after a specific date.
     */
    @Test
    @DisplayName("Should count users created after date")
    void testCountUsersCreatedAfter() {
        logger.debug("Test: Counting users created after date");

        User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(user1);

        User user2 = new User(OTHER_EMAIL, PASSWORD_HASH);
        userRepository.save(user2);

        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(1);
        long count = userRepository.countUsersCreatedAfter(cutoffDate);

        assertEquals(2, count);

        logger.debug("Test passed: Counted {} users created after date", count);
    }

    /**
     * Tests counting users with future cutoff date returns zero.
     */
    @Test
    @DisplayName("Should return zero for future cutoff date")
    void testCountUsersCreatedAfterFuture() {
        logger.debug("Test: Counting users with future cutoff date");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(user);

        LocalDateTime futureCutoff = LocalDateTime.now().plusHours(1);
        long count = userRepository.countUsersCreatedAfter(futureCutoff);

        assertEquals(0, count);

        logger.debug("Test passed: Zero users counted for future cutoff");
    }

    // Update Tests

    /**
     * Tests updating a user's email.
     */
    @Test
    @DisplayName("Should update user email")
    void testUpdateUserEmail() {
        logger.debug("Test: Updating user email");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        User savedUser = userRepository.save(user);

        savedUser.setEmail(OTHER_EMAIL);
        User updatedUser = userRepository.save(savedUser);

        assertEquals(OTHER_EMAIL, updatedUser.getEmail());

        logger.debug("Test passed: User email updated to {}", OTHER_EMAIL);
    }

    /**
     * Tests that updated_at timestamp changes on update.
     */
    @Test
    @DisplayName("Should update updated_at timestamp on save")
    void testUpdatedAtTimestamp() {
        logger.debug("Test: Checking updated_at timestamp changes");

        User user = new User(TEST_EMAIL, PASSWORD_HASH);
        User savedUser = userRepository.save(user);
        userRepository.flush();
        Long userId = savedUser.getId();
        LocalDateTime originalUpdatedAt = savedUser.getUpdatedAt();

        await().pollDelay(100, MILLISECONDS).until(() -> true);

        User userToUpdate = userRepository.findById(userId).orElseThrow();
        userToUpdate.setEmail(OTHER_EMAIL);
        User updatedUser = userRepository.saveAndFlush(userToUpdate);

        assertTrue(updatedUser.getUpdatedAt().isAfter(originalUpdatedAt),
                () -> String.format("Expected updated_at (%s) to be after original (%s)",
                        updatedUser.getUpdatedAt(), originalUpdatedAt));

        logger.debug("Test passed: updated_at timestamp changed");
    }

    // Batch Operation Tests

    /**
     * Tests finding all users.
     */
    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        logger.debug("Test: Finding all users");

        User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
        User user2 = new User(OTHER_EMAIL, PASSWORD_HASH);
        User user3 = new User(THIRD_EMAIL, PASSWORD_HASH);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        List<User> allUsers = userRepository.findAll();

        assertEquals(3, allUsers.size());

        logger.debug("Test passed: Found {} users", allUsers.size());
    }

    /**
     * Tests counting all users.
     */
    @Test
    @DisplayName("Should count all users")
    void testCount() {
        logger.debug("Test: Counting all users");

        User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
        User user2 = new User(OTHER_EMAIL, PASSWORD_HASH);

        userRepository.save(user1);
        userRepository.save(user2);

        long count = userRepository.count();

        assertEquals(2, count);

        logger.debug("Test passed: Total count is {}", count);
    }

    /**
     * Tests deleting all users.
     */
    @Test
    @DisplayName("Should delete all users")
    void testDeleteAll() {
        logger.debug("Test: Deleting all users");

        User user1 = new User(TEST_EMAIL, PASSWORD_HASH);
        User user2 = new User(OTHER_EMAIL, PASSWORD_HASH);

        userRepository.save(user1);
        userRepository.save(user2);

        userRepository.deleteAll();

        assertEquals(0, userRepository.count());

        logger.debug("Test passed: All users deleted");
    }
}