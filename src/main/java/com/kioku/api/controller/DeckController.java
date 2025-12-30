package com.kioku.api.controller;

import com.kioku.api.dto.CreateDeckRequest;
import com.kioku.api.dto.DeckResponse;
import com.kioku.api.dto.ErrorResponse;
import com.kioku.api.entity.Deck;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    /**
     * Create a new deck
     * POST /api/decks
     */
    @PostMapping
    public ResponseEntity<?> createDeck(
            @CurrentUser Long userId,
            @Valid @RequestBody CreateDeckRequest request) {
        try {
            Deck deck = deckService.createDeck(userId, request.getName(), request.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED).body(new DeckResponse(deck));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all decks for current user
     * GET /api/decks
     */
    @GetMapping
    public ResponseEntity<List<DeckResponse>> getUserDecks(@CurrentUser Long userId) {
        List<Deck> decks = deckService.getUserDecks(userId);
        List<DeckResponse> response = decks.stream()
                .map(DeckResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific deck
     * GET /api/decks/{deckId}
     */
    @GetMapping("/{deckId}")
    public ResponseEntity<?> getDeck(
            @CurrentUser Long userId,
            @PathVariable Long deckId) {
        return deckService.getDeck(deckId, userId)
                .map(deck -> ResponseEntity.ok((Object) new DeckResponse(deck)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Deck not found or access denied")));
    }

    /**
     * Update a deck
     * PUT /api/decks/{deckId}
     */
    @PutMapping("/{deckId}")
    public ResponseEntity<?> updateDeck(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @Valid @RequestBody CreateDeckRequest request) {
        try {
            Deck deck = deckService.updateDeck(deckId, userId, request.getName(), request.getDescription());
            return ResponseEntity.ok(new DeckResponse(deck));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete a deck
     * DELETE /api/decks/{deckId}
     */
    @DeleteMapping("/{deckId}")
    public ResponseEntity<?> deleteDeck(
            @CurrentUser Long userId,
            @PathVariable Long deckId) {
        try {
            deckService.deleteDeck(deckId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}