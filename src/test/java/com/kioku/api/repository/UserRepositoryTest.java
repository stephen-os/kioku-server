package com.kioku.api.repository;

import com.kioku.api.BaseIntegrationTest;
import com.kioku.api.entity.UserEntity;
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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        assertNotNull(savedUserEntity.getId());
        assertEquals(TEST_EMAIL, savedUserEntity.getEmail());
        assertEquals(PASSWORD_HASH, savedUserEntity.getPasswordHash());
        assertNotNull(savedUserEntity.getCreatedAt());
        assertNotNull(savedUserEntity.getUpdatedAt());

        logger.debug("Test passed: User saved with id={}", savedUserEntity.getId());
    }

    /**
     * Tests finding a user by ID.
     */
    @Test
    @DisplayName("Should find user by ID")
    void testFindById() {
        logger.debug("Test: Finding user by ID");

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        Optional<UserEntity> found = userRepository.findById(savedUserEntity.getId());

        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());

        logger.debug("Test passed: User found by id={}", savedUserEntity.getId());
    }

    /**
     * Tests that findById returns empty for non-existent ID.
     */
    @Test
    @DisplayName("Should return empty when user ID not found")
    void testFindByIdNotFound() {
        logger.debug("Test: Finding non-existent user by ID");

        Optional<UserEntity> found = userRepository.findById(999L);

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        UserEntity savedUserEntity = userRepository.save(userEntity);
        Long userId = savedUserEntity.getId();

        userRepository.delete(savedUserEntity);

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(userEntity);

        Optional<UserEntity> found = userRepository.findByEmail(TEST_EMAIL);

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(userEntity);

        Optional<UserEntity> found = userRepository.findByEmail("TEST@EXAMPLE.COM");

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

        Optional<UserEntity> found = userRepository.findByEmail("nonexistent@example.com");

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(userEntity);

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(userEntity);

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        userEntity.setEmailVerificationToken(VERIFICATION_TOKEN);
        userRepository.save(userEntity);

        Optional<UserEntity> found = userRepository.findByEmailVerificationToken(VERIFICATION_TOKEN);

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

        Optional<UserEntity> found = userRepository.findByEmailVerificationToken("invalid-token");

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        userEntity.setPasswordResetToken(RESET_TOKEN);
        userRepository.save(userEntity);

        Optional<UserEntity> found = userRepository.findByPasswordResetToken(RESET_TOKEN);

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

        Optional<UserEntity> found = userRepository.findByPasswordResetToken("invalid-token");

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

        UserEntity activeUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        activeUserEntity.setStatus(UserEntity.UserStatus.ACTIVE);
        userRepository.save(activeUserEntity);

        UserEntity suspendedUserEntity = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        suspendedUserEntity.setStatus(UserEntity.UserStatus.SUSPENDED);
        userRepository.save(suspendedUserEntity);

        List<UserEntity> activeUserEntities = userRepository.findByStatus(UserEntity.UserStatus.ACTIVE);
        List<UserEntity> suspendedUserEntities = userRepository.findByStatus(UserEntity.UserStatus.SUSPENDED);

        assertEquals(1, activeUserEntities.size());
        assertEquals(TEST_EMAIL, activeUserEntities.get(0).getEmail());

        assertEquals(1, suspendedUserEntities.size());
        assertEquals(OTHER_EMAIL, suspendedUserEntities.get(0).getEmail());

        logger.debug("Test passed: Users found by status");
    }

    /**
     * Tests finding users by email verification status.
     */
    @Test
    @DisplayName("Should find users by email verified status")
    void testFindByEmailVerified() {
        logger.debug("Test: Finding users by email verification status");

        UserEntity verifiedUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        verifiedUserEntity.setEmailVerified(true);
        userRepository.save(verifiedUserEntity);

        UserEntity unverifiedUserEntity = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        unverifiedUserEntity.setEmailVerified(false);
        userRepository.save(unverifiedUserEntity);

        List<UserEntity> verified = userRepository.findByEmailVerified(true);
        List<UserEntity> unverified = userRepository.findByEmailVerified(false);

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

        UserEntity lockedUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        lockedUserEntity.setLockedUntil(LocalDateTime.now().plusHours(1));
        userRepository.save(lockedUserEntity);

        UserEntity unlockedUserEntity = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        userRepository.save(unlockedUserEntity);

        UserEntity expiredLockUserEntity = new UserEntity(THIRD_EMAIL, PASSWORD_HASH);
        expiredLockUserEntity.setLockedUntil(LocalDateTime.now().minusHours(1));
        userRepository.save(expiredLockUserEntity);

        List<UserEntity> lockedAccounts = userRepository.findLockedAccounts(LocalDateTime.now());

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

        UserEntity deletedUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        deletedUserEntity.softDelete();
        userRepository.save(deletedUserEntity);

        UserEntity activeUserEntity = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        userRepository.save(activeUserEntity);

        List<UserEntity> deletedAccounts = userRepository.findDeletedAccounts();

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

        UserEntity activeUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        activeUserEntity.setStatus(UserEntity.UserStatus.ACTIVE);
        userRepository.save(activeUserEntity);

        UserEntity suspendedUserEntity = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        suspendedUserEntity.setStatus(UserEntity.UserStatus.SUSPENDED);
        userRepository.save(suspendedUserEntity);

        UserEntity lockedUserEntity = new UserEntity(THIRD_EMAIL, PASSWORD_HASH);
        lockedUserEntity.setStatus(UserEntity.UserStatus.ACTIVE);
        lockedUserEntity.setLockedUntil(LocalDateTime.now().plusHours(1));
        userRepository.save(lockedUserEntity);

        List<UserEntity> activeAccounts = userRepository.findActiveAccounts(LocalDateTime.now());

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

        UserEntity userEntity1 = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(userEntity1);

        UserEntity userEntity2 = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        userRepository.save(userEntity2);

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        userRepository.save(userEntity);

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

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        savedUserEntity.setEmail(OTHER_EMAIL);
        UserEntity updatedUserEntity = userRepository.save(savedUserEntity);

        assertEquals(OTHER_EMAIL, updatedUserEntity.getEmail());

        logger.debug("Test passed: User email updated to {}", OTHER_EMAIL);
    }

    /**
     * Tests that updated_at timestamp changes on update.
     */
    @Test
    @DisplayName("Should update updated_at timestamp on save")
    void testUpdatedAtTimestamp() {
        logger.debug("Test: Checking updated_at timestamp changes");

        UserEntity userEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        UserEntity savedUserEntity = userRepository.save(userEntity);
        userRepository.flush();
        Long userId = savedUserEntity.getId();
        LocalDateTime originalUpdatedAt = savedUserEntity.getUpdatedAt();

        await().pollDelay(100, MILLISECONDS).until(() -> true);

        UserEntity userEntityToUpdate = userRepository.findById(userId).orElseThrow();
        userEntityToUpdate.setEmail(OTHER_EMAIL);
        UserEntity updatedUserEntity = userRepository.saveAndFlush(userEntityToUpdate);

        assertTrue(updatedUserEntity.getUpdatedAt().isAfter(originalUpdatedAt),
                () -> String.format("Expected updated_at (%s) to be after original (%s)",
                        updatedUserEntity.getUpdatedAt(), originalUpdatedAt));

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

        UserEntity userEntity1 = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        UserEntity userEntity2 = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        UserEntity userEntity3 = new UserEntity(THIRD_EMAIL, PASSWORD_HASH);

        userRepository.save(userEntity1);
        userRepository.save(userEntity2);
        userRepository.save(userEntity3);

        List<UserEntity> allUserEntities = userRepository.findAll();

        assertEquals(3, allUserEntities.size());

        logger.debug("Test passed: Found {} users", allUserEntities.size());
    }

    /**
     * Tests counting all users.
     */
    @Test
    @DisplayName("Should count all users")
    void testCount() {
        logger.debug("Test: Counting all users");

        UserEntity userEntity1 = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        UserEntity userEntity2 = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);

        userRepository.save(userEntity1);
        userRepository.save(userEntity2);

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

        UserEntity userEntity1 = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        UserEntity userEntity2 = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);

        userRepository.save(userEntity1);
        userRepository.save(userEntity2);

        userRepository.deleteAll();

        assertEquals(0, userRepository.count());

        logger.debug("Test passed: All users deleted");
    }
}