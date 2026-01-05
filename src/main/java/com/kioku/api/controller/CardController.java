package com.kioku.api.controller;

import com.kioku.api.dto.request.CreateCardRequest;
import com.kioku.api.dto.request.UpdateCardRequest;
import com.kioku.api.dto.response.CardResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.entity.Card;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.CardService;
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
 * REST controller for flashcard operations within decks.
 *
 * <p>This controller provides endpoints for managing flashcards (cards) within
 * specific decks, including CRUD operations, search, filtering, and tag management.
 *
 * <p><strong>Card Management Endpoints:</strong>
 * <ul>
 *   <li>POST /api/decks/{deckId}/cards - Create a card</li>
 *   <li>GET /api/decks/{deckId}/cards - Get all cards (with optional search/filter)</li>
 *   <li>GET /api/decks/{deckId}/cards/{cardId} - Get a specific card</li>
 *   <li>PUT /api/decks/{deckId}/cards/{cardId} - Update a card</li>
 *   <li>DELETE /api/decks/{deckId}/cards/{cardId} - Delete a card</li>
 * </ul>
 *
 * <p><strong>Tag Management Endpoints:</strong>
 * <ul>
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
 *   <li>Duplicate detection (front/back combination)</li>
 *   <li>Full-text search across card content</li>
 *   <li>Tag-based filtering</li>
 *   <li>Automatic timestamp management</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong>
 * All exceptions are handled by {@link GlobalExceptionHandler}:
 * <ul>
 *   <li>400 Bad Request - Validation errors, duplicates</li>
 *   <li>404 Not Found - Card, deck, or tag not found</li>
 *   <li>403 Forbidden - User doesn't own the deck</li>
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
     * Creates a new flashcard in a deck.
     *
     * <p><strong>Endpoint:</strong> POST /api/decks/{deckId}/cards
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "front": "What is Java?",
     *   "back": "A programming language",
     *   "notes": "Object-oriented"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Checks for duplicate cards (same front/back)</li>
     *   <li>Validates field constraints</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 201 Created with CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck to add the card to
     * @param request the card creation request
     * @return ResponseEntity containing the created card
     * @throws IllegalArgumentException if duplicate or access denied (handled by GlobalExceptionHandler)
     */
    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @Valid @RequestBody CreateCardRequest request) {

        logger.debug("Creating card in deck {} for user {}", deckId, userId);

        Card card = cardService.createCard(
                userId,
                deckId,
                request.getFront(),
                request.getBack(),
                request.getNotes()
        );

        logger.info("Card {} created successfully in deck {}", card.getId(), deckId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CardResponse(card));
    }

    /**
     * Retrieves cards in a deck with optional search and filtering.
     *
     * <p><strong>Endpoint:</strong> GET /api/decks/{deckId}/cards
     *
     * <p><strong>Query Parameters:</strong>
     * <ul>
     *   <li>{@code search} - Search term to filter cards by front/back text (optional)</li>
     *   <li>{@code tagId} - Tag ID to filter cards by tag (optional)</li>
     * </ul>
     *
     * <p><strong>Behavior:</strong>
     * <ul>
     *   <li>If {@code search} is provided and not blank: returns cards matching search term</li>
     *   <li>Else if {@code tagId} is provided: returns cards with that tag</li>
     *   <li>Otherwise: returns all cards in deck</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with List of CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param search optional search term to filter cards
     * @param tagId optional tag ID to filter cards
     * @return ResponseEntity containing the list of cards
     * @throws IllegalArgumentException if deck not found or access denied (handled by GlobalExceptionHandler)
     */
    @GetMapping
    public ResponseEntity<List<CardResponse>> getDeckCards(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long tagId) {

        logger.debug("Retrieving cards for deck {} (search: {}, tagId: {})", deckId, search, tagId);

        List<Card> cards;

        // Priority: search > tagId > all
        if (search != null && !search.isBlank()) {
            logger.debug("Searching cards with term: {}", search);
            cards = cardService.searchCards(userId, deckId, search);
        } else if (tagId != null) {
            logger.debug("Filtering cards by tag: {}", tagId);
            cards = cardService.getCardsByTag(userId, deckId, tagId);
        } else {
            logger.debug("Retrieving all cards");
            cards = cardService.getDeckCards(userId, deckId);
        }

        List<CardResponse> response = cards.stream()
                .map(CardResponse::new)
                .collect(Collectors.toList());

        logger.debug("Retrieved {} cards from deck {}", response.size(), deckId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific card by ID.
     *
     * <p><strong>Endpoint:</strong> GET /api/decks/{deckId}/cards/{cardId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card exists in the specified deck</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card to retrieve
     * @return ResponseEntity containing the card
     * @throws IllegalArgumentException if card not found or access denied (handled by GlobalExceptionHandler)
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId) {

        logger.debug("Retrieving card {} from deck {} for user {}", cardId, deckId, userId);

        Optional<Card> cardOptional = cardService.getCard(userId, deckId, cardId);

        if (cardOptional.isEmpty()) {
            logger.warn("Card {} not found in deck {}", cardId, deckId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Card not found or access denied"));
        }

        Card card = cardOptional.get();
        logger.debug("Card {} retrieved successfully", cardId);
        return ResponseEntity.ok(new CardResponse(card));
    }

    /**
     * Updates an existing card.
     *
     * <p><strong>Endpoint:</strong> PUT /api/decks/{deckId}/cards/{cardId}
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "front": "What is Spring?",
     *   "back": "A Java framework",
     *   "notes": "For building applications"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Checks that update doesn't create duplicate card</li>
     *   <li>Validates field constraints</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with updated CardResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card to update
     * @param request the update request containing new card content
     * @return ResponseEntity containing the updated card
     * @throws IllegalArgumentException if duplicate or access denied (handled by GlobalExceptionHandler)
     */
    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponse> updateCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardRequest request) {

        logger.debug("Updating card {} in deck {} for user {}", cardId, deckId, userId);

        Card card = cardService.updateCard(
                userId,
                deckId,
                cardId,
                request.getFront(),
                request.getBack(),
                request.getNotes()
        );

        logger.info("Card {} updated successfully", cardId);
        return ResponseEntity.ok(new CardResponse(card));
    }

    /**
     * Deletes a card.
     *
     * <p><strong>Endpoint:</strong> DELETE /api/decks/{deckId}/cards/{cardId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card exists in the specified deck</li>
     * </ul>
     *
     * <p><strong>Side Effects:</strong>
     * Removes all tag associations from the card.
     *
     * <p><strong>Response:</strong> 204 No Content
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card to delete
     * @return ResponseEntity with no content
     * @throws IllegalArgumentException if card not found or access denied (handled by GlobalExceptionHandler)
     */
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId) {

        logger.debug("Deleting card {} from deck {} for user {}", cardId, deckId, userId);

        cardService.deleteCard(userId, deckId, cardId);

        logger.info("Card {} deleted successfully", cardId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a tag to a card.
     *
     * <p><strong>Endpoint:</strong> POST /api/decks/{deckId}/cards/{cardId}/tags/{tagId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card and tag exist in the deck</li>
     *   <li>Tag must belong to the same deck as the card</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with updated CardResponse including the new tag
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card
     * @param tagId the ID of the tag to add
     * @return ResponseEntity containing the updated card with tags
     * @throws IllegalArgumentException if card/tag not found or access denied (handled by GlobalExceptionHandler)
     */
    @PostMapping("/{cardId}/tags/{tagId}")
    public ResponseEntity<CardResponse> addTagToCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @PathVariable Long tagId) {

        logger.debug("Adding tag {} to card {} in deck {}", tagId, cardId, deckId);

        Card card = cardService.addTagToCard(userId, deckId, cardId, tagId);

        logger.info("Tag {} added to card {} successfully", tagId, cardId);
        return ResponseEntity.ok(new CardResponse(card));
    }

    /**
     * Removes a tag from a card.
     *
     * <p><strong>Endpoint:</strong> DELETE /api/decks/{deckId}/cards/{cardId}/tags/{tagId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card and tag exist in the deck</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with updated CardResponse without the removed tag
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param cardId the ID of the card
     * @param tagId the ID of the tag to remove
     * @return ResponseEntity containing the updated card without the tag
     * @throws IllegalArgumentException if card/tag not found or access denied (handled by GlobalExceptionHandler)
     */
    @DeleteMapping("/{cardId}/tags/{tagId}")
    public ResponseEntity<CardResponse> removeTagFromCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @PathVariable Long tagId) {

        logger.debug("Removing tag {} from card {} in deck {}", tagId, cardId, deckId);

        Card card = cardService.removeTagFromCard(userId, deckId, cardId, tagId);

        logger.info("Tag {} removed from card {} successfully", tagId, cardId);
        return ResponseEntity.ok(new CardResponse(card));
    }
}