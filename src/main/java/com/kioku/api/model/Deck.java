package com.kioku.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a flashcard deck containing cards and tags.
 *
 * @author Stephen Watson
 */
@Entity
@Table(name = "decks",
        indexes = {
                @Index(name = "idx_deck_created_at", columnList = "created_at")
        }
)
public class Deck {

    /**
     * The unique identifier for this deck.
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The deck name.
     * Maximum 255 characters.
     */
    @NotBlank(message = "Deck name is required")
    @Size(max = 255, message = "Deck name must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Description of the deck's purpose or contents.
     * Never null - empty string if not set. Maximum 1000 characters.
     */
    @Size(max = 1000, message = "Deck description must not exceed 1000 characters")
    @Column(length = 1000, nullable = false)
    private String description = "";

    /**
     * Cards belonging to this deck.
     * Unidirectional relationship - cards don't know their deck.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "deck_id")
    private Set<Card> cards = new HashSet<>();

    /**
     * Tags defined for this deck.
     * Tags are deck-scoped and can be applied to cards.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "deck_id")
    private Set<Tag> tags = new HashSet<>();

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
    protected Deck() {
    }

    /**
     * Creates a new deck with a name.
     *
     * @param name the deck name
     * @throws IllegalArgumentException if name is null or empty
     */
    public Deck(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name cannot be null or empty");
        }
        this.name = name.trim();
    }

    /**
     * Creates a new deck with a name and description.
     *
     * @param name        the deck name
     * @param description optional description
     * @throws IllegalArgumentException if name is null or empty
     */
    public Deck(String name, String description) {
        this(name);
        setDescription(description);
    }

    /**
     * Gets the deck's unique identifier.
     *
     * @return the deck ID, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
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
     * @return the description (never null, may be empty)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the deck description.
     * Null or whitespace-only values are converted to empty string.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        if (description == null) {
            this.description = "";
        } else {
            this.description = description.trim();
        }
    }

    /**
     * Gets the cards in this deck.
     *
     * @return the set of cards
     */
    public Set<Card> getCards() {
        return cards;
    }

    /**
     * Adds a card to this deck.
     *
     * @param card the card to add
     * @throws IllegalArgumentException if card is null
     */
    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        this.cards.add(card);
    }

    /**
     * Removes a card from this deck.
     *
     * @param card the card to remove
     * @throws IllegalArgumentException if card is null
     */
    public void removeCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        this.cards.remove(card);
    }

    /**
     * Gets the number of cards in this deck.
     *
     * @return the card count
     */
    public int getCardCount() {
        return cards.size();
    }

    /**
     * Gets all tags defined for this deck.
     *
     * @return the set of tags
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Adds a tag to this deck.
     *
     * @param tag the tag to add
     * @throws IllegalArgumentException if tag is null
     */
    public void addTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        this.tags.add(tag);
    }

    /**
     * Removes a tag from this deck.
     * Also removes the tag from all cards in this deck.
     *
     * @param tag the tag to remove
     * @throws IllegalArgumentException if tag is null
     */
    public void removeTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        // Remove tag from all cards first
        for (Card card : cards) {
            card.removeTag(tag);
        }
        this.tags.remove(tag);
    }

    /**
     * Gets all tags that are currently applied to at least one card.
     *
     * @return set of tags in use
     */
    public Set<Tag> getTagsInUse() {
        return cards.stream()
                .flatMap(card -> card.getTags().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Gets all tags that are not applied to any card.
     *
     * @return set of unused tags
     */
    public Set<Tag> getUnusedTags() {
        Set<Tag> inUse = getTagsInUse();
        return tags.stream()
                .filter(tag -> !inUse.contains(tag))
                .collect(Collectors.toSet());
    }

    /**
     * Checks if a tag is currently applied to any card in this deck.
     *
     * @param tag the tag to check
     * @return {@code true} if the tag is in use, {@code false} otherwise
     */
    public boolean isTagInUse(Tag tag) {
        if (tag == null) {
            return false;
        }
        return cards.stream()
                .anyMatch(card -> card.getTags().contains(tag));
    }

    /**
     * Removes all tags that are not applied to any card.
     *
     * @return the set of tags that were removed
     */
    public Set<Tag> removeUnusedTags() {
        Set<Tag> unused = getUnusedTags();
        tags.removeAll(unused);
        return unused;
    }

    /**
     * Creates a new tag for this deck.
     *
     * @param name the tag name
     * @return the created tag
     * @throws IllegalArgumentException if name is null/empty or tag already exists
     */
    public Tag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
        String trimmedName = name.trim();

        // Check for duplicate
        boolean exists = tags.stream()
                .anyMatch(t -> t.getName().equalsIgnoreCase(trimmedName));
        if (exists) {
            throw new IllegalArgumentException("Tag with name '" + trimmedName + "' already exists in this deck");
        }

        Tag tag = new Tag(trimmedName);
        tags.add(tag);
        return tag;
    }

    /**
     * Finds a tag by name in this deck.
     *
     * @param name the tag name to find
     * @return the tag, or {@code null} if not found
     */
    public Tag findTagByName(String name) {
        if (name == null) {
            return null;
        }
        return tags.stream()
                .filter(t -> t.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
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
     * Checks if this deck equals another object.
     *
     * Two decks are equal if they have the same ID.
     *
     * @param o the object to compare
     * @return {@code true} if equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deck deck = (Deck) o;
        return id != null && Objects.equals(id, deck.id);
    }

    /**
     * Returns the hash code for this deck.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }

    /**
     * Returns a string representation of this deck.
     *
     * @return a string representation of this deck
     */
    @Override
    public String toString() {
        return "Deck{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cardCount=" + cards.size() +
                ", tagCount=" + tags.size() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
