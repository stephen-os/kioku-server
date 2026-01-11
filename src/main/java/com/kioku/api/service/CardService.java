package com.kioku.api.service;

import com.kioku.api.repository.CardRepository;
import com.kioku.api.repository.DeckRepository;
import com.kioku.api.model.CodeLanguage;
import com.kioku.api.model.ContentType;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Card;
import com.kioku.api.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final DeckRepository deckRepository;
    private final DeckService deckService;
    private final TagService tagService;

    /**
     * Constructs a CardService with required dependencies.
     *
     * @param cardRepository the card repository
     * @param deckRepository the deck repository for fetching decks with cards
     * @param deckService the deck service for ownership verification
     * @param tagService the tag service for tag operations
     */
    public CardService(CardRepository cardRepository, DeckRepository deckRepository,
                       DeckService deckService, TagService tagService) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
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
     *   <li>Validates language is provided when content type is CODE</li>
     * </ul>
     *
     * @param userId the ID of the user creating the card
     * @param deckId the ID of the deck to add the card to
     * @param front the front text of the card
     * @param back the back text of the card
     * @param notes optional notes for the card
     * @param frontType content type for front (TEXT or CODE), defaults to TEXT
     * @param backType content type for back (TEXT or CODE), defaults to TEXT
     * @param frontLanguage programming language for front (required if frontType is CODE)
     * @param backLanguage programming language for back (required if backType is CODE)
     * @return the created card
     * @throws IllegalArgumentException if user doesn't own deck, card is duplicate, or validation fails
     */
    @Transactional
    public Card createCard(Long userId, Long deckId, String front, String back, String notes,
                           ContentType frontType, ContentType backType,
                           CodeLanguage frontLanguage, CodeLanguage backLanguage) {
        logger.debug("Creating card in deck id={} for user id={}", deckId, userId);

        // Validate content type and language combinations
        validateContentTypeAndLanguage(frontType, frontLanguage, "front");
        validateContentTypeAndLanguage(backType, backLanguage, "back");

        // Get deck and verify ownership
        Deck deck = deckService.getDeckOrThrow(deckId, userId);

        // Check for duplicate
        if (cardRepository.existsByDeckIdAndFrontAndBack(deckId, front, back)) {
            logger.warn("Duplicate card detected in deck id={}: front='{}', back='{}'", deckId, front, back);
            throw new IllegalArgumentException("A card with this front and back already exists in this deck");
        }

        // Create card and add to deck
        Card card = new Card(front, back);
        if (notes != null && !notes.isBlank()) {
            card.setNotes(notes);
        }

        // Set content types (default to TEXT if null)
        card.setFrontType(frontType != null ? frontType : ContentType.TEXT);
        card.setBackType(backType != null ? backType : ContentType.TEXT);
        card.setFrontLanguage(frontLanguage);
        card.setBackLanguage(backLanguage);

        deck.addCard(card);

        Card savedCard = cardRepository.save(card);

        // Re-fetch with tags eagerly loaded to prevent LazyInitializationException
        return cardRepository.findByIdWithTags(savedCard.getCardId()).orElseThrow();
    }

    /**
     * Validates that a language is provided when content type is CODE.
     *
     * @param contentType the content type
     * @param language the language
     * @param side "front" or "back" for error messages
     * @throws IllegalArgumentException if CODE type without language
     */
    private void validateContentTypeAndLanguage(ContentType contentType, CodeLanguage language, String side) {
        if (contentType == ContentType.CODE && language == null) {
            throw new IllegalArgumentException("Language is required when " + side + " content type is CODE");
        }
    }

    /**
     * Retrieves all cards in a deck with tags eagerly loaded.
     *
     * <p><strong>Security:</strong> Verifies user owns the deck.
     *
     * <p><strong>Performance:</strong> Uses EntityGraph to fetch deck with cards and tags
     * in a single query, preventing LazyInitializationException and N+1 queries.
     *
     * <p><strong>Important:</strong> Ownership check uses existsByIdAndUserId rather than
     * getDeckOrThrow to avoid loading the deck into the first-level cache without cards,
     * which would cause the subsequent EntityGraph query to return cached deck without cards.
     *
     * @param userId the ID of the user requesting the cards
     * @param deckId the ID of the deck
     * @return list of cards in the deck with tags loaded
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public List<Card> getDeckCards(Long userId, Long deckId) {
        logger.debug("Getting cards for deck id={}, user id={}", deckId, userId);

        // Verify deck ownership WITHOUT loading deck into cache (important!)
        // If we use getDeckOrThrow, the deck gets cached without cards, and
        // findByIdWithCardsAndTags may return the cached deck instead of fetching with EntityGraph
        if (!deckRepository.existsByIdAndUserId(deckId, userId)) {
            logger.warn("Deck not found or access denied: deck id={}, user id={}", deckId, userId);
            throw new IllegalArgumentException("Deck not found or access denied: " + deckId);
        }

        // Fetch deck with cards and tags eagerly loaded via EntityGraph
        Deck deck = deckRepository.findByIdWithCardsAndTags(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found: " + deckId));

        // Convert Set to List and sort by createdAt
        List<Card> cards = new ArrayList<>(deck.getCards());
        cards.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return a.getCreatedAt().compareTo(b.getCreatedAt());
        });

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
    public Optional<Card> getCard(Long userId, Long deckId, Long cardId) {
        logger.debug("Getting card id={} from deck id={} for user id={}", cardId, deckId, userId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Fetch card with tags eagerly loaded
        Optional<Card> card = cardRepository.findByIdAndDeckId(cardId, deckId);

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
     *   <li>Validates language is provided when content type is CODE</li>
     * </ul>
     *
     * @param userId the ID of the user updating the card
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to update
     * @param front the new front text
     * @param back the new back text
     * @param notes the new notes (can be null)
     * @param frontType content type for front (TEXT or CODE), defaults to TEXT
     * @param backType content type for back (TEXT or CODE), defaults to TEXT
     * @param frontLanguage programming language for front (required if frontType is CODE)
     * @param backLanguage programming language for back (required if backType is CODE)
     * @return the updated card with tags loaded
     * @throws IllegalArgumentException if user doesn't own deck, card not found, duplicate, or validation fails
     */
    @Transactional
    public Card updateCard(Long userId, Long deckId, Long cardId, String front, String back, String notes,
                           ContentType frontType, ContentType backType,
                           CodeLanguage frontLanguage, CodeLanguage backLanguage) {
        logger.debug("Updating card id={} in deck id={} for user id={}", cardId, deckId, userId);

        // Validate content type and language combinations
        validateContentTypeAndLanguage(frontType, frontLanguage, "front");
        validateContentTypeAndLanguage(backType, backLanguage, "back");

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Get existing card
        Card card = cardRepository.findByIdAndDeckId(cardId, deckId)
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

        // Update content types (default to TEXT if null)
        card.setFrontType(frontType != null ? frontType : ContentType.TEXT);
        card.setBackType(backType != null ? backType : ContentType.TEXT);
        card.setFrontLanguage(frontLanguage);
        card.setBackLanguage(backLanguage);

        cardRepository.save(card);
        logger.info("Card updated: id={} in deck id={}", cardId, deckId);

        // Re-fetch with tags eagerly loaded to prevent LazyInitializationException
        return cardRepository.findByIdWithTags(cardId).orElseThrow();
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
        Card card = cardRepository.findByIdAndDeckId(cardId, deckId)
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
    public List<Card> searchCards(Long userId, Long deckId, String searchTerm) {
        logger.debug("Searching cards in deck id={} for term: {}", deckId, searchTerm);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Search with tags eagerly loaded
        List<Card> cards = cardRepository.searchByDeckId(deckId, searchTerm);

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
    public List<Card> getCardsByTag(Long userId, Long deckId, Long tagId) {
        logger.debug("Getting cards in deck id={} with tag id={}", deckId, tagId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Verify tag exists and belongs to deck
        tagService.getTag(userId, deckId, tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found in this deck"));

        // Fetch cards with tags eagerly loaded
        List<Card> cards = cardRepository.findByDeckIdAndTagId(deckId, tagId);

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
    public Card addTagToCard(Long userId, Long deckId, Long cardId, Long tagId) {
        logger.debug("Adding tag id={} to card id={} in deck id={}", tagId, cardId, deckId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Get card
        Card card = cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> {
                    logger.warn("Card id={} not found in deck id={}", cardId, deckId);
                    return new IllegalArgumentException("Card not found in this deck");
                });

        // Get tag and verify it belongs to the same deck
        Tag tag = tagService.getTag(userId, deckId, tagId)
                .orElseThrow(() -> {
                    logger.warn("Tag id={} not found in deck id={}", tagId, deckId);
                    return new IllegalArgumentException("Tag not found in this deck");
                });

        // Add tag to card (Set prevents duplicates automatically)
        card.addTag(tag);

        cardRepository.save(card);
        logger.info("Tag id={} added to card id={}", tagId, cardId);

        // Re-fetch with tags eagerly loaded to prevent LazyInitializationException
        return cardRepository.findByIdWithTags(cardId).orElseThrow();
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
    public Card removeTagFromCard(Long userId, Long deckId, Long cardId, Long tagId) {
        logger.debug("Removing tag id={} from card id={} in deck id={}", tagId, cardId, deckId);

        // Verify deck ownership
        deckService.getDeckOrThrow(deckId, userId);

        // Get card
        Card card = cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> {
                    logger.warn("Card id={} not found in deck id={}", cardId, deckId);
                    return new IllegalArgumentException("Card not found in this deck");
                });

        // Get tag
        Tag tag = tagService.getTag(userId, deckId, tagId)
                .orElseThrow(() -> {
                    logger.warn("Tag id={} not found in deck id={}", tagId, deckId);
                    return new IllegalArgumentException("Tag not found in this deck");
                });

        // Remove tag from card
        card.removeTag(tag);

        cardRepository.save(card);
        logger.info("Tag id={} removed from card id={}", tagId, cardId);

        // Re-fetch with tags eagerly loaded to prevent LazyInitializationException
        return cardRepository.findByIdWithTags(cardId).orElseThrow();
    }
}