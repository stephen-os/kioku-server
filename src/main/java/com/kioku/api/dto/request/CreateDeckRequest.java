package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new flashcard deck.
 *
 * <p>This request object is used when creating a deck to organize flashcards.
 * Each deck has a required name and an optional description.
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Name: Required, maximum 255 characters</li>
 *   <li>Description: Optional, maximum 1000 characters</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "name": "Japanese Verbs",
 *   "description": "Common Japanese verbs for JLPT N5 level"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class CreateDeckRequest {

    /**
     * The name of the deck.
     * Required field with maximum length of 255 characters.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    /**
     * Optional description of the deck's purpose or contents.
     * Maximum length of 1000 characters.
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * Default constructor for JSON deserialization.
     */
    public CreateDeckRequest() {}

    /**
     * Constructs a CreateDeckRequest with specified values.
     *
     * @param name the name of the deck
     * @param description optional description of the deck
     */
    public CreateDeckRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the name of the deck.
     *
     * @return the deck name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the deck.
     *
     * @param name the deck name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the optional description of the deck.
     *
     * @return the description, or null if not provided
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the optional description of the deck.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}