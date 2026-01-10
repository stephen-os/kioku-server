package com.kioku.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a tag that can be applied to cards within a deck.
 *
 * @author Stephen Watson
 */
@Entity
@Table(name = "tags")
public class Tag {

    /**
     * The unique identifier for this tag.
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The tag name.
     * Maximum 100 characters.
     */
    @NotBlank(message = "Tag name is required")
    @Size(max = 100, message = "Tag name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Cards that have this tag applied.
     * Bidirectional many-to-many relationship with Card owning the relationship.
     */
    @ManyToMany(mappedBy = "tags")
    private Set<Card> cards = new HashSet<>();

    /**
     * Default constructor for JPA.
     */
    protected Tag() {
    }

    /**
     * Creates a new tag with the specified name.
     *
     * @param name the tag name
     * @throws IllegalArgumentException if name is null or empty
     */
    public Tag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
        this.name = name.trim();
    }

    /**
     * Gets the tag's unique identifier.
     *
     * @return the tag ID, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
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
    public Set<Card> getCards() {
        return cards;
    }

    /**
     * Gets the number of cards using this tag.
     *
     * @return the card count
     */
    public int getCardCount() {
        return cards.size();
    }

    /**
     * Checks if this tag is applied to any cards.
     *
     * @return {@code true} if at least one card has this tag
     */
    public boolean isInUse() {
        return !cards.isEmpty();
    }

    /**
     * Checks if this tag equals another object.
     *
     * Two tags are equal if they have the same ID.
     *
     * @param o the object to compare
     * @return {@code true} if equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id != null && Objects.equals(id, tag.id);
    }

    /**
     * Returns the hash code for this tag.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }

    /**
     * Returns a string representation of this tag.
     *
     * @return a string representation of this tag
     */
    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cardCount=" + cards.size() +
                '}';
    }
}
