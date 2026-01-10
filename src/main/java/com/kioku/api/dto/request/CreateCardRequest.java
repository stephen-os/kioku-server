package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new flashcard.
 *
 * <p>This request object is used when creating a flashcard within a specific deck.
 * It contains the front text (question), back text (answer), and optional notes.
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Front: Required, maximum 500 characters</li>
 *   <li>Back: Required, maximum 500 characters</li>
 *   <li>Notes: Optional, maximum 1000 characters</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "front": "This is the front",
 *   "back": "This is the back",
 *   "notes": "This is an example card"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class CreateCardRequest {

    /**
     * The front side of the flashcard (typically the question or term).
     * Required field with maximum length of 500 characters.
     */
    @NotBlank(message = "Front is required")
    @Size(max = 500, message = "Front must not exceed 500 characters")
    private String front;

    /**
     * The back side of the flashcard (typically the answer or definition).
     * Required field with maximum length of 500 characters.
     */
    @NotBlank(message = "Back is required")
    @Size(max = 500, message = "Back must not exceed 500 characters")
    private String back;

    /**
     * Optional notes or additional information about the flashcard.
     * Maximum length of 1000 characters.
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    /**
     * Default constructor for JSON deserialization.
     */
    public CreateCardRequest() {}

    /**
     * Constructs a CreateCardRequest with specified values.
     *
     * @param front the front text of the flashcard
     * @param back the back text of the flashcard
     * @param notes optional notes about the flashcard
     */
    public CreateCardRequest(String front, String back, String notes) {
        this.front = front;
        this.back = back;
        this.notes = notes;
    }

    /**
     * Gets the front text of the flashcard.
     *
     * @return the front text
     */
    public String getFront() {
        return front;
    }

    /**
     * Sets the front text of the flashcard.
     *
     * @param front the front text to set
     */
    public void setFront(String front) {
        this.front = front;
    }

    /**
     * Gets the back text of the flashcard.
     *
     * @return the back text
     */
    public String getBack() {
        return back;
    }

    /**
     * Sets the back text of the flashcard.
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

    @Override
    public String toString() {
        return "CreateCardRequest{" +
                "front='" + front + '\'' +
                ", back='" + back + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}