package com.kioku.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for importing a tag as part of a deck import.
 *
 * <p>Represents a tag to be created during deck import.
 * Tags can be pre-defined in the deck import request, or they can be
 * inferred from the tags listed in individual cards.
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Tag name is required and limited to 50 characters</li>
 *   <li>Tag names are case-sensitive</li>
 *   <li>Duplicate tag names within a deck are not allowed</li>
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
public class TagImportDto {

    /**
     * The name of the tag.
     */
    @NotBlank(message = "Tag name cannot be blank")
    @Size(max = 50, message = "Tag name cannot exceed 50 characters")
    private String name;

    /**
     * Default constructor for JSON deserialization.
     */
    public TagImportDto() {
    }

    /**
     * Creates a TagImportDto with the specified name.
     *
     * @param name the tag name
     */
    public TagImportDto(String name) {
        this.name = name;
    }

    // Getters and Setters

    /**
     * Gets the tag name.
     *
     * @return the tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the tag name.
     *
     * @param name the tag name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TagImportDto{" +
                "name='" + name + '\'' +
                '}';
    }
}