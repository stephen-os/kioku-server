package com.kioku.api.service;

import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.UserEntity;
import com.kioku.api.repository.DeckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Deck entity business logic.
 *
 * <p>This service provides:
 * <ul>
 *   <li>Deck creation with duplicate name detection</li>
 *   <li>Deck retrieval with ownership verification</li>
 *   <li>Deck updates with duplicate prevention</li>
 *   <li>Deck deletion</li>
 *   <li>Ownership verification utilities</li>
 * </ul>
 *
 * <p><strong>Security:</strong> All methods verify that the user owns the deck
 * before performing operations. Methods that accept {@code userId} will check
 * ownership and throw exceptions if access is denied.
 *
 * <p><strong>Duplicate Detection:</strong>
 * Deck names must be unique per user. The same name can exist across different
 * users, but a single user cannot have two decks with the same name.
 *
 * <p><strong>Transaction Management:</strong>
 * <ul>
 *   <li>Class is annotated with {@code @Transactional} for write operations</li>
 *   <li>Read-only methods could use {@code @Transactional(readOnly = true)}</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class DeckService {

    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);

    private final DeckRepository deckRepository;
    private final UserService userService;

    /**
     * Constructs a new DeckService.
     *
     * @param deckRepository the deck repository
     * @param userService the user service for user verification
     */
    public DeckService(DeckRepository deckRepository, UserService userService) {
        this.deckRepository = deckRepository;
        this.userService = userService;
    }

    /**
     * Creates a new deck for a user.
     *
     * <p>Verifies that:
     * <ul>
     *   <li>The user exists</li>
     *   <li>No deck with the same name exists for this user</li>
     * </ul>
     *
     * @param userId the user ID
     * @param name the deck name
     * @param description optional deck description
     * @return the created deck
     * @throws IllegalArgumentException if user doesn't exist or duplicate name exists
     */
    public DeckEntity createDeck(Long userId, String name, String description) {
        logger.debug("Creating deck '{}' for user id={}", name, userId);

        UserEntity userEntity = userService.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", userId);
                    return new IllegalArgumentException("User not found: " + userId);
                });

        // Check for duplicate name
        List<DeckEntity> existingDeckEntities = deckRepository.findByUserId(userId);
        boolean nameExists = existingDeckEntities.stream()
                .anyMatch(d -> d.getName().equals(name));

        if (nameExists) {
            logger.warn("Deck creation failed: duplicate name '{}' for user id={}", name, userId);
            throw new IllegalArgumentException("Deck with name '" + name + "' already exists");
        }

        DeckEntity deckEntity = new DeckEntity(userEntity, name, description);
        DeckEntity savedDeck = deckRepository.save(deckEntity);

        logger.debug("Deck created successfully with id={}", savedDeck.getId());
        return savedDeck;
    }

    /**
     * Gets all decks for a user.
     *
     * @param userId the user ID
     * @return list of user's decks
     */
    public List<DeckEntity> getUserDecks(Long userId) {
        logger.debug("Getting all decks for user id={}", userId);

        List<DeckEntity> decks = deckRepository.findByUserId(userId);
        logger.debug("Found {} decks for user id={}", decks.size(), userId);

        return decks;
    }

    /**
     * Gets a specific deck.
     *
     * <p>Verifies that the user owns the deck.
     *
     * @param deckId the deck ID
     * @param userId the user ID
     * @return an Optional containing the deck if found and owned, empty otherwise
     */
    public Optional<DeckEntity> getDeck(Long deckId, Long userId) {
        logger.debug("Getting deck id={} for user id={}", deckId, userId);

        Optional<DeckEntity> deck = deckRepository.findByIdAndUserId(deckId, userId);
        logger.debug("Deck found: {}", deck.isPresent());

        return deck;
    }

    /**
     * Gets a deck or throws exception if not found or not owned.
     *
     * @param deckId the deck ID
     * @param userId the user ID
     * @return the deck
     * @throws IllegalArgumentException if deck not found or access denied
     */
    public DeckEntity getDeckOrThrow(Long deckId, Long userId) {
        return getDeck(deckId, userId)
                .orElseThrow(() -> {
                    logger.warn("Deck not found or access denied: deck id={}, user id={}", deckId, userId);
                    return new IllegalArgumentException("Deck not found or access denied: " + deckId);
                });
    }

    /**
     * Updates a deck.
     *
     * <p>Verifies that:
     * <ul>
     *   <li>The user owns the deck</li>
     *   <li>The update doesn't create a duplicate name</li>
     * </ul>
     *
     * @param deckId the deck ID
     * @param userId the user ID
     * @param name the new deck name
     * @param description the new deck description
     * @return the updated deck
     * @throws IllegalArgumentException if access denied or update would create duplicate
     */
    public DeckEntity updateDeck(Long deckId, Long userId, String name, String description) {
        logger.debug("Updating deck id={} for user id={}", deckId, userId);

        DeckEntity deckEntity = getDeckOrThrow(deckId, userId);

        // Check if new name conflicts with another deck
        if (!deckEntity.getName().equals(name)) {
            List<DeckEntity> userDeckEntities = deckRepository.findByUserId(userId);
            boolean nameExists = userDeckEntities.stream()
                    .anyMatch(d -> !d.getId().equals(deckId) && d.getName().equals(name));

            if (nameExists) {
                logger.warn("Deck update failed: duplicate name '{}' for user id={}", name, userId);
                throw new IllegalArgumentException("Deck with name '" + name + "' already exists");
            }
        }

        deckEntity.setName(name);
        deckEntity.setDescription(description);
        DeckEntity updatedDeck = deckRepository.save(deckEntity);

        logger.debug("Deck id={} updated successfully", deckId);
        return updatedDeck;
    }

    /**
     * Deletes a deck.
     *
     * <p>This will cascade delete all cards and tags in the deck.
     *
     * @param deckId the deck ID
     * @param userId the user ID
     * @throws IllegalArgumentException if deck not found or access denied
     */
    public void deleteDeck(Long deckId, Long userId) {
        logger.debug("Deleting deck id={} for user id={}", deckId, userId);

        DeckEntity deckEntity = getDeckOrThrow(deckId, userId);
        deckRepository.delete(deckEntity);

        logger.debug("Deck id={} deleted successfully", deckId);
    }

    /**
     * Checks if a user owns a deck.
     *
     * @param deckId the deck ID
     * @param userId the user ID
     * @return {@code true} if user owns the deck, {@code false} otherwise
     */
    public boolean userOwnsDeck(Long deckId, Long userId) {
        boolean owns = deckRepository.existsByIdAndUserId(deckId, userId);
        logger.debug("User id={} owns deck id={}: {}", userId, deckId, owns);
        return owns;
    }
}