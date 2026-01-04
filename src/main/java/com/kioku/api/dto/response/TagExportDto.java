package com.kioku.api.dto.response;

import com.kioku.api.entity.TagEntity;

/**
 * DTO for exporting a tag as part of a deck export.
 *
 * <p>Represents a tag in the export response, including:
 * <ul>
 *   <li>Tag ID</li>
 *   <li>Tag name</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "id": 456,
 *   "name": "verbs"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class TagExportDto {

    /**
     * The tag's unique identifier.
     */
    private Long id;

    /**
     * The name of the tag.
     */
    private String name;

    /**
     * Default constructor for JSON serialization.
     */
    public TagExportDto() {
    }

    /**
     * Creates a TagExportDto from a TagEntity.
     *
     * @param tag the tag entity to export
     */
    public TagExportDto(TagEntity tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }

    /**
     * Creates a TagExportDto with all fields.
     *
     * @param id the tag ID
     * @param name the tag name
     */
    public TagExportDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters

    /**
     * Gets the tag ID.
     *
     * @return the tag ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the tag ID.
     *
     * @param id the tag ID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
}