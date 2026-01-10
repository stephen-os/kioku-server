package com.kioku.api.dto.response;

import com.kioku.api.model.Tag;

/**
 * Data Transfer Object for tag response.
 *
 * <p>This response object represents a tag that can be associated with flashcards
 * within a deck. Tags are deck-scoped, meaning the same tag name can exist in
 * different decks.
 *
 * <p><strong>Response Fields:</strong>
 * <ul>
 *   <li>id: Unique identifier for the tag</li>
 *   <li>name: The tag name (case-sensitive)</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "id": 1,
 *   "name": "verbs"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class TagResponse {

    /**
     * The unique identifier of the tag.
     */
    private Long id;

    /**
     * The name of the tag.
     * Tag names are case-sensitive and unique within a deck.
     */
    private String name;

    /**
     * Default constructor for JSON serialization.
     */
    public TagResponse() {}

    /**
     * Constructs a TagResponse with the specified id and name.
     *
     * @param id the tag ID
     * @param name the tag name
     */
    public TagResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructs a TagResponse from a Tag.
     *
     * @param tag the Tag to convert
     */
    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }

    /**
     * Gets the tag's unique identifier.
     *
     * @return the tag ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the tag's unique identifier.
     *
     * @param id the tag ID to set
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
     * @param name the tag name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}