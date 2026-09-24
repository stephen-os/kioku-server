package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;


/**
 * A single flashcard.
 *
 * <p>Tag membership lives here as an array of tag ids rather than in a join
 * table. Syncing join rows means syncing relationships, which conflict in ways
 * entities do not; as a column, retagging is just another card edit.
 */
@Entity
@Table(name = "cards")
public class Card extends SyncableEntity {

    @Column(name = "deck_id", nullable = false)
    private UUID deckId;

    @NotBlank(message = "Card front is required")
    @Size(max = 500, message = "Card front must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String front;

    @Enumerated(EnumType.STRING)
    @Column(name = "front_type", nullable = false, length = 10)
    private ContentType frontType = ContentType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(name = "front_language", length = 20)
    private CodeLanguage frontLanguage;

    @NotBlank(message = "Card back is required")
    @Size(max = 500, message = "Card back must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String back;

    @Enumerated(EnumType.STRING)
    @Column(name = "back_type", nullable = false, length = 10)
    private ContentType backType = ContentType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(name = "back_language", length = 20)
    private CodeLanguage backLanguage;

    @Size(max = 1000, message = "Card notes must not exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private int position = 0;

    /**
     * Tags applied to this card.
     *
     * <p>Deliberately not a foreign key: a Postgres array cannot carry one, so
     * a deleted tag leaves a dangling id that reads filter out.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tag_ids", nullable = false, columnDefinition = "uuid[]")
    private UUID[] tagIds = new UUID[0];

    protected Card() {
    }

    public UUID getDeckId() { return deckId; }
    public void setDeckId(UUID deckId) { this.deckId = deckId; }

    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }

    public ContentType getFrontType() { return frontType; }
    public void setFrontType(ContentType frontType) { this.frontType = frontType; }

    public CodeLanguage getFrontLanguage() { return frontLanguage; }
    public void setFrontLanguage(CodeLanguage frontLanguage) { this.frontLanguage = frontLanguage; }

    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }

    public ContentType getBackType() { return backType; }
    public void setBackType(ContentType backType) { this.backType = backType; }

    public CodeLanguage getBackLanguage() { return backLanguage; }
    public void setBackLanguage(CodeLanguage backLanguage) { this.backLanguage = backLanguage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public UUID[] getTagIds() { return tagIds; }
    public void setTagIds(UUID[] tagIds) {
        this.tagIds = tagIds == null ? new UUID[0] : tagIds;
    }

    @Override
    public String toString() {
        return "Card{id=" + getId() + ", deckId=" + deckId + "}";
    }
}
