package com.kioku.api.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CardResponse DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Constructor initialization</li>
 *   <li>Entity conversion</li>
 *   <li>Getter and setter methods</li>
 *   <li>JSON serialization/deserialization</li>
 *   <li>Tag association handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("CardResponse DTO Tests")
class CardResponseTest {

    private static final Logger logger = LoggerFactory.getLogger(CardResponseTest.class);

    private ObjectMapper objectMapper;

    // Test data constants
    private static final Long TEST_CARD_ID = 1L;
    private static final String TEST_FRONT = "食べる";
    private static final String TEST_BACK = "to eat";
    private static final String TEST_NOTES = "ru-verb, ichidan verb";

    private CardEntity testCard;
    private DeckEntity testDeck;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up CardResponse test");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new UserEntity("user@example.com", "hashedPassword");
        testUser.setId(1L);

        testDeck = new DeckEntity(testUser, "Test Deck", "Test Description");
        testDeck.setId(1L);

        testCard = new CardEntity(testDeck, TEST_FRONT, TEST_BACK);
        testCard.setId(TEST_CARD_ID);
        testCard.setNotes(TEST_NOTES);
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create response with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        CardResponse response = new CardResponse();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getFront()).isNull();
        assertThat(response.getBack()).isNull();
        assertThat(response.getNotes()).isNull();
        assertThat(response.getTags()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create response from CardEntity")
    void testEntityConstructor() {
        logger.debug("Test: Entity constructor");

        CardResponse response = new CardResponse(testCard);

        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);
        assertThat(response.getFront()).isEqualTo(TEST_FRONT);
        assertThat(response.getBack()).isEqualTo(TEST_BACK);
        assertThat(response.getNotes()).isEqualTo(TEST_NOTES);
        assertThat(response.getTags()).isEmpty();
        assertThat(response.getCreatedAt()).isEqualTo(testCard.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(testCard.getUpdatedAt());

        logger.debug("Test passed: Entity constructor works");
    }

    @Test
    @DisplayName("Should convert tags from entity to response")
    void testEntityConstructorWithTags() {
        logger.debug("Test: Entity constructor with tags");

        TagEntity tag1 = new TagEntity(testDeck, "verbs");
        tag1.setId(1L);
        TagEntity tag2 = new TagEntity(testDeck, "JLPT-N5");
        tag2.setId(2L);

        testCard.addTag(tag1);
        testCard.addTag(tag2);

        CardResponse response = new CardResponse(testCard);

        assertThat(response.getTags()).hasSize(2);
        assertThat(response.getTags()).extracting("name")
                .containsExactlyInAnyOrder("verbs", "JLPT-N5");

        logger.debug("Test passed: Tags converted correctly");
    }

    @Test
    @DisplayName("Should handle empty tags set")
    void testEntityConstructorWithEmptyTags() {
        logger.debug("Test: Entity constructor with empty tags");

        CardResponse response = new CardResponse(testCard);

        assertThat(response.getTags()).isEmpty();
        assertThat(response.getTags()).isNotNull();

        logger.debug("Test passed: Empty tags handled");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get id correctly")
    void testSetAndGetId() {
        logger.debug("Test: Set and get id");

        CardResponse response = new CardResponse();
        response.setId(TEST_CARD_ID);

        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);

        logger.debug("Test passed: Id setter and getter work");
    }

    @Test
    @DisplayName("Should set and get front correctly")
    void testSetAndGetFront() {
        logger.debug("Test: Set and get front");

        CardResponse response = new CardResponse();
        response.setFront(TEST_FRONT);

        assertThat(response.getFront()).isEqualTo(TEST_FRONT);

        logger.debug("Test passed: Front setter and getter work");
    }

    @Test
    @DisplayName("Should set and get back correctly")
    void testSetAndGetBack() {
        logger.debug("Test: Set and get back");

        CardResponse response = new CardResponse();
        response.setBack(TEST_BACK);

        assertThat(response.getBack()).isEqualTo(TEST_BACK);

        logger.debug("Test passed: Back setter and getter work");
    }

    @Test
    @DisplayName("Should set and get notes correctly")
    void testSetAndGetNotes() {
        logger.debug("Test: Set and get notes");

        CardResponse response = new CardResponse();
        response.setNotes(TEST_NOTES);

        assertThat(response.getNotes()).isEqualTo(TEST_NOTES);

        logger.debug("Test passed: Notes setter and getter work");
    }

    @Test
    @DisplayName("Should set and get tags correctly")
    void testSetAndGetTags() {
        logger.debug("Test: Set and get tags");

        Set<TagResponse> tags = new HashSet<>();
        tags.add(new TagResponse(1L, "verbs"));
        tags.add(new TagResponse(2L, "JLPT-N5"));

        CardResponse response = new CardResponse();
        response.setTags(tags);

        assertThat(response.getTags()).hasSize(2);
        assertThat(response.getTags()).extracting("name")
                .containsExactlyInAnyOrder("verbs", "JLPT-N5");

        logger.debug("Test passed: Tags setter and getter work");
    }

    @Test
    @DisplayName("Should set and get createdAt correctly")
    void testSetAndGetCreatedAt() {
        logger.debug("Test: Set and get createdAt");

        CardResponse response = new CardResponse();
        LocalDateTime testCreatedAt = LocalDateTime.now();
        response.setCreatedAt(testCreatedAt);

        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);

        logger.debug("Test passed: CreatedAt setter and getter work");
    }

    @Test
    @DisplayName("Should set and get updatedAt correctly")
    void testSetAndGetUpdatedAt() {
        logger.debug("Test: Set and get updatedAt");

        CardResponse response = new CardResponse();
        LocalDateTime testUpdatedAt = LocalDateTime.now();
        response.setUpdatedAt(testUpdatedAt);

        assertThat(response.getUpdatedAt()).isEqualTo(testUpdatedAt);

        logger.debug("Test passed: UpdatedAt setter and getter work");
    }

    // JSON Serialization Tests

    @Test
    @DisplayName("Should serialize to JSON correctly")
    void testSerializeToJson() throws Exception {
        logger.debug("Test: Serialize to JSON");

        CardResponse response = new CardResponse(testCard);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"id\":" + TEST_CARD_ID);
        assertThat(json).contains("\"front\":\"" + TEST_FRONT + "\"");
        assertThat(json).contains("\"back\":\"" + TEST_BACK + "\"");
        assertThat(json).contains("\"notes\":\"" + TEST_NOTES + "\"");

        logger.debug("Test passed: Serialization works");
    }

    @Test
    @DisplayName("Should deserialize from JSON correctly")
    void testDeserializeFromJson() throws Exception {
        logger.debug("Test: Deserialize from JSON");

        String json = String.format(
                "{\"id\":%d,\"front\":\"%s\",\"back\":\"%s\",\"notes\":\"%s\"," +
                "\"tags\":[],\"createdAt\":\"2024-01-15T10:30:00\",\"updatedAt\":\"2024-01-15T11:45:00\"}",
                TEST_CARD_ID, TEST_FRONT, TEST_BACK, TEST_NOTES
        );

        CardResponse response = objectMapper.readValue(json, CardResponse.class);

        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);
        assertThat(response.getFront()).isEqualTo(TEST_FRONT);
        assertThat(response.getBack()).isEqualTo(TEST_BACK);
        assertThat(response.getNotes()).isEqualTo(TEST_NOTES);
        assertThat(response.getTags()).isEmpty();

        logger.debug("Test passed: Deserialization works");
    }

    @Test
    @DisplayName("Should handle round-trip JSON serialization")
    void testJsonRoundTrip() throws Exception {
        logger.debug("Test: JSON round-trip");

        CardResponse original = new CardResponse(testCard);

        String json = objectMapper.writeValueAsString(original);
        CardResponse deserialized = objectMapper.readValue(json, CardResponse.class);

        assertThat(deserialized.getId()).isEqualTo(original.getId());
        assertThat(deserialized.getFront()).isEqualTo(original.getFront());
        assertThat(deserialized.getBack()).isEqualTo(original.getBack());
        assertThat(deserialized.getNotes()).isEqualTo(original.getNotes());
        assertThat(deserialized.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(deserialized.getUpdatedAt()).isEqualTo(original.getUpdatedAt());

        logger.debug("Test passed: Round-trip serialization works");
    }

    // Null Handling Tests

    @Test
    @DisplayName("Should handle null notes in entity")
    void testNullNotes() {
        logger.debug("Test: Null notes");

        testCard.setNotes(null);
        CardResponse response = new CardResponse(testCard);

        assertThat(response.getNotes()).isNull();

        logger.debug("Test passed: Null notes handled");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle very long front text")
    void testVeryLongFront() {
        logger.debug("Test: Very long front text");

        String longText = "a".repeat(500);
        testCard.setFront(longText);
        CardResponse response = new CardResponse(testCard);

        assertThat(response.getFront()).hasSize(500);
        assertThat(response.getFront()).isEqualTo(longText);

        logger.debug("Test passed: Very long front text handled");
    }

    @Test
    @DisplayName("Should handle special characters in text")
    void testSpecialCharacters() {
        logger.debug("Test: Special characters in text");

        testCard.setFront("日本語 & français! 🎌");
        testCard.setBack("Japanese & French! 🎌");
        CardResponse response = new CardResponse(testCard);

        assertThat(response.getFront()).isEqualTo("日本語 & français! 🎌");
        assertThat(response.getBack()).isEqualTo("Japanese & French! 🎌");

        logger.debug("Test passed: Special characters handled");
    }

    @Test
    @DisplayName("Should handle multiple tags correctly")
    void testMultipleTags() {
        logger.debug("Test: Multiple tags");

        for (int i = 1; i <= 5; i++) {
            TagEntity tag = new TagEntity(testDeck, "tag" + i);
            tag.setId((long) i);
            testCard.addTag(tag);
        }

        CardResponse response = new CardResponse(testCard);

        assertThat(response.getTags()).hasSize(5);

        logger.debug("Test passed: Multiple tags handled");
    }

    @Test
    @DisplayName("Should copy createdAt from CardEntity without losing precision")
    void shouldPreserveTimestampPrecision() {
        logger.debug("Test: Preserve timestamp precision");

        CardResponse response = new CardResponse(testCard);

        assertThat(response.getCreatedAt())
                .isEqualTo(testCard.getCreatedAt());

        logger.debug("Test passed: Preserve timestamp precision");
    }

    @Test
    @DisplayName("Should handle empty string notes")
    void testEmptyStringNotes() {
        logger.debug("Test: Empty string notes");

        testCard.setNotes("");
        CardResponse response = new CardResponse(testCard);

        assertThat(response.getNotes()).isEmpty();

        logger.debug("Test passed: Empty string notes handled");
    }

    @Test
    @DisplayName("Should handle zero id")
    void testZeroId() {
        logger.debug("Test: Zero id");

        CardResponse response = new CardResponse();
        response.setId(0L);

        assertThat(response.getId()).isEqualTo(0L);

        logger.debug("Test passed: Zero id handled");
    }
}