package com.kioku.api.service;

import com.kioku.api.entity.CardEntity;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing flashcards (cards) within decks.
 *
 * <p>This service provides business logic for:
 * <ul>
 *   <li>Creating, reading, updating, and deleting cards</li>
 *   <li>Searching and filtering cards by content</li>
 *   <li>Filtering cards by tags</li>
 *   <li>Managing card-tag associations</li>
 *   <li>Duplicate detection (front/back combination)</li>
 * </ul>
 *
 * <p><strong>Security:</strong>
 * All methods verify deck ownership before performing operations by calling
 * {@link DeckService#getDeckOrThrow(Long, Long)}, which throws an exception
 * if the user doesn't own the deck.
 *
 * <p><strong>Duplicate Prevention:</strong>
 * Cards with the same front/back text combination within a deck are not allowed.
 * The comparison is case-sensitive and exact match.
 *
 * <p><strong>Tag Loading:</strong>
 * All card retrieval methods use eager loading of tags via {@code LEFT JOIN FETCH}
 * to prevent LazyInitializationException when accessing tags outside of transactions.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Service
public class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final DeckService deckService;
    private final TagService tagService;

    /**
     * Constructs a CardService with required dependencies.
     *
     * @param cardRepository the card repository
     * @param deckService the deck service for ownership verification
     * @param tagService the tag service for tag operations
     */
    public CardService(CardRepository cardRepository, DeckService deckService, TagService tagService) {
        this.cardRepository = cardRepository;
        this.deckService = deckService;
        this.tagService = tagService;
    }

    /**
     * Creates a new card in a deck.
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Checks for duplicate front/back combination</li>
     *   <li>Ensures front and back are not null or blank</li>
     * </ul>
     *
     * @param userId the ID of the user creating the card
     * @param deckId the ID of the deck to add the card to
     * @param front the front text of the card
     * @param back the back text of the card
     * @param notes optional notes for the card
     * @return the created card
     * @throws IllegalArgumentException if user doesn't own deck or card is duplicate
     */
    @Transactional
    public CardEntity createCard(Long userId, Long deckId, String front, String back, String notes) {
        logger.debug("Creating card in deck id={} for user id={}", deckId, userId);

        // Get deck and verify ownership
        DeckEntity deck = deckService.getDeckOrThrow(deckId, userId);

        // Check for duplicate
        if (cardRepository.existsByDeckIdAndFrontAndBack(deckId, front, back)) {
            logger.warn("Duplicate card detected in deck id={}: front='{}', back='{}'", deckId, front, back);
            throw new IllegalArgumentException("A card with this front and back already exists in this deck");
        }

        // Create card
        CardEntity card = new CardEntity(deck, front, back);
        if (notes != null && !notes.isBlank()) {
            card.setNotes(notes);
        }

        CardEntity savedCard = cardRepository.save(card);
        logger.info("Card created: id={} in deck id={}", savedCard.getId(), deckId);

        return savedCard;
    }

    /**
     * Retrieves all cards in a deck with tags eagerly loaded.
     *
     * <p><strong>Security:</strong> Verifies user owns the deck.
     *
     * <p><strong>Performance:</strong> Tags are eagerly loaded to prevent
     * LazyInitializationException when accessing tags in DTOs.
     *
     * @param userId the ID of the user requesting the cards
     * @param deckId the ID of the deck
     * @return list of cards in the deck with tags loaded
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public List<CardEntity> getDeckCards(Long userId, Long deckId) {
        logger.debug("Getting cards for deck id={}, user id={}", deckId, userId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Fetch cards with tags eagerly loaded
        List<CardEntity> cards = cardRepository.findByDeckId(deckId);

        logger.debug("Found {} cards in deck id={}", cards.size(), deckId);
        return cards;
    }

    /**
     * Retrieves a specific card by ID within a deck with tags eagerly loaded.
     *
     * <p><strong>Security:</strong> Verifies user owns the deck and card exists in that deck.
     *
     * @param userId the ID of the user requesting the card
     * @param deckId the ID of the deck
     * @param cardId the ID of the card
     * @return an Optional containing the card with tags if found, empty otherwise
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public Optional<CardEntity> getCard(Long userId, Long deckId, Long cardId) {
        logger.debug("Getting card id={} from deck id={} for user id={}", cardId, deckId, userId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Fetch card with tags eagerly loaded
        Optional<CardEntity> card = cardRepository.findByIdAndDeckId(cardId, deckId);

        logger.debug("Card found: {}", card.isPresent());
        return card;
    }

    /**
     * Updates an existing card.
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card exists in the deck</li>
     *   <li>Checks for duplicate front/back (excluding the card being updated)</li>
     * </ul>
     *
     * @param userId the ID of the user updating the card
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to update
     * @param front the new front text
     * @param back the new back text
     * @param notes the new notes (can be null)
     * @return the updated card with tags loaded
     * @throws IllegalArgumentException if user doesn't own deck, card not found, or duplicate
     */
    @Transactional
    public CardEntity updateCard(Long userId, Long deckId, Long cardId, String front, String back, String notes) {
        logger.debug("Updating card id={} in deck id={} for user id={}", cardId, deckId, userId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Get existing card
        CardEntity card = cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> {
                    logger.warn("Card id={} not found in deck id={}", cardId, deckId);
                    return new IllegalArgumentException("Card not found in this deck");
                });

        // Check for duplicate (excluding this card)
        if (cardRepository.existsByDeckIdAndFrontAndBackAndIdNot(deckId, front, back, cardId)) {
            logger.warn("Update would create duplicate in deck id={}: front='{}', back='{}'", deckId, front, back);
            throw new IllegalArgumentException("A card with this front and back already exists in this deck");
        }

        // Update card
        card.setFront(front);
        card.setBack(back);
        card.setNotes(notes);

        CardEntity updatedCard = cardRepository.save(card);
        logger.info("Card updated: id={} in deck id={}", cardId, deckId);

        return updatedCard;
    }

    /**
     * Deletes a card from a deck.
     *
     * <p><strong>Side Effects:</strong>
     * All tag associations for this card are automatically removed by JPA
     * cascade settings.
     *
     * @param userId the ID of the user deleting the card
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to delete
     * @throws IllegalArgumentException if user doesn't own deck or card not found
     */
    @Transactional
    public void deleteCard(Long userId, Long deckId, Long cardId) {
        logger.debug("Deleting card id={} from deck id={} for user id={}", cardId, deckId, userId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Verify card exists in deck
        CardEntity card = cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> {
                    logger.warn("Card id={} not found in deck id={}", cardId, deckId);
                    return new IllegalArgumentException("Card not found in this deck");
                });

        cardRepository.delete(card);
        logger.info("Card deleted: id={} from deck id={}", cardId, deckId);
    }

    /**
     * Searches for cards by text content with tags eagerly loaded.
     *
     * <p>Performs a case-insensitive partial match search on both front and back text.
     *
     * @param userId the ID of the user performing the search
     * @param deckId the ID of the deck to search in
     * @param searchTerm the text to search for
     * @return list of matching cards with tags loaded
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public List<CardEntity> searchCards(Long userId, Long deckId, String searchTerm) {
        logger.debug("Searching cards in deck id={} for term: {}", deckId, searchTerm);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Search with tags eagerly loaded
        List<CardEntity> cards = cardRepository.searchByDeckId(deckId, searchTerm);

        logger.debug("Found {} cards matching search term in deck id={}", cards.size(), deckId);
        return cards;
    }

    /**
     * Retrieves all cards in a deck that have a specific tag with tags eagerly loaded.
     *
     * <p><strong>Security:</strong>
     * Verifies user owns both the deck and the tag.
     *
     * @param userId the ID of the user requesting the cards
     * @param deckId the ID of the deck
     * @param tagId the ID of the tag to filter by
     * @return list of cards with the specified tag
     * @throws IllegalArgumentException if user doesn't own deck or tag
     */
    @Transactional(readOnly = true)
    public List<CardEntity> getCardsByTag(Long userId, Long deckId, Long tagId) {
        logger.debug("Getting cards in deck id={} with tag id={}", deckId, tagId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Verify tag exists and belongs to deck
        tagService.getTag(userId, deckId, tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found in this deck"));

        // Fetch cards with tags eagerly loaded
        List<CardEntity> cards = cardRepository.findByDeckIdAndTagId(deckId, tagId);

        logger.debug("Found {} cards with tag id={} in deck id={}", cards.size(), tagId, deckId);
        return cards;
    }

    /**
     * Adds a tag to a card.
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card exists in the deck</li>
     *   <li>Verifies tag exists and belongs to the same deck</li>
     *   <li>Prevents adding duplicate tags to the same card</li>
     * </ul>
     *
     * @param userId the ID of the user adding the tag
     * @param deckId the ID of the deck
     * @param cardId the ID of the card
     * @param tagId the ID of the tag to add
     * @return the updated card with tags loaded
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public CardEntity addTagToCard(Long userId, Long deckId, Long cardId, Long tagId) {
        logger.debug("Adding tag id={} to card id={} in deck id={}", tagId, cardId, deckId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Get card
        CardEntity card = cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> {
                    logger.warn("Card id={} not found in deck id={}", cardId, deckId);
                    return new IllegalArgumentException("Card not found in this deck");
                });

        // Get tag and verify it belongs to the same deck
        TagEntity tag = tagService.getTag(userId, deckId, tagId)
                .orElseThrow(() -> {
                    logger.warn("Tag id={} not found in deck id={}", tagId, deckId);
                    return new IllegalArgumentException("Tag not found in this deck");
                });

        // Add tag to card (Set prevents duplicates automatically)
        card.addTag(tag);

        CardEntity savedCard = cardRepository.save(card);
        logger.info("Tag id={} added to card id={}", tagId, cardId);

        return savedCard;
    }

    /**
     * Removes a tag from a card.
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies card exists in the deck</li>
     *   <li>Verifies tag exists</li>
     * </ul>
     *
     * @param userId the ID of the user removing the tag
     * @param deckId the ID of the deck
     * @param cardId the ID of the card
     * @param tagId the ID of the tag to remove
     * @return the updated card with tags loaded
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public CardEntity removeTagFromCard(Long userId, Long deckId, Long cardId, Long tagId) {
        logger.debug("Removing tag id={} from card id={} in deck id={}", tagId, cardId, deckId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Get card
        CardEntity card = cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> {
                    logger.warn("Card id={} not found in deck id={}", cardId, deckId);
                    return new IllegalArgumentException("Card not found in this deck");
                });

        // Get tag
        TagEntity tag = tagService.getTag(userId, deckId, tagId)
                .orElseThrow(() -> {
                    logger.warn("Tag id={} not found in deck id={}", tagId, deckId);
                    return new IllegalArgumentException("Tag not found in this deck");
                });

        // Remove tag from card
        card.removeTag(tag);

        CardEntity savedCard = cardRepository.save(card);
        logger.info("Tag id={} removed from card id={}", tagId, cardId);

        return savedCard;
    }
}