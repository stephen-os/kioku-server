package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for importing a card as part of a deck import.
 *
 * <p>Represents a single flashcard to be created during deck import.
 * Cards can optionally include tags that will be associated with the card.
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Front text is required and limited to 1000 characters</li>
 *   <li>Back text is required and limited to 1000 characters</li>
 *   <li>Notes are optional and limited to 2000 characters</li>
 *   <li>Tag names are optional</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "front": "食べる",
 *   "back": "to eat",
 *   "notes": "ru-verb",
 *   "tags": ["verbs", "food"]
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class CardImportDto {

    /**
     * The front of the card (question/prompt).
     */
    @NotBlank(message = "Card front cannot be blank")
    @Size(max = 1000, message = "Card front cannot exceed 1000 characters")
    private String front;

    /**
     * The back of the card (answer).
     */
    @NotBlank(message = "Card back cannot be blank")
    @Size(max = 1000, message = "Card back cannot exceed 1000 characters")
    private String back;

    /**
     * Optional notes about the card.
     */
    @Size(max = 2000, message = "Card notes cannot exceed 2000 characters")
    private String notes;

    /**
     * Optional list of tag names to associate with this card.
     * Tags will be created if they don't exist in the deck.
     */
    private List<String> tags;

    /**
     * Default constructor for JSON deserialization.
     */
    public CardImportDto() {
        this.tags = new ArrayList<>();
    }

    /**
     * Creates a CardImportDto with all fields.
     *
     * @param front the front of the card
     * @param back the back of the card
     * @param notes optional notes
     * @param tags optional list of tag names
     */
    public CardImportDto(String front, String back, String notes, List<String> tags) {
        this.front = front;
        this.back = back;
        this.notes = notes;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    /**
     * Creates a CardImportDto without notes or tags.
     *
     * @param front the front of the card
     * @param back the back of the card
     */
    public CardImportDto(String front, String back) {
        this(front, back, null, new ArrayList<>());
    }

    // Getters and Setters

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
     * Checks if this card has any tags.
     *
     * @return true if tags are present, false otherwise
     */
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }
}