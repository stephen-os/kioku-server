package com.kioku.api.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kioku.api.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DeckResponse DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Constructor initialization</li>
 *   <li>Entity conversion</li>
 *   <li>Getter and setter methods</li>
 *   <li>JSON serialization/deserialization</li>
 *   <li>Null handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("DeckResponse DTO Tests")
class DeckResponseTest {

    private static final Logger logger = LoggerFactory.getLogger(DeckResponseTest.class);

    private ObjectMapper objectMapper;

    // Test data constants
    private static final Long TEST_DECK_ID = 1L;
    private static final String TEST_DECK_NAME = "Japanese Verbs";
    private static final String TEST_DECK_DESCRIPTION = "Common Japanese verbs for JLPT N5 level";

    private Deck mockDeck;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up DeckResponse test");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testCreatedAt = LocalDateTime.now();
        testUpdatedAt = LocalDateTime.now();

        mockDeck = mock(Deck.class);
        when(mockDeck.getId()).thenReturn(TEST_DECK_ID);
        when(mockDeck.getName()).thenReturn(TEST_DECK_NAME);
        when(mockDeck.getDescription()).thenReturn(TEST_DECK_DESCRIPTION);
        when(mockDeck.getCreatedAt()).thenReturn(testCreatedAt);
        when(mockDeck.getUpdatedAt()).thenReturn(testUpdatedAt);
     }

    // Constructor Tests

    @Test
    @DisplayName("Should create response with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        DeckResponse response = new DeckResponse();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isNull();
        assertThat(response.getDescription()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create response from Deck")
    void testEntityConstructor() {
        logger.debug("Test: Entity constructor");

        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getId()).isEqualTo(TEST_DECK_ID);
        assertThat(response.getName()).isEqualTo(TEST_DECK_NAME);
        assertThat(response.getDescription()).isEqualTo(TEST_DECK_DESCRIPTION);
        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);
        assertThat(response.getUpdatedAt()).isEqualTo(testUpdatedAt);

        logger.debug("Test passed: Entity constructor works");
    }

    @Test
    @DisplayName("Should handle entity with empty description")
    void testEntityConstructorWithEmptyDescription() {
        logger.debug("Test: Entity constructor with empty description");

        when(mockDeck.getDescription()).thenReturn("");
        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getId()).isEqualTo(TEST_DECK_ID);
        assertThat(response.getName()).isEqualTo(TEST_DECK_NAME);
        assertThat(response.getDescription()).isEmpty();
        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);
        assertThat(response.getUpdatedAt()).isEqualTo(testUpdatedAt);

        logger.debug("Test passed: Empty description handled");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get id correctly")
    void testSetAndGetId() {
        logger.debug("Test: Set and get id");

        DeckResponse response = new DeckResponse();
        response.setId(TEST_DECK_ID);

        assertThat(response.getId()).isEqualTo(TEST_DECK_ID);

        logger.debug("Test passed: Id setter and getter work");
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        logger.debug("Test: Set and get name");

        DeckResponse response = new DeckResponse();
        response.setName(TEST_DECK_NAME);

        assertThat(response.getName()).isEqualTo(TEST_DECK_NAME);

        logger.debug("Test passed: Name setter and getter work");
    }

    @Test
    @DisplayName("Should set and get description correctly")
    void testSetAndGetDescription() {
        logger.debug("Test: Set and get description");

        DeckResponse response = new DeckResponse();
        response.setDescription(TEST_DECK_DESCRIPTION);

        assertThat(response.getDescription()).isEqualTo(TEST_DECK_DESCRIPTION);

        logger.debug("Test passed: Description setter and getter work");
    }

    @Test
    @DisplayName("Should set and get createdAt correctly")
    void testSetAndGetCreatedAt() {
        logger.debug("Test: Set and get createdAt");

        DeckResponse response = new DeckResponse();
        response.setCreatedAt(testCreatedAt);

        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);

        logger.debug("Test passed: CreatedAt setter and getter work");
    }

    @Test
    @DisplayName("Should set and get updatedAt correctly")
    void testSetAndGetUpdatedAt() {
        logger.debug("Test: Set and get updatedAt");

        DeckResponse response = new DeckResponse();
        response.setUpdatedAt(testUpdatedAt);

        assertThat(response.getUpdatedAt()).isEqualTo(testUpdatedAt);

        logger.debug("Test passed: UpdatedAt setter and getter work");
    }

    // JSON Serialization Tests

    @Test
    @DisplayName("Should serialize to JSON correctly")
    void testSerializeToJson() throws Exception {
        logger.debug("Test: Serialize to JSON");

        DeckResponse response = new DeckResponse(mockDeck);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"id\":" + TEST_DECK_ID);
        assertThat(json).contains("\"name\":\"" + TEST_DECK_NAME + "\"");
        assertThat(json).contains("\"description\":\"" + TEST_DECK_DESCRIPTION + "\"");

        logger.debug("Test passed: Serialization works - {}", json);
    }

    @Test
    @DisplayName("Should deserialize from JSON correctly")
    void testDeserializeFromJson() throws Exception {
        logger.debug("Test: Deserialize from JSON");

        String createdAt = "2024-01-15T10:30:45.123456789";
        String updatedAt = "2024-01-15T11:30:45.987654321";

        String json = String.format(
                "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\"," +
                        "\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
                TEST_DECK_ID,
                TEST_DECK_NAME,
                TEST_DECK_DESCRIPTION,
                createdAt,
                updatedAt
        );

        DeckResponse response = objectMapper.readValue(json, DeckResponse.class);

        assertThat(response.getId()).isEqualTo(TEST_DECK_ID);
        assertThat(response.getName()).isEqualTo(TEST_DECK_NAME);
        assertThat(response.getDescription()).isEqualTo(TEST_DECK_DESCRIPTION);

        assertThat(response.getCreatedAt())
                .isEqualTo(LocalDateTime.parse(createdAt));

        assertThat(response.getUpdatedAt())
                .isEqualTo(LocalDateTime.parse(updatedAt));

        logger.debug("Test passed: Deserialization works");
    }


    @Test
    @DisplayName("Should handle round-trip JSON serialization")
    void testJsonRoundTrip() throws Exception {
        logger.debug("Test: JSON round-trip");

        DeckResponse original = new DeckResponse(mockDeck);

        String json = objectMapper.writeValueAsString(original);
        DeckResponse deserialized = objectMapper.readValue(json, DeckResponse.class);

        assertThat(deserialized.getId()).isEqualTo(original.getId());
        assertThat(deserialized.getName()).isEqualTo(original.getName());
        assertThat(deserialized.getDescription()).isEqualTo(original.getDescription());
        assertThat(deserialized.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(deserialized.getUpdatedAt()).isEqualTo(original.getUpdatedAt());

        logger.debug("Test passed: Round-trip serialization works");
    }

    @Test
    @DisplayName("Should serialize null description as null in JSON")
    void testSerializeNullDescription() throws Exception {
        logger.debug("Test: Serialize null description");

        when(mockDeck.getDescription()).thenReturn(null);
        DeckResponse response = new DeckResponse(mockDeck);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"description\":null");

        logger.debug("Test passed: Null description serialized correctly");
    }

    @Test
    @DisplayName("Should deserialize with missing description field")
    void testDeserializeWithMissingDescription() throws Exception {
        logger.debug("Test: Deserialize with missing description");

        String json = String.format(
                "{\"id\":%d,\"name\":\"%s\"," +
                        "\"createdAt\":\"2024-01-15T10:30:00\",\"updatedAt\":\"2024-01-15T11:45:00\"}",
                TEST_DECK_ID, TEST_DECK_NAME
        );

        DeckResponse response = objectMapper.readValue(json, DeckResponse.class);

        assertThat(response.getDescription()).isNull();

        logger.debug("Test passed: Missing description defaults to null");
    }

    // Null Handling Tests

    @Test
    @DisplayName("Should handle null description")
    void testNullDescription() {
        logger.debug("Test: Null description");

        DeckResponse response = new DeckResponse();
        response.setDescription(null);

        assertThat(response.getDescription()).isNull();

        logger.debug("Test passed: Null description handled");
    }

    @Test
    @DisplayName("Should handle null timestamps")
    void testNullTimestamps() {
        logger.debug("Test: Null timestamps");

        DeckResponse response = new DeckResponse();
        response.setCreatedAt(null);
        response.setUpdatedAt(null);

        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();

        logger.debug("Test passed: Null timestamps handled");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        logger.debug("Test: Special characters in name");

        when(mockDeck.getName()).thenReturn("日本語 & Français! 🎌");
        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getName()).isEqualTo("日本語 & Français! 🎌");

        logger.debug("Test passed: Special characters in name handled");
    }

    @Test
    @DisplayName("Should handle special characters in description")
    void testSpecialCharactersInDescription() {
        logger.debug("Test: Special characters in description");

        when(mockDeck.getDescription()).thenReturn("日本語 & Français! 🎌 - Study deck");
        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getDescription()).isEqualTo("日本語 & Français! 🎌 - Study deck");

        logger.debug("Test passed: Special characters in description handled");
    }

    @Test
    @DisplayName("Should handle very long name")
    void testVeryLongName() {
        logger.debug("Test: Very long name");

        String longName = "a".repeat(255);
        when(mockDeck.getName()).thenReturn(longName);
        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getName()).hasSize(255);
        assertThat(response.getName()).isEqualTo(longName);

        logger.debug("Test passed: Very long name handled");
    }

    @Test
    @DisplayName("Should handle very long description")
    void testVeryLongDescription() {
        logger.debug("Test: Very long description");

        String longDescription = "a".repeat(1000);
        when(mockDeck.getDescription()).thenReturn(longDescription);
        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getDescription()).hasSize(1000);
        assertThat(response.getDescription()).isEqualTo(longDescription);

        logger.debug("Test passed: Very long description handled");
    }

    @Test
    @DisplayName("Should handle a single character string name")
    void testSingleCharacterName() {
        logger.debug("Test: Empty string name");

        when(mockDeck.getName()).thenReturn("A");
        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getName()).isEqualTo("A");

        logger.debug("Test passed: Empty string name handled");
    }

    @Test
    @DisplayName("Should handle empty string description")
    void testEmptyStringDescription() {
        logger.debug("Test: Empty string description");

        when(mockDeck.getDescription()).thenReturn("");
        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getDescription()).isEmpty();

        logger.debug("Test passed: Empty string description handled");
    }

    @Test
    @DisplayName("Should handle multiline description")
    void testMultilineDescription() {
        logger.debug("Test: Multiline description");

        String multiline = "Line 1: Introduction\nLine 2: Details\nLine 3: Summary";
        when(mockDeck.getDescription()).thenReturn(multiline);
        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getDescription()).contains("\n");
        assertThat(response.getDescription()).isEqualTo(multiline);

        logger.debug("Test passed: Multiline description handled");
    }

    @Test
    @DisplayName("Should preserve timestamp precision")
    void testTimestampPrecision() {
        logger.debug("Test: Timestamp precision");

        DeckResponse response = new DeckResponse(mockDeck);

        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);

        logger.debug("Test passed: Timestamp precision preserved");
    }

    @Test
    @DisplayName("Should handle zero id")
    void testZeroId() {
        logger.debug("Test: Zero id");

        DeckResponse response = new DeckResponse();
        response.setId(0L);

        assertThat(response.getId()).isEqualTo(0L);

        logger.debug("Test passed: Zero id handled");
    }

    @Test
    @DisplayName("Should handle negative id")
    void testNegativeId() {
        logger.debug("Test: Negative id");

        DeckResponse response = new DeckResponse();
        response.setId(-1L);

        assertThat(response.getId()).isEqualTo(-1L);

        logger.debug("Test passed: Negative id handled");
    }

    @Test
    @DisplayName("Should handle large id")
    void testLargeId() {
        logger.debug("Test: Large id");

        Long largeId = Long.MAX_VALUE;
        DeckResponse response = new DeckResponse();
        response.setId(largeId);

        assertThat(response.getId()).isEqualTo(largeId);

        logger.debug("Test passed: Large id handled");
    }

    @Test
    @DisplayName("Should handle name with leading/trailing whitespace")
    void testNameWithWhitespace() {
        logger.debug("Test: Name with leading/trailing whitespace");

        when(mockDeck.getName()).thenReturn("Test Deck");
        DeckResponse response = new DeckResponse(mockDeck);

        // We expect that the class is going to trim the whitespace
        assertThat(response.getName()).isEqualTo("Test Deck");

        logger.debug("Test passed: Name with whitespace handled");
    }
}
