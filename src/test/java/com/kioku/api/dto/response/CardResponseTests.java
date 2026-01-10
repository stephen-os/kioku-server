package com.kioku.api.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kioku.api.model.Card;
import com.kioku.api.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
class CardResponseTests {

    private static final Logger logger = LoggerFactory.getLogger(CardResponseTests.class);

    private ObjectMapper objectMapper;

    // Test data constants
    private static final Long TEST_CARD_ID = 1L;
    private static final String TEST_FRONT = "食べる";
    private static final String TEST_BACK = "to eat";
    private static final String TEST_NOTES = "ru-verb, ichidan verb";

    private Card mockCard;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up CardResponse test");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testCreatedAt = LocalDateTime.now();
        testUpdatedAt = LocalDateTime.now();

        mockCard = mock(Card.class);
        when(mockCard.getCardId()).thenReturn(TEST_CARD_ID);
        when(mockCard.getFront()).thenReturn(TEST_FRONT);
        when(mockCard.getBack()).thenReturn(TEST_BACK);
        when(mockCard.getNotes()).thenReturn(TEST_NOTES);
        when(mockCard.getTags()).thenReturn(Collections.emptySet());
        when(mockCard.getCreatedAt()).thenReturn(testCreatedAt);
        when(mockCard.getUpdatedAt()).thenReturn(testUpdatedAt);
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
    @DisplayName("Should create response from Card")
    void testEntityConstructor() {
        logger.debug("Test: Entity constructor");

        CardResponse response = new CardResponse(mockCard);

        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);
        assertThat(response.getFront()).isEqualTo(TEST_FRONT);
        assertThat(response.getBack()).isEqualTo(TEST_BACK);
        assertThat(response.getNotes()).isEqualTo(TEST_NOTES);
        assertThat(response.getTags()).isEmpty();
        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);
        assertThat(response.getUpdatedAt()).isEqualTo(testUpdatedAt);

        logger.debug("Test passed: Entity constructor works");
    }

    @Test
    @DisplayName("Should convert tags from entity to response")
    void testEntityConstructorWithTags() {
        logger.debug("Test: Entity constructor with tags");

        Tag mockTag1 = mock(Tag.class);
        when(mockTag1.getId()).thenReturn(1L);
        when(mockTag1.getName()).thenReturn("verbs");

        Tag mockTag2 = mock(Tag.class);
        when(mockTag2.getId()).thenReturn(2L);
        when(mockTag2.getName()).thenReturn("JLPT-N5");

        Set<Tag> tags = new HashSet<>();
        tags.add(mockTag1);
        tags.add(mockTag2);

        when(mockCard.getTags()).thenReturn(tags);

        CardResponse response = new CardResponse(mockCard);

        assertThat(response.getTags()).hasSize(2);
        assertThat(response.getTags()).extracting("name")
                .containsExactlyInAnyOrder("verbs", "JLPT-N5");

        logger.debug("Test passed: Tags converted correctly");
    }

    @Test
    @DisplayName("Should handle empty tags set")
    void testEntityConstructorWithEmptyTags() {
        logger.debug("Test: Entity constructor with empty tags");

        CardResponse response = new CardResponse(mockCard);

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
        LocalDateTime createdAt = LocalDateTime.now();
        response.setCreatedAt(createdAt);

        assertThat(response.getCreatedAt()).isEqualTo(createdAt);

        logger.debug("Test passed: CreatedAt setter and getter work");
    }

    @Test
    @DisplayName("Should set and get updatedAt correctly")
    void testSetAndGetUpdatedAt() {
        logger.debug("Test: Set and get updatedAt");

        CardResponse response = new CardResponse();
        LocalDateTime updatedAt = LocalDateTime.now();
        response.setUpdatedAt(updatedAt);

        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);

        logger.debug("Test passed: UpdatedAt setter and getter work");
    }

    // JSON Serialization Tests

    @Test
    @DisplayName("Should serialize to JSON correctly")
    void testSerializeToJson() throws Exception {
        logger.debug("Test: Serialize to JSON");

        CardResponse response = new CardResponse(mockCard);

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

        CardResponse original = new CardResponse(mockCard);

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

        when(mockCard.getNotes()).thenReturn(null);
        CardResponse response = new CardResponse(mockCard);

        assertThat(response.getNotes()).isNull();

        logger.debug("Test passed: Null notes handled");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle very long front text")
    void testVeryLongFront() {
        logger.debug("Test: Very long front text");

        String longText = "a".repeat(500);
        when(mockCard.getFront()).thenReturn(longText);
        CardResponse response = new CardResponse(mockCard);

        assertThat(response.getFront()).hasSize(500);
        assertThat(response.getFront()).isEqualTo(longText);

        logger.debug("Test passed: Very long front text handled");
    }

    @Test
    @DisplayName("Should handle special characters in text")
    void testSpecialCharacters() {
        logger.debug("Test: Special characters in text");

        when(mockCard.getFront()).thenReturn("日本語 & français! 🎌");
        when(mockCard.getBack()).thenReturn("Japanese & French! 🎌");
        CardResponse response = new CardResponse(mockCard);

        assertThat(response.getFront()).isEqualTo("日本語 & français! 🎌");
        assertThat(response.getBack()).isEqualTo("Japanese & French! 🎌");

        logger.debug("Test passed: Special characters handled");
    }

    @Test
    @DisplayName("Should handle multiple tags correctly")
    void testMultipleTags() {
        logger.debug("Test: Multiple tags");

        Set<Tag> tags = new HashSet<>();
        for (int i = 1; i <= 5; i++) {
            Tag mockTag = mock(Tag.class);
            when(mockTag.getId()).thenReturn((long) i);
            when(mockTag.getName()).thenReturn("tag" + i);
            tags.add(mockTag);
        }
        when(mockCard.getTags()).thenReturn(tags);

        CardResponse response = new CardResponse(mockCard);

        assertThat(response.getTags()).hasSize(5);

        logger.debug("Test passed: Multiple tags handled");
    }

    @Test
    @DisplayName("Should copy createdAt from Card without losing precision")
    void shouldPreserveTimestampPrecision() {
        logger.debug("Test: Preserve timestamp precision");

        CardResponse response = new CardResponse(mockCard);

        assertThat(response.getCreatedAt())
                .isEqualTo(testCreatedAt);

        logger.debug("Test passed: Preserve timestamp precision");
    }

    @Test
    @DisplayName("Should handle empty string notes")
    void testEmptyStringNotes() {
        logger.debug("Test: Empty string notes");

        when(mockCard.getNotes()).thenReturn("");
        CardResponse response = new CardResponse(mockCard);

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
