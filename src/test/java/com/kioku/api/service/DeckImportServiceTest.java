package com.kioku.api.service;

import com.kioku.api.dto.request.CardImportDto;
import com.kioku.api.dto.request.DeckImportRequest;
import com.kioku.api.dto.request.TagImportDto;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeckImportService.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Successful deck import with cards and tags</li>
 *   <li>Tag deduplication</li>
 *   <li>Duplicate deck name detection</li>
 *   <li>Duplicate card detection within import</li>
 *   <li>Tag creation from card references</li>
 *   <li>Error handling and validation</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DeckImportService Unit Tests")
class DeckImportServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(DeckImportServiceTest.class);

    private static final Long USER_ID = 1L;
    private static final Long DECK_ID = 100L;
    private static final Long TAG_ID = 200L;
    private static final Long CARD_ID = 300L;
    private static final String DECK_NAME = "Japanese N5";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary";

    @Mock
    private DeckService deckService;

    @Mock
    private CardService cardService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private DeckImportService importService;

    private User testUser;
    private Deck testDeck;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up DeckImportService test");

        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(USER_ID);

        testDeck = mock(Deck.class);
        when(testDeck.getId()).thenReturn(DECK_ID);
        when(testDeck.getName()).thenReturn(DECK_NAME);
    }

    // Successful Import Tests

    @Test
    @DisplayName("Should import deck with cards only (no tags)")
    void testImportDeckWithCardsOnly() {
        logger.debug("Test: Import deck with cards only");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat"),
                new CardImportDto("飲む", "to drink")
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);
        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenReturn(mock(Card.class));

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        assertEquals(DECK_ID, result.getId());
        verify(deckService).isDuplicateName(USER_ID, DECK_NAME);
        verify(deckService).createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION);
        verify(cardService, times(2)).createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any());
        verify(tagService, never()).createTag(anyLong(), anyLong(), anyString());

        logger.debug("Test passed: Deck imported with cards only");
    }

    @Test
    @DisplayName("Should import deck with explicit tags")
    void testImportDeckWithExplicitTags() {
        logger.debug("Test: Import deck with explicit tags");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat")
        );
        List<TagImportDto> tags = Arrays.asList(
                new TagImportDto("verbs"),
                new TagImportDto("food")
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards, tags);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);

        Tag verbsTag = mock(Tag.class);
        when(verbsTag.getId()).thenReturn(TAG_ID);
        Tag foodTag = mock(Tag.class);
        when(foodTag.getId()).thenReturn(TAG_ID + 1);

        when(tagService.createTag(USER_ID, DECK_ID, "verbs")).thenReturn(verbsTag);
        when(tagService.createTag(USER_ID, DECK_ID, "food")).thenReturn(foodTag);
        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenReturn(mock(Card.class));

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        verify(tagService).createTag(USER_ID, DECK_ID, "verbs");
        verify(tagService).createTag(USER_ID, DECK_ID, "food");

        logger.debug("Test passed: Deck imported with explicit tags");
    }

    @Test
    @DisplayName("Should import deck with cards containing tag references")
    void testImportDeckWithCardTags() {
        logger.debug("Test: Import deck with card tag references");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat", null, Arrays.asList("verbs", "food")),
                new CardImportDto("飲む", "to drink", null, Arrays.asList("verbs"))
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);

        Tag verbsTag = mock(Tag.class);
        when(verbsTag.getId()).thenReturn(TAG_ID);
        Tag foodTag = mock(Tag.class);
        when(foodTag.getId()).thenReturn(TAG_ID + 1);

        when(tagService.createTag(USER_ID, DECK_ID, "verbs")).thenReturn(verbsTag);
        when(tagService.createTag(USER_ID, DECK_ID, "food")).thenReturn(foodTag);

        Card card1 = mock(Card.class);
        when(card1.getId()).thenReturn(CARD_ID);
        Card card2 = mock(Card.class);
        when(card2.getId()).thenReturn(CARD_ID + 1);

        when(cardService.createCard(USER_ID, DECK_ID, "食べる", "to eat", null)).thenReturn(card1);
        when(cardService.createCard(USER_ID, DECK_ID, "飲む", "to drink", null)).thenReturn(card2);

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        verify(tagService).createTag(USER_ID, DECK_ID, "verbs");
        verify(tagService).createTag(USER_ID, DECK_ID, "food");
        verify(cardService).addTagToCard(USER_ID, DECK_ID, CARD_ID, TAG_ID);
        verify(cardService).addTagToCard(USER_ID, DECK_ID, CARD_ID, TAG_ID + 1);
        verify(cardService).addTagToCard(USER_ID, DECK_ID, CARD_ID + 1, TAG_ID);

        logger.debug("Test passed: Deck imported with card tag references");
    }

    @Test
    @DisplayName("Should deduplicate tags by name")
    void testTagDeduplication() {
        logger.debug("Test: Tag deduplication");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat", null, Arrays.asList("verbs")),
                new CardImportDto("飲む", "to drink", null, Arrays.asList("verbs"))
        );
        List<TagImportDto> tags = Arrays.asList(
                new TagImportDto("verbs")
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards, tags);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);

        Tag verbsTag = mock(Tag.class);
        when(verbsTag.getId()).thenReturn(TAG_ID);
        when(tagService.createTag(USER_ID, DECK_ID, "verbs")).thenReturn(verbsTag);

        Card card1 = mock(Card.class);
        when(card1.getId()).thenReturn(CARD_ID);
        Card card2 = mock(Card.class);
        when(card2.getId()).thenReturn(CARD_ID + 1);

        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenReturn(card1, card2);

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        // Tag should only be created once
        verify(tagService, times(1)).createTag(USER_ID, DECK_ID, "verbs");

        logger.debug("Test passed: Tags deduplicated correctly");
    }

    @Test
    @DisplayName("Should import deck with cards having notes")
    void testImportDeckWithCardNotes() {
        logger.debug("Test: Import deck with card notes");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat", "ru-verb", null)
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);
        when(cardService.createCard(USER_ID, DECK_ID, "食べる", "to eat", "ru-verb"))
                .thenReturn(mock(Card.class));

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        verify(cardService).createCard(USER_ID, DECK_ID, "食べる", "to eat", "ru-verb");

        logger.debug("Test passed: Cards with notes imported correctly");
    }

    // Error Tests

    @Test
    @DisplayName("Should throw exception when deck name already exists")
    void testImportDeckDuplicateName() {
        logger.debug("Test: Import deck with duplicate name");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat")
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            importService.importDeck(USER_ID, request);
        });

        verify(deckService).isDuplicateName(USER_ID, DECK_NAME);
        verify(deckService, never()).createDeck(anyLong(), anyString(), anyString());

        logger.debug("Test passed: Duplicate deck name rejected");
    }

    @Test
    @DisplayName("Should throw exception when duplicate cards in import")
    void testImportDeckDuplicateCards() {
        logger.debug("Test: Import deck with duplicate cards");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat"),
                new CardImportDto("食べる", "to eat")  // Duplicate
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);
        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenReturn(mock(Card.class));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            importService.importDeck(USER_ID, request);
        });

        assertTrue(exception.getMessage().contains("Duplicate card"));
        assertTrue(exception.getMessage().contains("食べる"));

        logger.debug("Test passed: Duplicate cards rejected");
    }

    @Test
    @DisplayName("Should throw exception when card creation fails")
    void testImportDeckCardCreationFailure() {
        logger.debug("Test: Import deck with card creation failure");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat")
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);
        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("Card validation failed"));

        assertThrows(IllegalArgumentException.class, () -> {
            importService.importDeck(USER_ID, request);
        });

        logger.debug("Test passed: Card creation failure propagated");
    }

    @Test
    @DisplayName("Should throw exception when tag creation fails")
    void testImportDeckTagCreationFailure() {
        logger.debug("Test: Import deck with tag creation failure");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat")
        );
        List<TagImportDto> tags = Arrays.asList(
                new TagImportDto("verbs")
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards, tags);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);
        when(tagService.createTag(USER_ID, DECK_ID, "verbs"))
                .thenThrow(new IllegalArgumentException("Tag validation failed"));

        assertThrows(IllegalArgumentException.class, () -> {
            importService.importDeck(USER_ID, request);
        });

        logger.debug("Test passed: Tag creation failure propagated");
    }

    // Large Import Tests

    @Test
    @DisplayName("Should import deck with many cards")
    void testImportDeckManyCards() {
        logger.debug("Test: Import deck with many cards");

        List<CardImportDto> cards = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            cards.add(new CardImportDto("Front " + i, "Back " + i));
        }
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);
        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenReturn(mock(Card.class));

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        verify(cardService, times(100)).createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any());

        logger.debug("Test passed: Large import handled correctly");
    }

    @Test
    @DisplayName("Should import deck with many tags")
    void testImportDeckManyTags() {
        logger.debug("Test: Import deck with many tags");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat")
        );
        List<TagImportDto> tags = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            tags.add(new TagImportDto("tag" + i));
        }
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards, tags);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);
        when(tagService.createTag(eq(USER_ID), eq(DECK_ID), anyString()))
                .thenReturn(mock(Tag.class));
        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenReturn(mock(Card.class));

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        verify(tagService, times(50)).createTag(eq(USER_ID), eq(DECK_ID), anyString());

        logger.debug("Test passed: Many tags imported correctly");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should import deck with null description")
    void testImportDeckNullDescription() {
        logger.debug("Test: Import deck with null description");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat")
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, null, cards);

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, null)).thenReturn(testDeck);
        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenReturn(mock(Card.class));

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        verify(deckService).createDeck(USER_ID, DECK_NAME, null);

        logger.debug("Test passed: Null description handled correctly");
    }

    @Test
    @DisplayName("Should import deck with empty tag list")
    void testImportDeckEmptyTags() {
        logger.debug("Test: Import deck with empty tag list");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat")
        );
        DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards, Arrays.asList());

        when(deckService.isDuplicateName(USER_ID, DECK_NAME)).thenReturn(false);
        when(deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION)).thenReturn(testDeck);
        when(cardService.createCard(eq(USER_ID), eq(DECK_ID), anyString(), anyString(), any()))
                .thenReturn(mock(Card.class));

        Deck result = importService.importDeck(USER_ID, request);

        assertNotNull(result);
        verify(tagService, never()).createTag(anyLong(), anyLong(), anyString());

        logger.debug("Test passed: Empty tag list handled correctly");
    }
}