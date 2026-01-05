package com.kioku.api.service;

import com.kioku.api.dto.response.DeckExportResponse;
import com.kioku.api.entity.Card;
import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeckExportService.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Successful deck export with cards and tags</li>
 *   <li>Export of empty decks</li>
 *   <li>Export metadata generation</li>
 *   <li>Ownership verification</li>
 *   <li>Error handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DeckExportService Unit Tests")
class DeckExportServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(DeckExportServiceTest.class);

    private static final Long USER_ID = 1L;
    private static final Long DECK_ID = 100L;
    private static final String DECK_NAME = "Japanese N5";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary";

    @Mock
    private DeckService deckService;

    @Mock
    private CardService cardService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private DeckExportService exportService;

    private User testUser;
    private Deck testDeck;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up DeckExportService test");

        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(USER_ID);

        testDeck = mock(Deck.class);
        when(testDeck.getId()).thenReturn(DECK_ID);
        when(testDeck.getName()).thenReturn(DECK_NAME);
        when(testDeck.getDescription()).thenReturn(DECK_DESCRIPTION);
    }

    // Successful Export Tests

    @Test
    @DisplayName("Should export deck with cards and tags")
    void testExportDeckWithCardsAndTags() {
        logger.debug("Test: Export deck with cards and tags");

        // Setup deck
        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);

        // Setup cards
        Card card1 = mock(Card.class);
        when(card1.getId()).thenReturn(1L);
        when(card1.getFront()).thenReturn("食べる");
        when(card1.getBack()).thenReturn("to eat");
        when(card1.getTags()).thenReturn(Collections.emptySet());

        Card card2 = mock(Card.class);
        when(card2.getId()).thenReturn(2L);
        when(card2.getFront()).thenReturn("飲む");
        when(card2.getBack()).thenReturn("to drink");
        when(card2.getTags()).thenReturn(Collections.emptySet());

        List<Card> cards = Arrays.asList(card1, card2);
        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(cards);

        // Setup tags
        Tag tag1 = mock(Tag.class);
        when(tag1.getId()).thenReturn(1L);
        when(tag1.getName()).thenReturn("verbs");

        Tag tag2 = mock(Tag.class);
        when(tag2.getId()).thenReturn(2L);
        when(tag2.getName()).thenReturn("food");

        List<Tag> tags = Arrays.asList(tag1, tag2);
        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(tags);

        // Export
        DeckExportResponse response = exportService.exportDeck(USER_ID, DECK_ID);

        // Verify
        assertNotNull(response);
        assertEquals(DECK_ID, response.getId());
        assertEquals(DECK_NAME, response.getName());
        assertEquals(DECK_DESCRIPTION, response.getDescription());
        assertEquals(2, response.getCards().size());
        assertEquals(2, response.getTags().size());
        assertNotNull(response.getMetadata());
        assertEquals(2, response.getMetadata().getCardCount());
        assertEquals(2, response.getMetadata().getTagCount());
        assertEquals("1.0", response.getMetadata().getVersion());

        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(cardService).getDeckCards(USER_ID, DECK_ID);
        verify(tagService).getDeckTags(USER_ID, DECK_ID);

        logger.debug("Test passed: Deck exported with cards and tags");
    }

    @Test
    @DisplayName("Should export empty deck")
    void testExportEmptyDeck() {
        logger.debug("Test: Export empty deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());
        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

        DeckExportResponse response = exportService.exportDeck(USER_ID, DECK_ID);

        assertNotNull(response);
        assertEquals(DECK_ID, response.getId());
        assertEquals(DECK_NAME, response.getName());
        assertTrue(response.getCards().isEmpty());
        assertTrue(response.getTags().isEmpty());
        assertEquals(0, response.getMetadata().getCardCount());
        assertEquals(0, response.getMetadata().getTagCount());

        logger.debug("Test passed: Empty deck exported");
    }

    @Test
    @DisplayName("Should export deck with cards only (no tags)")
    void testExportDeckWithCardsOnly() {
        logger.debug("Test: Export deck with cards only");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);

        Card card = mock(Card.class);
        when(card.getId()).thenReturn(1L);
        when(card.getFront()).thenReturn("食べる");
        when(card.getBack()).thenReturn("to eat");
        when(card.getTags()).thenReturn(Collections.emptySet());

        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Arrays.asList(card));
        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

        DeckExportResponse response = exportService.exportDeck(USER_ID, DECK_ID);

        assertNotNull(response);
        assertEquals(1, response.getCards().size());
        assertEquals(0, response.getTags().size());
        assertEquals(1, response.getMetadata().getCardCount());
        assertEquals(0, response.getMetadata().getTagCount());

        logger.debug("Test passed: Deck with cards only exported");
    }

    @Test
    @DisplayName("Should export deck with tags only (no cards)")
    void testExportDeckWithTagsOnly() {
        logger.debug("Test: Export deck with tags only");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

        Tag tag = mock(Tag.class);
        when(tag.getId()).thenReturn(1L);
        when(tag.getName()).thenReturn("verbs");

        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Arrays.asList(tag));

        DeckExportResponse response = exportService.exportDeck(USER_ID, DECK_ID);

        assertNotNull(response);
        assertEquals(0, response.getCards().size());
        assertEquals(1, response.getTags().size());
        assertEquals(0, response.getMetadata().getCardCount());
        assertEquals(1, response.getMetadata().getTagCount());

        logger.debug("Test passed: Deck with tags only exported");
    }

    @Test
    @DisplayName("Should export deck with null description")
    void testExportDeckWithNullDescription() {
        logger.debug("Test: Export deck with null description");

        when(testDeck.getDescription()).thenReturn(null);
        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());
        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

        DeckExportResponse response = exportService.exportDeck(USER_ID, DECK_ID);

        assertNotNull(response);
        assertNull(response.getDescription());

        logger.debug("Test passed: Deck with null description exported");
    }

    // Error Handling Tests

    @Test
    @DisplayName("Should throw exception when deck not found")
    void testExportDeckNotFound() {
        logger.debug("Test: Export deck not found");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            exportService.exportDeck(USER_ID, DECK_ID);
        });

        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(cardService, never()).getDeckCards(anyLong(), anyLong());
        verify(tagService, never()).getDeckTags(anyLong(), anyLong());

        logger.debug("Test passed: Exception thrown for deck not found");
    }

    @Test
    @DisplayName("Should throw exception when user doesn't own deck")
    void testExportDeckUnauthorized() {
        logger.debug("Test: Export deck unauthorized");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            exportService.exportDeck(USER_ID, DECK_ID);
        });

        verify(cardService, never()).getDeckCards(anyLong(), anyLong());
        verify(tagService, never()).getDeckTags(anyLong(), anyLong());

        logger.debug("Test passed: Exception thrown for unauthorized access");
    }

    // Large Export Tests

    @Test
    @DisplayName("Should export deck with many cards")
    void testExportDeckManyCards() {
        logger.debug("Test: Export deck with many cards");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);

        // Create 100 mock cards
        List<Card> cards = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Card card = mock(Card.class);
            when(card.getId()).thenReturn((long) i);
            when(card.getFront()).thenReturn("Front " + i);
            when(card.getBack()).thenReturn("Back " + i);
            when(card.getTags()).thenReturn(Collections.emptySet());
            cards.add(card);
        }

        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(cards);
        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

        DeckExportResponse response = exportService.exportDeck(USER_ID, DECK_ID);

        assertNotNull(response);
        assertEquals(100, response.getCards().size());
        assertEquals(100, response.getMetadata().getCardCount());

        logger.debug("Test passed: Large deck exported");
    }

    @Test
    @DisplayName("Should export deck with many tags")
    void testExportDeckManyTags() {
        logger.debug("Test: Export deck with many tags");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

        // Create 50 mock tags
        List<Tag> tags = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Tag tag = mock(Tag.class);
            when(tag.getId()).thenReturn((long) i);
            when(tag.getName()).thenReturn("tag" + i);
            tags.add(tag);
        }

        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(tags);

        DeckExportResponse response = exportService.exportDeck(USER_ID, DECK_ID);

        assertNotNull(response);
        assertEquals(50, response.getTags().size());
        assertEquals(50, response.getMetadata().getTagCount());

        logger.debug("Test passed: Deck with many tags exported");
    }

    // Metadata Tests

    @Test
    @DisplayName("Should include export metadata")
    void testExportMetadata() {
        logger.debug("Test: Export metadata");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());
        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

        DeckExportResponse response = exportService.exportDeck(USER_ID, DECK_ID);

        assertNotNull(response.getMetadata());
        assertNotNull(response.getMetadata().getExportDate());
        assertEquals("1.0", response.getMetadata().getVersion());
        assertEquals(0, response.getMetadata().getCardCount());
        assertEquals(0, response.getMetadata().getTagCount());

        logger.debug("Test passed: Export metadata included");
    }

    @Test
    @DisplayName("Should verify service call order")
    void testServiceCallOrder() {
        logger.debug("Test: Service call order");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(testDeck);
        when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());
        when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

        exportService.exportDeck(USER_ID, DECK_ID);

        // Verify services called in correct order
        var inOrder = inOrder(deckService, cardService, tagService);
        inOrder.verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        inOrder.verify(cardService).getDeckCards(USER_ID, DECK_ID);
        inOrder.verify(tagService).getDeckTags(USER_ID, DECK_ID);

        logger.debug("Test passed: Services called in correct order");
    }
}