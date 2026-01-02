package com.kioku.api.repository;

import com.kioku.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserEntity entity database operations.
 *
 * <p>This repository provides methods for:
 * <ul>
 *   <li>Finding users by email, ID, and verification tokens</li>
 *   <li>Checking email existence (case-insensitive)</li>
 *   <li>Finding users by status</li>
 *   <li>Finding locked or deleted accounts</li>
 * </ul>
 *
 * <p>All email queries are case-insensitive to ensure consistent user lookup
 * regardless of how the email is entered.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by email address.
     *
     * <p>Email lookup is case-insensitive. Both the stored email and search
     * parameter are converted to lowercase for comparison.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UserEntity> findByEmail(@Param("email") String email);

    /**
     * Checks if a user with the given email exists.
     *
     * <p>Email lookup is case-insensitive. Both the stored email and search
     * parameter are converted to lowercase for comparison.
     *
     * @param email the email address to check
     * @return {@code true} if a user with this email exists, {@code false} otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Finds a user by email verification token.
     *
     * <p>Used during the email verification process to locate the user
     * who needs to verify their email.
     *
     * @param token the email verification token
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<UserEntity> findByEmailVerificationToken(String token);

    /**
     * Finds a user by password reset token.
     *
     * <p>Used during the password reset process to locate the user
     * who requested a password reset.
     *
     * @param token the password reset token
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<UserEntity> findByPasswordResetToken(String token);

    /**
     * Finds all users with a specific status.
     *
     * @param status the user status to filter by
     * @return a list of users with the given status
     */
    List<UserEntity> findByStatus(UserEntity.UserStatus status);

    /**
     * Finds all currently locked accounts.
     *
     * <p>Returns users whose lockedUntil timestamp is in the future.
     *
     * @param now the current timestamp
     * @return a list of locked users
     */
    @Query("SELECT u FROM UserEntity u WHERE u.lockedUntil > :now")
    List<UserEntity> findLockedAccounts(@Param("now") LocalDateTime now);

    /**
     * Finds all soft-deleted accounts.
     *
     * <p>Returns users whose deletedAt timestamp is not null.
     *
     * @return a list of soft-deleted users
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NOT NULL")
    List<UserEntity> findDeletedAccounts();

    /**
     * Finds all active, non-deleted, non-locked users.
     *
     * <p>A user is considered active if:
     * <ul>
     *   <li>Status is ACTIVE</li>
     *   <li>Not soft-deleted (deletedAt is null)</li>
     *   <li>Not locked (lockedUntil is null or in the past)</li>
     * </ul>
     *
     * @param now the current timestamp
     * @return a list of active users
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' " +
            "AND u.deletedAt IS NULL " +
            "AND (u.lockedUntil IS NULL OR u.lockedUntil < :now)")
    List<UserEntity> findActiveAccounts(@Param("now") LocalDateTime now);

    /**
     * Finds users by email verification status.
     *
     * @param emailVerified {@code true} to find verified users, {@code false} for unverified
     * @return a list of users with the specified verification status
     */
    List<UserEntity> findByEmailVerified(boolean emailVerified);

    /**
     * Counts the number of users created after a specific date.
     *
     * <p>Useful for analytics and reporting on user growth.
     *
     * @param date the date to count from
     * @return the number of users created after the given date
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt > :date")
    long countUsersCreatedAfter(@Param("date") LocalDateTime date);
}