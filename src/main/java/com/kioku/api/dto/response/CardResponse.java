package com.kioku.api.dto.response;

import com.kioku.api.entity.CardEntity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for flashcard response.
 *
 * <p>This response object represents a flashcard with all its associated data,
 * including tags, timestamps, and optional audio URLs for pronunciation.
 *
 * <p><strong>Response Fields:</strong>
 * <ul>
 *   <li>id: Unique identifier for the card</li>
 *   <li>front: Front side text (question/term)</li>
 *   <li>back: Back side text (answer/definition)</li>
 *   <li>notes: Optional additional notes</li>
 *   <li>frontAudioUrl: Optional audio URL for front pronunciation</li>
 *   <li>backAudioUrl: Optional audio URL for back pronunciation</li>
 *   <li>tags: Set of associated tags</li>
 *   <li>createdAt: Card creation timestamp</li>
 *   <li>updatedAt: Last update timestamp</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "id": 1,
 *   "front": "食べる",
 *   "back": "to eat",
 *   "notes": "ru-verb, ichidan verb",
 *   "frontAudioUrl": null,
 *   "backAudioUrl": null,
 *   "tags": [
 *     {"id": 1, "name": "verbs"},
 *     {"id": 2, "name": "JLPT-N5"}
 *   ],
 *   "createdAt": "2024-01-15T10:30:00",
 *   "updatedAt": "2024-01-15T10:30:00"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class CardResponse {

    /**
     * The unique identifier of the flashcard.
     */
    private Long id;

    /**
     * The front side of the flashcard (typically the question or term).
     */
    private String front;

    /**
     * The back side of the flashcard (typically the answer or definition).
     */
    private String back;

    /**
     * Optional notes or additional information about the flashcard.
     */
    private String notes;

    /**
     * Set of tags associated with this flashcard.
     */
    private Set<TagResponse> tags;

    /**
     * The timestamp when the flashcard was created.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when the flashcard was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JSON serialization.
     */
    public CardResponse() {}

    /**
     * Constructs a CardResponse from a CardEntity.
     * Automatically converts associated tags to TagResponse objects.
     *
     * @param card the CardEntity to convert
     */
    public CardResponse(CardEntity card) {
        this.id = card.getId();
        this.front = card.getFront();
        this.back = card.getBack();
        this.notes = card.getNotes();
        this.tags = card.getTags().stream()
                .map(TagResponse::new)
                .collect(Collectors.toSet());
        this.createdAt = card.getCreatedAt();
        this.updatedAt = card.getUpdatedAt();
    }

    /**
     * Gets the flashcard's unique identifier.
     *
     * @return the card ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the flashcard's unique identifier.
     *
     * @param id the card ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the front side text of the flashcard.
     *
     * @return the front text
     */
    public String getFront() {
        return front;
    }

    /**
     * Sets the front side text of the flashcard.
     *
     * @param front the front text to set
     */
    public void setFront(String front) {
        this.front = front;
    }

    /**
     * Gets the back side text of the flashcard.
     *
     * @return the back text
     */
    public String getBack() {
        return back;
    }

    /**
     * Sets the back side text of the flashcard.
     *
     * @param back the back text to set
     */
    public void setBack(String back) {
        this.back = back;
    }

    /**
     * Gets the optional notes about the flashcard.
     *
     * @return the notes, or null if not provided
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the optional notes about the flashcard.
     *
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Gets the set of tags associated with this flashcard.
     *
     * @return the set of tags
     */
    public Set<TagResponse> getTags() {
        return tags;
    }

    /**
     * Sets the set of tags associated with this flashcard.
     *
     * @param tags the set of tags to set
     */
    public void setTags(Set<TagResponse> tags) {
        this.tags = tags;
    }

    /**
     * Gets the timestamp when the flashcard was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the flashcard was created.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the flashcard was last updated.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the flashcard was last updated.
     *
     * @param updatedAt the last update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}