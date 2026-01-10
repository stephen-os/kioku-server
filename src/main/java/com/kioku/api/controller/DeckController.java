package com.kioku.api.controller;

import com.kioku.api.dto.request.CreateDeckRequest;
import com.kioku.api.dto.request.DeckImportRequest;
import com.kioku.api.dto.request.UpdateDeckRequest;
import com.kioku.api.dto.response.DeckExportResponse;
import com.kioku.api.dto.response.DeckResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.model.Deck;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.DeckService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for flashcard deck operations.
 *
 * <p>This controller provides endpoints for managing flashcard decks,
 * including creation, retrieval, updating, and deletion.
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>POST /api/decks - Create a new deck</li>
 *   <li>GET /api/decks - Get all decks for current user</li>
 *   <li>GET /api/decks/{deckId} - Get a specific deck</li>
 *   <li>PUT /api/decks/{deckId} - Update a deck</li>
 *   <li>DELETE /api/decks/{deckId} - Delete a deck</li>
 * </ul>
 *
 * <p><strong>Authentication:</strong>
 * All endpoints require authentication. The authenticated user ID is automatically
 * resolved via the {@code @CurrentUser} annotation.
 *
 * <p><strong>Authorization:</strong>
 * Users can only access and modify their own decks. The service layer
 * enforces ownership verification.
 *
 * <p><strong>Features:</strong>
 * <ul>
 *   <li>Duplicate name detection (per user)</li>
 *   <li>Automatic timestamp management</li>
 *   <li>Cascade deletion (deletes all cards and tags)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong>
 * All exceptions are handled by {@link GlobalExceptionHandler}:
 * <ul>
 *   <li>400 Bad Request - Validation errors, duplicate names</li>
 *   <li>404 Not Found - Deck not found</li>
 *   <li>403 Forbidden - User doesn't own the deck</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private static final Logger logger = LoggerFactory.getLogger(DeckController.class);

    private final DeckService deckService;

    /**
     * Constructs a DeckController with required dependencies.
     *
     * @param deckService the deck service for business logic
     */
    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    /**
     * Creates a new flashcard deck.
     *
     * <p><strong>Endpoint:</strong> POST /api/decks
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "name": "Japanese Verbs",
     *   "description": "Common Japanese verbs for JLPT N5 level"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Checks for duplicate deck names (per user)</li>
     *   <li>Validates field constraints</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 201 Created with DeckResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param request the deck creation request containing name and description
     * @return ResponseEntity containing the created deck
     * @throws IllegalArgumentException if duplicate name exists (handled by GlobalExceptionHandler)
     */
    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(
            @CurrentUser Long userId,
            @Valid @RequestBody CreateDeckRequest request) {

        logger.debug("Creating deck '{}' for user {}", request.getName(), userId);

        Deck deck = deckService.createDeck(userId, request.getName(), request.getDescription());

        logger.info("Deck {} created successfully for user {}", deck.getId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DeckResponse(deck));
    }

    /**
     * Retrieves all decks for the authenticated user.
     *
     * <p><strong>Endpoint:</strong> GET /api/decks
     *
     * <p><strong>Response:</strong> 200 OK with List of DeckResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @return ResponseEntity containing the list of user's decks
     */
    @GetMapping
    public ResponseEntity<List<DeckResponse>> getUserDecks(@CurrentUser Long userId) {

        logger.debug("Retrieving all decks for user {}", userId);

        List<Deck> decks = deckService.getUserDecks(userId);
        List<DeckResponse> response = decks.stream()
                .map(DeckResponse::new)
                .collect(Collectors.toList());

        logger.debug("Retrieved {} decks for user {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific deck by ID.
     *
     * <p><strong>Endpoint:</strong> GET /api/decks/{deckId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies deck exists</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with DeckResponse, or 404 Not Found
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck to retrieve
     * @return ResponseEntity containing the deck
     * @throws IllegalArgumentException if deck not found or access denied (handled by GlobalExceptionHandler)
     */
    @GetMapping("/{deckId}")
    public ResponseEntity<?> getDeck(
            @CurrentUser Long userId,
            @PathVariable Long deckId) {

        logger.debug("Retrieving deck {} for user {}", deckId, userId);

        Optional<Deck> deckOptional = deckService.getDeck(deckId, userId);

        if (deckOptional.isEmpty()) {
            logger.warn("Deck {} not found or access denied for user {}", deckId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Deck not found or access denied"));
        }

        Deck deck = deckOptional.get();
        logger.debug("Deck {} retrieved successfully", deckId);
        return ResponseEntity.ok(new DeckResponse(deck));
    }

    /**
     * Updates an existing deck.
     *
     * <p><strong>Endpoint:</strong> PUT /api/decks/{deckId}
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "name": "Updated Deck Name",
     *   "description": "Updated description"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Checks that updated name doesn't conflict with another deck</li>
     *   <li>Validates field constraints</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with updated DeckResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck to update
     * @param request the update request containing new name and description
     * @return ResponseEntity containing the updated deck
     * @throws IllegalArgumentException if duplicate name or access denied (handled by GlobalExceptionHandler)
     */
    @PutMapping("/{deckId}")
    public ResponseEntity<DeckResponse> updateDeck(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @Valid @RequestBody UpdateDeckRequest request) {

        logger.debug("Updating deck {} for user {}", deckId, userId);

        Deck deck = deckService.updateDeck(deckId, userId, request.getName(), request.getDescription());

        logger.info("Deck {} updated successfully", deckId);
        return ResponseEntity.ok(new DeckResponse(deck));
    }

    /**
     * Deletes a deck.
     *
     * <p><strong>Endpoint:</strong> DELETE /api/decks/{deckId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies deck exists</li>
     * </ul>
     *
     * <p><strong>Side Effects:</strong>
     * Deletes all cards and tags associated with the deck (cascade delete).
     *
     * <p><strong>Response:</strong> 204 No Content
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck to delete
     * @return ResponseEntity with no content
     * @throws IllegalArgumentException if deck not found or access denied (handled by GlobalExceptionHandler)
     */
    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(
            @CurrentUser Long userId,
            @PathVariable Long deckId) {

        logger.debug("Deleting deck {} for user {}", deckId, userId);

        deckService.deleteDeck(deckId, userId);

        logger.info("Deck {} deleted successfully", deckId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Imports a complete deck with cards and tags.
     *
     * <p>This endpoint accepts a JSON payload containing a deck with all its cards
     * and tags, and creates them in a single atomic transaction.
     *
     * <p><strong>Request Body Example:</strong>
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
     *     }
     *   ],
     *   "tags": [
     *     {"name": "verbs"},
     *     {"name": "food"}
     *   ]
     * }
     * </pre>
     *
     * @param userId the ID of the authenticated user
     * @param request the deck import request
     * @return the created deck response
     */
    @PostMapping("/import")
    public ResponseEntity<DeckResponse> importDeck(
            @CurrentUser Long userId,
            @Valid @RequestBody DeckImportRequest request) {

        logger.debug("Import deck request from user id={}: name={}, {} cards, {} tags",
                userId, request.getName(), request.getCardCount(), request.getTagCount());

        Deck deck = deckService.importDeck(userId, request);
        DeckResponse response = new DeckResponse(deck);

        logger.info("Deck imported successfully: deckId={}, name='{}'", deck.getId(), deck.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    @GetMapping("/{deckId}/export")
    public ResponseEntity<DeckExportResponse> exportDeck(
            @CurrentUser Long userId,
            @PathVariable Long deckId) {

        logger.debug("Export deck request from user id={} for deck id={}", userId, deckId);

        DeckExportResponse response = deckService.exportDeck(userId, deckId);

        logger.info("Deck exported successfully: deckId={}, name='{}'", deckId, response.getName());

        return ResponseEntity.ok(response);
    }
}