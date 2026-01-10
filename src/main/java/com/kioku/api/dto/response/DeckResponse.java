package com.kioku.api.dto.response;

import com.kioku.api.model.Deck;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for deck response.
 *
 * <p>This response object represents a flashcard deck with its metadata.
 * Decks are containers that organize flashcards and tags for a specific
 * subject or topic.
 *
 * <p><strong>Response Fields:</strong>
 * <ul>
 *   <li>id: Unique identifier for the deck</li>
 *   <li>name: The deck name</li>
 *   <li>description: Optional description of the deck's purpose</li>
 *   <li>createdAt: Deck creation timestamp</li>
 *   <li>updatedAt: Last update timestamp</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "id": 1,
 *   "name": "Japanese Verbs",
 *   "description": "Common Japanese verbs for JLPT N5 level",
 *   "createdAt": "2024-01-15T10:30:00",
 *   "updatedAt": "2024-01-15T10:30:00"
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class DeckResponse {

    /**
     * The unique identifier of the deck.
     */
    private Long id;

    /**
     * The name of the deck.
     */
    private String name;

    /**
     * Optional description of the deck's purpose or contents.
     */
    private String description;

    /**
     * The timestamp when the deck was created.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when the deck was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JSON serialization.
     */
    public DeckResponse() {}

    /**
     * Constructs a DeckResponse from a Deck.
     *
     * @param deck the Deck to convert
     */
    public DeckResponse(Deck deck) {
        this.id = deck.getId();
        this.name = deck.getName();
        this.description = deck.getDescription();
        this.createdAt = deck.getCreatedAt();
        this.updatedAt = deck.getUpdatedAt();
    }

    /**
     * Gets the deck's unique identifier.
     *
     * @return the deck ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the deck's unique identifier.
     *
     * @param id the deck ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the deck name.
     *
     * @return the deck name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the deck name.
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

    /**
     * Gets the timestamp when the deck was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the deck was created.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the deck was last updated.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the deck was last updated.
     *
     * @param updatedAt the last update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}