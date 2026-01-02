package com.kioku.api.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a tag for organizing cards within a deck.
 *
 * <p>Tags are deck-specific organizational tools that:
 * <ul>
 *   <li>Belong to a single deck (not shared across decks)</li>
 *   <li>Can be applied to multiple cards within that deck</li>
 *   <li>Have unique names within their deck</li>
 *   <li>Are owned by a user (inherited from deck ownership)</li>
 * </ul>
 *
 * <p><strong>Deck Isolation:</strong> Tags created in one deck cannot be used
 * in another deck. For example, "verbs" tags in a Japanese deck are separate
 * from "verbs" tags in a Spanish deck.
 *
 * <p><strong>Bidirectional Relationships:</strong>
 * <ul>
 *   <li>Many-to-One with {@link DeckEntity} (each tag belongs to one deck)</li>
 *   <li>Many-to-One with {@link UserEntity} (for ownership tracking)</li>
 *   <li>Many-to-Many with {@link CardEntity} (tags can be applied to multiple cards)</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tags",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"deck_id", "name"})
        },
        indexes = {
                @Index(name = "idx_tag_deck_id", columnList = "deck_id"),
                @Index(name = "idx_tag_user_id", columnList = "user_id")
        }
)
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this tag (inherited from deck ownership).
     * Stored for query optimization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /**
     * The deck this tag belongs to.
     * Tags are deck-specific and cannot be shared across decks.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private DeckEntity deck;

    /**
     * The tag name (e.g., "verbs", "N5", "important").
     * Must be unique within the deck.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Cards that have this tag applied.
     * Bidirectional many-to-many relationship.
     */
    @ManyToMany(mappedBy = "tags")
    private Set<CardEntity> cards = new HashSet<>();

    /**
     * Default constructor for JPA.
     */
    public TagEntity() {
    }

    /**
     * Creates a new tag within a deck.
     *
     * @param deck the deck this tag belongs to
     * @param name the tag name
     * @throws IllegalArgumentException if deck or name is null/empty
     */
    public TagEntity(DeckEntity deck, String name) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
        this.deck = deck;
        this.user = deck.getUser(); // Inherit user from deck
        this.name = name.trim();
    }

    // Getters and Setters

    /**
     * Gets the tag's unique identifier.
     *
     * @return the tag ID, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the tag's unique identifier.
     * Should only be used by JPA.
     *
     * @param id the tag ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user who owns this tag.
     *
     * @return the owner user
     */
    public UserEntity getUser() {
        return user;
    }

    /**
     * Sets the user who owns this tag.
     *
     * <p><strong>Warning:</strong> This should match the deck's user.
     * Use the constructor to ensure consistency.
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
     * Gets the deck this tag belongs to.
     *
     * @return the parent deck
     */
    public DeckEntity getDeck() {
        return deck;
    }

    /**
     * Sets the deck this tag belongs to.
     *
     * <p><strong>Warning:</strong> Changing the deck will not automatically
     * update the user or remove the tag from cards in the old deck.
     *
     * @param deck the parent deck
     * @throws IllegalArgumentException if deck is null
     */
    public void setDeck(DeckEntity deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        this.deck = deck;
    }

    /**
     * Gets the tag name.
     *
     * @return the tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the tag name.
     *
     * @param name the tag name
     * @throws IllegalArgumentException if name is null or empty
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
        this.name = name.trim();
    }

    /**
     * Gets all cards that have this tag.
     *
     * @return the set of cards with this tag
     */
    public Set<CardEntity> getCards() {
        return cards;
    }

    /**
     * Sets the cards that have this tag.
     *
     * <p><strong>Warning:</strong> This replaces all existing cards.
     * Use {@link CardEntity#addTag(TagEntity)} to manage individual card-tag associations.
     *
     * @param cards the set of cards
     */
    public void setCards(Set<CardEntity> cards) {
        this.cards = cards != null ? cards : new HashSet<>();
    }

    /**
     * Checks if this tag equals another object.
     *
     * <p>Two tags are equal if they have the same ID and name.
     * Tags without IDs are only equal to themselves.
     *
     * @param o the object to compare
     * @return {@code true} if equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagEntity tagEntity = (TagEntity) o;
        return id != null && Objects.equals(id, tagEntity.id) && Objects.equals(name, tagEntity.name);
    }

    /**
     * Returns the hash code for this tag.
     *
     * <p>Based on the tag's ID and name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    /**
     * Returns a string representation of this tag.
     *
     * <p>Includes ID and name but excludes card details to prevent
     * circular references.
     *
     * @return a string representation of this tag
     */
    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", deckId=" + (deck != null ? deck.getId() : null) +
                ", cardCount=" + (cards != null ? cards.size() : 0) +
                '}';
    }
}