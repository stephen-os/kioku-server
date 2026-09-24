package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;


/**
 * A tag, scoped to a deck.
 *
 * <p>Card membership is not stored here. A card carries the tag ids it uses in
 * {@link Card#getTagIds()}, so retagging is an ordinary card edit resolved by
 * the same last-write-wins rule as any other field.
 */
@Entity
@Table(name = "tags")
public class Tag extends SyncableEntity {

    @Column(name = "deck_id", nullable = false)
    private UUID deckId;

    @NotBlank(message = "Tag name is required")
    @Size(max = 100, message = "Tag name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int position = 0;

    protected Tag() {
    }

    public UUID getDeckId() { return deckId; }
    public void setDeckId(UUID deckId) { this.deckId = deckId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    @Override
    public String toString() {
        return "Tag{id=" + getId() + ", name='" + name + "'}";
    }
}
