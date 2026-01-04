package com.kioku.api.service;

import com.kioku.api.dto.response.DeckExportResponse;
import com.kioku.api.entity.CardEntity;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for exporting complete decks with cards and tags.
 *
 * <p>This service orchestrates the export process by coordinating with
 * DeckService, CardService, and TagService to retrieve a complete deck
 * and package it into a JSON-serializable response.
 *
 * <p><strong>Export Process:</strong>
 * <ol>
 *   <li>Verify user owns the deck</li>
 *   <li>Retrieve deck metadata</li>
 *   <li>Retrieve all cards in the deck</li>
 *   <li>Retrieve all tags in the deck</li>
 *   <li>Package into export response with metadata</li>
 * </ol>
 *
 * <p><strong>Export Format:</strong>
 * The export response includes:
 * <ul>
 *   <li>Complete deck information (ID, name, description, timestamps)</li>
 *   <li>All cards with their associated tags</li>
 *   <li>All tags defined in the deck</li>
 *   <li>Export metadata (version, counts, export timestamp)</li>
 * </ul>
 *
 * <p><strong>Usage:</strong>
 * The exported JSON can be saved and later re-imported using the
 * DeckImportService, making it useful for:
 * <ul>
 *   <li>Backup and restore</li>
 *   <li>Sharing decks with other users</li>
 *   <li>Migration between systems</li>
 *   <li>Version control of study materials</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Service
public class DeckExportService {

    private static final Logger logger = LoggerFactory.getLogger(DeckExportService.class);

    private final DeckService deckService;
    private final CardService cardService;
    private final TagService tagService;

    /**
     * Constructs a DeckExportService with required dependencies.
     *
     * @param deckService the deck service
     * @param cardService the card service
     * @param tagService the tag service
     */
    public DeckExportService(DeckService deckService, CardService cardService, TagService tagService) {
        this.deckService = deckService;
        this.cardService = cardService;
        this.tagService = tagService;
    }

    /**
     * Exports a complete deck with all cards and tags.
     *
     * <p>This method retrieves a deck and all its associated data (cards and tags)
     * and packages them into an export response that can be saved as JSON and
     * later re-imported.
     *
     * <p><strong>Export Contents:</strong>
     * <ul>
     *   <li>Deck metadata (ID, name, description, timestamps)</li>
     *   <li>All cards with their tags</li>
     *   <li>All tags in the deck</li>
     *   <li>Export metadata (version, counts, export timestamp)</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong>
     * <pre>
     * DeckExportResponse export = exportService.exportDeck(userId, deckId);
     * String json = objectMapper.writeValueAsString(export);
     * // Save json to file or send to client
     * </pre>
     *
     * @param userId the ID of the user exporting the deck
     * @param deckId the ID of the deck to export
     * @return the export response containing the complete deck
     * @throws IllegalArgumentException if deck not found or access denied
     */
    @Transactional(readOnly = true)
    public DeckExportResponse exportDeck(Long userId, Long deckId) {
        logger.debug("Exporting deck id={} for user id={}", deckId, userId);

        // Get deck with ownership check
        DeckEntity deck = deckService.getDeckOrThrow(deckId, userId);
        logger.debug("Deck retrieved: name={}", deck.getName());

        // Get all cards in the deck
        List<CardEntity> cards = cardService.getDeckCards(userId, deckId);
        logger.debug("Retrieved {} cards", cards.size());

        // Get all tags in the deck
        List<TagEntity> tags = tagService.getDeckTags(userId, deckId);
        logger.debug("Retrieved {} tags", tags.size());

        // Build export response
        DeckExportResponse response = new DeckExportResponse(deck, cards, tags);

        logger.info("Deck exported successfully: deckId={}, name='{}', {} cards, {} tags",
                deckId, deck.getName(), cards.size(), tags.size());

        return response;
    }
}