package com.kioku.api.dto.response;

import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DeckExportResponse.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Construction from entities</li>
 *   <li>Constructor behavior</li>
 *   <li>Getter and setter functionality</li>
 *   <li>Metadata generation</li>
 *   <li>Nested ExportMetadata class</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("DeckExportResponse Tests")
class DeckExportResponseTests {

    private static final Logger logger = LoggerFactory.getLogger(DeckExportResponseTests.class);

    private static final Long DECK_ID = 123L;
    private static final String DECK_NAME = "Japanese N5";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary";

    private Deck mockDeck;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up DeckExportResponse test");

        testCreatedAt = LocalDateTime.now();
        testUpdatedAt = LocalDateTime.now();

        mockDeck = mock(Deck.class);
        when(mockDeck.getId()).thenReturn(DECK_ID);
        when(mockDeck.getName()).thenReturn(DECK_NAME);
        when(mockDeck.getDescription()).thenReturn(DECK_DESCRIPTION);
        when(mockDeck.getCreatedAt()).thenReturn(testCreatedAt);
        when(mockDeck.getUpdatedAt()).thenReturn(testUpdatedAt);
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create DeckExportResponse with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        DeckExportResponse response = new DeckExportResponse();

        assertNotNull(response.getCards());
        assertTrue(response.getCards().isEmpty());
        assertNotNull(response.getTags());
        assertTrue(response.getTags().isEmpty());
        assertNotNull(response.getMetadata());

        logger.debug("Test passed: Default constructor initializes lists and metadata");
    }

    @Test
    @DisplayName("Should create DeckExportResponse from entities without cards or tags")
    void testEntityConstructorEmpty() {
        logger.debug("Test: Entity constructor without cards or tags");

        List<Card> cards = Arrays.asList();
        List<Tag> tags = Arrays.asList();

        DeckExportResponse response = new DeckExportResponse(mockDeck, cards, tags);

        assertEquals(DECK_ID, response.getId());
        assertEquals(DECK_NAME, response.getName());
        assertEquals(DECK_DESCRIPTION, response.getDescription());
        assertNotNull(response.getCards());
        assertTrue(response.getCards().isEmpty());
        assertNotNull(response.getTags());
        assertTrue(response.getTags().isEmpty());
        assertNotNull(response.getMetadata());
        assertEquals(0, response.getMetadata().getCardCount());
        assertEquals(0, response.getMetadata().getTagCount());

        logger.debug("Test passed: Empty deck export created");
    }

    @Test
    @DisplayName("Should create DeckExportResponse from entities with cards")
    void testEntityConstructorWithCards() {
        logger.debug("Test: Entity constructor with cards");

        Card mockCard1 = mock(Card.class);
        when(mockCard1.getCardId()).thenReturn(1L);
        when(mockCard1.getFront()).thenReturn("食べる");
        when(mockCard1.getBack()).thenReturn("to eat");
        when(mockCard1.getNotes()).thenReturn(null);
        when(mockCard1.getTags()).thenReturn(Collections.emptySet());
        when(mockCard1.getCreatedAt()).thenReturn(testCreatedAt);
        when(mockCard1.getUpdatedAt()).thenReturn(testUpdatedAt);

        Card mockCard2 = mock(Card.class);
        when(mockCard2.getCardId()).thenReturn(2L);
        when(mockCard2.getFront()).thenReturn("飲む");
        when(mockCard2.getBack()).thenReturn("to drink");
        when(mockCard2.getNotes()).thenReturn(null);
        when(mockCard2.getTags()).thenReturn(Collections.emptySet());
        when(mockCard2.getCreatedAt()).thenReturn(testCreatedAt);
        when(mockCard2.getUpdatedAt()).thenReturn(testUpdatedAt);

        List<Card> cards = Arrays.asList(mockCard1, mockCard2);
        List<Tag> tags = Arrays.asList();

        DeckExportResponse response = new DeckExportResponse(mockDeck, cards, tags);

        assertEquals(DECK_ID, response.getId());
        assertEquals(DECK_NAME, response.getName());
        assertEquals(2, response.getCards().size());
        assertEquals(0, response.getTags().size());
        assertEquals(2, response.getMetadata().getCardCount());
        assertEquals(0, response.getMetadata().getTagCount());

        logger.debug("Test passed: Deck export with cards created");
    }

    @Test
    @DisplayName("Should create DeckExportResponse from entities with tags")
    void testEntityConstructorWithTags() {
        logger.debug("Test: Entity constructor with tags");

        Tag mockTag1 = mock(Tag.class);
        when(mockTag1.getId()).thenReturn(1L);
        when(mockTag1.getName()).thenReturn("verbs");

        Tag mockTag2 = mock(Tag.class);
        when(mockTag2.getId()).thenReturn(2L);
        when(mockTag2.getName()).thenReturn("food");

        List<Card> cards = Arrays.asList();
        List<Tag> tags = Arrays.asList(mockTag1, mockTag2);

        DeckExportResponse response = new DeckExportResponse(mockDeck, cards, tags);

        assertEquals(DECK_ID, response.getId());
        assertEquals(DECK_NAME, response.getName());
        assertEquals(0, response.getCards().size());
        assertEquals(2, response.getTags().size());
        assertEquals(0, response.getMetadata().getCardCount());
        assertEquals(2, response.getMetadata().getTagCount());

        logger.debug("Test passed: Deck export with tags created");
    }

    @Test
    @DisplayName("Should create DeckExportResponse from entities with cards and tags")
    void testEntityConstructorWithCardsAndTags() {
        logger.debug("Test: Entity constructor with cards and tags");

        Tag mockVerbTag = mock(Tag.class);
        when(mockVerbTag.getId()).thenReturn(1L);
        when(mockVerbTag.getName()).thenReturn("verbs");

        Tag mockFoodTag = mock(Tag.class);
        when(mockFoodTag.getId()).thenReturn(2L);
        when(mockFoodTag.getName()).thenReturn("food");

        Set<Tag> cardTags = new HashSet<>();
        cardTags.add(mockVerbTag);
        cardTags.add(mockFoodTag);

        Card mockCard1 = mock(Card.class);
        when(mockCard1.getCardId()).thenReturn(1L);
        when(mockCard1.getFront()).thenReturn("食べる");
        when(mockCard1.getBack()).thenReturn("to eat");
        when(mockCard1.getNotes()).thenReturn(null);
        when(mockCard1.getTags()).thenReturn(cardTags);
        when(mockCard1.getCreatedAt()).thenReturn(testCreatedAt);
        when(mockCard1.getUpdatedAt()).thenReturn(testUpdatedAt);

        Card mockCard2 = mock(Card.class);
        when(mockCard2.getCardId()).thenReturn(2L);
        when(mockCard2.getFront()).thenReturn("飲む");
        when(mockCard2.getBack()).thenReturn("to drink");
        when(mockCard2.getNotes()).thenReturn(null);
        when(mockCard2.getTags()).thenReturn(cardTags);
        when(mockCard2.getCreatedAt()).thenReturn(testCreatedAt);
        when(mockCard2.getUpdatedAt()).thenReturn(testUpdatedAt);

        List<Card> cards = Arrays.asList(mockCard1, mockCard2);
        List<Tag> tags = Arrays.asList(mockVerbTag, mockFoodTag);

        DeckExportResponse response = new DeckExportResponse(mockDeck, cards, tags);

        assertEquals(DECK_ID, response.getId());
        assertEquals(DECK_NAME, response.getName());
        assertEquals(DECK_DESCRIPTION, response.getDescription());
        assertEquals(2, response.getCards().size());
        assertEquals(2, response.getTags().size());
        assertEquals(2, response.getMetadata().getCardCount());
        assertEquals(2, response.getMetadata().getTagCount());

        // Verify cards have tags
        CardExportDto exportedCard1 = response.getCards().get(0);
        assertEquals(2, exportedCard1.getTags().size());
        assertTrue(exportedCard1.getTags().contains("verbs"));
        assertTrue(exportedCard1.getTags().contains("food"));

        logger.debug("Test passed: Complete deck export created");
    }

    @Test
    @DisplayName("Should handle deck with empty description")
    void testEntityConstructorWithEmptyDescription() {
        logger.debug("Test: Entity constructor with empty description");

        Deck mockDeckNoDesc = mock(Deck.class);
        when(mockDeckNoDesc.getId()).thenReturn(DECK_ID);
        when(mockDeckNoDesc.getName()).thenReturn(DECK_NAME);
        when(mockDeckNoDesc.getDescription()).thenReturn("");
        when(mockDeckNoDesc.getCreatedAt()).thenReturn(testCreatedAt);
        when(mockDeckNoDesc.getUpdatedAt()).thenReturn(testUpdatedAt);

        List<Card> cards = Arrays.asList();
        List<Tag> tags = Arrays.asList();

        DeckExportResponse response = new DeckExportResponse(mockDeckNoDesc, cards, tags);

        assertEquals(DECK_ID, response.getId());
        assertEquals(DECK_NAME, response.getName());
        assertEquals("", response.getDescription());

        logger.debug("Test passed: Empty description handled correctly");
    }

    // Getter/Setter Tests

    @Test
    @DisplayName("Should get and set id correctly")
    void testGetSetId() {
        logger.debug("Test: Get/set id");

        DeckExportResponse response = new DeckExportResponse();
        response.setId(456L);

        assertEquals(456L, response.getId());

        logger.debug("Test passed: ID getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set name correctly")
    void testGetSetName() {
        logger.debug("Test: Get/set name");

        DeckExportResponse response = new DeckExportResponse();
        response.setName("Spanish Vocabulary");

        assertEquals("Spanish Vocabulary", response.getName());

        logger.debug("Test passed: Name getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set description correctly")
    void testGetSetDescription() {
        logger.debug("Test: Get/set description");

        DeckExportResponse response = new DeckExportResponse();
        response.setDescription("Common Spanish phrases");

        assertEquals("Common Spanish phrases", response.getDescription());

        logger.debug("Test passed: Description getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set createdAt correctly")
    void testGetSetCreatedAt() {
        logger.debug("Test: Get/set createdAt");

        DeckExportResponse response = new DeckExportResponse();
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 10, 30);
        response.setCreatedAt(time);

        assertEquals(time, response.getCreatedAt());

        logger.debug("Test passed: CreatedAt getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set updatedAt correctly")
    void testGetSetUpdatedAt() {
        logger.debug("Test: Get/set updatedAt");

        DeckExportResponse response = new DeckExportResponse();
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 11, 30);
        response.setUpdatedAt(time);

        assertEquals(time, response.getUpdatedAt());

        logger.debug("Test passed: UpdatedAt getter/setter work correctly");
    }

    @Test
    @DisplayName("Should handle setting null cards")
    void testSetNullCards() {
        logger.debug("Test: Setting null cards");

        DeckExportResponse response = new DeckExportResponse();
        response.setCards(null);

        assertNotNull(response.getCards());
        assertTrue(response.getCards().isEmpty());

        logger.debug("Test passed: Setting null cards creates empty list");
    }

    @Test
    @DisplayName("Should handle setting null tags")
    void testSetNullTags() {
        logger.debug("Test: Setting null tags");

        DeckExportResponse response = new DeckExportResponse();
        response.setTags(null);

        assertNotNull(response.getTags());
        assertTrue(response.getTags().isEmpty());

        logger.debug("Test passed: Setting null tags creates empty list");
    }

    // ExportMetadata Tests

    @Test
    @DisplayName("Should create ExportMetadata with default constructor")
    void testMetadataDefaultConstructor() {
        logger.debug("Test: ExportMetadata default constructor");

        DeckExportResponse.ExportMetadata metadata = new DeckExportResponse.ExportMetadata();

        assertEquals("1.0", metadata.getVersion());
        assertNotNull(metadata.getExportDate());
        assertEquals(0, metadata.getCardCount());
        assertEquals(0, metadata.getTagCount());

        logger.debug("Test passed: ExportMetadata defaults set correctly");
    }

    @Test
    @DisplayName("Should create ExportMetadata with counts")
    void testMetadataWithCounts() {
        logger.debug("Test: ExportMetadata with counts");

        DeckExportResponse.ExportMetadata metadata = new DeckExportResponse.ExportMetadata(50, 10);

        assertEquals("1.0", metadata.getVersion());
        assertNotNull(metadata.getExportDate());
        assertEquals(50, metadata.getCardCount());
        assertEquals(10, metadata.getTagCount());

        logger.debug("Test passed: ExportMetadata counts set correctly");
    }

    @Test
    @DisplayName("Should get and set metadata version")
    void testMetadataGetSetVersion() {
        logger.debug("Test: ExportMetadata get/set version");

        DeckExportResponse.ExportMetadata metadata = new DeckExportResponse.ExportMetadata();
        metadata.setVersion("2.0");

        assertEquals("2.0", metadata.getVersion());

        logger.debug("Test passed: Metadata version getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set metadata exportDate")
    void testMetadataGetSetExportDate() {
        logger.debug("Test: ExportMetadata get/set exportDate");

        DeckExportResponse.ExportMetadata metadata = new DeckExportResponse.ExportMetadata();
        LocalDateTime time = LocalDateTime.of(2024, 1, 22, 9, 15);
        metadata.setExportDate(time);

        assertEquals(time, metadata.getExportDate());

        logger.debug("Test passed: Metadata exportDate getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set metadata cardCount")
    void testMetadataGetSetCardCount() {
        logger.debug("Test: ExportMetadata get/set cardCount");

        DeckExportResponse.ExportMetadata metadata = new DeckExportResponse.ExportMetadata();
        metadata.setCardCount(100);

        assertEquals(100, metadata.getCardCount());

        logger.debug("Test passed: Metadata cardCount getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set metadata tagCount")
    void testMetadataGetSetTagCount() {
        logger.debug("Test: ExportMetadata get/set tagCount");

        DeckExportResponse.ExportMetadata metadata = new DeckExportResponse.ExportMetadata();
        metadata.setTagCount(25);

        assertEquals(25, metadata.getTagCount());

        logger.debug("Test passed: Metadata tagCount getter/setter work correctly");
    }

    @Test
    @DisplayName("Should replace metadata object")
    void testReplaceMetadata() {
        logger.debug("Test: Replace metadata object");

        DeckExportResponse response = new DeckExportResponse();
        DeckExportResponse.ExportMetadata newMetadata = new DeckExportResponse.ExportMetadata(100, 20);
        newMetadata.setVersion("2.0");

        response.setMetadata(newMetadata);

        assertEquals("2.0", response.getMetadata().getVersion());
        assertEquals(100, response.getMetadata().getCardCount());
        assertEquals(20, response.getMetadata().getTagCount());

        logger.debug("Test passed: Metadata replaced successfully");
    }

    // Large Deck Tests

    @Test
    @DisplayName("Should handle large deck export")
    void testLargeDeckExport() {
        logger.debug("Test: Large deck export");

        // Create 100 cards
        List<Card> cards = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Card mockCard = mock(Card.class);
            when(mockCard.getCardId()).thenReturn((long) i);
            when(mockCard.getFront()).thenReturn("Front " + i);
            when(mockCard.getBack()).thenReturn("Back " + i);
            when(mockCard.getNotes()).thenReturn(null);
            when(mockCard.getTags()).thenReturn(Collections.emptySet());
            when(mockCard.getCreatedAt()).thenReturn(testCreatedAt);
            when(mockCard.getUpdatedAt()).thenReturn(testUpdatedAt);
            cards.add(mockCard);
        }

        // Create 20 tags
        List<Tag> tags = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Tag mockTag = mock(Tag.class);
            when(mockTag.getId()).thenReturn((long) i);
            when(mockTag.getName()).thenReturn("tag" + i);
            tags.add(mockTag);
        }

        DeckExportResponse response = new DeckExportResponse(mockDeck, cards, tags);

        assertEquals(100, response.getCards().size());
        assertEquals(20, response.getTags().size());
        assertEquals(100, response.getMetadata().getCardCount());
        assertEquals(20, response.getMetadata().getTagCount());

        logger.debug("Test passed: Large deck export handled correctly");
    }
}
