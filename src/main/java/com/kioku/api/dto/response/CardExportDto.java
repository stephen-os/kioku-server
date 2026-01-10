package com.kioku.api.dto.response;

import com.kioku.api.model.Card;
import com.kioku.api.model.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for exporting a card as part of a deck export.
 *
 * <p>Represents a single flashcard in the export response, including:
 * <ul>
 *   <li>Card content (front, back, notes)</li>
 *   <li>Associated tag names</li>
 *   <li>Metadata (creation/update timestamps)</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "id": 123,
 *   "front": "食べる",
 *   "back": "to eat",
 *   "notes": "ru-verb",
 *   "tags": ["verbs", "food"],
 *   "createdAt": "2024-01-15T10:30:00",
 *   "updatedAt": "2024-01-15T10:30:00"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class CardExportDto {

    /**
     * The card's unique identifier.
     */
    private Long id;

    /**
     * The front of the card (question/prompt).
     */
    private String front;

    /**
     * The back of the card (answer).
     */
    private String back;

    /**
     * Optional notes about the card.
     */
    private String notes;

    /**
     * List of tag names associated with this card.
     */
    private List<String> tags;

    /**
     * Timestamp when the card was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the card was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JSON serialization.
     */
    public CardExportDto() {
        this.tags = new ArrayList<>();
    }

    /**
     * Creates a CardExportDto from a Card.
     *
     * @param card the card entity to export
     */
    public CardExportDto(Card card) {
        this.id = card.getCardId();
        this.front = card.getFront();
        this.back = card.getBack();
        this.notes = card.getNotes();
        this.tags = card.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
        this.createdAt = card.getCreatedAt();
        this.updatedAt = card.getUpdatedAt();
    }

    /**
     * Creates a CardExportDto with all fields.
     *
     * @param id the card ID
     * @param front the front of the card
     * @param back the back of the card
     * @param notes the card notes
     * @param tags the list of tag names
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     */
    public CardExportDto(Long id, String front, String back, String notes,
                         List<String> tags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.notes = notes;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    /**
     * Gets the card ID.
     *
     * @return the card ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the card ID.
     *
     * @param id the card ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the front of the card.
     *
     * @return the front text
     */
    public String getFront() {
        return front;
    }

    /**
     * Sets the front of the card.
     *
     * @param front the front text
     */
    public void setFront(String front) {
        this.front = front;
    }

    /**
     * Gets the back of the card.
     *
     * @return the back text
     */
    public String getBack() {
        return back;
    }

    /**
     * Sets the back of the card.
     *
     * @param back the back text
     */
    public void setBack(String back) {
        this.back = back;
    }

    /**
     * Gets the card notes.
     *
     * @return the notes, or null if not set
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the card notes.
     *
     * @param notes the notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Gets the list of tag names.
     *
     * @return the list of tag names (never null)
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the list of tag names.
     *
     * @param tags the list of tag names
     */
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt the last update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Checks if this card has any tags.
     *
     * @return true if tags are present, false otherwise
     */
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }
}