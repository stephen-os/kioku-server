package com.kioku.api.service;

import com.kioku.api.entity.CardEntity;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.repository.CardRepository;
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
class CardServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CardServiceTest.class);

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
    private DeckService deckService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private CardService cardService;

    private DeckEntity testDeckEntity;
    private CardEntity testCard;
    private TagEntity testTagEntity;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up CardService test");

        testDeckEntity = mock(DeckEntity.class);
        when(testDeckEntity.getId()).thenReturn(DECK_ID);

        testCard = mock(CardEntity.class);
        when(testCard.getId()).thenReturn(CARD_ID);
        when(testCard.getFront()).thenReturn(CARD_FRONT);
        when(testCard.getBack()).thenReturn(CARD_BACK);
        when(testCard.getDeck()).thenReturn(testDeckEntity);

        testTagEntity = mock(TagEntity.class);
        when(testTagEntity.getId()).thenReturn(TAG_ID);
        when(testTagEntity.getName()).thenReturn(TAG_NAME);
    }

    // Card Creation Tests

    /**
     * Tests successful card creation.
     */
    @Test
    @DisplayName("Should create card successfully")
    void testCreateCard() {
        logger.debug("Test: Creating card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeckEntity);
        when(cardRepository.existsByDeckIdAndFrontAndBack(DECK_ID, CARD_FRONT, CARD_BACK)).thenReturn(false);
        when(cardRepository.save(any(CardEntity.class))).thenReturn(testCard);

        CardEntity created = cardService.createCard(USER_ID, DECK_ID, CARD_FRONT, CARD_BACK, CARD_NOTES);

        assertNotNull(created);
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(cardRepository).existsByDeckIdAndFrontAndBack(DECK_ID, CARD_FRONT, CARD_BACK);
        verify(cardRepository).save(any(CardEntity.class));

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

        verify(cardRepository, never()).save(any(CardEntity.class));

        logger.debug("Test passed: Exception thrown for unauthorized access");
    }

    /**
     * Tests that creating duplicate card fails.
     */
    @Test
    @DisplayName("Should throw exception when creating duplicate card")
    void testCreateCardDuplicate() {
        logger.debug("Test: Creating duplicate card");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeckEntity);
        when(cardRepository.existsByDeckIdAndFrontAndBack(DECK_ID, CARD_FRONT, CARD_BACK)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(USER_ID, DECK_ID, CARD_FRONT, CARD_BACK, CARD_NOTES);
        });

        verify(cardRepository, never()).save(any(CardEntity.class));

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

        List<CardEntity> expectedCards = Arrays.asList(testCard, mock(CardEntity.class));
        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeckEntity);
        when(cardRepository.findByDeckId(DECK_ID)).thenReturn(expectedCards);

        List<CardEntity> cards = cardService.getDeckCards(USER_ID, DECK_ID);

        assertEquals(2, cards.size());
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(cardRepository).findByDeckId(DECK_ID);

        logger.debug("Test passed: Got {} cards", cards.size());
    }

    /**
     * Tests that getting cards fails if user doesn't own deck.
     */
    @Test
    @DisplayName("Should throw exception when getting cards from unowned deck")
    void testGetDeckCardsUnauthorized() {
        logger.debug("Test: Getting cards from unowned deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getDeckCards(USER_ID, DECK_ID);
        });

        verify(cardRepository, never()).findByDeckId(anyLong());

        logger.debug("Test passed: Exception thrown for unauthorized access");
    }

    /**
     * Tests getting a specific card.
     */
    @Test
    @DisplayName("Should get specific card")
    void testGetCard() {
        logger.debug("Test: Getting specific card");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));

        Optional<CardEntity> card = cardService.getCard(USER_ID, DECK_ID, CARD_ID);

        assertTrue(card.isPresent());
        assertEquals(testCard, card.get());
        verify(deckService).userOwnsDeck(DECK_ID, USER_ID);
        verify(cardRepository).findByIdAndDeckId(CARD_ID, DECK_ID);

        logger.debug("Test passed: Card retrieved successfully");
    }

    /**
     * Tests that getting card returns empty if user doesn't own deck.
     */
    @Test
    @DisplayName("Should return empty when user doesn't own deck")
    void testGetCardUnauthorized() {
        logger.debug("Test: Getting card when user doesn't own deck");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(false);

        Optional<CardEntity> card = cardService.getCard(USER_ID, DECK_ID, CARD_ID);

        assertFalse(card.isPresent());
        verify(cardRepository, never()).findByIdAndDeckId(anyLong(), anyLong());

        logger.debug("Test passed: Empty returned for unauthorized access");
    }

    /**
     * Tests that getCard returns card when found.
     */
    @Test
    @DisplayName("Should return card with getCard")
    void testgetCard() {
        logger.debug("Test: Getting card with getCard");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));

        Optional<CardEntity> card = cardService.getCard(USER_ID, DECK_ID, CARD_ID);

        assertNotNull(card.isPresent());
        assertEquals(testCard, card);

        logger.debug("Test passed: Card retrieved successfully");
    }

    /**
     * Tests that getCard throws exception when not found.
     */
    @Test
    @DisplayName("Should throw exception when card not found")
    void testgetCardNotFound() {
        logger.debug("Test: Getting non-existent card with getCard");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getCard(USER_ID, DECK_ID, CARD_ID);
        });

        logger.debug("Test passed: Exception thrown for not found");
    }

    // Card Update Tests

    /**
     * Tests successful card update.
     */
    @Test
    @DisplayName("Should update card successfully")
    void testUpdateCard() {
        logger.debug("Test: Updating card");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(testCard.getFront()).thenReturn(CARD_FRONT);
        when(testCard.getBack()).thenReturn(CARD_BACK);
        when(cardRepository.existsByDeckIdAndFrontAndBack(DECK_ID, UPDATED_FRONT, UPDATED_BACK)).thenReturn(false);
        when(cardRepository.save(testCard)).thenReturn(testCard);

        CardEntity updated = cardService.updateCard(USER_ID, DECK_ID, CARD_ID, UPDATED_FRONT, UPDATED_BACK, CARD_NOTES);

        assertNotNull(updated);
        verify(testCard).setFront(UPDATED_FRONT);
        verify(testCard).setBack(UPDATED_BACK);
        verify(testCard).setNotes(CARD_NOTES);
        verify(cardRepository).save(testCard);

        logger.debug("Test passed: Card updated successfully");
    }

    /**
     * Tests that update fails if it would create duplicate.
     */
    @Test
    @DisplayName("Should throw exception when update would create duplicate")
    void testUpdateCardDuplicate() {
        logger.debug("Test: Updating card to duplicate values");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(testCard.getFront()).thenReturn(CARD_FRONT);
        when(testCard.getBack()).thenReturn(CARD_BACK);
        when(cardRepository.existsByDeckIdAndFrontAndBack(DECK_ID, UPDATED_FRONT, UPDATED_BACK)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.updateCard(USER_ID, DECK_ID, CARD_ID, UPDATED_FRONT, UPDATED_BACK, CARD_NOTES);
        });

        verify(cardRepository, never()).save(any(CardEntity.class));

        logger.debug("Test passed: Exception thrown for duplicate");
    }

    /**
     * Tests that updating to same values doesn't check for duplicates.
     */
    @Test
    @DisplayName("Should allow update with same front and back")
    void testUpdateCardSameValues() {
        logger.debug("Test: Updating card with same front and back");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(testCard.getFront()).thenReturn(CARD_FRONT);
        when(testCard.getBack()).thenReturn(CARD_BACK);
        when(cardRepository.save(testCard)).thenReturn(testCard);

        // Update to same front/back but different notes
        CardEntity updated = cardService.updateCard(USER_ID, DECK_ID, CARD_ID, CARD_FRONT, CARD_BACK, "New notes");

        assertNotNull(updated);
        verify(cardRepository, never()).existsByDeckIdAndFrontAndBack(anyLong(), anyString(), anyString());
        verify(cardRepository).save(testCard);

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

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
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

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.deleteCard(USER_ID, DECK_ID, CARD_ID);
        });

        verify(cardRepository, never()).delete(any(CardEntity.class));

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

        List<CardEntity> expectedCards = Arrays.asList(testCard);
        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeckEntity);
        when(cardRepository.searchByDeckId(DECK_ID, SEARCH_TERM)).thenReturn(expectedCards);

        List<CardEntity> results = cardService.searchCards(USER_ID, DECK_ID, SEARCH_TERM);

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

        List<CardEntity> expectedCards = Arrays.asList(testCard);
        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeckEntity);
        when(tagService.getTagOrThrow(USER_ID, TAG_ID)).thenReturn(testTagEntity);
        when(cardRepository.findByDeckIdAndTagId(DECK_ID, TAG_ID)).thenReturn(expectedCards);

        List<CardEntity> results = cardService.getCardsByTag(USER_ID, DECK_ID, TAG_ID);

        assertEquals(1, results.size());
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(tagService).getTagOrThrow(USER_ID, TAG_ID);
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
     * Tests that getting cards by tag fails if user doesn't own tag.
     */
    @Test
    @DisplayName("Should throw exception when using unowned tag")
    void testGetCardsByTagUnownedTag() {
        logger.debug("Test: Getting cards by unowned tag");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeckEntity);
        when(tagService.getTagOrThrow(USER_ID, TAG_ID))
                .thenThrow(new IllegalArgumentException("Tag not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getCardsByTag(USER_ID, DECK_ID, TAG_ID);
        });

        verify(cardRepository, never()).findByDeckIdAndTagId(anyLong(), anyLong());

        logger.debug("Test passed: Exception thrown for unowned tag");
    }

    /**
     * Tests adding tag to card.
     */
    @Test
    @DisplayName("Should add tag to card")
    void testAddTagToCard() {
        logger.debug("Test: Adding tag to card");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(tagService.getTagOrThrow(USER_ID, TAG_ID)).thenReturn(testTagEntity);
        when(cardRepository.save(testCard)).thenReturn(testCard);

        CardEntity result = cardService.addTagToCard(USER_ID, DECK_ID, CARD_ID, TAG_ID);

        assertNotNull(result);
        verify(testCard).addTag(testTagEntity);
        verify(cardRepository).save(testCard);

        logger.debug("Test passed: Tag added to card");
    }

    /**
     * Tests removing tag from card.
     */
    @Test
    @DisplayName("Should remove tag from card")
    void testRemoveTagFromCard() {
        logger.debug("Test: Removing tag from card");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(cardRepository.findByIdAndDeckId(CARD_ID, DECK_ID)).thenReturn(Optional.of(testCard));
        when(tagService.getTagOrThrow(USER_ID, TAG_ID)).thenReturn(testTagEntity);
        when(cardRepository.save(testCard)).thenReturn(testCard);

        CardEntity result = cardService.removeTagFromCard(USER_ID, DECK_ID, CARD_ID, TAG_ID);

        assertNotNull(result);
        verify(testCard).removeTag(testTagEntity);
        verify(cardRepository).save(testCard);

        logger.debug("Test passed: Tag removed from card");
    }
}