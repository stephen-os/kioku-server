package com.kioku.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a user account.
 *
 * <p>A user account contains:
 * <ul>
 *   <li>Email address (unique, normalized to lowercase)</li>
 *   <li>Password hash (bcrypt, 60 characters)</li>
 *   <li>Email verification status and tokens</li>
 *   <li>Password reset tokens</li>
 *   <li>Account status (ACTIVE, SUSPENDED, DELETED, PENDING_VERIFICATION)</li>
 *   <li>Security features (failed login tracking, account locking)</li>
 *   <li>Ownership of multiple decks</li>
 * </ul>
 *
 * <p><strong>Bidirectional Relationships:</strong>
 * <ul>
 *   <li>One-to-Many with {@link Deck} (user owns multiple decks)</li>
 * </ul>
 *
 * <p><strong>Security Features:</strong>
 * <ul>
 *   <li>Account locking after 5 failed login attempts (15 minute duration)</li>
 *   <li>Email verification token (expires after 24 hours)</li>
 *   <li>Password reset token (expires after 24 hours)</li>
 *   <li>Soft delete support (preserves data, marks as deleted)</li>
 * </ul>
 *
 * <p><strong>Cascade Operations:</strong>
 * Deleting a user cascades to all owned decks, which cascade to their cards and tags.
 * Removing a deck from the user's collection triggers orphanRemoval.
 *
 * <p><strong>Timestamps:</strong>
 * <ul>
 *   <li>{@code createdAt}: Set automatically on first save (immutable)</li>
 *   <li>{@code updatedAt}: Updated automatically on every save</li>
 *   <li>{@code lastLoginAt}: Updated via {@link #recordSuccessfulLogin()}</li>
 *   <li>{@code deletedAt}: Set via {@link #softDelete()}, cleared via {@link #restore()}</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The email address of this user.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(unique = true, nullable = false)
    private String email;

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
    private LocalDateTime emailVerificationSentAt;

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
    private LocalDateTime passwordResetSentAt;

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
    private LocalDateTime lockedUntil;

    /**
     * The timestamp when this user last logged in.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * The timestamp when this user was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp when this user was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * The timestamp when this user was deleted, if any.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * The version of this entity, used for optimistic locking.
     */
    @Version
    private Long version;

    /**
     * The decks owned by this user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Deck> decks = new HashSet<>();

    /**
     * JPA lifecycle callback executed before persisting a new user.
     *
     * <p>This method:
     * <ul>
     *   <li>Sets the created_at timestamp</li>
     *   <li>Sets the updated_at timestamp</li>
     *   <li>Normalizes the email to lowercase and trims whitespace</li>
     * </ul>
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
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
     * <p>This method:
     * <ul>
     *   <li>Updates the updated_at timestamp</li>
     *   <li>Normalizes the email to lowercase and trims whitespace</li>
     * </ul>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
     * <p>The email is automatically normalized to lowercase and trimmed.
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
     * <p>An account is considered locked if the lockedUntil timestamp is set
     * and is in the future.
     *
     * @return {@code true} if the account is locked, {@code false} otherwise
     */
    public boolean isLocked() {
        boolean locked = lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
        logger.debug("User id={} lock status checked: locked={}, lockedUntil={}",
                id, locked, lockedUntil);
        return locked;
    }

    /**
     * Checks if the user account has been soft-deleted.
     *
     * <p>Soft-deleted accounts have a deletedAt timestamp set but are not
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
     * <p>An account is considered active if:
     * <ul>
     *   <li>Status is ACTIVE</li>
     *   <li>Not currently locked</li>
     *   <li>Not soft-deleted</li>
     * </ul>
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
     * <p>Increments the failed login attempt counter. If the counter reaches 5,
     * the account is automatically locked for 15 minutes.
     *
     * <p><strong>Security Note:</strong> This helps prevent brute-force attacks
     * on user accounts.
     */
    public void recordFailedLoginAttempt() {
        int previousAttempts = failedLoginAttempts;
        failedLoginAttempts++;

        if (failedLoginAttempts >= 5) {
            lockedUntil = LocalDateTime.now().plusMinutes(15);
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
     * <p>This method:
     * <ul>
     *   <li>Resets the failed login attempt counter to 0</li>
     *   <li>Clears any account lock</li>
     *   <li>Updates the last login timestamp</li>
     * </ul>
     */
    public void recordSuccessfulLogin() {
        int previousAttempts = failedLoginAttempts;
        failedLoginAttempts = 0;
        lockedUntil = null;
        lastLoginAt = LocalDateTime.now();

        logger.debug("User id={} successful login recorded: failedAttempts {} -> 0, lastLoginAt={}",
                id, previousAttempts, lastLoginAt);
    }

    /**
     * Soft deletes this user account.
     *
     * <p>Sets the deletedAt timestamp and changes status to DELETED.
     * The user data remains in the database but is marked as deleted.
     *
     * <p><strong>Note:</strong> Soft-deleted accounts can be restored using
     * {@link #restore()}.
     */
    public void softDelete() {
        UserStatus previousStatus = status;
        deletedAt = LocalDateTime.now();
        status = UserStatus.DELETED;

        logger.debug("User id={} soft deleted: status {} -> {}, deletedAt={}",
                id, previousStatus, status, deletedAt);
    }

    /**
     * Restores a soft-deleted user account.
     *
     * <p>Clears the deletedAt timestamp and sets status back to ACTIVE.
     *
     * <p><strong>Note:</strong> This does not restore other account states
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
     * <p>This token is used to verify the user's email address. The token
     * should be sent to the user's email and expires after 24 hours.
     *
     * @param token the verification token (typically a UUID or secure random string)
     */
    public void setEmailVerificationToken(String token) {
        this.emailVerificationToken = token;
        this.emailVerificationSentAt = LocalDateTime.now();

        logger.debug("User id={} email verification token set, sentAt={}",
                id, emailVerificationSentAt);
    }

    /**
     * Marks the user's email as verified.
     *
     * <p>This method:
     * <ul>
     *   <li>Sets emailVerified to true</li>
     *   <li>Clears the verification token</li>
     *   <li>Clears the token sent timestamp</li>
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
     * <p>This token is used to allow the user to reset their password.
     * The token expires after 24 hours.
     *
     * @param token the password reset token (typically a UUID or secure random string)
     */
    public void setPasswordResetToken(String token) {
        this.passwordResetToken = token;
        this.passwordResetSentAt = LocalDateTime.now();

        logger.debug("User id={} password reset token set, sentAt={}",
                id, passwordResetSentAt);
    }

    /**
     * Clears the password reset token after it has been used.
     *
     * <p>This should be called after a successful password reset to prevent
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
     * <p>Password reset tokens expire 24 hours after being sent.
     *
     * @return {@code true} if the token is expired or not set, {@code false} if still valid
     */
    public boolean isPasswordResetTokenExpired() {
        if (passwordResetSentAt == null) {
            logger.debug("User id={} password reset token check: no token sent", id);
            return true;
        }

        boolean expired = passwordResetSentAt.plusHours(24).isBefore(LocalDateTime.now());
        logger.debug("User id={} password reset token expired: {}, sentAt={}, expiresAt={}",
                id, expired, passwordResetSentAt, passwordResetSentAt.plusHours(24));

        return expired;
    }

    /**
     * Checks if the email verification token has expired.
     *
     * <p>Email verification tokens expire 24 hours after being sent.
     *
     * @return {@code true} if the token is expired or not set, {@code false} if still valid
     */
    public boolean isEmailVerificationTokenExpired() {
        if (emailVerificationSentAt == null) {
            logger.debug("User id={} email verification token check: no token sent", id);
            return true;
        }

        boolean expired = emailVerificationSentAt.plusHours(24).isBefore(LocalDateTime.now());
        logger.debug("User id={} email verification token expired: {}, sentAt={}, expiresAt={}",
                id, expired, emailVerificationSentAt, emailVerificationSentAt.plusHours(24));

        return expired;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for this user.
     *
     * @return the user ID, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this user.
     *
     * <p><strong>Note:</strong> This should typically only be called by JPA.
     *
     * @param id the user ID
     */
    public void setId(Long id) {
        logger.debug("Setting user ID: {} -> {}", this.id, id);
        this.id = id;
    }

    /**
     * Gets the user's email address.
     *
     * <p>Email addresses are stored in lowercase.
     *
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * <p>The email is automatically normalized to lowercase and trimmed.
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
     * <p><strong>Note:</strong> Use {@link #verifyEmail()} instead to properly
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
     * <p><strong>Security Note:</strong> This token should be treated as sensitive
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
    public LocalDateTime getEmailVerificationSentAt() {
        return emailVerificationSentAt;
    }

    /**
     * Gets the bcrypt password hash for this user.
     *
     * <p><strong>Security Warning:</strong> This method must only be used by
     * the service layer for password verification. The password hash must
     * NEVER be exposed in:
     * <ul>
     *   <li>API responses or DTOs (use @JsonIgnore)</li>
     *   <li>Logs or toString() methods</li>
     *   <li>Error messages</li>
     * </ul>
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
     * <p><strong>Security Warning:</strong> Passwords should only be set
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
     * <p><strong>Security Note:</strong> This token should be treated as sensitive
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
    public LocalDateTime getPasswordResetSentAt() {
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
     * <p><strong>Note:</strong> Use {@link #recordFailedLoginAttempt()} or
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
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    /**
     * Sets the timestamp until which this account should be locked.
     *
     * @param lockedUntil the lock expiration timestamp
     */
    public void setLockedUntil(LocalDateTime lockedUntil) {
        LocalDateTime previous = this.lockedUntil;
        this.lockedUntil = lockedUntil;
        logger.debug("User id={} lockedUntil changed: {} -> {}", id, previous, lockedUntil);
    }

    /**
     * Gets the timestamp of the user's last successful login.
     *
     * @return the last login timestamp, or {@code null} if never logged in
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Sets the timestamp of the user's last login.
     *
     * <p><strong>Note:</strong> Use {@link #recordSuccessfulLogin()} instead
     * to properly record a login.
     *
     * @param lastLoginAt the last login timestamp
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        LocalDateTime previous = this.lastLoginAt;
        this.lastLoginAt = lastLoginAt;
        logger.debug("User id={} lastLoginAt changed: {} -> {}", id, previous, lastLoginAt);
    }

    /**
     * Gets the timestamp when this user was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the timestamp when this user was last updated.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Gets the timestamp when this user was soft-deleted.
     *
     * @return the deletion timestamp, or {@code null} if not deleted
     */
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    /**
     * Gets the version number for optimistic locking.
     *
     * <p>This is managed by JPA and incremented on each update to prevent
     * concurrent modification conflicts.
     *
     * @return the version number
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Gets all decks owned by this user.
     *
     * @return the set of decks (never null)
     */
    public Set<Deck> getDecks() {
        return decks;
    }

    /**
     * Adds a deck to this user's collection.
     *
     * <p>Maintains bidirectional relationship by also setting this user
     * as the deck's owner.
     *
     * @param deck the deck to add
     * @throws IllegalArgumentException if deck is null
     * @throws IllegalStateException if deck already belongs to another user
     */
    public void addDeck(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        if (deck.getUser() != null && deck.getUser() != this) {
            throw new IllegalStateException("Deck already belongs to another user");
        }
        this.decks.add(deck);
        if (deck.getUser() != this) {
            deck.setUser(this);
        }
        logger.debug("UserEntity id={} added deck id={}", id, deck.getId());
    }

    /**
     * Removes a deck from this user's collection.
     *
     * <p>Maintains bidirectional relationship. Due to orphanRemoval=true,
     * the removed deck will be deleted from the database.
     *
     * @param deck the deck to remove
     * @throws IllegalArgumentException if deck is null
     */
    public void removeDeck(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        this.decks.remove(deck);

        logger.debug("UserEntity id={} removed deck id={}", id, deck.getId());
    }

    /**
     * Compares this user to another object for equality.
     *
     * <p>Two users are considered equal if they have the same ID.
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
     * <p>The hash code is based solely on the user ID.
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
     * <p><strong>Security Note:</strong> This does not include sensitive
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