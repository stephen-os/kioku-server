package com.kioku.api.service;

import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.UserEntity;
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
    public DeckEntity createDeck(Long userId, String name, String description) {
        UserEntity userEntity = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check for duplicate name
        List<DeckEntity> existingDeckEntities = deckRepository.findByUserId(userId);
        boolean nameExists = existingDeckEntities.stream()
                .anyMatch(d -> d.getName().equals(name));

        if (nameExists) {
            throw new IllegalArgumentException("Deck with name '" + name + "' already exists");
        }

        DeckEntity deckEntity = new DeckEntity(userEntity, name, description);
        return deckRepository.save(deckEntity);
    }

    /**
     * Get all decks for a user
     */
    public List<DeckEntity> getUserDecks(Long userId) {
        return deckRepository.findByUserId(userId);
    }

    /**
     * Get a specific deck (with ownership check)
     */
    public Optional<DeckEntity> getDeck(Long deckId, Long userId) {
        return deckRepository.findByIdAndUserId(deckId, userId);
    }

    /**
     * Get a deck or throw exception if not found or not owned
     */
    public DeckEntity getDeckOrThrow(Long deckId, Long userId) {
        return getDeck(deckId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Deck not found or access denied: " + deckId));
    }

    /**
     * Update a deck (with ownership check)
     */
    public DeckEntity updateDeck(Long deckId, Long userId, String name, String description) {
        DeckEntity deckEntity = getDeckOrThrow(deckId, userId);

        // Check if new name conflicts with another deck
        if (!deckEntity.getName().equals(name)) {
            List<DeckEntity> userDeckEntities = deckRepository.findByUserId(userId);
            boolean nameExists = userDeckEntities.stream()
                    .anyMatch(d -> !d.getId().equals(deckId) && d.getName().equals(name));

            if (nameExists) {
                throw new IllegalArgumentException("Deck with name '" + name + "' already exists");
            }
        }

        deckEntity.setName(name);
        deckEntity.setDescription(description);
        return deckRepository.save(deckEntity);
    }

    /**
     * Delete a deck (with ownership check)
     */
    public void deleteDeck(Long deckId, Long userId) {
        DeckEntity deckEntity = getDeckOrThrow(deckId, userId);
        deckRepository.delete(deckEntity);
    }

    /**
     * Check if user owns deck
     */
    public boolean userOwnsDeck(Long deckId, Long userId) {
        return deckRepository.existsByIdAndUserId(deckId, userId);
    }
}