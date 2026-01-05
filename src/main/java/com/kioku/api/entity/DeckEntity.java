package com.kioku.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a flashcard deck.
 *
 * <p>A deck is a collection of flashcards ({@link CardEntity}) that:
 * <ul>
 *   <li>Belongs to a single user</li>
 *   <li>Has a unique name per user</li>
 *   <li>Contains cards for studying</li>
 *   <li>Can have deck-specific tags for organization</li>
 * </ul>
 *
 * <p><strong>Naming:</strong> Deck names must be unique per user. Different
 * users can have decks with the same name.
 *
 * <p><strong>Relationships:</strong>
 * <ul>
 *   <li>Many-to-One with {@link UserEntity} (each deck belongs to one user)</li>
 *   <li>One-to-Many with {@link CardEntity} (deck contains multiple cards)</li>
 *   <li>One-to-Many with {@link TagEntity} (deck can have multiple tags)</li>
 * </ul>
 *
 * <p><strong>Edit Support:</strong>
 * Name and description are editable. Use setters to update deck properties.
 * The {@code updatedAt} timestamp is automatically updated on save.
 *
 * <p><strong>Timestamps:</strong>
 * <ul>
 *   <li>{@code createdAt}: Set automatically on first save (immutable)</li>
 *   <li>{@code updatedAt}: Updated automatically on every save</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "decks",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "name"})
        },
        indexes = {
                @Index(name = "idx_deck_user_id", columnList = "user_id"),
                @Index(name = "idx_deck_created_at", columnList = "created_at")
        }
)
public class DeckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Version field for optimistic locking.
     * Prevents concurrent modification conflicts.
     */
    @Version
    private Long version;

    /**
     * The user who owns this deck.
     * Cannot be null - every deck must belong to a user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /**
     * The deck name.
     * Must be unique per user, maximum 255 characters.
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Optional description of the deck's purpose or contents.
     * Maximum 1000 characters.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Timestamp when the deck was created.
     * Set automatically on first save, immutable thereafter.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the deck was last updated.
     * Updated automatically on every save.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Cards belonging to this deck.
     * Cascade ALL means when deck is deleted, all cards are deleted too.
     * orphanRemoval ensures cards removed from the collection are also deleted.
     */
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CardEntity> cards = new HashSet<>();

    /**
     * Tags belonging to this deck.
     * Cascade ALL means when deck is deleted, all tags are deleted too.
     * orphanRemoval ensures tags removed from the collection are also deleted.
     */
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TagEntity> tags = new HashSet<>();

    /**
     * JPA lifecycle callback - sets timestamps on creation.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback - updates timestamp on modification.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Default constructor for JPA.
     */
    public DeckEntity() {
    }

    /**
     * Creates a new deck with required fields.
     *
     * @param user the user who owns this deck
     * @param name the deck name
     * @param description optional description
     * @throws IllegalArgumentException if user or name is null/empty
     */
    public DeckEntity(UserEntity user, String name, String description) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }
        this.user = user;
        this.name = name.trim();

        if (description != null) {
            String trimmed = description.trim();
            this.description = trimmed.isEmpty() ? null : trimmed;
        } else {
            this.description = null;
        }
    }

    // Getters and Setters

    /**
     * Gets the deck's unique identifier.
     *
     * @return the deck ID, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the deck's unique identifier.
     * Should only be used by JPA.
     *
     * @param id the deck ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the version for optimistic locking.
     *
     * @return the version number
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version for optimistic locking.
     * Should only be used by JPA.
     *
     * @param version the version number
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Gets the user who owns this deck.
     *
     * @return the owner user
     */
    public UserEntity getUser() {
        return user;
    }

    /**
     * Sets the user who owns this deck.
     *
     * <p><strong>Warning:</strong> Changing deck ownership can have
     * security implications. Ensure proper authorization before changing.
     *
     * @param user the owner user
     * @throws IllegalArgumentException if user is null
     */
    public void setUser(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
    }

    /**
     * Gets the deck name.
     *
     * @return the deck name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the deck name.
     *
     * <p><strong>Note:</strong> The name must be unique per user.
     * Service layer should verify uniqueness before saving.
     *
     * @param name the deck name
     * @throws IllegalArgumentException if name is null or empty
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }
        this.name = name.trim();
    }

    /**
     * Gets the deck description.
     *
     * @return the description, or {@code null} if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the deck description.
     *
     * @param description the description, or {@code null} to clear
     */
    public void setDescription(String description) {
        if (description != null) {
            String trimmed = description.trim();
            this.description = trimmed.isEmpty() ? null : trimmed;
        } else {
            this.description = null;
        }
    }

    /**
     * Gets the timestamp when this deck was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the timestamp when this deck was last updated.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Gets the cards in this deck.
     *
     * @return the set of cards (never null)
     */
    public Set<CardEntity> getCards() {
        return cards;
    }

    /**
     * Gets the tags in this deck.
     *
     * @return the set of tags (never null)
     */
    public Set<TagEntity> getTags() {
        return tags;
    }

    /**
     * Checks if this deck equals another object.
     *
     * <p>Two decks are equal if they have the same ID and name.
     * Decks without IDs are only equal to themselves.
     *
     * @param o the object to compare
     * @return {@code true} if equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeckEntity deck = (DeckEntity) o;
        return id != null && Objects.equals(id, deck.id) && Objects.equals(name, deck.name);
    }

    /**
     * Returns the hash code for this deck.
     *
     * <p>Based on the deck's ID and name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id, name) : super.hashCode();
    }

    /**
     * Returns a string representation of this deck.
     *
     * <p>Includes deck details but excludes user email/password for security.
     *
     * @return a string representation of this deck
     */
    @Override
    public String toString() {
        return "Deck{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", userId=" + (user != null ? user.getId() : null) +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}