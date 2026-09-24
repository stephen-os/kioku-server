package com.kioku.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Model representing a user account.
 * 
 * @author Stephen Watson
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_status", columnList = "status"),
                @Index(name = "idx_user_created_at", columnList = "created_at")
        }
)
public class User {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    /**
     * The unique identifier for this user.
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * The email address of this user.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(unique = true, nullable = false)
    private String email;

    /** How this account presents itself. Falls back to the email when unset. */
    @Column(name = "display_name", length = 100)
    private String displayName;

    /** Identifier of a built-in avatar, resolved to an image by the client. */
    @Column(nullable = false, length = 50)
    private String avatar = "avatar-smile";

    /**
     * Whether this user's email address has been verified.
     */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /**
     * The token used to verify this user's email address, if any.
     */
    @Column(name = "email_verification_token")
    @Size(max = 255, message = "Email verification token must not exceed 255 characters")
    private String emailVerificationToken;

    /**
     * The timestamp when this user's email verification was sent, if any.
     */
    @Column(name = "email_verification_sent_at")
    private Instant emailVerificationSentAt;

    /**
     * The new email address awaiting verification during an email change.
     */
    @Email(message = "Pending email must be valid")
    @Size(max = 255, message = "Pending email must not exceed 255 characters")
    @Column(name = "pending_email")
    private String pendingEmail;

    /**
     * The token used to verify the pending email change.
     */
    @Column(name = "pending_email_token")
    @Size(max = 255, message = "Pending email token must not exceed 255 characters")
    private String pendingEmailToken;

    /**
     * The timestamp when the pending email verification was sent.
     */
    @Column(name = "pending_email_sent_at")
    private Instant pendingEmailSentAt;

    /**
     * The hashed password for this user.
     */
    @NotBlank(message = "Password hash is required")
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    /**
     * The token used to reset this user's password, if any.
     */
    @Column(name = "password_reset_token")
    @Size(max = 255, message = "Password reset token must not exceed 255 characters")
    private String passwordResetToken;

    /**
     * The timestamp when this user account was reset, if any.
     */
    @Column(name = "password_reset_sent_at")
    private Instant passwordResetSentAt;

    /**
     * The status of this user account.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * The number of failed login attempts for this user.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    /**
     * The timestamp when this user account was locked, if any.
     */
    @Column(name = "locked_until")
    private Instant lockedUntil;

    /**
     * The timestamp when this user last logged in.
     */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * The timestamp when this user was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * The timestamp when this user was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * The timestamp when this user was deleted, if any.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * The version of this entity, used for optimistic locking.
     */
    @Version
    private Long version;

    /**
     * JPA lifecycle callback executed before persisting a new user.
     *
     * This method:
     * Sets the created_at timestamp
     * Sets the updated_at timestamp
     * Normalizes the email to lowercase and trims whitespace
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (email != null) {
            email = email.toLowerCase().trim();
        }

        logger.debug("PrePersist: Creating new user with status={}", status);
    }

    /**
     * JPA lifecycle callback executed before updating an existing user.
     *
     * This method:
     * Updates the updated_at timestamp
     * Normalizes the email to lowercase and trims whitespace
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (email != null) {
            email = email.toLowerCase().trim();
        }

        logger.debug("PreUpdate: Updating user id={}, status={}, emailVerified={}",
                id, status, emailVerified);
    }

    /**
     * Default constructor required by JPA.
     */
    public User() {
        logger.debug("Creating new User instance (no-args constructor)");
    }

    /**
     * Constructs a new User with the specified email and password hash.
     *
     * The email is automatically normalized to lowercase and trimmed.
     *
     * @param email the user's email address (will be normalized)
     * @param passwordHash the bcrypt hashed password (should be 60 characters)
     */
    public User(String email, String passwordHash) {
        this.email = email != null ? email.toLowerCase().trim() : null;
        this.passwordHash = passwordHash;

        logger.debug("Creating new User with constructor");
    }

    /**
     * Checks if the user account is currently locked due to failed login attempts.
     *
     * An account is considered locked if the lockedUntil timestamp is set
     * and is in the future.
     *
     * @return {@code true} if the account is locked, {@code false} otherwise
     */
    public boolean isLocked() {
        boolean locked = lockedUntil != null && lockedUntil.isAfter(Instant.now());
        logger.debug("User id={} lock status checked: locked={}, lockedUntil={}",
                id, locked, lockedUntil);
        return locked;
    }

    /**
     * Checks if the user account has been soft-deleted.
     *
     * Soft-deleted accounts have a deletedAt timestamp set but are not
     * physically removed from the database.
     *
     * @return {@code true} if the account is soft-deleted, {@code false} otherwise
     */
    public boolean isDeleted() {
        boolean deleted = deletedAt != null;
        logger.debug("User id={} deletion status checked: deleted={}, deletedAt={}",
                id, deleted, deletedAt);
        return deleted;
    }

    /**
     * Checks if the user account is active and available for use.
     *
     * An account is considered active if:
     * Status is ACTIVE
     * Not currently locked
     * Not soft-deleted
     *
     * @return {@code true} if the account is fully active, {@code false} otherwise
     */
    public boolean isActive() {
        boolean active = status == UserStatus.ACTIVE && !isLocked() && !isDeleted();
        logger.debug("User id={} active status checked: active={}, status={}",
                id, active, status);
        return active;
    }

    /**
     * Records a failed login attempt for this user.
     *
     * Increments the failed login attempt counter. If the counter reaches 5,
     * the account is automatically locked for 15 minutes.
     *
     * Security Note: This helps prevent brute-force attacks
     * on user accounts.
     */
    public void recordFailedLoginAttempt() {
        int previousAttempts = failedLoginAttempts;
        failedLoginAttempts++;

        if (failedLoginAttempts >= 5) {
            lockedUntil = Instant.now().plus(15, ChronoUnit.MINUTES);
            logger.debug("User id={} account locked after {} failed login attempts, lockedUntil={}",
                    id, failedLoginAttempts, lockedUntil);
        } else {
            logger.debug("User id={} failed login attempt recorded: {} -> {}",
                    id, previousAttempts, failedLoginAttempts);
        }
    }

    /**
     * Records a successful login for this user.
     *
     * This method:
     * <ul>
     *   Resets the failed login attempt counter to 0
     *   Clears any account lock
     *   Updates the last login timestamp
     * </ul>
     */
    public void recordSuccessfulLogin() {
        int previousAttempts = failedLoginAttempts;
        failedLoginAttempts = 0;
        lockedUntil = null;
        lastLoginAt = Instant.now();

        logger.debug("User id={} successful login recorded: failedAttempts {} -> 0, lastLoginAt={}",
                id, previousAttempts, lastLoginAt);
    }

    /**
     * Soft deletes this user account.
     *
     * Sets the deletedAt timestamp and changes status to DELETED.
     * The user data remains in the database but is marked as deleted.
     *
     * Note: Soft-deleted accounts can be restored using
     * {@link #restore()}.
     */
    public void softDelete() {
        UserStatus previousStatus = status;
        deletedAt = Instant.now();
        status = UserStatus.DELETED;

        logger.debug("User id={} soft deleted: status {} -> {}, deletedAt={}",
                id, previousStatus, status, deletedAt);
    }

    /**
     * Restores a soft-deleted user account.
     *
     * Clears the deletedAt timestamp and sets status back to ACTIVE.
     *
     * Note: This does not restore other account states
     * like email verification or account locks.
     */
    public void restore() {
        UserStatus previousStatus = status;
        deletedAt = null;
        status = UserStatus.ACTIVE;

        logger.debug("User id={} restored: status {} -> {}, deletedAt cleared",
                id, previousStatus, status);
    }

    /**
     * Sets the email verification token for this user.
     *
     * This token is used to verify the user's email address. The token
     * should be sent to the user's email and expires after 24 hours.
     *
     * @param token the verification token (typically a UUID or secure random string)
     */
    public void setEmailVerificationToken(String token) {
        this.emailVerificationToken = token;
        this.emailVerificationSentAt = Instant.now();

        logger.debug("User id={} email verification token set, sentAt={}",
                id, emailVerificationSentAt);
    }

    /**
     * Marks the user's email as verified.
     *
     * This method:
     * <ul>
     *   Sets emailVerified to true
     *   Clears the verification token
     *   Clears the token sent timestamp
     * </ul>
     */
    public void verifyEmail() {
        boolean previouslyVerified = this.emailVerified;
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationSentAt = null;

        logger.debug("User id={} email verified: emailVerified {} -> true, token cleared",
                id, previouslyVerified);
    }

    /**
     * Sets the password reset token for this user.
     *
     * This token is used to allow the user to reset their password.
     * The token expires after 24 hours.
     *
     * @param token the password reset token (typically a UUID or secure random string)
     */
    public void setPasswordResetToken(String token) {
        this.passwordResetToken = token;
        this.passwordResetSentAt = Instant.now();

        logger.debug("User id={} password reset token set, sentAt={}",
                id, passwordResetSentAt);
    }

    /**
     * Clears the password reset token after it has been used.
     *
     * This should be called after a successful password reset to prevent
     * the token from being reused.
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetSentAt = null;

        logger.debug("User id={} password reset token cleared", id);
    }

    /**
     * Checks if the password reset token has expired.
     *
     * Password reset tokens expire 24 hours after being sent.
     *
     * @return {@code true} if the token is expired or not set, {@code false} if still valid
     */
    public boolean isPasswordResetTokenExpired() {
        if (passwordResetSentAt == null) {
            logger.debug("User id={} password reset token check: no token sent", id);
            return true;
        }

        boolean expired = passwordResetSentAt.plus(24, ChronoUnit.HOURS).isBefore(Instant.now());
        logger.debug("User id={} password reset token expired: {}, sentAt={}, expiresAt={}",
                id, expired, passwordResetSentAt, passwordResetSentAt.plus(24, ChronoUnit.HOURS));

        return expired;
    }

    /**
     * Checks if the email verification token has expired.
     *
     * Email verification tokens expire 24 hours after being sent.
     *
     * @return {@code true} if the token is expired or not set, {@code false} if still valid
     */
    public boolean isEmailVerificationTokenExpired() {
        if (emailVerificationSentAt == null) {
            logger.debug("User id={} email verification token check: no token sent", id);
            return true;
        }

        boolean expired = emailVerificationSentAt.plus(24, ChronoUnit.HOURS).isBefore(Instant.now());
        logger.debug("User id={} email verification token expired: {}, sentAt={}, expiresAt={}",
                id, expired, emailVerificationSentAt, emailVerificationSentAt.plus(24, ChronoUnit.HOURS));

        return expired;
    }

    /**
     * Initiates an email change by storing the pending email and verification token.
     *
     * @param newEmail the new email address to change to
     * @param token the verification token
     */
    public void setPendingEmailChange(String newEmail, String token) {
        this.pendingEmail = newEmail != null ? newEmail.toLowerCase().trim() : null;
        this.pendingEmailToken = token;
        this.pendingEmailSentAt = Instant.now();

        logger.debug("User id={} pending email change initiated to {}, sentAt={}",
                id, pendingEmail, pendingEmailSentAt);
    }

    /**
     * Completes the pending email change by updating the email and clearing pending fields.
     *
     * @return the new email address that was set
     */
    public String confirmPendingEmailChange() {
        String newEmail = this.pendingEmail;
        this.email = this.pendingEmail;
        this.pendingEmail = null;
        this.pendingEmailToken = null;
        this.pendingEmailSentAt = null;

        logger.debug("User id={} email change confirmed to {}", id, email);
        return newEmail;
    }

    /**
     * Clears any pending email change.
     */
    public void clearPendingEmailChange() {
        this.pendingEmail = null;
        this.pendingEmailToken = null;
        this.pendingEmailSentAt = null;

        logger.debug("User id={} pending email change cleared", id);
    }

    /**
     * Checks if the pending email token has expired.
     *
     * Pending email tokens expire 24 hours after being sent.
     *
     * @return {@code true} if the token is expired or not set, {@code false} if still valid
     */
    public boolean isPendingEmailTokenExpired() {
        if (pendingEmailSentAt == null) {
            logger.debug("User id={} pending email token check: no token sent", id);
            return true;
        }

        boolean expired = pendingEmailSentAt.plus(24, ChronoUnit.HOURS).isBefore(Instant.now());
        logger.debug("User id={} pending email token expired: {}, sentAt={}, expiresAt={}",
                id, expired, pendingEmailSentAt, pendingEmailSentAt.plus(24, ChronoUnit.HOURS));

        return expired;
    }

    /**
     * Gets the pending email address awaiting verification.
     *
     * @return the pending email address, or {@code null} if no change is pending
     */
    public String getPendingEmail() {
        return pendingEmail;
    }

    /**
     * Gets the pending email verification token.
     *
     * @return the pending email token, or {@code null} if no change is pending
     */
    public String getPendingEmailToken() {
        return pendingEmailToken;
    }

    /**
     * Gets the timestamp when the pending email verification was sent.
     *
     * @return the timestamp, or {@code null} if no change is pending
     */
    public Instant getPendingEmailSentAt() {
        return pendingEmailSentAt;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for this user.
     *
     * @return the user ID, or {@code null} if not yet persisted
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this user.
     *
     * Note: This should typically only be called by JPA.
     *
     * @param id the user ID
     */
    public void setId(UUID id) {
        logger.debug("Setting user ID: {} -> {}", this.id, id);
        this.id = id;
    }

    /**
     * Gets the user's email address.
     *
     * Email addresses are stored in lowercase.
     *
     * @return the user's email address
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null || displayName.isBlank()
                ? null
                : displayName.trim();
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        if (avatar != null && !avatar.isBlank()) {
            this.avatar = avatar.trim();
        }
    }

    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * The email is automatically normalized to lowercase and trimmed.
     *
     * @param email the user's email address
     */
    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
        logger.debug("User id={} email updated", id);
    }

    /**
     * Checks if the user's email address has been verified.
     *
     * @return {@code true} if email is verified, {@code false} otherwise
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Sets the email verification status.
     *
     * Note: Use {@link #verifyEmail()} instead to properly
     * verify an email and clear the verification token.
     *
     * @param emailVerified {@code true} to mark as verified, {@code false} otherwise
     */
    public void setEmailVerified(boolean emailVerified) {
        boolean previous = this.emailVerified;
        this.emailVerified = emailVerified;
        logger.debug("User id={} emailVerified changed: {} -> {}", id, previous, emailVerified);
    }

    /**
     * Gets the email verification token.
     *
     * Security Note: This token should be treated as sensitive
     * and not exposed in API responses.
     *
     * @return the email verification token, or {@code null} if not set
     */
    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    /**
     * Gets the timestamp when the email verification token was sent.
     *
     * @return the timestamp when the verification token was sent, or {@code null} if not set
     */
    public Instant getEmailVerificationSentAt() {
        return emailVerificationSentAt;
    }

    /**
     * Gets the bcrypt password hash for this user.
     *
     * Security Warning: This method must only be used by
     * the service layer for password verification. The password hash must
     * NEVER be exposed in:
     * API responses or DTOs (use @JsonIgnore)
     * Logs or toString() methods
     * Error messages
     *
     * @return the bcrypt password hash (60 characters)
     */
    public String getPasswordHash() {
        logger.debug("Accessing password hash for user id={}", id);
        return passwordHash;
    }

    /**
     * Sets the bcrypt password hash for this user.
     *
     * Security Warning: Passwords should only be set
     * through UserService which handles proper bcrypt encoding. Direct
     * use of this setter bypasses security measures.
     *
     * @param passwordHash the bcrypt password hash (60 characters)
     */
    public void setPasswordHash(String passwordHash) {
        logger.debug("Setting password hash for user id={}", id);
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the password reset token.
     *
     * Security Note: This token should be treated as sensitive
     * and not exposed in API responses.
     *
     * @return the password reset token, or {@code null} if not set
     */
    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    /**
     * Gets the timestamp when the password reset token was sent.
     *
     * @return the timestamp when the reset token was sent, or {@code null} if not set
     */
    public Instant getPasswordResetSentAt() {
        return passwordResetSentAt;
    }

    /**
     * Gets the current status of this user account.
     *
     * @return the user status (ACTIVE, SUSPENDED, DELETED, or PENDING_VERIFICATION)
     */
    public UserStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of this user account.
     *
     * @param status the new user status
     */
    public void setStatus(UserStatus status) {
        UserStatus previous = this.status;
        this.status = status;
        logger.debug("User id={} status changed: {} -> {}", id, previous, status);
    }

    /**
     * Gets the number of consecutive failed login attempts.
     *
     * @return the failed login attempt count
     */
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    /**
     * Sets the number of failed login attempts.
     *
     * Note: Use {@link #recordFailedLoginAttempt()} or
     * {@link #recordSuccessfulLogin()} instead to properly manage login attempts.
     *
     * @param failedLoginAttempts the failed login attempt count
     */
    public void setFailedLoginAttempts(int failedLoginAttempts) {
        int previous = this.failedLoginAttempts;
        this.failedLoginAttempts = failedLoginAttempts;
        logger.debug("User id={} failedLoginAttempts changed: {} -> {}",
                id, previous, failedLoginAttempts);
    }

    /**
     * Gets the timestamp until which this account is locked.
     *
     * @return the lock expiration timestamp, or {@code null} if not locked
     */
    public Instant getLockedUntil() {
        return lockedUntil;
    }

    /**
     * Sets the timestamp until which this account should be locked.
     *
     * @param lockedUntil the lock expiration timestamp
     */
    public void setLockedUntil(Instant lockedUntil) {
        Instant previous = this.lockedUntil;
        this.lockedUntil = lockedUntil;
        logger.debug("User id={} lockedUntil changed: {} -> {}", id, previous, lockedUntil);
    }

    /**
     * Gets the timestamp of the user's last successful login.
     *
     * @return the last login timestamp, or {@code null} if never logged in
     */
    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Sets the timestamp of the user's last login.
     *
     * Note: Use {@link #recordSuccessfulLogin()} instead
     * to properly record a login.
     *
     * @param lastLoginAt the last login timestamp
     */
    public void setLastLoginAt(Instant lastLoginAt) {
        Instant previous = this.lastLoginAt;
        this.lastLoginAt = lastLoginAt;
        logger.debug("User id={} lastLoginAt changed: {} -> {}", id, previous, lastLoginAt);
    }

    /**
     * Gets the timestamp when this user was created.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the timestamp when this user was last updated.
     *
     * @return the last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Gets the timestamp when this user was soft-deleted.
     *
     * @return the deletion timestamp, or {@code null} if not deleted
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * Gets the version number for optimistic locking.
     *
     * This is managed by JPA and incremented on each update to prevent
     * concurrent modification conflicts.
     *
     * @return the version number
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Compares this user to another object for equality.
     *
     * Two users are considered equal if they have the same ID.
     *
     * @param o the object to compare to
     * @return {@code true} if the users have the same ID, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    /**
     * Generates a hash code for this user.
     *
     * The hash code is based solely on the user ID.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this user.
     *
     * Security Note: This does not include sensitive
     * information like emails, password hashes, or tokens.
     *
     * @return a string representation including ID, status, and verification status
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", emailVerified=" + emailVerified +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    /**
     * Enumeration of possible user account statuses.
     */
    public enum UserStatus {
        /** Account is active and available for use */
        ACTIVE,

        /** Account has been suspended by an administrator */
        SUSPENDED,

        /** Account has been soft-deleted */
        DELETED,

        /** Account is awaiting email verification */
        PENDING_VERIFICATION
    }
}