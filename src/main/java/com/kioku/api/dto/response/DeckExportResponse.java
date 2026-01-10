package com.kioku.api.dto.response;

import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for exporting a complete deck with all cards and tags.
 *
 * <p>Represents the export response for an entire deck, including:
 * <ul>
 *   <li>Deck metadata (ID, name, description, timestamps)</li>
 *   <li>All cards in the deck with their tags</li>
 *   <li>All tags in the deck</li>
 *   <li>Export metadata (version, counts, export timestamp)</li>
 * </ul>
 *
 * <p><strong>Export Format:</strong>
 * This response can be saved as JSON and later re-imported using the
 * import endpoint. The format is designed to be both human-readable
 * and machine-parseable.
 *
 * <p><strong>Example JSON:</strong>
 * <pre>
 * {
 *   "id": 123,
 *   "name": "Japanese N5 Vocabulary",
 *   "description": "Essential JLPT N5 vocabulary",
 *   "createdAt": "2024-01-15T10:30:00",
 *   "updatedAt": "2024-01-20T14:45:00",
 *   "cards": [
 *     {
 *       "id": 456,
 *       "front": "食べる",
 *       "back": "to eat",
 *       "notes": "ru-verb",
 *       "tags": ["verbs", "food"],
 *       "createdAt": "2024-01-15T10:35:00",
 *       "updatedAt": "2024-01-15T10:35:00"
 *     }
 *   ],
 *   "tags": [
 *     {"id": 789, "name": "verbs"},
 *     {"id": 790, "name": "food"}
 *   ],
 *   "metadata": {
 *     "exportDate": "2024-01-22T09:15:00",
 *     "version": "1.0",
 *     "cardCount": 50,
 *     "tagCount": 5
 *   }
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
public class DeckExportResponse {

    /**
     * The deck's unique identifier.
     */
    private Long id;

    /**
     * The name of the deck.
     */
    private String name;

    /**
     * Optional description of the deck.
     */
    private String description;

    /**
     * Timestamp when the deck was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the deck was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * List of all cards in the deck.
     */
    private List<CardExportDto> cards;

    /**
     * List of all tags in the deck.
     */
    private List<TagExportDto> tags;

    /**
     * Export metadata (version, counts, export timestamp).
     */
    private ExportMetadata metadata;

    /**
     * Default constructor for JSON serialization.
     */
    public DeckExportResponse() {
        this.cards = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.metadata = new ExportMetadata();
    }

    /**
     * Creates a DeckExportResponse from a Deck with cards and tags.
     *
     * @param deck the deck entity to export
     * @param cards the list of cards in the deck
     * @param tags the list of tags in the deck
     */
    public DeckExportResponse(Deck deck, List<Card> cards, List<Tag> tags) {
        this.id = deck.getId();
        this.name = deck.getName();
        this.description = deck.getDescription();
        this.createdAt = deck.getCreatedAt();
        this.updatedAt = deck.getUpdatedAt();

        this.cards = cards.stream()
                .map(CardExportDto::new)
                .collect(Collectors.toList());

        this.tags = tags.stream()
                .map(TagExportDto::new)
                .collect(Collectors.toList());

        this.metadata = new ExportMetadata(cards.size(), tags.size());
    }

    // Getters and Setters

    /**
     * Gets the deck ID.
     *
     * @return the deck ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the deck ID.
     *
     * @param id the deck ID
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
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt the last update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the list of cards.
     *
     * @return the list of cards (never null)
     */
    public List<CardExportDto> getCards() {
        return cards;
    }

    /**
     * Sets the list of cards.
     *
     * @param cards the list of cards
     */
    public void setCards(List<CardExportDto> cards) {
        this.cards = cards != null ? cards : new ArrayList<>();
    }

    /**
     * Gets the list of tags.
     *
     * @return the list of tags (never null)
     */
    public List<TagExportDto> getTags() {
        return tags;
    }

    /**
     * Sets the list of tags.
     *
     * @param tags the list of tags
     */
    public void setTags(List<TagExportDto> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    /**
     * Gets the export metadata.
     *
     * @return the export metadata
     */
    public ExportMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the export metadata.
     *
     * @param metadata the export metadata
     */
    public void setMetadata(ExportMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Nested class for export metadata.
     *
     * <p>Contains information about the export itself:
     * <ul>
     *   <li>Export timestamp</li>
     *   <li>Format version</li>
     *   <li>Card and tag counts</li>
     * </ul>
     */
    public static class ExportMetadata {

        /**
         * The format version for compatibility tracking.
         */
        private String version;

        /**
         * Timestamp when this export was created.
         */
        private LocalDateTime exportDate;

        /**
         * Total number of cards in the export.
         */
        private int cardCount;

        /**
         * Total number of tags in the export.
         */
        private int tagCount;

        /**
         * Default constructor.
         */
        public ExportMetadata() {
            this.version = "1.0";
            this.exportDate = LocalDateTime.now();
            this.cardCount = 0;
            this.tagCount = 0;
        }

        /**
         * Creates export metadata with counts.
         *
         * @param cardCount the number of cards
         * @param tagCount the number of tags
         */
        public ExportMetadata(int cardCount, int tagCount) {
            this.version = "1.0";
            this.exportDate = LocalDateTime.now();
            this.cardCount = cardCount;
            this.tagCount = tagCount;
        }

        // Getters and Setters

        /**
         * Gets the format version.
         *
         * @return the format version
         */
        public String getVersion() {
            return version;
        }

        /**
         * Sets the format version.
         *
         * @param version the format version
         */
        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * Gets the export timestamp.
         *
         * @return the export timestamp
         */
        public LocalDateTime getExportDate() {
            return exportDate;
        }

        /**
         * Sets the export timestamp.
         *
         * @param exportDate the export timestamp
         */
        public void setExportDate(LocalDateTime exportDate) {
            this.exportDate = exportDate;
        }

        /**
         * Gets the card count.
         *
         * @return the card count
         */
        public int getCardCount() {
            return cardCount;
        }

        /**
         * Sets the card count.
         *
         * @param cardCount the card count
         */
        public void setCardCount(int cardCount) {
            this.cardCount = cardCount;
        }

        /**
         * Gets the tag count.
         *
         * @return the tag count
         */
        public int getTagCount() {
            return tagCount;
        }

        /**
         * Sets the tag count.
         *
         * @param tagCount the tag count
         */
        public void setTagCount(int tagCount) {
            this.tagCount = tagCount;
        }
    }
}