package com.kioku.api.service;

import com.kioku.api.entity.Deck;
import com.kioku.api.entity.User;
import com.kioku.api.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserService userService;

    public DeckService(DeckRepository deckRepository, UserService userService) {
        this.deckRepository = deckRepository;
        this.userService = userService;
    }

    /**
     * Create a new deck for a user
     */
    public Deck createDeck(Long userId, String name, String description) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check for duplicate name
        List<Deck> existingDecks = deckRepository.findByUserId(userId);
        boolean nameExists = existingDecks.stream()
                .anyMatch(d -> d.getName().equals(name));

        if (nameExists) {
            throw new IllegalArgumentException("Deck with name '" + name + "' already exists");
        }

        Deck deck = new Deck(user, name, description);
        return deckRepository.save(deck);
    }

    /**
     * Get all decks for a user
     */
    public List<Deck> getUserDecks(Long userId) {
        return deckRepository.findByUserId(userId);
    }

    /**
     * Get a specific deck (with ownership check)
     */
    public Optional<Deck> getDeck(Long deckId, Long userId) {
        return deckRepository.findByIdAndUserId(deckId, userId);
    }

    /**
     * Get a deck or throw exception if not found or not owned
     */
    public Deck getDeckOrThrow(Long deckId, Long userId) {
        return getDeck(deckId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Deck not found or access denied: " + deckId));
    }

    /**
     * Update a deck (with ownership check)
     */
    public Deck updateDeck(Long deckId, Long userId, String name, String description) {
        Deck deck = getDeckOrThrow(deckId, userId);

        // Check if new name conflicts with another deck
        if (!deck.getName().equals(name)) {
            List<Deck> userDecks = deckRepository.findByUserId(userId);
            boolean nameExists = userDecks.stream()
                    .anyMatch(d -> !d.getId().equals(deckId) && d.getName().equals(name));

            if (nameExists) {
                throw new IllegalArgumentException("Deck with name '" + name + "' already exists");
            }
        }

        deck.setName(name);
        deck.setDescription(description);
        return deckRepository.save(deck);
    }

    /**
     * Delete a deck (with ownership check)
     */
    public void deleteDeck(Long deckId, Long userId) {
        Deck deck = getDeckOrThrow(deckId, userId);
        deckRepository.delete(deck);
    }

    /**
     * Check if user owns deck
     */
    public boolean userOwnsDeck(Long deckId, Long userId) {
        return deckRepository.existsByIdAndUserId(deckId, userId);
    }
}