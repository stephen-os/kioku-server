package com.kioku.api.controller;

import com.kioku.api.dto.request.CreateCardRequest;
import com.kioku.api.dto.request.UpdateCardRequest;
import com.kioku.api.dto.response.CardResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.entity.CardEntity;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.CardService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for flashcard operations within decks.
 *
 * <p>This controller provides endpoints for managing flashcards within specific decks,
 * including creation, retrieval, updating, deletion, and tag management.
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>POST /api/decks/{deckId}/cards - Create a new card in a deck</li>
 *   <li>GET /api/decks/{deckId}/cards - Get all cards in a deck (with optional filters)</li>
 *   <li>GET /api/decks/{deckId}/cards/{cardId} - Get a specific card</li>
 *   <li>PUT /api/decks/{deckId}/cards/{cardId} - Update a card</li>
 *   <li>DELETE /api/decks/{deckId}/cards/{cardId} - Delete a card</li>
 *   <li>POST /api/decks/{deckId}/cards/{cardId}/tags/{tagId} - Add tag to card</li>
 *   <li>DELETE /api/decks/{deckId}/cards/{cardId}/tags/{tagId} - Remove tag from card</li>
 * </ul>
 *
 * <p><strong>Authentication:</strong>
 * All endpoints require authentication. The authenticated user ID is automatically
 * resolved via the {@code @CurrentUser} annotation.
 *
 * <p><strong>Authorization:</strong>
 * Users can only access and modify cards in their own decks. The service layer
 * enforces ownership verification.
 *
 * <p><strong>Features:</strong>
 * <ul>
 *   <li>Search cards by text content (front or back)</li>
 *   <li>Filter cards by tag</li>
 *   <li>Duplicate detection on create and update</li>
 *   <li>Tag management (add/remove tags from cards)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong>
 * <ul>
 *   <li>400 Bad Request - Validation errors, duplicates, invalid input</li>
 *   <li>404 Not Found - Card, deck, or tag not found</li>
 *   <li>403 Forbidden - User doesn't own the deck/tag</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/decks/{deckId}/cards")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    private final CardService cardService;

    /**
     * Constructs a CardController with required dependencies.
     *
     * @param cardService the card service for business logic
     */
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Creates a new flashcard in the specified deck.
     *
     * <p><strong>Endpoint:</strong> POST /api/decks/{deckId}/cards
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "front": "Question text",
     *   "back": "Answer text",
     *   "notes": "Optional notes"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Checks for duplicate cards (same front and back)</li>
     *   <li>Validates field constraints</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 201 Created with CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck to add the card to
     * @param request the card creation request containing front, back, and notes
     * @return ResponseEntity containing the created card or error response
     */
    @PostMapping
    public ResponseEntity<?> createCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @Valid @RequestBody CreateCardRequest request) {

        logger.debug("Creating card in deck {} for user {}", deckId, userId);

        try {
            CardEntity card = cardService.createCard(
                    userId,
                    deckId,
                    request.getFront(),
                    request.getBack(),
                    request.getNotes()
            );

            logger.info("Card {} created successfully in deck {}", card.getId(), deckId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new CardResponse(card));

        } catch (IllegalArgumentException e) {
            logger.error("Card creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Retrieves all flashcards in the specified deck with optional filtering.
     *
     * <p><strong>Endpoint:</strong> GET /api/decks/{deckId}/cards
     *
     * <p><strong>Query Parameters:</strong>
     * <ul>
     *   <li>{@code search} - Search term to filter cards by text content (optional)</li>
     *   <li>{@code tagId} - Tag ID to filter cards by tag (optional)</li>
     * </ul>
     *
     * <p><strong>Filtering Behavior:</strong>
     * <ul>
     *   <li>If {@code search} is provided: Returns cards matching the search term</li>
     *   <li>If {@code tagId} is provided: Returns cards with that tag</li>
     *   <li>If neither provided: Returns all cards in the deck</li>
     *   <li>{@code search} takes precedence over {@code tagId} if both provided</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with List of CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param search optional search term to filter cards
     * @param tagId optional tag ID to filter cards
     * @return ResponseEntity containing the list of cards or error response
     */
    @GetMapping
    public ResponseEntity<?> getDeckCards(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long tagId) {

        logger.debug("Retrieving cards for deck {} by user {} (search='{}', tagId={})",
                deckId, userId, search, tagId);

        try {
            List<CardEntity> cards;

            if (search != null && !search.isBlank()) {
                logger.debug("Searching cards with term '{}'", search);
                cards = cardService.searchCards(userId, deckId, search);
            } else if (tagId != null) {
                logger.debug("Filtering cards by tag {}", tagId);
                cards = cardService.getCardsByTag(userId, deckId, tagId);
            } else {
                logger.debug("Retrieving all cards in deck");
                cards = cardService.getDeckCards(userId, deckId);
            }

            List<CardResponse> response = cards.stream()
                    .map(CardResponse::new)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} cards from deck {}", response.size(), deckId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Error retrieving cards: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Retrieves a specific flashcard by ID.
     *
     * <p><strong>Endpoint:</strong> GET /api/decks/{deckId}/cards/{cardId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card exists in the specified deck</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with CardResponse, or 404 Not Found
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card to retrieve
     * @return ResponseEntity containing the card or error response
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId) {

        logger.debug("Retrieving card {} from deck {} for user {}", cardId, deckId, userId);

        return cardService.getCard(userId, deckId, cardId)
                .map(card -> {
                    logger.debug("Card {} retrieved successfully", cardId);
                    return ResponseEntity.ok((Object) new CardResponse(card));
                })
                .orElseGet(() -> {
                    logger.warn("Card {} not found or access denied", cardId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse("Card not found or access denied"));
                });
    }

    /**
     * Updates an existing flashcard.
     *
     * <p><strong>Endpoint:</strong> PUT /api/decks/{deckId}/cards/{cardId}
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "front": "Updated question",
     *   "back": "Updated answer",
     *   "notes": "Updated notes"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Checks that update doesn't create duplicate</li>
     *   <li>Validates field constraints</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with updated CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card to update
     * @param request the update request containing new values
     * @return ResponseEntity containing the updated card or error response
     */
    @PutMapping("/{cardId}")
    public ResponseEntity<?> updateCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardRequest request) {

        logger.debug("Updating card {} in deck {} for user {}", cardId, deckId, userId);

        try {
            CardEntity card = cardService.updateCard(
                    userId,
                    deckId,
                    cardId,
                    request.getFront(),
                    request.getBack(),
                    request.getNotes()
            );

            logger.info("Card {} updated successfully", cardId);
            return ResponseEntity.ok(new CardResponse(card));

        } catch (IllegalArgumentException e) {
            logger.error("Card update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Deletes a flashcard.
     *
     * <p><strong>Endpoint:</strong> DELETE /api/decks/{deckId}/cards/{cardId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card exists in the specified deck</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 204 No Content
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card to delete
     * @return ResponseEntity with no content or error response
     */
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId) {

        logger.debug("Deleting card {} from deck {} for user {}", cardId, deckId, userId);

        try {
            cardService.deleteCard(userId, deckId, cardId);

            logger.info("Card {} deleted successfully", cardId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            logger.error("Card deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Adds a tag to a flashcard.
     *
     * <p><strong>Endpoint:</strong> POST /api/decks/{deckId}/cards/{cardId}/tags/{tagId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies user owns the tag</li>
     *   <li>Verifies card and tag exist</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with updated CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card
     * @param tagId the ID of the tag to add
     * @return ResponseEntity containing the updated card or error response
     */
    @PostMapping("/{cardId}/tags/{tagId}")
    public ResponseEntity<?> addTagToCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @PathVariable Long tagId) {

        logger.debug("Adding tag {} to card {} in deck {} for user {}",
                tagId, cardId, deckId, userId);

        try {
            CardEntity card = cardService.addTagToCard(userId, deckId, cardId, tagId);

            logger.info("Tag {} added to card {} successfully", tagId, cardId);
            return ResponseEntity.ok(new CardResponse(card));

        } catch (IllegalArgumentException e) {
            logger.error("Add tag failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Removes a tag from a flashcard.
     *
     * <p><strong>Endpoint:</strong> DELETE /api/decks/{deckId}/cards/{cardId}/tags/{tagId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies user owns the tag</li>
     *   <li>Verifies card and tag exist</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with updated CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card
     * @param tagId the ID of the tag to remove
     * @return ResponseEntity containing the updated card or error response
     */
    @DeleteMapping("/{cardId}/tags/{tagId}")
    public ResponseEntity<?> removeTagFromCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @PathVariable Long tagId) {

        logger.debug("Removing tag {} from card {} in deck {} for user {}",
                tagId, cardId, deckId, userId);

        try {
            CardEntity card = cardService.removeTagFromCard(userId, deckId, cardId, tagId);

            logger.info("Tag {} removed from card {} successfully", tagId, cardId);
            return ResponseEntity.ok(new CardResponse(card));

        } catch (IllegalArgumentException e) {
            logger.error("Remove tag failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}