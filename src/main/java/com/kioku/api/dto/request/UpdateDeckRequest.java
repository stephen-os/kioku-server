package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating an existing flashcard deck.
 *
 * <p>This request object is used when updating a deck's information.
 * Both name and description can be updated.
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
 *   "name": "Japanese Verbs - Updated",
 *   "description": "Updated description for JLPT N4 level"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class UpdateDeckRequest {

    /**
     * The updated name of the deck.
     * Required field with maximum length of 255 characters.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    /**
     * Optional updated description of the deck's purpose or contents.
     * Maximum length of 1000 characters.
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * Default constructor for JSON deserialization.
     */
    public UpdateDeckRequest() {}

    /**
     * Constructs an UpdateDeckRequest with specified values.
     *
     * @param name the updated name of the deck
     * @param description optional updated description of the deck
     */
    public UpdateDeckRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the updated name of the deck.
     *
     * @return the deck name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the updated name of the deck.
     *
     * @param name the deck name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the updated optional description of the deck.
     *
     * @return the description, or null if not provided
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the updated optional description of the deck.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}