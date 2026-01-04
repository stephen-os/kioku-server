package com.kioku.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for importing a complete deck with cards and tags.
 *
 * <p>Represents a request to import an entire deck including:
 * <ul>
 *   <li>Deck metadata (name, description)</li>
 *   <li>All cards in the deck</li>
 *   <li>All tags (optional - can be inferred from cards)</li>
 * </ul>
 *
 * <p><strong>Import Behavior:</strong>
 * <ul>
 *   <li>Deck name must be unique for the user</li>
 *   <li>All cards are created in a single transaction</li>
 *   <li>Tags are created if they don't exist</li>
 *   <li>Duplicate cards within the import are rejected</li>
 *   <li>If any validation fails, the entire import is rolled back</li>
 * </ul>
 *
 * <p><strong>Validation Rules:</strong>
 * <ul>
 *   <li>Deck name is required and limited to 255 characters</li>
 *   <li>Description is optional and limited to 1000 characters</li>
 *   <li>At least one card is required</li>
 *   <li>All cards must pass validation</li>
 *   <li>Tags are optional and validated if provided</li>
 * </ul>
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "name": "Japanese N5 Vocabulary",
 *   "description": "Essential JLPT N5 vocabulary",
 *   "cards": [
 *     {
 *       "front": "食べる",
 *       "back": "to eat",
 *       "notes": "ru-verb",
 *       "tags": ["verbs", "food"]
 *     },
 *     {
 *       "front": "飲む",
 *       "back": "to drink",
 *       "notes": "u-verb",
 *       "tags": ["verbs", "food"]
 *     }
 *   ],
 *   "tags": [
 *     {"name": "verbs"},
 *     {"name": "food"}
 *   ]
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class DeckImportRequest {

    /**
     * The name of the deck to import.
     */
    @NotBlank(message = "Deck name cannot be blank")
    @Size(max = 255, message = "Deck name cannot exceed 255 characters")
    private String name;

    /**
     * Optional description of the deck.
     */
    @Size(max = 1000, message = "Deck description cannot exceed 1000 characters")
    private String description;

    /**
     * List of cards to import.
     * At least one card is required.
     */
    @NotNull(message = "Cards list cannot be null")
    @Size(min = 1, message = "At least one card is required")
    @Valid
    private List<CardImportDto> cards;

    /**
     * Optional list of tags to create.
     * Tags can also be inferred from cards.
     */
    @Valid
    private List<TagImportDto> tags;

    /**
     * Default constructor for JSON deserialization.
     */
    public DeckImportRequest() {
        this.cards = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    /**
     * Creates a DeckImportRequest with all fields.
     *
     * @param name the deck name
     * @param description the deck description
     * @param cards the list of cards
     * @param tags the list of tags
     */
    public DeckImportRequest(String name, String description, List<CardImportDto> cards, List<TagImportDto> tags) {
        this.name = name;
        this.description = description;
        this.cards = cards != null ? cards : new ArrayList<>();
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    /**
     * Creates a DeckImportRequest without tags.
     *
     * @param name the deck name
     * @param description the deck description
     * @param cards the list of cards
     */
    public DeckImportRequest(String name, String description, List<CardImportDto> cards) {
        this(name, description, cards, new ArrayList<>());
    }

    // Getters and Setters

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
     * @param name the deck name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the deck description.
     *
     * @return the deck description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the deck description.
     *
     * @param description the deck description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the list of cards to import.
     *
     * @return the list of cards (never null)
     */
    public List<CardImportDto> getCards() {
        return cards;
    }

    /**
     * Sets the list of cards to import.
     *
     * @param cards the list of cards
     */
    public void setCards(List<CardImportDto> cards) {
        this.cards = cards != null ? cards : new ArrayList<>();
    }

    /**
     * Gets the list of tags to create.
     *
     * @return the list of tags (never null)
     */
    public List<TagImportDto> getTags() {
        return tags;
    }

    /**
     * Sets the list of tags to create.
     *
     * @param tags the list of tags
     */
    public void setTags(List<TagImportDto> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    /**
     * Checks if this import has any tags defined.
     *
     * @return true if tags are present, false otherwise
     */
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    /**
     * Gets the total number of cards in this import.
     *
     * @return the card count
     */
    public int getCardCount() {
        return cards != null ? cards.size() : 0;
    }

    /**
     * Gets the total number of tags in this import.
     *
     * @return the tag count
     */
    public int getTagCount() {
        return tags != null ? tags.size() : 0;
    }
}