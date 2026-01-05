package com.kioku.api.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TagResponse DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Constructor initialization</li>
 *   <li>Entity conversion</li>
 *   <li>Getter and setter methods</li>
 *   <li>JSON serialization/deserialization</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("TagResponse DTO Tests")
class TagResponseTest {

    private static final Logger logger = LoggerFactory.getLogger(TagResponseTest.class);

    private ObjectMapper objectMapper;

    // Test data constants
    private static final Long TEST_TAG_ID = 1L;
    private static final String TEST_TAG_NAME = "verbs";

    private Tag testTag;
    private Deck testDeck;
    private User testUser;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up TagResponse test");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new User("user@example.com", "hashedPassword");
        testUser.setId(1L);

        testDeck = new Deck(testUser, "Test Deck", "Test Description");
        testDeck.setId(1L);

        testTag = new Tag(testDeck, TEST_TAG_NAME);
        testTag.setId(TEST_TAG_ID);
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create response with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        TagResponse response = new TagResponse();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create response with parameterized constructor")
    void testParameterizedConstructor() {
        logger.debug("Test: Parameterized constructor");

        TagResponse response = new TagResponse(TEST_TAG_ID, TEST_TAG_NAME);

        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.getName()).isEqualTo(TEST_TAG_NAME);

        logger.debug("Test passed: Parameterized constructor works");
    }

    @Test
    @DisplayName("Should create response from Tag")
    void testEntityConstructor() {
        logger.debug("Test: Entity constructor");

        TagResponse response = new TagResponse(testTag);

        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.getName()).isEqualTo(TEST_TAG_NAME);

        logger.debug("Test passed: Entity constructor works");
    }

    @Test
    @DisplayName("Should handle entity with different values")
    void testEntityConstructorWithDifferentValues() {
        logger.debug("Test: Entity constructor with different values");

        Tag customTag = new Tag(testDeck, "JLPT-N5");
        customTag.setId(99L);

        TagResponse response = new TagResponse(customTag);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getName()).isEqualTo("JLPT-N5");

        logger.debug("Test passed: Different entity values handled");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get id correctly")
    void testSetAndGetId() {
        logger.debug("Test: Set and get id");

        TagResponse response = new TagResponse();
        response.setId(TEST_TAG_ID);

        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);

        logger.debug("Test passed: Id setter and getter work");
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        logger.debug("Test: Set and get name");

        TagResponse response = new TagResponse();
        response.setName(TEST_TAG_NAME);

        assertThat(response.getName()).isEqualTo(TEST_TAG_NAME);

        logger.debug("Test passed: Name setter and getter work");
    }

    // JSON Serialization Tests

    @Test
    @DisplayName("Should serialize to JSON correctly")
    void testSerializeToJson() throws Exception {
        logger.debug("Test: Serialize to JSON");

        TagResponse response = new TagResponse(testTag);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"id\":" + TEST_TAG_ID);
        assertThat(json).contains("\"name\":\"" + TEST_TAG_NAME + "\"");

        logger.debug("Test passed: Serialization works - {}", json);
    }

    @Test
    @DisplayName("Should deserialize from JSON correctly")
    void testDeserializeFromJson() throws Exception {
        logger.debug("Test: Deserialize from JSON");

        String json = String.format("{\"id\":%d,\"name\":\"%s\"}", TEST_TAG_ID, TEST_TAG_NAME);

        TagResponse response = objectMapper.readValue(json, TagResponse.class);

        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.getName()).isEqualTo(TEST_TAG_NAME);

        logger.debug("Test passed: Deserialization works");
    }

    @Test
    @DisplayName("Should handle round-trip JSON serialization")
    void testJsonRoundTrip() throws Exception {
        logger.debug("Test: JSON round-trip");

        TagResponse original = new TagResponse(testTag);

        String json = objectMapper.writeValueAsString(original);
        TagResponse deserialized = objectMapper.readValue(json, TagResponse.class);

        assertThat(deserialized.getId()).isEqualTo(original.getId());
        assertThat(deserialized.getName()).isEqualTo(original.getName());

        logger.debug("Test passed: Round-trip serialization works");
    }

    @Test
    @DisplayName("Should serialize null fields as null in JSON")
    void testSerializeNullFields() throws Exception {
        logger.debug("Test: Serialize null fields");

        TagResponse response = new TagResponse();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"id\":null");
        assertThat(json).contains("\"name\":null");

        logger.debug("Test passed: Null fields serialized correctly");
    }

    @Test
    @DisplayName("Should deserialize with missing fields")
    void testDeserializeWithMissingFields() throws Exception {
        logger.debug("Test: Deserialize with missing fields");

        String json = "{}";

        TagResponse response = objectMapper.readValue(json, TagResponse.class);

        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isNull();

        logger.debug("Test passed: Missing fields default to null");
    }

    // Null Handling Tests

    @Test
    @DisplayName("Should handle null id")
    void testNullId() {
        logger.debug("Test: Null id");

        TagResponse response = new TagResponse(null, TEST_TAG_NAME);

        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isEqualTo(TEST_TAG_NAME);

        logger.debug("Test passed: Null id handled");
    }

    @Test
    @DisplayName("Should handle null name")
    void testNullName() {
        logger.debug("Test: Null name");

        TagResponse response = new TagResponse(TEST_TAG_ID, null);

        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.getName()).isNull();

        logger.debug("Test passed: Null name handled");
    }

    @Test
    @DisplayName("Should handle both fields null")
    void testBothFieldsNull() {
        logger.debug("Test: Both fields null");

        TagResponse response = new TagResponse(null, null);

        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isNull();

        logger.debug("Test passed: Both null fields handled");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        logger.debug("Test: Special characters in name");

        Tag specialTag = new Tag(testDeck, "N5-動詞");
        specialTag.setId(TEST_TAG_ID);

        TagResponse response = new TagResponse(specialTag);

        assertThat(response.getName()).isEqualTo("N5-動詞");

        logger.debug("Test passed: Special characters in name handled");
    }

    @Test
    @DisplayName("Should handle emoji in name")
    void testEmojiInName() {
        logger.debug("Test: Emoji in name");

        Tag emojiTag = new Tag(testDeck, "important⭐");
        emojiTag.setId(TEST_TAG_ID);

        TagResponse response = new TagResponse(emojiTag);

        assertThat(response.getName()).isEqualTo("important⭐");

        logger.debug("Test passed: Emoji in name handled");
    }

    @Test
    @DisplayName("Should handle very long name")
    void testVeryLongName() {
        logger.debug("Test: Very long name");

        String longName = "a".repeat(100);
        Tag longTag = new Tag(testDeck, longName);
        longTag.setId(TEST_TAG_ID);

        TagResponse response = new TagResponse(longTag);

        assertThat(response.getName()).hasSize(100);
        assertThat(response.getName()).isEqualTo(longName);

        logger.debug("Test passed: Very long name handled");
    }

    @Test
    @DisplayName("Should handle single character name")
    void testSingleCharacterName() {
        logger.debug("Test: Single character name");

        Tag singleTag = new Tag(testDeck, "A");
        singleTag.setId(TEST_TAG_ID);

        TagResponse response = new TagResponse(singleTag);

        assertThat(response.getName()).isEqualTo("A");

        logger.debug("Test passed: Single character name handled");
    }

    @Test
    @DisplayName("Should handle numeric name")
    void testNumericName() {
        logger.debug("Test: Numeric name");

        Tag numericTag = new Tag(testDeck, "123");
        numericTag.setId(TEST_TAG_ID);

        TagResponse response = new TagResponse(numericTag);

        assertThat(response.getName()).isEqualTo("123");

        logger.debug("Test passed: Numeric name handled");
    }

    @Test
    @DisplayName("Should handle name with spaces")
    void testNameWithSpaces() {
        logger.debug("Test: Name with spaces");

        Tag spaceTag = new Tag(testDeck, "ru verbs");
        spaceTag.setId(TEST_TAG_ID);

        TagResponse response = new TagResponse(spaceTag);

        assertThat(response.getName()).isEqualTo("ru verbs");

        logger.debug("Test passed: Name with spaces handled");
    }

    @Test
    @DisplayName("Should preserve case sensitivity in name")
    void testCaseSensitivity() {
        logger.debug("Test: Case sensitivity in name");

        Tag upperTag = new Tag(testDeck, "VERBS");
        upperTag.setId(1L);

        Tag lowerTag = new Tag(testDeck, "verbs");
        lowerTag.setId(2L);

        TagResponse upperResponse = new TagResponse(upperTag);
        TagResponse lowerResponse = new TagResponse(lowerTag);

        assertThat(upperResponse.getName()).isEqualTo("VERBS");
        assertThat(lowerResponse.getName()).isEqualTo("verbs");
        assertThat(upperResponse.getName()).isNotEqualTo(lowerResponse.getName());

        logger.debug("Test passed: Case sensitivity preserved");
    }

    @Test
    @DisplayName("Should handle zero id")
    void testZeroId() {
        logger.debug("Test: Zero id");

        TagResponse response = new TagResponse(0L, TEST_TAG_NAME);

        assertThat(response.getId()).isEqualTo(0L);

        logger.debug("Test passed: Zero id handled");
    }

    @Test
    @DisplayName("Should handle negative id")
    void testNegativeId() {
        logger.debug("Test: Negative id");

        TagResponse response = new TagResponse(-1L, TEST_TAG_NAME);

        assertThat(response.getId()).isEqualTo(-1L);

        logger.debug("Test passed: Negative id handled");
    }

    @Test
    @DisplayName("Should handle large id")
    void testLargeId() {
        logger.debug("Test: Large id");

        Long largeId = Long.MAX_VALUE;
        TagResponse response = new TagResponse(largeId, TEST_TAG_NAME);

        assertThat(response.getId()).isEqualTo(largeId);

        logger.debug("Test passed: Large id handled");
    }

    @Test
    @DisplayName("Should handle empty string name")
    void testEmptyStringName() {
        logger.debug("Test: Empty string name");

        TagResponse response = new TagResponse(TEST_TAG_ID, "");

        assertThat(response.getName()).isEmpty();

        logger.debug("Test passed: Empty string name handled");
    }

    @Test
    @DisplayName("Should handle name with hyphens")
    void testNameWithHyphens() {
        logger.debug("Test: Name with hyphens");

        Tag hyphenTag = new Tag(testDeck, "JLPT-N5");
        hyphenTag.setId(TEST_TAG_ID);

        TagResponse response = new TagResponse(hyphenTag);

        assertThat(response.getName()).isEqualTo("JLPT-N5");

        logger.debug("Test passed: Name with hyphens handled");
    }

    @Test
    @DisplayName("Should handle name with underscores")
    void testNameWithUnderscores() {
        logger.debug("Test: Name with underscores");

        Tag underscoreTag = new Tag(testDeck, "ru_verbs");
        underscoreTag.setId(TEST_TAG_ID);

        TagResponse response = new TagResponse(underscoreTag);

        assertThat(response.getName()).isEqualTo("ru_verbs");

        logger.debug("Test passed: Name with underscores handled");
    }
}