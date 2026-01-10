package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new tag within a deck.
 *
 * <p>This request object is used when creating a tag to categorize flashcards
 * within a specific deck. Tags are deck-scoped, meaning the same tag name can
 * exist in different decks.
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Name: Required, maximum 100 characters</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "name": "verbs"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class CreateTagRequest {

    /**
     * The name of the tag.
     * Required field with maximum length of 100 characters.
     * Tag names are case-sensitive and must be unique within a deck.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /**
     * Default constructor for JSON deserialization.
     */
    public CreateTagRequest() {}

    /**
     * Constructs a CreateTagRequest with the specified name.
     *
     * @param name the name of the tag
     */
    public CreateTagRequest(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the tag.
     *
     * @return the tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the tag.
     *
     * @param name the tag name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CreateTagRequest{" +
                "name='" + name + '\'' +
                '}';
    }
}