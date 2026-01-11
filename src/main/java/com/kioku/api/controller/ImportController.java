package com.kioku.api.controller;

import com.kioku.api.dto.request.CardImportDto;
import com.kioku.api.dto.request.DeckImportRequest;
import com.kioku.api.dto.response.DeckResponse;
import com.kioku.api.model.Card;
import com.kioku.api.model.CodeLanguage;
import com.kioku.api.model.ContentType;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import com.kioku.api.repository.DeckRepository;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.DeckService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for import operations.
 *
 * <p>This controller provides endpoints for importing data into the system,
 * orchestrating multiple services to handle bulk operations.
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>POST /api/import/deck - Import a complete deck with cards and tags</li>
 * </ul>
 *
 * <p><strong>Implementation Note:</strong>
 * This controller creates cards and tags directly on the Deck entity rather than
 * using CardService/TagService to avoid JPA relationship issues. The Deck has
 * unidirectional @OneToMany relationships with orphanRemoval=true, and reloading
 * the deck multiple times within a transaction causes Hibernate to orphan previously
 * created cards.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/import")
public class ImportController {

    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);

    private final DeckService deckService;
    private final DeckRepository deckRepository;

    public ImportController(DeckService deckService, DeckRepository deckRepository) {
        this.deckService = deckService;
        this.deckRepository = deckRepository;
    }

    /**
     * Imports a complete deck with cards and tags.
     *
     * <p>This endpoint accepts a JSON payload containing a deck with all its cards
     * and tags, and creates them in a single atomic transaction.
     *
     * <p><strong>Request Body Example:</strong>
     * <pre>
     * {
     *   "name": "Japanese N5 Vocabulary",
     *   "description": "Essential JLPT N5 vocabulary",
     *   "cards": [
     *     {
     *       "front": "食べる",
     *       "back": "to eat",
     *       "notes": "ru-verb",
     *       "tags": ["verbs", "food"]
     *     }
     *   ],
     *   "tags": [
     *     {"name": "verbs"},
     *     {"name": "food"}
     *   ]
     * }
     * </pre>
     *
     * @param userId the ID of the authenticated user
     * @param request the deck import request
     * @return the created deck response
     */
    @PostMapping("/deck")
    @Transactional
    public ResponseEntity<DeckResponse> importDeck(
            @CurrentUser Long userId,
            @Valid @RequestBody DeckImportRequest request) {

        logger.debug("Import deck request from user id={}: name={}, {} cards, {} tags",
                userId, request.getName(), request.getCardCount(), request.getTagCount());

        // 1. Check for duplicate deck name
        if (deckService.isDuplicateName(userId, request.getName())) {
            logger.warn("Import failed: Deck name '{}' already exists for user id={}", request.getName(), userId);
            throw new IllegalArgumentException("A deck with this name already exists");
        }

        // 2. Create the deck - IMPORTANT: keep this single instance for all operations
        Deck deck = deckService.createDeck(userId, request.getName(), request.getDescription());
        logger.debug("Deck created: deckId={}", deck.getId());

        // 3. Create tags directly on the deck (deduplicate by name)
        Map<String, Tag> tagMap = createTagsOnDeck(deck, request);
        logger.debug("Created {} unique tags", tagMap.size());

        // 4. Create cards directly on the deck and associate tags
        createCardsOnDeck(deck, request, tagMap);

        // 5. Save the deck - cascade will persist cards and tags
        deckRepository.save(deck);

        logger.info("Deck imported successfully: deckId={}, name='{}', {} cards, {} tags",
                deck.getId(), deck.getName(), deck.getCardCount(), tagMap.size());

        return ResponseEntity.status(HttpStatus.CREATED).body(new DeckResponse(deck));
    }

    /**
     * Creates all tags directly on the deck instance, deduplicating by name.
     * This avoids reloading the deck from the database which would cause JPA
     * relationship issues with orphanRemoval.
     */
    private Map<String, Tag> createTagsOnDeck(Deck deck, DeckImportRequest request) {
        Map<String, Tag> tagMap = new HashMap<>();

        if (request.hasTags()) {
            for (var tagDto : request.getTags()) {
                String tagName = tagDto.getName();
                if (!tagMap.containsKey(tagName)) {
                    Tag tag = new Tag(tagName);
                    deck.addTag(tag);
                    tagMap.put(tagName, tag);
                    logger.debug("Created tag: {}", tagName);
                }
            }
        }

        return tagMap;
    }

    /**
     * Creates all cards directly on the deck instance and associates them with tags.
     * This avoids reloading the deck from the database which would cause JPA
     * relationship issues with orphanRemoval.
     */
    private void createCardsOnDeck(Deck deck, DeckImportRequest request, Map<String, Tag> tagMap) {
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

            // Validate content type and language combinations
            validateContentTypeAndLanguage(cardDto.getFrontType(), cardDto.getFrontLanguage(), "front", cardIndex);
            validateContentTypeAndLanguage(cardDto.getBackType(), cardDto.getBackLanguage(), "back", cardIndex);

            // Create the card directly
            Card card = new Card(cardDto.getFront(), cardDto.getBack());
            if (cardDto.getNotes() != null && !cardDto.getNotes().isBlank()) {
                card.setNotes(cardDto.getNotes());
            }

            // Set content types (default to TEXT if null)
            card.setFrontType(cardDto.getFrontType() != null ? cardDto.getFrontType() : ContentType.TEXT);
            card.setBackType(cardDto.getBackType() != null ? cardDto.getBackType() : ContentType.TEXT);
            card.setFrontLanguage(cardDto.getFrontLanguage());
            card.setBackLanguage(cardDto.getBackLanguage());

            // Associate tags with the card
            if (cardDto.hasTags()) {
                for (String tagName : cardDto.getTags()) {
                    // Create tag if it doesn't exist yet
                    if (!tagMap.containsKey(tagName)) {
                        Tag tag = new Tag(tagName);
                        deck.addTag(tag);
                        tagMap.put(tagName, tag);
                        logger.debug("Created tag from card reference: {}", tagName);
                    }
                    card.addTag(tagMap.get(tagName));
                }
            }

            // Add card to deck - the cascade will handle persistence
            deck.addCard(card);
            logger.debug("Created card {}/{}: {}", cardIndex, request.getCardCount(), cardDto.getFront());
        }
    }

    /**
     * Validates that a language is provided when content type is CODE.
     *
     * @param contentType the content type
     * @param language the language
     * @param side "front" or "back" for error messages
     * @param cardIndex the card index for error messages
     * @throws IllegalArgumentException if CODE type without language
     */
    private void validateContentTypeAndLanguage(ContentType contentType, CodeLanguage language, String side, int cardIndex) {
        if (contentType == ContentType.CODE && language == null) {
            throw new IllegalArgumentException(
                    String.format("Card at index %d: language is required when %s content type is CODE", cardIndex, side));
        }
    }
}
