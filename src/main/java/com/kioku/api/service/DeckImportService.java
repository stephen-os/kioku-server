package com.kioku.api.service;

import com.kioku.api.dto.request.CardImportDto;
import com.kioku.api.dto.request.DeckImportRequest;
import com.kioku.api.entity.CardEntity;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service for importing complete decks with cards and tags.
 *
 * <p>This service orchestrates the import process by coordinating with
 * DeckService, CardService, and TagService to create a complete deck
 * in a single atomic transaction.
 *
 * <p><strong>Import Process:</strong>
 * <ol>
 *   <li>Validate deck name is unique for the user</li>
 *   <li>Create the deck</li>
 *   <li>Create all tags (deduplicated by name)</li>
 *   <li>Create all cards (checking for duplicates within import)</li>
 *   <li>Associate tags with cards</li>
 * </ol>
 *
 * <p><strong>Transaction Behavior:</strong>
 * The entire import is wrapped in a single transaction. If any step fails,
 * the entire import is rolled back, leaving no partial data.
 *
 * <p><strong>Duplicate Detection:</strong>
 * <ul>
 *   <li>Deck name must be unique for the user</li>
 *   <li>Cards with same front/back within the import are rejected</li>
 *   <li>Tag names are deduplicated (case-sensitive)</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Service
public class DeckImportService {

    private static final Logger logger = LoggerFactory.getLogger(DeckImportService.class);

    private final DeckService deckService;
    private final CardService cardService;
    private final TagService tagService;

    /**
     * Constructs a DeckImportService with required dependencies.
     *
     * @param deckService the deck service
     * @param cardService the card service
     * @param tagService the tag service
     */
    public DeckImportService(DeckService deckService, CardService cardService, TagService tagService) {
        this.deckService = deckService;
        this.cardService = cardService;
        this.tagService = tagService;
    }

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
     * <p><strong>Example Usage:</strong>
     * <pre>
     * DeckImportRequest request = new DeckImportRequest(
     *     "Japanese N5",
     *     "JLPT N5 vocabulary",
     *     cards,
     *     tags
     * );
     * DeckEntity deck = importService.importDeck(userId, request);
     * </pre>
     *
     * @param userId the ID of the user importing the deck
     * @param request the import request containing deck, cards, and tags
     * @return the created deck entity
     * @throws IllegalArgumentException if deck name already exists, duplicate cards found, or validation fails
     */
    @Transactional
    public DeckEntity importDeck(Long userId, DeckImportRequest request) {
        logger.debug("Importing deck for user id={}: name={}, {} cards, {} tags",
                userId, request.getName(), request.getCardCount(), request.getTagCount());

        // 1. Check for duplicate deck name
        if (deckService.isDuplicateName(userId, request.getName())) {
            logger.warn("Import failed: Deck name '{}' already exists for user id={}", request.getName(), userId);
            throw new IllegalArgumentException("A deck with this name already exists");
        }

        // 2. Create the deck
        DeckEntity deck = deckService.createDeck(userId, request.getName(), request.getDescription());
        logger.debug("Deck created: deckId={}", deck.getId());

        // 3. Create tags (deduplicate by name)
        Map<String, TagEntity> tagMap = createTags(userId, deck.getId(), request);
        logger.debug("Created {} unique tags", tagMap.size());

        // 4. Create cards and associate tags
        createCardsWithTags(userId, deck.getId(), request, tagMap);

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
    private Map<String, TagEntity> createTags(Long userId, Long deckId, DeckImportRequest request) {
        Map<String, TagEntity> tagMap = new HashMap<>();

        // Add tags from explicit tag list
        if (request.hasTags()) {
            for (var tagDto : request.getTags()) {
                String tagName = tagDto.getName();
                if (!tagMap.containsKey(tagName)) {
                    TagEntity tag = tagService.createTag(userId, deckId, tagName);
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
    private void createCardsWithTags(Long userId, Long deckId, DeckImportRequest request,
                                     Map<String, TagEntity> tagMap) {
        Set<String> processedCards = new HashSet<>();
        int cardIndex = 0;

        for (CardImportDto cardDto : request.getCards()) {
            cardIndex++;

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
            CardEntity card = cardService.createCard(
                    userId,
                    deckId,
                    cardDto.getFront(),
                    cardDto.getBack(),
                    cardDto.getNotes()
            );
            logger.debug("Created card {}/{}: {}", cardIndex, request.getCardCount(), cardDto.getFront());

            // Associate tags with the card
            if (cardDto.hasTags()) {
                associateTagsWithCard(userId, deckId, card.getId(), cardDto, tagMap);
            }
        }
    }

    /**
     * Associates tags with a card, creating tags if they don't exist.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardId the card ID
     * @param cardDto the card import DTO
     * @param tagMap the map of existing tags
     */
    private void associateTagsWithCard(Long userId, Long deckId, Long cardId,
                                       CardImportDto cardDto, Map<String, TagEntity> tagMap) {
        for (String tagName : cardDto.getTags()) {
            // Create tag if it doesn't exist yet
            if (!tagMap.containsKey(tagName)) {
                TagEntity tag = tagService.createTag(userId, deckId, tagName);
                tagMap.put(tagName, tag);
                logger.debug("Created tag from card reference: {}", tagName);
            }

            // Add tag to card
            TagEntity tag = tagMap.get(tagName);
            cardService.addTagToCard(userId, deckId, cardId, tag.getId());
        }
    }
}