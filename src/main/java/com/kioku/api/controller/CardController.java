package com.kioku.api.controller;

import com.kioku.api.dto.CardResponse;
import com.kioku.api.dto.CreateCardRequest;
import com.kioku.api.dto.ErrorResponse;
import com.kioku.api.entity.Card;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/decks/{deckId}/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Create a new card
     * POST /api/decks/{deckId}/cards
     */
    @PostMapping
    public ResponseEntity<?> createCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @Valid @RequestBody CreateCardRequest request) {
        try {
            Card card = cardService.createCard(userId, deckId, request.getFront(), request.getBack(), request.getNotes());
            return ResponseEntity.status(HttpStatus.CREATED).body(new CardResponse(card));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all cards in a deck
     * GET /api/decks/{deckId}/cards
     */
    @GetMapping
    public ResponseEntity<?> getDeckCards(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long tagId) {
        try {
            List<Card> cards;

            if (search != null && !search.isBlank()) {
                cards = cardService.searchCards(userId, deckId, search);
            } else if (tagId != null) {
                cards = cardService.getCardsByTag(userId, deckId, tagId);
            } else {
                cards = cardService.getDeckCards(userId, deckId);
            }

            List<CardResponse> response = cards.stream()
                    .map(CardResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get a specific card
     * GET /api/decks/{deckId}/cards/{cardId}
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId) {
        return cardService.getCard(userId, deckId, cardId)
                .map(card -> ResponseEntity.ok((Object) new CardResponse(card)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Card not found or access denied")));
    }

    /**
     * Update a card
     * PUT /api/decks/{deckId}/cards/{cardId}
     */
    @PutMapping("/{cardId}")
    public ResponseEntity<?> updateCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @Valid @RequestBody CreateCardRequest request) {
        try {
            Card card = cardService.updateCard(userId, deckId, cardId, request.getFront(), request.getBack(), request.getNotes());
            return ResponseEntity.ok(new CardResponse(card));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete a card
     * DELETE /api/decks/{deckId}/cards/{cardId}
     */
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId) {
        try {
            cardService.deleteCard(userId, deckId, cardId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Add tag to card
     * POST /api/decks/{deckId}/cards/{cardId}/tags/{tagId}
     */
    @PostMapping("/{cardId}/tags/{tagId}")
    public ResponseEntity<?> addTagToCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @PathVariable Long tagId) {
        try {
            Card card = cardService.addTagToCard(userId, deckId, cardId, tagId);
            return ResponseEntity.ok(new CardResponse(card));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Remove tag from card
     * DELETE /api/decks/{deckId}/cards/{cardId}/tags/{tagId}
     */
    @DeleteMapping("/{cardId}/tags/{tagId}")
    public ResponseEntity<?> removeTagFromCard(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @PathVariable Long tagId) {
        try {
            Card card = cardService.removeTagFromCard(userId, deckId, cardId, tagId);
            return ResponseEntity.ok(new CardResponse(card));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}