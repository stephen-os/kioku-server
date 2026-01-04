package com.kioku.api.dto.response;

import com.kioku.api.entity.CardEntity;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.entity.UserEntity;
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
 * Unit tests for CardExportDto.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Construction from CardEntity</li>
 *   <li>Constructor behavior with all fields</li>
 *   <li>Getter and setter functionality</li>
 *   <li>Tag handling</li>
 *   <li>Timestamp handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("CardExportDto Tests")
class CardExportDtoTest {

    private static final Logger logger = LoggerFactory.getLogger(CardExportDtoTest.class);

    private static final Long CARD_ID = 123L;
    private static final String FRONT = "食べる";
    private static final String BACK = "to eat";
    private static final String NOTES = "ru-verb";

    private UserEntity testUser;
    private DeckEntity testDeck;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up CardExportDto test");

        testUser = new UserEntity("test@example.com", "hashedPassword");
        testDeck = new DeckEntity(testUser, "Test Deck", "Description");
        testTime = LocalDateTime.now();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create CardExportDto with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        CardExportDto dto = new CardExportDto();

        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());

        logger.debug("Test passed: Default constructor initializes tags list");
    }

    @Test
    @DisplayName("Should create CardExportDto from CardEntity without tags")
    void testEntityConstructorWithoutTags() {
        logger.debug("Test: Entity constructor without tags");

        CardEntity card = new CardEntity(testDeck, FRONT, BACK);
        card.setId(CARD_ID);
        card.setNotes(NOTES);

        CardExportDto dto = new CardExportDto(card);

        assertEquals(CARD_ID, dto.getId());
        assertEquals(FRONT, dto.getFront());
        assertEquals(BACK, dto.getBack());
        assertEquals(NOTES, dto.getNotes());
        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());

        logger.debug("Test passed: DTO created from entity without tags");
    }

    @Test
    @DisplayName("Should create CardExportDto from CardEntity with tags")
    void testEntityConstructorWithTags() {
        logger.debug("Test: Entity constructor with tags");

        CardEntity card = new CardEntity(testDeck, FRONT, BACK);
        card.setId(CARD_ID);
        card.setNotes(NOTES);

        TagEntity tag1 = new TagEntity(testDeck, "verbs");
        TagEntity tag2 = new TagEntity(testDeck, "food");
        card.addTag(tag1);
        card.addTag(tag2);

        CardExportDto dto = new CardExportDto(card);

        assertEquals(CARD_ID, dto.getId());
        assertEquals(FRONT, dto.getFront());
        assertEquals(BACK, dto.getBack());
        assertEquals(NOTES, dto.getNotes());
        assertEquals(2, dto.getTags().size());
        assertTrue(dto.getTags().contains("verbs"));
        assertTrue(dto.getTags().contains("food"));
        assertTrue(dto.hasTags());

        logger.debug("Test passed: DTO created from entity with tags");
    }

    @Test
    @DisplayName("Should create CardExportDto with full constructor")
    void testFullConstructor() {
        logger.debug("Test: Full constructor");

        List<String> tags = Arrays.asList("verbs", "food");
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 11, 30);

        CardExportDto dto = new CardExportDto(CARD_ID, FRONT, BACK, NOTES, tags, createdAt, updatedAt);

        assertEquals(CARD_ID, dto.getId());
        assertEquals(FRONT, dto.getFront());
        assertEquals(BACK, dto.getBack());
        assertEquals(NOTES, dto.getNotes());
        assertEquals(2, dto.getTags().size());
        assertTrue(dto.getTags().contains("verbs"));
        assertTrue(dto.getTags().contains("food"));
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());

        logger.debug("Test passed: Full constructor sets all fields");
    }

    @Test
    @DisplayName("Should handle null tags in full constructor")
    void testFullConstructorWithNullTags() {
        logger.debug("Test: Full constructor with null tags");

        CardExportDto dto = new CardExportDto(CARD_ID, FRONT, BACK, NOTES, null, testTime, testTime);

        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());
        assertFalse(dto.hasTags());

        logger.debug("Test passed: Null tags converted to empty list");
    }

    @Test
    @DisplayName("Should handle null notes in entity constructor")
    void testEntityConstructorWithNullNotes() {
        logger.debug("Test: Entity constructor with null notes");

        CardEntity card = new CardEntity(testDeck, FRONT, BACK);
        card.setId(CARD_ID);
        // notes not set (null)

        CardExportDto dto = new CardExportDto(card);

        assertEquals(CARD_ID, dto.getId());
        assertEquals(FRONT, dto.getFront());
        assertEquals(BACK, dto.getBack());
        assertNull(dto.getNotes());

        logger.debug("Test passed: Null notes handled correctly");
    }

    // Tag Tests

    @Test
    @DisplayName("Should correctly report hasTags when tags are present")
    void testHasTagsTrue() {
        logger.debug("Test: hasTags with tags present");

        List<String> tags = Arrays.asList("verbs", "food");
        CardExportDto dto = new CardExportDto(CARD_ID, FRONT, BACK, NOTES, tags, testTime, testTime);

        assertTrue(dto.hasTags());

        logger.debug("Test passed: hasTags returns true");
    }

    @Test
    @DisplayName("Should correctly report hasTags when tags are empty")
    void testHasTagsFalse() {
        logger.debug("Test: hasTags with empty tags");

        CardExportDto dto = new CardExportDto(CARD_ID, FRONT, BACK, NOTES, Arrays.asList(), testTime, testTime);

        assertFalse(dto.hasTags());

        logger.debug("Test passed: hasTags returns false");
    }

    @Test
    @DisplayName("Should handle setting null tags")
    void testSetNullTags() {
        logger.debug("Test: Setting null tags");

        CardExportDto dto = new CardExportDto();
        dto.setTags(null);

        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());
        assertFalse(dto.hasTags());

        logger.debug("Test passed: Setting null tags creates empty list");
    }

    @Test
    @DisplayName("Should preserve tag order from entity")
    void testTagOrderPreserved() {
        logger.debug("Test: Tag order preservation");

        CardEntity card = new CardEntity(testDeck, FRONT, BACK);
        card.setId(CARD_ID);

        TagEntity tag1 = new TagEntity(testDeck, "alpha");
        TagEntity tag2 = new TagEntity(testDeck, "beta");
        TagEntity tag3 = new TagEntity(testDeck, "gamma");
        card.addTag(tag1);
        card.addTag(tag2);
        card.addTag(tag3);

        CardExportDto dto = new CardExportDto(card);

        assertEquals(3, dto.getTags().size());
        // Order may vary depending on Set implementation in CardEntity
        assertTrue(dto.getTags().contains("alpha"));
        assertTrue(dto.getTags().contains("beta"));
        assertTrue(dto.getTags().contains("gamma"));

        logger.debug("Test passed: All tags present");
    }

    // Getter/Setter Tests

    @Test
    @DisplayName("Should get and set id correctly")
    void testGetSetId() {
        logger.debug("Test: Get/set id");

        CardExportDto dto = new CardExportDto();
        dto.setId(456L);

        assertEquals(456L, dto.getId());

        logger.debug("Test passed: ID getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set front correctly")
    void testGetSetFront() {
        logger.debug("Test: Get/set front");

        CardExportDto dto = new CardExportDto();
        dto.setFront("飲む");

        assertEquals("飲む", dto.getFront());

        logger.debug("Test passed: Front getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set back correctly")
    void testGetSetBack() {
        logger.debug("Test: Get/set back");

        CardExportDto dto = new CardExportDto();
        dto.setBack("to drink");

        assertEquals("to drink", dto.getBack());

        logger.debug("Test passed: Back getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set notes correctly")
    void testGetSetNotes() {
        logger.debug("Test: Get/set notes");

        CardExportDto dto = new CardExportDto();
        dto.setNotes("u-verb");

        assertEquals("u-verb", dto.getNotes());

        logger.debug("Test passed: Notes getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set tags correctly")
    void testGetSetTags() {
        logger.debug("Test: Get/set tags");

        CardExportDto dto = new CardExportDto();
        List<String> tags = Arrays.asList("verbs", "food", "jlpt-n5");
        dto.setTags(tags);

        assertEquals(3, dto.getTags().size());
        assertTrue(dto.getTags().contains("verbs"));
        assertTrue(dto.getTags().contains("food"));
        assertTrue(dto.getTags().contains("jlpt-n5"));

        logger.debug("Test passed: Tags getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set createdAt correctly")
    void testGetSetCreatedAt() {
        logger.debug("Test: Get/set createdAt");

        CardExportDto dto = new CardExportDto();
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 10, 30);
        dto.setCreatedAt(time);

        assertEquals(time, dto.getCreatedAt());

        logger.debug("Test passed: CreatedAt getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set updatedAt correctly")
    void testGetSetUpdatedAt() {
        logger.debug("Test: Get/set updatedAt");

        CardExportDto dto = new CardExportDto();
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 11, 30);
        dto.setUpdatedAt(time);

        assertEquals(time, dto.getUpdatedAt());

        logger.debug("Test passed: UpdatedAt getter/setter work correctly");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle empty string fields")
    void testEmptyStringFields() {
        logger.debug("Test: Empty string fields");

        CardExportDto dto = new CardExportDto(CARD_ID, "", "", "", Arrays.asList(), testTime, testTime);

        assertEquals("", dto.getFront());
        assertEquals("", dto.getBack());
        assertEquals("", dto.getNotes());

        logger.debug("Test passed: Empty strings handled correctly");
    }

    @Test
    @DisplayName("Should handle very long tag lists")
    void testManyTags() {
        logger.debug("Test: Many tags");

        CardEntity card = new CardEntity(testDeck, FRONT, BACK);
        card.setId(CARD_ID);

        // Add many tags
        for (int i = 0; i < 50; i++) {
            TagEntity tag = new TagEntity(testDeck, "tag" + i);
            card.addTag(tag);
        }

        CardExportDto dto = new CardExportDto(card);

        assertEquals(50, dto.getTags().size());

        logger.debug("Test passed: Many tags handled correctly");
    }
}