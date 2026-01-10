package com.kioku.api.service;

import com.kioku.api.repository.DeckRepository;
import com.kioku.api.dto.request.CardImportDto;
import com.kioku.api.dto.request.DeckImportRequest;
import com.kioku.api.dto.response.DeckExportResponse;
import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import com.kioku.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
 *   <li>Deck import with cards and tags</li>
 *   <li>Deck export to JSON-serializable response</li>
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
    private final CardService cardService;
    private final TagService tagService;

    /**
     * Constructs a new DeckService.
     *
     * @param deckRepository the deck repository
     * @param userService the user service for user verification
     * @param cardService the card service for card operations
     * @param tagService the tag service for tag operations
     */
    public DeckService(DeckRepository deckRepository, UserService userService,
                       CardService cardService, TagService tagService) {
        this.deckRepository = deckRepository;
        this.userService = userService;
        this.cardService = cardService;
        this.tagService = tagService;
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
    public Deck createDeck(Long userId, String name, String description) {
        logger.debug("Creating deck '{}' for user id={}", name, userId);

        User user = userService.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", userId);
                    return new IllegalArgumentException("User not found: " + userId);
                });

        // Check for duplicate name
        List<Deck> existingDeckEntities = deckRepository.findByUserId(userId);
        boolean nameExists = existingDeckEntities.stream()
                .anyMatch(d -> d.getName().equals(name));

        if (nameExists) {
            logger.warn("Deck creation failed: duplicate name '{}' for user id={}", name, userId);
            throw new IllegalArgumentException("Deck with name '" + name + "' already exists");
        }

        Deck deck = new Deck(name, description);
        user.addDeck(deck);
        // Save through user to properly set user_id foreign key (unidirectional relationship)
        User savedUser = userService.save(user);

        // Get the deck with its generated ID from the saved user's collection
        Deck savedDeck = savedUser.getDecks().stream()
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Deck not found after save"));

        logger.debug("Deck created successfully with id={}", savedDeck.getId());
        return savedDeck;
    }

    /**
     * Gets all decks for a user.
     *
     * @param userId the user ID
     * @return list of user's decks
     */
    public List<Deck> getUserDecks(Long userId) {
        logger.debug("Getting all decks for user id={}", userId);

        List<Deck> decks = deckRepository.findByUserId(userId);
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
    public Optional<Deck> getDeck(Long deckId, Long userId) {
        logger.debug("Getting deck id={} for user id={}", deckId, userId);

        Optional<Deck> deck = deckRepository.findByIdAndUserId(deckId, userId);
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
    public Deck getDeckOrThrow(Long deckId, Long userId) {
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
    public Deck updateDeck(Long deckId, Long userId, String name, String description) {
        logger.debug("Updating deck id={} for user id={}", deckId, userId);

        Deck deck = getDeckOrThrow(deckId, userId);

        // Check if new name conflicts with another deck
        if (!deck.getName().equals(name)) {
            List<Deck> userDeckEntities = deckRepository.findByUserId(userId);
            boolean nameExists = userDeckEntities.stream()
                    .anyMatch(d -> !d.getId().equals(deckId) && d.getName().equals(name));

            if (nameExists) {
                logger.warn("Deck update failed: duplicate name '{}' for user id={}", name, userId);
                throw new IllegalArgumentException("Deck with name '" + name + "' already exists");
            }
        }

        deck.setName(name);
        deck.setDescription(description);
        Deck updatedDeck = deckRepository.save(deck);

        logger.debug("Deck id={} updated successfully", deckId);
        return updatedDeck;
    }

    /**
     * Deletes a deck.
     *
     * <p>This will cascade delete all cards and tags in the deck via orphanRemoval.
     *
     * @param deckId the deck ID
     * @param userId the user ID
     * @throws IllegalArgumentException if deck not found or access denied
     */
    public void deleteDeck(Long deckId, Long userId) {
        logger.debug("Deleting deck id={} for user id={}", deckId, userId);

        Deck deck = getDeckOrThrow(deckId, userId);

        // Get user and remove deck from collection (triggers orphanRemoval)
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.removeDeck(deck);
        userService.save(user);

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

    /**
     * Checks if a deck name already exists for the user.
     *
     * <p>Deck names must be unique per user. This method performs a
     * case-sensitive check for the exact deck name.
     *
     * @param userId the ID of the user
     * @param deckName the deck name to check
     * @return true if a deck with this name exists, false otherwise
     */
    public boolean isDuplicateName(Long userId, String deckName) {
        logger.debug("Checking for duplicate deck name '{}' for user id={}", deckName, userId);

        boolean isDuplicate = deckRepository.existsByUserIdAndName(userId, deckName);

        if (isDuplicate) {
            logger.debug("Duplicate deck name found: '{}' for user id={}", deckName, userId);
        }

        return isDuplicate;
    }

    // ==================== Import Methods ====================

    /**
     * Imports a complete deck with cards and tags.
     *
     * <p>This method performs a bulk import of an entire deck including all cards
     * and tags in a single transaction. If any part of the import fails, the entire
     * operation is rolled back.
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Deck name must be unique for the user</li>
     *   <li>Cards with duplicate front/back within the import are rejected</li>
     *   <li>All validation rules from normal card/tag creation apply</li>
     * </ul>
     *
     * @param userId the ID of the user importing the deck
     * @param request the import request containing deck, cards, and tags
     * @return the created deck entity
     * @throws IllegalArgumentException if deck name already exists, duplicate cards found, or validation fails
     */
    public Deck importDeck(Long userId, DeckImportRequest request) {
        logger.debug("Importing deck for user id={}: name={}, {} cards, {} tags",
                userId, request.getName(), request.getCardCount(), request.getTagCount());

        // 1. Check for duplicate deck name
        if (isDuplicateName(userId, request.getName())) {
            logger.warn("Import failed: Deck name '{}' already exists for user id={}", request.getName(), userId);
            throw new IllegalArgumentException("A deck with this name already exists");
        }

        // 2. Create the deck
        Deck deck = createDeck(userId, request.getName(), request.getDescription());
        logger.debug("Deck created: deckId={}", deck.getId());

        // 3. Create tags (deduplicate by name)
        Map<String, Tag> tagMap = createTagsForImport(userId, deck.getId(), request);
        logger.debug("Created {} unique tags", tagMap.size());

        // 4. Create cards and associate tags
        createCardsWithTagsForImport(userId, deck.getId(), request, tagMap);

        logger.info("Deck imported successfully: deckId={}, {} cards, {} tags",
                deck.getId(), request.getCardCount(), tagMap.size());

        return deck;
    }

    /**
     * Creates all tags from the import request, deduplicating by name.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param request the import request
     * @return a map of tag names to tag entities
     */
    private Map<String, Tag> createTagsForImport(Long userId, Long deckId, DeckImportRequest request) {
        Map<String, Tag> tagMap = new HashMap<>();

        // Add tags from explicit tag list
        if (request.hasTags()) {
            for (var tagDto : request.getTags()) {
                String tagName = tagDto.getName();
                if (!tagMap.containsKey(tagName)) {
                    Tag tag = tagService.createTag(userId, deckId, tagName);
                    tagMap.put(tagName, tag);
                    logger.debug("Created tag: {}", tagName);
                }
            }
        }

        return tagMap;
    }

    /**
     * Creates all cards and associates them with tags.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param request the import request
     * @param tagMap the map of existing tags
     * @throws IllegalArgumentException if duplicate cards are found within the import
     */
    private void createCardsWithTagsForImport(Long userId, Long deckId, DeckImportRequest request,
                                              Map<String, Tag> tagMap) {
        Set<String> processedCards = new HashSet<>();
        int cardIndex = 0;

        for (CardImportDto cardDto : request.getCards()) {
            cardIndex++;

            logger.info(cardDto.toString());

            // Check for duplicates within the import
            String cardKey = cardDto.getFront() + "|" + cardDto.getBack();
            if (processedCards.contains(cardKey)) {
                logger.warn("Duplicate card found at index {}: '{}' / '{}'",
                        cardIndex, cardDto.getFront(), cardDto.getBack());
                throw new IllegalArgumentException(
                        String.format("Duplicate card at index %d: '%s' / '%s'",
                                cardIndex, cardDto.getFront(), cardDto.getBack()));
            }
            processedCards.add(cardKey);

            // Create the card
            Card card = cardService.createCard(
                    userId,
                    deckId,
                    cardDto.getFront(),
                    cardDto.getBack(),
                    cardDto.getNotes()
            );
            logger.debug("Created card {}/{}: {}", cardIndex, request.getCardCount(), cardDto.getFront());

            // Associate tags with the card directly (avoids native query lookup issues)
            if (cardDto.hasTags()) {
                associateTagsWithCardForImport(userId, deckId, card, cardDto, tagMap);
            }
        }
    }

    /**
     * Associates tags with a card, creating tags if they don't exist.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param card the card entity to associate tags with
     * @param cardDto the card import DTO containing tag names
     * @param tagMap the map of existing tags (mutated to include newly created tags)
     */
    private void associateTagsWithCardForImport(Long userId, Long deckId, Card card,
                                                CardImportDto cardDto, Map<String, Tag> tagMap) {
        for (String tagName : cardDto.getTags()) {
            // Create tag if it doesn't exist yet
            if (!tagMap.containsKey(tagName)) {
                Tag tag = tagService.createTag(userId, deckId, tagName);
                tagMap.put(tagName, tag);
                logger.debug("Created tag from card reference: {}", tagName);
            }

            // Add tag to card directly using the entity we already have
            Tag tag = tagMap.get(tagName);
            card.addTag(tag);
            logger.debug("Associated tag '{}' with card '{}'", tagName, card.getFront());
        }
    }

    // ==================== Export Methods ====================

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
     * @param userId the ID of the user exporting the deck
     * @param deckId the ID of the deck to export
     * @return the export response containing the complete deck
     * @throws IllegalArgumentException if deck not found or access denied
     */
    @Transactional(readOnly = true)
    public DeckExportResponse exportDeck(Long userId, Long deckId) {
        logger.debug("Exporting deck id={} for user id={}", deckId, userId);

        // Get deck with ownership check
        Deck deck = getDeckOrThrow(deckId, userId);
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

        return response;
    }
}