package com.kioku.api.dto.request;

import com.kioku.api.model.CodeLanguage;
import com.kioku.api.model.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new flashcard.
 *
 * <p>This request object is used when creating a flashcard within a specific deck.
 * It contains the front text (question), back text (answer), optional notes,
 * and content type information for rendering.
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Front: Required, maximum 500 characters</li>
 *   <li>Back: Required, maximum 500 characters</li>
 *   <li>Notes: Optional, maximum 1000 characters</li>
 *   <li>FrontType/BackType: Optional, defaults to TEXT</li>
 *   <li>FrontLanguage/BackLanguage: Required when corresponding type is CODE</li>
 * </ul>
 *
 * <p><strong>Example JSON (text card):</strong>
 * <pre>
 * {
 *   "front": "What is ownership in Rust?",
 *   "back": "A set of rules governing memory management",
 *   "notes": "Core concept"
 * }
 * </pre>
 *
 * <p><strong>Example JSON (code card):</strong>
 * <pre>
 * {
 *   "front": "How do you declare a mutable variable?",
 *   "frontType": "TEXT",
 *   "back": "let mut x = 5;",
 *   "backType": "CODE",
 *   "backLanguage": "RUST"
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
     * Content type for the front of the card.
     * Optional, defaults to TEXT if not provided.
     */
    private ContentType frontType;

    /**
     * Content type for the back of the card.
     * Optional, defaults to TEXT if not provided.
     */
    private ContentType backType;

    /**
     * Programming language for the front of the card.
     * Required when frontType is CODE.
     */
    private CodeLanguage frontLanguage;

    /**
     * Programming language for the back of the card.
     * Required when backType is CODE.
     */
    private CodeLanguage backLanguage;

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

    /**
     * Gets the content type for the front of the card.
     *
     * @return the front content type, or null if not set
     */
    public ContentType getFrontType() {
        return frontType;
    }

    /**
     * Sets the content type for the front of the card.
     *
     * @param frontType the front content type
     */
    public void setFrontType(ContentType frontType) {
        this.frontType = frontType;
    }

    /**
     * Gets the content type for the back of the card.
     *
     * @return the back content type, or null if not set
     */
    public ContentType getBackType() {
        return backType;
    }

    /**
     * Sets the content type for the back of the card.
     *
     * @param backType the back content type
     */
    public void setBackType(ContentType backType) {
        this.backType = backType;
    }

    /**
     * Gets the programming language for the front of the card.
     *
     * @return the front language, or null if not applicable
     */
    public CodeLanguage getFrontLanguage() {
        return frontLanguage;
    }

    /**
     * Sets the programming language for the front of the card.
     *
     * @param frontLanguage the front language
     */
    public void setFrontLanguage(CodeLanguage frontLanguage) {
        this.frontLanguage = frontLanguage;
    }

    /**
     * Gets the programming language for the back of the card.
     *
     * @return the back language, or null if not applicable
     */
    public CodeLanguage getBackLanguage() {
        return backLanguage;
    }

    /**
     * Sets the programming language for the back of the card.
     *
     * @param backLanguage the back language
     */
    public void setBackLanguage(CodeLanguage backLanguage) {
        this.backLanguage = backLanguage;
    }

    @Override
    public String toString() {
        return "CreateCardRequest{" +
                "front='" + front + '\'' +
                ", frontType=" + frontType +
                ", frontLanguage=" + frontLanguage +
                ", back='" + back + '\'' +
                ", backType=" + backType +
                ", backLanguage=" + backLanguage +
                ", notes='" + notes + '\'' +
                '}';
    }
}