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
 * Service layer for Card entity business logic.
 *
 * <p>This service provides:
 * <ul>
 *   <li>Card creation with duplicate detection</li>
 *   <li>Card retrieval with ownership verification</li>
 *   <li>Card updates with duplicate prevention</li>
 *   <li>Card deletion</li>
 *   <li>Card search by content</li>
 *   <li>Tag management for cards</li>
 * </ul>
 *
 * <p><strong>Security:</strong> All methods verify that the user owns the deck
 * before performing operations. Methods that accept {@code userId} will check
 * ownership and throw exceptions if access is denied.
 *
 * <p><strong>Transaction Management:</strong>
 * <ul>
 *   <li>Class is annotated with {@code @Transactional} for write operations</li>
 *   <li>Read-only methods should use {@code @Transactional(readOnly = true)}</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final DeckService deckService;
    private final TagService tagService;

    /**
     * Constructs a new CardService.
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
     * <p>Verifies that:
     * <ul>
     *   <li>The user owns the deck</li>
     *   <li>No duplicate card exists (same front and back in the deck)</li>
     * </ul>
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param front the front text (question/prompt)
     * @param back the back text (answer/translation)
     * @param notes optional notes
     * @return the created card
     * @throws IllegalArgumentException if user doesn't own deck or duplicate exists
     */
    public CardEntity createCard(Long userId, Long deckId, String front, String back, String notes) {
        logger.debug("Creating card in deck id={} for user id={}", deckId, userId);

        // Verify user owns the deck
        DeckEntity deckEntity = deckService.getDeckOrThrow(deckId, userId);

        // Check for duplicate
        if (cardRepository.existsByDeckIdAndFrontAndBack(deckId, front, back)) {
            logger.warn("Card creation failed: duplicate card in deck id={}", deckId);
            throw new IllegalArgumentException("Card with same front and back already exists in this deck");
        }

        CardEntity card = new CardEntity(deckEntity, front, back, notes);
        CardEntity savedCard = cardRepository.save(card);

        logger.debug("Card created successfully with id={}", savedCard.getId());
        return savedCard;
    }

    /**
     * Gets all cards in a deck.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @return list of cards in the deck
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public List<CardEntity> getDeckCards(Long userId, Long deckId) {
        logger.debug("Getting cards for deck id={}, user id={}", deckId, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        List<CardEntity> cards = cardRepository.findByDeckId(deckId);
        logger.debug("Found {} cards in deck id={}", cards.size(), deckId);

        return cards;
    }

    /**
     * Gets a specific card.
     *
     * <p>Verifies that the user owns the deck and the card exists in that deck.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardId the card ID
     * @return an Optional containing the card if found and owned, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<CardEntity> getCard(Long userId, Long deckId, Long cardId) {
        logger.debug("Getting card id={} from deck id={} for user id={}", cardId, deckId, userId);

        // Verify user owns the deck
        if (!deckService.userOwnsDeck(deckId, userId)) {
            logger.warn("Access denied: user id={} does not own deck id={}", userId, deckId);
            return Optional.empty();
        }

        Optional<CardEntity> card = cardRepository.findByIdAndDeckId(cardId, deckId);
        logger.debug("Card found: {}", card.isPresent());

        return card;
    }

    /**
     * Gets a card or throws exception if not found or not owned.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardId the card ID
     * @return the card
     * @throws IllegalArgumentException if card not found or access denied
     */
    @Transactional(readOnly = true)
    public CardEntity getCardOrThrow(Long userId, Long deckId, Long cardId) {
        return getCard(userId, deckId, cardId)
                .orElseThrow(() -> {
                    logger.warn("Card not found or access denied: card id={}, deck id={}, user id={}",
                            cardId, deckId, userId);
                    return new IllegalArgumentException("Card not found or access denied: " + cardId);
                });
    }

    /**
     * Updates a card.
     *
     * <p>Verifies that:
     * <ul>
     *   <li>The user owns the deck</li>
     *   <li>The update doesn't create a duplicate</li>
     * </ul>
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardId the card ID
     * @param front the new front text
     * @param back the new back text
     * @param notes the new notes (or null to clear)
     * @return the updated card
     * @throws IllegalArgumentException if access denied or update would create duplicate
     */
    public CardEntity updateCard(Long userId, Long deckId, Long cardId, String front, String back, String notes) {
        logger.debug("Updating card id={} in deck id={} for user id={}", cardId, deckId, userId);

        CardEntity card = getCardOrThrow(userId, deckId, cardId);

        // Check if update would create duplicate (only if front or back changed)
        if (!card.getFront().equals(front) || !card.getBack().equals(back)) {
            if (cardRepository.existsByDeckIdAndFrontAndBack(deckId, front, back)) {
                logger.warn("Card update failed: would create duplicate in deck id={}", deckId);
                throw new IllegalArgumentException("Card with same front and back already exists in this deck");
            }
        }

        card.setFront(front);
        card.setBack(back);
        card.setNotes(notes);

        CardEntity updatedCard = cardRepository.save(card);
        logger.debug("Card id={} updated successfully", cardId);

        return updatedCard;
    }

    /**
     * Deletes a card.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardId the card ID
     * @throws IllegalArgumentException if card not found or access denied
     */
    public void deleteCard(Long userId, Long deckId, Long cardId) {
        logger.debug("Deleting card id={} from deck id={} for user id={}", cardId, deckId, userId);

        CardEntity card = getCardOrThrow(userId, deckId, cardId);
        cardRepository.delete(card);

        logger.debug("Card id={} deleted successfully", cardId);
    }

    /**
     * Searches for cards in a deck by text content.
     *
     * <p>Searches both front and back text using case-insensitive partial matching.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param searchTerm the text to search for
     * @return list of matching cards
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public List<CardEntity> searchCards(Long userId, Long deckId, String searchTerm) {
        logger.debug("Searching cards in deck id={} for term='{}', user id={}", deckId, searchTerm, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        List<CardEntity> cards = cardRepository.searchByDeckId(deckId, searchTerm);
        logger.debug("Found {} cards matching search term", cards.size());

        return cards;
    }

    /**
     * Gets all cards in a deck that have a specific tag.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param tagId the tag ID
     * @return list of cards with the specified tag
     * @throws IllegalArgumentException if user doesn't own deck or tag
     */
    @Transactional(readOnly = true)
    public List<CardEntity> getCardsByTag(Long userId, Long deckId, Long tagId) {
        logger.debug("Getting cards by tag id={} in deck id={} for user id={}", tagId, deckId, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        // Verify user owns the tag
        tagService.getTagOrThrow(userId, tagId);

        List<CardEntity> cards = cardRepository.findByDeckIdAndTagId(deckId, tagId);
        logger.debug("Found {} cards with tag id={}", cards.size(), tagId);

        return cards;
    }

    /**
     * Adds a tag to a card.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardId the card ID
     * @param tagId the tag ID
     * @return the updated card
     * @throws IllegalArgumentException if access denied or tag already exists
     */
    public CardEntity addTagToCard(Long userId, Long deckId, Long cardId, Long tagId) {
        logger.debug("Adding tag id={} to card id={} in deck id={} for user id={}",
                tagId, cardId, deckId, userId);

        CardEntity card = getCardOrThrow(userId, deckId, cardId);
        TagEntity tag = tagService.getTagOrThrow(userId, tagId);

        card.addTag(tag);
        CardEntity updatedCard = cardRepository.save(card);

        logger.debug("Tag id={} added to card id={} successfully", tagId, cardId);
        return updatedCard;
    }

    /**
     * Removes a tag from a card.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardId the card ID
     * @param tagId the tag ID
     * @return the updated card
     * @throws IllegalArgumentException if access denied
     */
    public CardEntity removeTagFromCard(Long userId, Long deckId, Long cardId, Long tagId) {
        logger.debug("Removing tag id={} from card id={} in deck id={} for user id={}",
                tagId, cardId, deckId, userId);

        CardEntity card = getCardOrThrow(userId, deckId, cardId);
        TagEntity tag = tagService.getTagOrThrow(userId, tagId);

        card.removeTag(tag);
        CardEntity updatedCard = cardRepository.save(card);

        logger.debug("Tag id={} removed from card id={} successfully", tagId, cardId);
        return updatedCard;
    }

    /**
     * Checks if a card with the same front and back exists in a deck.
     *
     * <p><strong>Note:</strong> This method does NOT verify deck ownership.
     * It's intended for internal use or public duplicate checking.
     *
     * @param deckId the deck ID
     * @param front the front text
     * @param back the back text
     * @return {@code true} if duplicate exists, {@code false} otherwise
     */
    @Transactional(readOnly = true)
    public boolean isDuplicate(Long deckId, String front, String back) {
        boolean exists = cardRepository.existsByDeckIdAndFrontAndBack(deckId, front, back);
        logger.debug("Duplicate check for deck id={}: {}", deckId, exists);
        return exists;
    }

    /**
     * Counts the number of cards in a deck.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @return the number of cards
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public long countCards(Long userId, Long deckId) {
        logger.debug("Counting cards in deck id={} for user id={}", deckId, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        long count = cardRepository.countByDeckId(deckId);
        logger.debug("Deck id={} has {} cards", deckId, count);

        return count;
    }
}