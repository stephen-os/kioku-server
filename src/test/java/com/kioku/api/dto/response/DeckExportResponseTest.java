package com.kioku.api.dto.response;

import com.kioku.api.entity.Card;
import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
class DeckExportResponseTest {

    private static final Logger logger = LoggerFactory.getLogger(DeckExportResponseTest.class);

    private static final Long DECK_ID = 123L;
    private static final String DECK_NAME = "Japanese N5";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary";

    private User testUser;
    private Deck testDeck;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up DeckExportResponse test");

        testUser = new User("test@example.com", "hashedPassword");
        testDeck = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
        testDeck.setId(DECK_ID);
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

        DeckExportResponse response = new DeckExportResponse(testDeck, cards, tags);

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

        Card card1 = new Card(testDeck, "食べる", "to eat");
        card1.setId(1L);
        Card card2 = new Card(testDeck, "飲む", "to drink");
        card2.setId(2L);

        List<Card> cards = Arrays.asList(card1, card2);
        List<Tag> tags = Arrays.asList();

        DeckExportResponse response = new DeckExportResponse(testDeck, cards, tags);

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

        Tag tag1 = new Tag(testDeck, "verbs");
        tag1.setId(1L);
        Tag tag2 = new Tag(testDeck, "food");
        tag2.setId(2L);

        List<Card> cards = Arrays.asList();
        List<Tag> tags = Arrays.asList(tag1, tag2);

        DeckExportResponse response = new DeckExportResponse(testDeck, cards, tags);

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

        Tag verbTag = new Tag(testDeck, "verbs");
        verbTag.setId(1L);
        Tag foodTag = new Tag(testDeck, "food");
        foodTag.setId(2L);

        Card card1 = new Card(testDeck, "食べる", "to eat");
        card1.setId(1L);
        card1.addTag(verbTag);
        card1.addTag(foodTag);

        Card card2 = new Card(testDeck, "飲む", "to drink");
        card2.setId(2L);
        card2.addTag(verbTag);
        card2.addTag(foodTag);

        List<Card> cards = Arrays.asList(card1, card2);
        List<Tag> tags = Arrays.asList(verbTag, foodTag);

        DeckExportResponse response = new DeckExportResponse(testDeck, cards, tags);

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
    @DisplayName("Should handle deck with null description")
    void testEntityConstructorWithNullDescription() {
        logger.debug("Test: Entity constructor with null description");

        Deck deckNoDesc = new Deck(testUser, DECK_NAME, null);
        deckNoDesc.setId(DECK_ID);

        List<Card> cards = Arrays.asList();
        List<Tag> tags = Arrays.asList();

        DeckExportResponse response = new DeckExportResponse(deckNoDesc, cards, tags);

        assertEquals(DECK_ID, response.getId());
        assertEquals(DECK_NAME, response.getName());
        assertNull(response.getDescription());

        logger.debug("Test passed: Null description handled correctly");
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
            Card card = new Card(testDeck, "Front " + i, "Back " + i);
            card.setId((long) i);
            cards.add(card);
        }

        // Create 20 tags
        List<Tag> tags = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Tag tag = new Tag(testDeck, "tag" + i);
            tag.setId((long) i);
            tags.add(tag);
        }

        DeckExportResponse response = new DeckExportResponse(testDeck, cards, tags);

        assertEquals(100, response.getCards().size());
        assertEquals(20, response.getTags().size());
        assertEquals(100, response.getMetadata().getCardCount());
        assertEquals(20, response.getMetadata().getTagCount());

        logger.debug("Test passed: Large deck export handled correctly");
    }
}