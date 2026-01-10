package com.kioku.api.service;

import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import com.kioku.api.repository.CardRepository;
import com.kioku.api.repository.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CardService.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Card creation with ownership verification and duplicate detection</li>
 *   <li>Card retrieval with ownership checks</li>
 *   <li>Card updates with duplicate prevention</li>
 *   <li>Card deletion</li>
 *   <li>Card search functionality</li>
 *   <li>Tag management (add, remove)</li>
 *   <li>Card counting</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CardService Unit Tests")
class CardServiceTests {

    private static final Logger logger = LoggerFactory.getLogger(CardServiceTests.class);

    // Test data constants
    private static final Long USER_ID = 1L;
    private static final Long DECK_ID = 1L;
    private static final Long CARD_ID = 1L;
    private static final Long TAG_ID = 1L;
    private static final String CARD_FRONT = "食べる";
    private static final String CARD_BACK = "to eat";
    private static final String CARD_NOTES = "ru-verb";
    private static final String UPDATED_FRONT = "飲む";
    private static final String UPDATED_BACK = "to drink";
    private static final String SEARCH_TERM = "eat";
    private static final String TAG_NAME = "verbs";

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private DeckService deckService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private CardService cardService;

    private Deck testDeck;
    private Card testCard;
    private Tag testTag;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up CardService test");

        testDeck = mock(Deck.class);
        when(testDeck.getId()).thenReturn(DECK_ID);

        testCard = mock(Card.class);
        when(testCard.getCardId()).thenReturn(CARD_ID);
        when(testCard.getFront()).thenReturn(CARD_FRONT);
        when(testCard.getBack()).thenReturn(CARD_BACK);
        // Note: Card no longer has getDeck() - relationship is unidirectional

        testTag = mock(Tag.class);
        when(testTag.getId()).thenReturn(TAG_ID);
        when(testTag.getName()).thenReturn(TAG_NAME);
    }

    // Card Creation Tests

    /**
     * Tests successful card creation.
     */
    @Test
    @DisplayName("Should create card successfully")
    void testCreateCard() {
        logger.debug("Test: Creating card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.existsByDeckIdAndFrontAndBack(DECK_ID, CARD_FRONT, CARD_BACK)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardRepository.findByIdWithTags(CARD_ID)).thenReturn(Optional.of(testCard));

        Card created = cardService.createCard(USER_ID, DECK_ID, CARD_FRONT, CARD_BACK, CARD_NOTES);

        assertNotNull(created);
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(cardRepository).existsByDeckIdAndFrontAndBack(DECK_ID, CARD_FRONT, CARD_BACK);
        verify(cardRepository).save(any(Card.class));
        verify(cardRepository).findByIdWithTags(CARD_ID);

        logger.debug("Test passed: Card created successfully");
    }

    /**
     * Tests that creating card fails if user doesn't own deck.
     */
    @Test
    @DisplayName("Should throw exception when user doesn't own deck")
    void testCreateCardUserDoesNotOwnDeck() {
        logger.debug("Test: Creating card when user doesn't own deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(USER_ID, DECK_ID, CARD_FRONT, CARD_BACK, CARD_NOTES);
        });

        verify(cardRepository, never()).save(any(Card.class));

        logger.debug("Test passed: Exception thrown for unauthorized access");
    }

    /**
     * Tests that creating duplicate card fails.
     */
    @Test
    @DisplayName("Should throw exception when creating duplicate card")
    void testCreateCardDuplicate() {
        logger.debug("Test: Creating duplicate card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.existsByDeckIdAndFrontAndBack(DECK_ID, CARD_FRONT, CARD_BACK)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(USER_ID, DECK_ID, CARD_FRONT, CARD_BACK, CARD_NOTES);
        });

        verify(cardRepository, never()).save(any(Card.class));

        logger.debug("Test passed: Exception thrown for duplicate card");
    }

    // Card Retrieval Tests

    /**
     * Tests getting all cards in a deck.
     */
    @Test
    @DisplayName("Should get all cards in deck")
    void testGetDeckCards() {
        logger.debug("Test: Getting all cards in deck");

        // Create a real deck with cards for this test
        Deck deckWithCards = mock(Deck.class);
        Card card1 = mock(Card.class);
        Card card2 = mock(Card.class);
        when(card1.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(card2.getCreatedAt()).thenReturn(java.time.LocalDateTime.now().plusSeconds(1));
        java.util.Set<Card> cardSet = new java.util.HashSet<>(Arrays.asList(card1, card2));
        when(deckWithCards.getCards()).thenReturn(cardSet);

        // Use existsByIdAndUserId for ownership check (not getDeckOrThrow to avoid cache issues)
        when(deckRepository.existsByIdAndUserId(DECK_ID, USER_ID)).thenReturn(true);
        when(deckRepository.findByIdWithCardsAndTags(DECK_ID)).thenReturn(Optional.of(deckWithCards));

        List<Card> cards = cardService.getDeckCards(USER_ID, DECK_ID);

        assertEquals(2, cards.size());
        verify(deckRepository).existsByIdAndUserId(DECK_ID, USER_ID);
        verify(deckRepository).findByIdWithCardsAndTags(DECK_ID);

        logger.debug("Test passed: Got {} cards", cards.size());
    }

    /**
     * Tests that getting cards fails if user doesn't own deck.
     */
    @Test
    @DisplayName("Should throw exception when getting cards from unowned deck")
    void testGetDeckCardsUnauthorized() {
        logger.debug("Test: Getting cards from unowned deck");

        when(deckRepository.existsByIdAndUserId(DECK_ID, USER_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getDeckCards(USER_ID, DECK_ID);
        });

        verify(deckRepository, never()).findByIdWithCardsAndTags(anyLong());

        logger.debug("Test passed: Exception thrown for unauthorized access");
    }

    /**
     * Tests getting a specific card.
     */
    @Test
    @DisplayName("Should get specific card")
    void testGetCard() {
        logger.debug("Test: Getting specific card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));

        Optional<Card> card = cardService.getCard(USER_ID, DECK_ID, CARD_ID);

        assertTrue(card.isPresent());
        assertEquals(testCard, card.get());
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(cardRepository).findByIdAndDeckId(CARD_ID, DECK_ID);

        logger.debug("Test passed: Card retrieved successfully");
    }

    /**
     * Tests that getting card throws exception if user doesn't own deck.
     */
    @Test
    @DisplayName("Should throw exception when user doesn't own deck")
    void testGetCardUnauthorized() {
        logger.debug("Test: Getting card when user doesn't own deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getCard(USER_ID, DECK_ID, CARD_ID);
        });

        verify(cardRepository, never()).findByIdAndDeckId(anyLong(), anyLong());

        logger.debug("Test passed: Exception thrown for unauthorized access");
    }

    /**
     * Tests that getCard returns empty Optional when card not found.
     */
    @Test
    @DisplayName("Should return empty when card not found")
    void testGetCardNotFound() {
        logger.debug("Test: Getting non-existent card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.empty());

        Optional<Card> card = cardService.getCard(USER_ID, DECK_ID, CARD_ID);

        assertFalse(card.isPresent());

        logger.debug("Test passed: Empty returned for not found");
    }

    // Card Update Tests

    /**
     * Tests successful card update.
     */
    @Test
    @DisplayName("Should update card successfully")
    void testUpdateCard() {
        logger.debug("Test: Updating card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(cardRepository.existsByDeckIdAndFrontAndBackAndIdNot(DECK_ID, UPDATED_FRONT, UPDATED_BACK, CARD_ID)).thenReturn(false);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardRepository.findByIdWithTags(CARD_ID)).thenReturn(Optional.of(testCard));

        Card updated = cardService.updateCard(USER_ID, DECK_ID, CARD_ID, UPDATED_FRONT, UPDATED_BACK, CARD_NOTES);

        assertNotNull(updated);
        verify(testCard).setFront(UPDATED_FRONT);
        verify(testCard).setBack(UPDATED_BACK);
        verify(testCard).setNotes(CARD_NOTES);
        verify(cardRepository).save(testCard);
        verify(cardRepository).findByIdWithTags(CARD_ID);

        logger.debug("Test passed: Card updated successfully");
    }

    /**
     * Tests that update fails if it would create duplicate.
     */
    @Test
    @DisplayName("Should throw exception when update would create duplicate")
    void testUpdateCardDuplicate() {
        logger.debug("Test: Updating card to duplicate values");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(cardRepository.existsByDeckIdAndFrontAndBackAndIdNot(DECK_ID, UPDATED_FRONT, UPDATED_BACK, CARD_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.updateCard(USER_ID, DECK_ID, CARD_ID, UPDATED_FRONT, UPDATED_BACK, CARD_NOTES);
        });

        verify(cardRepository, never()).save(any(Card.class));

        logger.debug("Test passed: Exception thrown for duplicate");
    }

    /**
     * Tests that updating to same values checks for duplicates but passes.
     */
    @Test
    @DisplayName("Should allow update with same front and back")
    void testUpdateCardSameValues() {
        logger.debug("Test: Updating card with same front and back");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(cardRepository.existsByDeckIdAndFrontAndBackAndIdNot(DECK_ID, CARD_FRONT, CARD_BACK, CARD_ID)).thenReturn(false);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardRepository.findByIdWithTags(CARD_ID)).thenReturn(Optional.of(testCard));

        // Update to same front/back but different notes
        Card updated = cardService.updateCard(USER_ID, DECK_ID, CARD_ID, CARD_FRONT, CARD_BACK, "New notes");

        assertNotNull(updated);
        verify(cardRepository).save(testCard);
        verify(cardRepository).findByIdWithTags(CARD_ID);

        logger.debug("Test passed: Update with same values allowed");
    }

    // Card Deletion Tests

    /**
     * Tests successful card deletion.
     */
    @Test
    @DisplayName("Should delete card successfully")
    void testDeleteCard() {
        logger.debug("Test: Deleting card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));

        cardService.deleteCard(USER_ID, DECK_ID, CARD_ID);

        verify(cardRepository).delete(testCard);

        logger.debug("Test passed: Card deleted successfully");
    }

    /**
     * Tests that deleting non-existent card throws exception.
     */
    @Test
    @DisplayName("Should throw exception when deleting non-existent card")
    void testDeleteCardNotFound() {
        logger.debug("Test: Deleting non-existent card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.deleteCard(USER_ID, DECK_ID, CARD_ID);
        });

        verify(cardRepository, never()).delete(any(Card.class));

        logger.debug("Test passed: Exception thrown for not found");
    }

    // Search Tests

    /**
     * Tests searching cards.
     */
    @Test
    @DisplayName("Should search cards successfully")
    void testSearchCards() {
        logger.debug("Test: Searching cards");

        List<Card> expectedCards = Arrays.asList(testCard);
        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.searchByDeckId(DECK_ID, SEARCH_TERM)).thenReturn(expectedCards);

        List<Card> results = cardService.searchCards(USER_ID, DECK_ID, SEARCH_TERM);

        assertEquals(1, results.size());
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(cardRepository).searchByDeckId(DECK_ID, SEARCH_TERM);

        logger.debug("Test passed: Search returned {} results", results.size());
    }

    /**
     * Tests that searching fails if user doesn't own deck.
     */
    @Test
    @DisplayName("Should throw exception when searching unowned deck")
    void testSearchCardsUnauthorized() {
        logger.debug("Test: Searching cards in unowned deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.searchCards(USER_ID, DECK_ID, SEARCH_TERM);
        });

        verify(cardRepository, never()).searchByDeckId(anyLong(), anyString());

        logger.debug("Test passed: Exception thrown for unauthorized access");
    }

    // Tag Tests

    /**
     * Tests getting cards by tag.
     */
    @Test
    @DisplayName("Should get cards by tag")
    void testGetCardsByTag() {
        logger.debug("Test: Getting cards by tag");

        List<Card> expectedCards = Arrays.asList(testCard);
        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(tagService.getTag(USER_ID, DECK_ID, TAG_ID)).thenReturn(Optional.of(testTag));
        when(cardRepository.findByDeckIdAndTagId(DECK_ID, TAG_ID)).thenReturn(expectedCards);

        List<Card> results = cardService.getCardsByTag(USER_ID, DECK_ID, TAG_ID);

        assertEquals(1, results.size());
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(tagService).getTag(USER_ID, DECK_ID, TAG_ID);
        verify(cardRepository).findByDeckIdAndTagId(DECK_ID, TAG_ID);

        logger.debug("Test passed: Got {} cards with tag", results.size());
    }

    /**
     * Tests that getting cards by tag fails if user doesn't own deck.
     */
    @Test
    @DisplayName("Should throw exception when getting cards by tag from unowned deck")
    void testGetCardsByTagUnownedDeck() {
        logger.debug("Test: Getting cards by tag from unowned deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getCardsByTag(USER_ID, DECK_ID, TAG_ID);
        });

        verify(cardRepository, never()).findByDeckIdAndTagId(anyLong(), anyLong());

        logger.debug("Test passed: Exception thrown for unowned deck");
    }

    /**
     * Tests that getting cards by tag fails if tag not found.
     */
    @Test
    @DisplayName("Should throw exception when tag not found")
    void testGetCardsByTagNotFound() {
        logger.debug("Test: Getting cards by non-existent tag");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(tagService.getTag(USER_ID, DECK_ID, TAG_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getCardsByTag(USER_ID, DECK_ID, TAG_ID);
        });

        verify(cardRepository, never()).findByDeckIdAndTagId(anyLong(), anyLong());

        logger.debug("Test passed: Exception thrown for tag not found");
    }

    /**
     * Tests adding tag to card.
     */
    @Test
    @DisplayName("Should add tag to card")
    void testAddTagToCard() {
        logger.debug("Test: Adding tag to card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(tagService.getTag(USER_ID, DECK_ID, TAG_ID)).thenReturn(Optional.of(testTag));
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardRepository.findByIdWithTags(CARD_ID)).thenReturn(Optional.of(testCard));

        Card result = cardService.addTagToCard(USER_ID, DECK_ID, CARD_ID, TAG_ID);

        assertNotNull(result);
        verify(testCard).addTag(testTag);
        verify(cardRepository).save(testCard);
        verify(cardRepository).findByIdWithTags(CARD_ID);

        logger.debug("Test passed: Tag added to card");
    }

    /**
     * Tests removing tag from card.
     */
    @Test
    @DisplayName("Should remove tag from card")
    void testRemoveTagFromCard() {
        logger.debug("Test: Removing tag from card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(tagService.getTag(USER_ID, DECK_ID, TAG_ID)).thenReturn(Optional.of(testTag));
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardRepository.findByIdWithTags(CARD_ID)).thenReturn(Optional.of(testCard));

        Card result = cardService.removeTagFromCard(USER_ID, DECK_ID, CARD_ID, TAG_ID);

        assertNotNull(result);
        verify(testCard).removeTag(testTag);
        verify(cardRepository).save(testCard);
        verify(cardRepository).findByIdWithTags(CARD_ID);

        logger.debug("Test passed: Tag removed from card");
    }
}