package com.kioku.api.controller;

import com.kioku.api.dto.response.DeckExportResponse;
import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.CardService;
import com.kioku.api.service.DeckService;
import com.kioku.api.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for export operations.
 *
 * <p>This controller provides endpoints for exporting data from the system,
 * orchestrating multiple services to gather and package data.
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>GET /api/export/deck/{deckId} - Export a complete deck with cards and tags</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    private final DeckService deckService;
    private final CardService cardService;
    private final TagService tagService;

    public ExportController(DeckService deckService, CardService cardService, TagService tagService) {
        this.deckService = deckService;
        this.cardService = cardService;
        this.tagService = tagService;
    }

    /**
     * Exports a complete deck with all cards and tags.
     *
     * <p>This endpoint retrieves a deck and all its associated data, packaging
     * it into a JSON response that can be saved and later re-imported.
     *
     * <p><strong>Response Example:</strong>
     * <pre>
     * {
     *   "id": 123,
     *   "name": "Japanese N5 Vocabulary",
     *   "description": "Essential JLPT N5 vocabulary",
     *   "createdAt": "2024-01-15T10:30:00",
     *   "updatedAt": "2024-01-20T14:45:00",
     *   "cards": [...],
     *   "tags": [...],
     *   "metadata": {
     *     "exportDate": "2024-01-22T09:15:00",
     *     "version": "1.0",
     *     "cardCount": 50,
     *     "tagCount": 5
     *   }
     * }
     * </pre>
     *
     * @param userId the ID of the authenticated user
     * @param deckId the ID of the deck to export
     * @return the deck export response
     */
    @GetMapping("/deck/{deckId}")
    @Transactional(readOnly = true)
    public ResponseEntity<DeckExportResponse> exportDeck(
            @CurrentUser Long userId,
            @PathVariable Long deckId) {

        logger.debug("Export deck request from user id={} for deck id={}", userId, deckId);

        // Get deck with ownership check
        Deck deck = deckService.getDeckOrThrow(deckId, userId);
        logger.debug("Deck retrieved: name={}", deck.getName());

        // Get all cards in the deck
        List<Card> cards = cardService.getDeckCards(userId, deckId);
        logger.debug("Retrieved {} cards", cards.size());

        // Get all tags in the deck
        List<Tag> tags = tagService.getDeckTags(userId, deckId);
        logger.debug("Retrieved {} tags", tags.size());

        // Build export response
        DeckExportResponse response = new DeckExportResponse(deck, cards, tags);

        logger.info("Deck exported successfully: deckId={}, name='{}', {} cards, {} tags",
                deckId, deck.getName(), cards.size(), tags.size());

        return ResponseEntity.ok(response);
    }
}
