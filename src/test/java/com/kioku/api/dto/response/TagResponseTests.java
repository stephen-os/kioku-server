package com.kioku.api.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kioku.api.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
class TagResponseTests {

    private static final Logger logger = LoggerFactory.getLogger(TagResponseTests.class);

    private ObjectMapper objectMapper;

    // Test data constants
    private static final Long TEST_TAG_ID = 1L;
    private static final String TEST_TAG_NAME = "verbs";

    private Tag mockTag;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up TagResponse test");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockTag = mock(Tag.class);
        when(mockTag.getId()).thenReturn(TEST_TAG_ID);
        when(mockTag.getName()).thenReturn(TEST_TAG_NAME);
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

        TagResponse response = new TagResponse(mockTag);

        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.getName()).isEqualTo(TEST_TAG_NAME);

        logger.debug("Test passed: Entity constructor works");
    }

    @Test
    @DisplayName("Should handle entity with different values")
    void testEntityConstructorWithDifferentValues() {
        logger.debug("Test: Entity constructor with different values");

        Tag customMockTag = mock(Tag.class);
        when(customMockTag.getId()).thenReturn(99L);
        when(customMockTag.getName()).thenReturn("JLPT-N5");

        TagResponse response = new TagResponse(customMockTag);

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

        TagResponse response = new TagResponse(mockTag);

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

        TagResponse original = new TagResponse(mockTag);

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

        Tag specialMockTag = mock(Tag.class);
        when(specialMockTag.getId()).thenReturn(TEST_TAG_ID);
        when(specialMockTag.getName()).thenReturn("N5-動詞");

        TagResponse response = new TagResponse(specialMockTag);

        assertThat(response.getName()).isEqualTo("N5-動詞");

        logger.debug("Test passed: Special characters in name handled");
    }

    @Test
    @DisplayName("Should handle emoji in name")
    void testEmojiInName() {
        logger.debug("Test: Emoji in name");

        Tag emojiMockTag = mock(Tag.class);
        when(emojiMockTag.getId()).thenReturn(TEST_TAG_ID);
        when(emojiMockTag.getName()).thenReturn("important⭐");

        TagResponse response = new TagResponse(emojiMockTag);

        assertThat(response.getName()).isEqualTo("important⭐");

        logger.debug("Test passed: Emoji in name handled");
    }

    @Test
    @DisplayName("Should handle very long name")
    void testVeryLongName() {
        logger.debug("Test: Very long name");

        String longName = "a".repeat(100);
        Tag longMockTag = mock(Tag.class);
        when(longMockTag.getId()).thenReturn(TEST_TAG_ID);
        when(longMockTag.getName()).thenReturn(longName);

        TagResponse response = new TagResponse(longMockTag);

        assertThat(response.getName()).hasSize(100);
        assertThat(response.getName()).isEqualTo(longName);

        logger.debug("Test passed: Very long name handled");
    }

    @Test
    @DisplayName("Should handle single character name")
    void testSingleCharacterName() {
        logger.debug("Test: Single character name");

        Tag singleMockTag = mock(Tag.class);
        when(singleMockTag.getId()).thenReturn(TEST_TAG_ID);
        when(singleMockTag.getName()).thenReturn("A");

        TagResponse response = new TagResponse(singleMockTag);

        assertThat(response.getName()).isEqualTo("A");

        logger.debug("Test passed: Single character name handled");
    }

    @Test
    @DisplayName("Should handle numeric name")
    void testNumericName() {
        logger.debug("Test: Numeric name");

        Tag numericMockTag = mock(Tag.class);
        when(numericMockTag.getId()).thenReturn(TEST_TAG_ID);
        when(numericMockTag.getName()).thenReturn("123");

        TagResponse response = new TagResponse(numericMockTag);

        assertThat(response.getName()).isEqualTo("123");

        logger.debug("Test passed: Numeric name handled");
    }

    @Test
    @DisplayName("Should handle name with spaces")
    void testNameWithSpaces() {
        logger.debug("Test: Name with spaces");

        Tag spaceMockTag = mock(Tag.class);
        when(spaceMockTag.getId()).thenReturn(TEST_TAG_ID);
        when(spaceMockTag.getName()).thenReturn("ru verbs");

        TagResponse response = new TagResponse(spaceMockTag);

        assertThat(response.getName()).isEqualTo("ru verbs");

        logger.debug("Test passed: Name with spaces handled");
    }

    @Test
    @DisplayName("Should preserve case sensitivity in name")
    void testCaseSensitivity() {
        logger.debug("Test: Case sensitivity in name");

        Tag upperMockTag = mock(Tag.class);
        when(upperMockTag.getId()).thenReturn(1L);
        when(upperMockTag.getName()).thenReturn("VERBS");

        Tag lowerMockTag = mock(Tag.class);
        when(lowerMockTag.getId()).thenReturn(2L);
        when(lowerMockTag.getName()).thenReturn("verbs");

        TagResponse upperResponse = new TagResponse(upperMockTag);
        TagResponse lowerResponse = new TagResponse(lowerMockTag);

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

        Tag hyphenMockTag = mock(Tag.class);
        when(hyphenMockTag.getId()).thenReturn(TEST_TAG_ID);
        when(hyphenMockTag.getName()).thenReturn("JLPT-N5");

        TagResponse response = new TagResponse(hyphenMockTag);

        assertThat(response.getName()).isEqualTo("JLPT-N5");

        logger.debug("Test passed: Name with hyphens handled");
    }

    @Test
    @DisplayName("Should handle name with underscores")
    void testNameWithUnderscores() {
        logger.debug("Test: Name with underscores");

        Tag underscoreMockTag = mock(Tag.class);
        when(underscoreMockTag.getId()).thenReturn(TEST_TAG_ID);
        when(underscoreMockTag.getName()).thenReturn("ru_verbs");

        TagResponse response = new TagResponse(underscoreMockTag);

        assertThat(response.getName()).isEqualTo("ru_verbs");

        logger.debug("Test passed: Name with underscores handled");
    }
}
