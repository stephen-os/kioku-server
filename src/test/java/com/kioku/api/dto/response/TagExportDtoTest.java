package com.kioku.api.dto.response;

import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TagExportDto.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Construction from TagEntity</li>
 *   <li>Constructor behavior with all fields</li>
 *   <li>Getter and setter functionality</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("TagExportDto Tests")
class TagExportDtoTest {

    private static final Logger logger = LoggerFactory.getLogger(TagExportDtoTest.class);

    private static final Long TAG_ID = 456L;
    private static final String TAG_NAME = "verbs";

    private UserEntity testUser;
    private DeckEntity testDeck;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up TagExportDto test");

        testUser = new UserEntity("test@example.com", "hashedPassword");
        testDeck = new DeckEntity(testUser, "Test Deck", "Description");
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create TagExportDto with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        TagExportDto dto = new TagExportDto();

        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());

        logger.debug("Test passed: Default constructor creates instance");
    }

    @Test
    @DisplayName("Should create TagExportDto from TagEntity")
    void testEntityConstructor() {
        logger.debug("Test: Entity constructor");

        TagEntity tag = new TagEntity(testDeck, TAG_NAME);
        tag.setId(TAG_ID);

        TagExportDto dto = new TagExportDto(tag);

        assertEquals(TAG_ID, dto.getId());
        assertEquals(TAG_NAME, dto.getName());

        logger.debug("Test passed: DTO created from entity");
    }

    @Test
    @DisplayName("Should create TagExportDto with full constructor")
    void testFullConstructor() {
        logger.debug("Test: Full constructor");

        TagExportDto dto = new TagExportDto(TAG_ID, TAG_NAME);

        assertEquals(TAG_ID, dto.getId());
        assertEquals(TAG_NAME, dto.getName());

        logger.debug("Test passed: Full constructor sets all fields");
    }

    // Entity Constructor Edge Cases

    @Test
    @DisplayName("Should handle entity with null ID")
    void testEntityConstructorWithNullId() {
        logger.debug("Test: Entity constructor with null ID");

        TagEntity tag = new TagEntity(testDeck, TAG_NAME);
        // ID not set (null)

        TagExportDto dto = new TagExportDto(tag);

        assertNull(dto.getId());
        assertEquals(TAG_NAME, dto.getName());

        logger.debug("Test passed: Null ID handled correctly");
    }

    @Test
    @DisplayName("Should handle entity with different tag names")
    void testEntityConstructorWithDifferentNames() {
        logger.debug("Test: Entity constructor with different names");

        String[] tagNames = {"verbs", "nouns", "adjectives", "jlpt-n5", "food"};

        for (String tagName : tagNames) {
            TagEntity tag = new TagEntity(testDeck, tagName);
            tag.setId(TAG_ID);

            TagExportDto dto = new TagExportDto(tag);

            assertEquals(TAG_ID, dto.getId());
            assertEquals(tagName, dto.getName());
        }

        logger.debug("Test passed: Different tag names handled correctly");
    }

    @Test
    @DisplayName("Should handle entity with unicode tag name")
    void testEntityConstructorWithUnicode() {
        logger.debug("Test: Entity constructor with unicode");

        TagEntity tag = new TagEntity(testDeck, "動詞");
        tag.setId(TAG_ID);

        TagExportDto dto = new TagExportDto(tag);

        assertEquals(TAG_ID, dto.getId());
        assertEquals("動詞", dto.getName());

        logger.debug("Test passed: Unicode tag name handled correctly");
    }

    @Test
    @DisplayName("Should handle entity with tag name containing special characters")
    void testEntityConstructorWithSpecialCharacters() {
        logger.debug("Test: Entity constructor with special characters");

        String[] specialNames = {
                "jlpt-n5",
                "common_verbs",
                "chapter 1",
                "verbs & nouns"
        };

        for (String tagName : specialNames) {
            TagEntity tag = new TagEntity(testDeck, tagName);
            tag.setId(TAG_ID);

            TagExportDto dto = new TagExportDto(tag);

            assertEquals(TAG_ID, dto.getId());
            assertEquals(tagName, dto.getName());
        }

        logger.debug("Test passed: Special characters handled correctly");
    }

    // Getter/Setter Tests

    @Test
    @DisplayName("Should get and set id correctly")
    void testGetSetId() {
        logger.debug("Test: Get/set id");

        TagExportDto dto = new TagExportDto();
        dto.setId(789L);

        assertEquals(789L, dto.getId());

        logger.debug("Test passed: ID getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set name correctly")
    void testGetSetName() {
        logger.debug("Test: Get/set name");

        TagExportDto dto = new TagExportDto();
        dto.setName("adjectives");

        assertEquals("adjectives", dto.getName());

        logger.debug("Test passed: Name getter/setter work correctly");
    }

    @Test
    @DisplayName("Should allow overwriting id")
    void testOverwriteId() {
        logger.debug("Test: Overwriting id");

        TagExportDto dto = new TagExportDto(TAG_ID, TAG_NAME);
        assertEquals(TAG_ID, dto.getId());

        dto.setId(999L);
        assertEquals(999L, dto.getId());

        logger.debug("Test passed: ID can be overwritten");
    }

    @Test
    @DisplayName("Should allow overwriting name")
    void testOverwriteName() {
        logger.debug("Test: Overwriting name");

        TagExportDto dto = new TagExportDto(TAG_ID, TAG_NAME);
        assertEquals(TAG_NAME, dto.getName());

        dto.setName("nouns");
        assertEquals("nouns", dto.getName());

        logger.debug("Test passed: Name can be overwritten");
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void testSetNullValues() {
        logger.debug("Test: Setting null values");

        TagExportDto dto = new TagExportDto(TAG_ID, TAG_NAME);

        dto.setId(null);
        dto.setName(null);

        assertNull(dto.getId());
        assertNull(dto.getName());

        logger.debug("Test passed: Null values handled correctly");
    }

    // Edge Cases

    @Test
    @DisplayName("Should handle empty string name")
    void testEmptyStringName() {
        logger.debug("Test: Empty string name");

        TagExportDto dto = new TagExportDto(TAG_ID, "");

        assertEquals("", dto.getName());

        logger.debug("Test passed: Empty string handled correctly");
    }

    @Test
    @DisplayName("Should handle very long tag name")
    void testVeryLongTagName() {
        logger.debug("Test: Very long tag name");

        String longName = "a".repeat(50);
        TagExportDto dto = new TagExportDto(TAG_ID, longName);

        assertEquals(longName, dto.getName());
        assertEquals(50, dto.getName().length());

        logger.debug("Test passed: Long tag name handled correctly");
    }

    @Test
    @DisplayName("Should handle zero and negative IDs")
    void testZeroAndNegativeIds() {
        logger.debug("Test: Zero and negative IDs");

        TagExportDto dto1 = new TagExportDto(0L, TAG_NAME);
        assertEquals(0L, dto1.getId());

        TagExportDto dto2 = new TagExportDto(-1L, TAG_NAME);
        assertEquals(-1L, dto2.getId());

        logger.debug("Test passed: Zero and negative IDs handled correctly");
    }

    @Test
    @DisplayName("Should handle very large ID values")
    void testVeryLargeId() {
        logger.debug("Test: Very large ID");

        Long largeId = Long.MAX_VALUE;
        TagExportDto dto = new TagExportDto(largeId, TAG_NAME);

        assertEquals(largeId, dto.getId());

        logger.debug("Test passed: Large ID handled correctly");
    }

    // Multiple Instance Tests

    @Test
    @DisplayName("Should create multiple independent instances")
    void testMultipleInstances() {
        logger.debug("Test: Multiple independent instances");

        TagExportDto dto1 = new TagExportDto(1L, "verbs");
        TagExportDto dto2 = new TagExportDto(2L, "nouns");
        TagExportDto dto3 = new TagExportDto(3L, "adjectives");

        assertEquals(1L, dto1.getId());
        assertEquals("verbs", dto1.getName());

        assertEquals(2L, dto2.getId());
        assertEquals("nouns", dto2.getName());

        assertEquals(3L, dto3.getId());
        assertEquals("adjectives", dto3.getName());

        logger.debug("Test passed: Multiple instances are independent");
    }
}