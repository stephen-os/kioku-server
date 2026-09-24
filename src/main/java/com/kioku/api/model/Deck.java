package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A flashcard deck.
 *
 * <p>Holds no collections. Cards and tags reference their deck by id and are
 * loaded through their own repositories, because sync moves entities
 * independently rather than as an object graph.
 */
@Entity
@Table(name = "decks")
public class Deck extends SyncableEntity {

    @NotBlank(message = "Deck name is required")
    @Size(max = 255, message = "Deck name must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    /** Never null; empty string when unset. */
    @Size(max = 1000, message = "Deck description must not exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String description = "";

    @Column(name = "shuffle_cards", nullable = false)
    private boolean shuffleCards = false;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;

    protected Deck() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public boolean isShuffleCards() { return shuffleCards; }
    public void setShuffleCards(boolean shuffleCards) { this.shuffleCards = shuffleCards; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    @Override
    public String toString() {
        return "Deck{id=" + getId() + ", name='" + name + "'}";
    }
}
