package com.kioku.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a flashcard within a deck.
 *
 * <p>A card contains:
 * <ul>
 *   <li>Front text (question/prompt)</li>
 *   <li>Back text (answer/translation)</li>
 *   <li>Optional notes for additional context</li>
 *   <li>Association with a parent deck</li>
 *   <li>Optional tags for organization</li>
 * </ul>
 *
 * <p><strong>Bidirectional Relationships:</strong>
 * <ul>
 *   <li>Many-to-One with {@link Deck} (each card belongs to one deck)</li>
 *   <li>Many-to-Many with {@link Tag} (cards can have multiple tags)</li>
 * </ul>
 *
 * <p><strong>Edit Support:</strong>
 * All content fields (front, back, notes) are editable. Use setters to update
 * card content. The {@code updatedAt} timestamp is automatically updated on save.
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
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_deck_id", columnList = "deck_id"),
        @Index(name = "idx_card_created_at", columnList = "created_at")
})
public class Card {

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
     * The deck this card belongs to.
     * Cannot be null - every card must belong to a deck.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    /**
     * Front of the card (question/prompt).
     * Maximum 500 characters.
     */
    @Column(nullable = false, length = 500)
    private String front;

    /**
     * Back of the card (answer/translation).
     * Maximum 500 characters.
     */
    @Column(nullable = false, length = 500)
    private String back;

    /**
     * Optional notes for additional context.
     * Maximum 1000 characters.
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Tags associated with this card for organization.
     * Bidirectional many-to-many relationship.
     */
    @ManyToMany
    @JoinTable(
            name = "card_tags",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * Timestamp when the card was created.
     * Set automatically on first save, immutable thereafter.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the card was last updated.
     * Updated automatically on every save.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
    public Card() {
    }

    /**
     * Creates a new card with required fields.
     *
     * @param deck the deck this card belongs to
     * @param front the front text (question/prompt)
     * @param back the back text (answer/translation)
     * @throws IllegalArgumentException if deck, front, or back is null
     */
    public Card(Deck deck, String front, String back) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        if (front == null || front.trim().isEmpty()) {
            throw new IllegalArgumentException("Front text cannot be null or empty");
        }
        if (back == null || back.trim().isEmpty()) {
            throw new IllegalArgumentException("Back text cannot be null or empty");
        }
        this.deck = deck;
        this.front = front;
        this.back = back;
    }

    /**
     * Creates a new card with optional notes.
     *
     * @param deck the deck this card belongs to
     * @param front the front text (question/prompt)
     * @param back the back text (answer/translation)
     * @param notes optional notes for additional context
     * @throws IllegalArgumentException if deck, front, or back is null
     */
    public Card(Deck deck, String front, String back, String notes) {
        this(deck, front, back);
        this.notes = notes;
    }

    // Getters and Setters

    /**
     * Gets the card's unique identifier.
     *
     * @return the card ID, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the card's unique identifier.
     * Should only be used by JPA.
     *
     * @param id the card ID
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
     * Gets the deck this card belongs to.
     *
     * @return the parent deck
     */
    public Deck getDeck() {
        return deck;
    }

    /**
     * Sets the deck this card belongs to.
     *
     * <p><strong>Note:</strong> Changing the deck does not automatically
     * update the old deck's card collection. Manage both sides of the
     * relationship manually.
     *
     * @param deck the parent deck
     * @throws IllegalArgumentException if deck is null
     */
    public void setDeck(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        this.deck = deck;
    }

    /**
     * Gets the front text (question/prompt).
     *
     * @return the front text
     */
    public String getFront() {
        return front;
    }

    /**
     * Sets the front text (question/prompt).
     *
     * @param front the front text
     * @throws IllegalArgumentException if front is null or empty
     */
    public void setFront(String front) {
        if (front == null || front.trim().isEmpty()) {
            throw new IllegalArgumentException("Front text cannot be null or empty");
        }
        this.front = front;
    }

    /**
     * Gets the back text (answer/translation).
     *
     * @return the back text
     */
    public String getBack() {
        return back;
    }

    /**
     * Sets the back text (answer/translation).
     *
     * @param back the back text
     * @throws IllegalArgumentException if back is null or empty
     */
    public void setBack(String back) {
        if (back == null || back.trim().isEmpty()) {
            throw new IllegalArgumentException("Back text cannot be null or empty");
        }
        this.back = back;
    }

    /**
     * Gets the optional notes.
     *
     * @return the notes, or {@code null} if not set
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the optional notes.
     *
     * @param notes the notes, or {@code null} to clear
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Gets all tags associated with this card.
     *
     * @return an unmodifiable set of tags
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Sets the tags for this card.
     *
     * <p><strong>Warning:</strong> This replaces all existing tags.
     * Use {@link #addTag(Tag)} or {@link #removeTag(Tag)} to modify
     * individual tags while maintaining bidirectional consistency.
     *
     * @param tags the new set of tags
     */
    public void setTags(Set<Tag> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    /**
     * Adds a tag to this card.
     *
     * <p>Maintains bidirectional relationship by also adding this card
     * to the tag's card collection.
     *
     * @param tag the tag to add
     * @throws IllegalArgumentException if tag is null
     */
    public void addTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        this.tags.add(tag);
        tag.getCards().add(this);
    }

    /**
     * Removes a tag from this card.
     *
     * <p>Maintains bidirectional relationship by also removing this card
     * from the tag's card collection.
     *
     * @param tag the tag to remove
     * @throws IllegalArgumentException if tag is null
     */
    public void removeTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        this.tags.remove(tag);
        tag.getCards().remove(this);
    }

    /**
     * Clears all tags from this card.
     *
     * <p>Maintains bidirectional relationship by removing this card
     * from all tag collections.
     */
    public void clearTags() {
        for (Tag tag : new HashSet<>(tags)) {
            removeTag(tag);
        }
    }

    /**
     * Gets the timestamp when this card was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the timestamp when this card was last updated.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Checks if this card equals another object.
     *
     * <p>Two cards are equal if they have the same ID.
     * Cards without IDs are only equal to themselves.
     *
     * @param o the object to compare
     * @return {@code true} if equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return id != null && Objects.equals(id, card.id);
    }

    /**
     * Returns the hash code for this card.
     *
     * <p>Based on the card's ID. Cards without IDs use identity hash code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }

    /**
     * Returns a string representation of this card.
     *
     * <p>Includes front, back, and metadata but excludes sensitive information
     * from related entities (deck user details, etc.).
     *
     * @return a string representation of this card
     */
    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", front='" + front + '\'' +
                ", back='" + back + '\'' +
                ", notes='" + notes + '\'' +
                ", tagCount=" + (tags != null ? tags.size() : 0) +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}