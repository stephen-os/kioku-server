package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;


/**
 * A tag, scoped to exactly one container: a deck or a quiz.
 *
 * <p>Cards draw from their deck's tags and questions from their quiz's. The
 * desktop kept two tables for this; here it is one entity with two scopes, so
 * only one type has to sync.
 *
 * <p>Membership is not stored here. A card or question carries the tag ids it
 * uses, so retagging is an ordinary field edit resolved by the same
 * last-write-wins rule.
 */
@Entity
@Table(name = "tags")
public class Tag extends SyncableEntity {

    /** Set when this tag belongs to a deck. Mutually exclusive with quizId. */
    @Column(name = "deck_id")
    private UUID deckId;

    /** Set when this tag belongs to a quiz. Mutually exclusive with deckId. */
    @Column(name = "quiz_id")
    private UUID quizId;

    @NotBlank(message = "Tag name is required")
    @Size(max = 100, message = "Tag name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int position = 0;

    /** JPA, and sync when it materialises an entity it has not seen before. */
    public Tag() {
    }

    public UUID getDeckId() { return deckId; }
    public void setDeckId(UUID deckId) { this.deckId = deckId; }

    public UUID getQuizId() { return quizId; }
    public void setQuizId(UUID quizId) { this.quizId = quizId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    @Override
    public String toString() {
        return "Tag{id=" + getId() + ", name='" + name + "'}";
    }
}
