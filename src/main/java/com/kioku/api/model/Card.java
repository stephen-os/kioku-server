package com.kioku.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents a flashcard within a deck.
 *
 * @author Stephen Watson
 */
@Entity
public class Card {

    /**
     * The unique identifier for this card.
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Front of the card (question/prompt).
     * Maximum 500 characters.
     */
    @NotBlank(message = "Front text is required")
    @Size(max = 500, message = "Front text must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String front;

    /**
     * Back of the card (answer/translation).
     * Maximum 500 characters.
     */
    @NotBlank(message = "Back text is required")
    @Size(max = 500, message = "Back text must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String back;

    /**
     * Optional notes for additional context.
     * Maximum 1000 characters.
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    /**
     * Content type for the front of the card.
     * Determines how the frontend should render the content.
     * Defaults to TEXT.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "front_type", length = 10, nullable = false)
    private ContentType frontType = ContentType.TEXT;

    /**
     * Content type for the back of the card.
     * Determines how the frontend should render the content.
     * Defaults to TEXT.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "back_type", length = 10, nullable = false)
    private ContentType backType = ContentType.TEXT;

    /**
     * Programming language for the front of the card.
     * Only applicable when frontType is CODE.
     * Used for syntax highlighting on the frontend.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "front_language", length = 20)
    private CodeLanguage frontLanguage;

    /**
     * Programming language for the back of the card.
     * Only applicable when backType is CODE.
     * Used for syntax highlighting on the frontend.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "back_language", length = 20)
    private CodeLanguage backLanguage;

    /**
     * The deck ID this card belongs to.
     * Read-only mapping to support JPQL queries with eager tag loading.
     * The actual relationship is managed by Deck.addCard().
     */
    @Column(name = "deck_id", insertable = false, updatable = false)
    private Long deckId;

    /**
     * Tags associated with this card for organization.
     * Bidirectional many-to-many relationship.
     * Eagerly loaded since tags are almost always needed when displaying cards.
     */
    @ManyToMany(fetch = FetchType.EAGER)
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
    protected Card() {
    }

    /**
     * Creates a new card with required front and back text.
     *
     * @param front the front text (question/prompt)
     * @param back  the back text (answer/translation)
     * @throws IllegalArgumentException if front or back is null/empty
     */
    public Card(String front, String back) {
        if (front == null || front.trim().isEmpty()) throw new IllegalArgumentException("Front text cannot be null or empty");
        if (back == null || back.trim().isEmpty()) throw new IllegalArgumentException("Back text cannot be null or empty");

        this.front = front.trim();
        this.back = back.trim();
    }

    /**
     * Gets the card's unique identifier.
     *
     * @return the card ID, or {@code null} if not yet persisted
     */
    public Long getCardId() {
        return id;
    }

    /**
     * Gets the deck ID this card belongs to.
     *
     * @return the deck ID, or {@code null} if not yet persisted
     */
    public Long getDeckId() {
        return deckId;
    }

    /**
     * Gets the front text of the card.
     *
     * @return the front text
     */
    public String getFront() {
        return front;
    }

    /**
     * Sets the front text of the card.
     *
     * @param front the front text
     * @throws IllegalArgumentException if front is null or empty
     */
    public void setFront(String front) {
        if (front == null || front.trim().isEmpty()) throw new IllegalArgumentException("Front text cannot be null or empty");
        this.front = front.trim();
    }

    /**
     * Gets the back text of the card.
     *
     * @return the back text
     */
    public String getBack() {
        return back;
    }

    /**
     * Sets the back text of the card.
     *
     * @param back the back text
     * @throws IllegalArgumentException if back is null or empty
     */
    public void setBack(String back) {
        if (back == null || back.trim().isEmpty()) throw new IllegalArgumentException("Back text cannot be null or empty");
        this.back = back.trim();
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
     * Gets the content type for the front of the card.
     *
     * @return the front content type
     */
    public ContentType getFrontType() {
        return frontType;
    }

    /**
     * Sets the content type for the front of the card.
     *
     * @param frontType the front content type
     */
    public void setFrontType(ContentType frontType) {
        this.frontType = frontType != null ? frontType : ContentType.TEXT;
    }

    /**
     * Gets the content type for the back of the card.
     *
     * @return the back content type
     */
    public ContentType getBackType() {
        return backType;
    }

    /**
     * Sets the content type for the back of the card.
     *
     * @param backType the back content type
     */
    public void setBackType(ContentType backType) {
        this.backType = backType != null ? backType : ContentType.TEXT;
    }

    /**
     * Gets the programming language for the front of the card.
     *
     * @return the front language, or {@code null} if not applicable
     */
    public CodeLanguage getFrontLanguage() {
        return frontLanguage;
    }

    /**
     * Sets the programming language for the front of the card.
     *
     * @param frontLanguage the front language
     */
    public void setFrontLanguage(CodeLanguage frontLanguage) {
        this.frontLanguage = frontLanguage;
    }

    /**
     * Gets the programming language for the back of the card.
     *
     * @return the back language, or {@code null} if not applicable
     */
    public CodeLanguage getBackLanguage() {
        return backLanguage;
    }

    /**
     * Sets the programming language for the back of the card.
     *
     * @param backLanguage the back language
     */
    public void setBackLanguage(CodeLanguage backLanguage) {
        this.backLanguage = backLanguage;
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
     * Adds a tag to this card.
     *
     * Maintains bidirectional relationship by also adding this card
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
     * Maintains bidirectional relationship by also removing this card
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
     * Maintains bidirectional relationship by removing this card
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
     * Two cards are equal if they have the same ID.
     * Cards without IDs are only equal to themselves and
     * other cards without IDs.
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
     * Based on the card's ID. Cards without IDs use identity hash code.
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
     * Includes front, back, and metadata but excludes sensitive information
     * from related entities (deck user details, etc.).
     *
     * @return a string representation of this card
     */
    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", front='" + front + '\'' +
                ", frontType=" + frontType +
                ", frontLanguage=" + frontLanguage +
                ", back='" + back + '\'' +
                ", backType=" + backType +
                ", backLanguage=" + backLanguage +
                ", notes='" + notes + '\'' +
                ", tagCount=" + (tags != null ? tags.size() : 0) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}