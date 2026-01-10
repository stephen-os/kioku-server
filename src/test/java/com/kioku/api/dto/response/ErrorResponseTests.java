package com.kioku.api.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Unit tests for ErrorResponse DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Constructor initialization</li>
 *   <li>Automatic timestamp generation</li>
 *   <li>Getter and setter methods</li>
 *   <li>JSON serialization/deserialization</li>
 *   <li>Error message handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("ErrorResponse DTO Tests")
class ErrorResponseTests {

    private static final Logger logger = LoggerFactory.getLogger(ErrorResponseTests.class);

    private ObjectMapper objectMapper;

    // Test data constants
    private static final String TEST_ERROR_MESSAGE = "Email already in use";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up ErrorResponse test");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create error response with constructor")
    void testConstructor() {
        logger.debug("Test: Constructor");

        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(TEST_ERROR_MESSAGE);
        assertThat(response.getTimestamp()).isNotNull();

        logger.debug("Test passed: Constructor works");
    }

    @Test
    @DisplayName("Should automatically set timestamp to current time")
    void testTimestampAutoSet() {
        logger.debug("Test: Automatic timestamp");

        LocalDateTime before = LocalDateTime.now();
        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);
        LocalDateTime after = LocalDateTime.now();

        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isAfterOrEqualTo(before);
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(after);

        logger.debug("Test passed: Timestamp automatically set to current time");
    }

    @Test
    @DisplayName("Should set timestamp within milliseconds of creation")
    void testTimestampPrecision() {
        logger.debug("Test: Timestamp precision");

        LocalDateTime now = LocalDateTime.now();
        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);

        // Timestamp should be within 100ms of now
        assertThat(response.getTimestamp()).isCloseTo(now, within(100, ChronoUnit.MILLIS));

        logger.debug("Test passed: Timestamp precision is within milliseconds");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get message correctly")
    void testSetAndGetMessage() {
        logger.debug("Test: Set and get message");

        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);
        String newMessage = "Invalid email format";
        response.setMessage(newMessage);

        assertThat(response.getMessage()).isEqualTo(newMessage);

        logger.debug("Test passed: Message setter and getter work");
    }

    @Test
    @DisplayName("Should set and get timestamp correctly")
    void testSetAndGetTimestamp() {
        logger.debug("Test: Set and get timestamp");

        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);
        LocalDateTime customTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        response.setTimestamp(customTimestamp);

        assertThat(response.getTimestamp()).isEqualTo(customTimestamp);

        logger.debug("Test passed: Timestamp setter and getter work");
    }

    // JSON Serialization Tests

    @Test
    @DisplayName("Should serialize to JSON correctly")
    void testSerializeToJson() throws Exception {
        logger.debug("Test: Serialize to JSON");

        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"message\":\"" + TEST_ERROR_MESSAGE + "\"");
        assertThat(json).contains("\"timestamp\":");

        logger.debug("Test passed: Serialization works - {}", json);
    }

    @Test
    @DisplayName("Should deserialize from JSON correctly")
    void testDeserializeFromJson() throws Exception {
        logger.debug("Test: Deserialize from JSON");

        // Fixed ISO-8601 timestamp string
        String timestampStr = "2024-01-15T10:30:00";
        String json = String.format(
                "{\"message\":\"%s\",\"timestamp\":\"%s\"}",
                TEST_ERROR_MESSAGE,
                timestampStr
        );

        ErrorResponse response = objectMapper.readValue(json, ErrorResponse.class);

        assertThat(response.getMessage()).isEqualTo(TEST_ERROR_MESSAGE);
        assertThat(response.getTimestamp())
                .isEqualTo(LocalDateTime.parse(timestampStr));

        logger.debug("Test passed: Deserialization works");
    }

    @Test
    @DisplayName("Should handle round-trip JSON serialization")
    void testJsonRoundTrip() throws Exception {
        logger.debug("Test: JSON round-trip");

        // Fixed timestamp
        LocalDateTime fixedTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        ErrorResponse original = new ErrorResponse(TEST_ERROR_MESSAGE);
        original.setTimestamp(fixedTimestamp);

        String json = objectMapper.writeValueAsString(original);
        ErrorResponse deserialized = objectMapper.readValue(json, ErrorResponse.class);

        assertThat(deserialized.getMessage()).isEqualTo(original.getMessage());
        assertThat(deserialized.getTimestamp()).isEqualTo(original.getTimestamp());

        logger.debug("Test passed: Round-trip serialization works");
    }

    // Error Message Tests

    @Test
    @DisplayName("Should handle null message")
    void testNullMessage() {
        logger.debug("Test: Null message");

        ErrorResponse response = new ErrorResponse(null);

        assertThat(response.getMessage()).isNull();
        assertThat(response.getTimestamp()).isNotNull();

        logger.debug("Test passed: Null message handled");
    }

    @Test
    @DisplayName("Should handle empty message")
    void testEmptyMessage() {
        logger.debug("Test: Empty message");

        ErrorResponse response = new ErrorResponse("");

        assertThat(response.getMessage()).isEmpty();
        assertThat(response.getTimestamp()).isNotNull();

        logger.debug("Test passed: Empty message handled");
    }

    @Test
    @DisplayName("Should handle blank message")
    void testBlankMessage() {
        logger.debug("Test: Blank message");

        ErrorResponse response = new ErrorResponse("   ");

        assertThat(response.getMessage()).isEqualTo("   ");
        assertThat(response.getTimestamp()).isNotNull();

        logger.debug("Test passed: Blank message handled");
    }

    @Test
    @DisplayName("Should handle very long message")
    void testVeryLongMessage() {
        logger.debug("Test: Very long message");

        String longMessage = "a".repeat(1000);
        ErrorResponse response = new ErrorResponse(longMessage);

        assertThat(response.getMessage()).hasSize(1000);
        assertThat(response.getMessage()).isEqualTo(longMessage);

        logger.debug("Test passed: Very long message handled");
    }

    @Test
    @DisplayName("Should handle message with special characters")
    void testMessageWithSpecialCharacters() {
        logger.debug("Test: Message with special characters");

        String specialMessage = "Error: 'user@example.com' - Invalid format! 🚫";
        ErrorResponse response = new ErrorResponse(specialMessage);

        assertThat(response.getMessage()).isEqualTo(specialMessage);

        logger.debug("Test passed: Special characters in message handled");
    }

    @Test
    @DisplayName("Should handle multiline message")
    void testMultilineMessage() {
        logger.debug("Test: Multiline message");

        String multilineMessage = "Error:\nLine 1: Email is invalid\nLine 2: Password is too short";
        ErrorResponse response = new ErrorResponse(multilineMessage);

        assertThat(response.getMessage()).contains("\n");
        assertThat(response.getMessage()).isEqualTo(multilineMessage);

        logger.debug("Test passed: Multiline message handled");
    }

    // Common Error Messages Tests

    @Test
    @DisplayName("Should handle validation error message")
    void testValidationErrorMessage() {
        logger.debug("Test: Validation error message");

        ErrorResponse response = new ErrorResponse("Email is required");

        assertThat(response.getMessage()).isEqualTo("Email is required");

        logger.debug("Test passed: Validation error message handled");
    }

    @Test
    @DisplayName("Should handle authentication error message")
    void testAuthenticationErrorMessage() {
        logger.debug("Test: Authentication error message");

        ErrorResponse response = new ErrorResponse("Invalid email or password");

        assertThat(response.getMessage()).isEqualTo("Invalid email or password");

        logger.debug("Test passed: Authentication error message handled");
    }

    @Test
    @DisplayName("Should handle not found error message")
    void testNotFoundErrorMessage() {
        logger.debug("Test: Not found error message");

        ErrorResponse response = new ErrorResponse("Deck not found or access denied");

        assertThat(response.getMessage()).isEqualTo("Deck not found or access denied");

        logger.debug("Test passed: Not found error message handled");
    }

    @Test
    @DisplayName("Should handle duplicate resource error message")
    void testDuplicateResourceErrorMessage() {
        logger.debug("Test: Duplicate resource error message");

        ErrorResponse response = new ErrorResponse("Email already in use");

        assertThat(response.getMessage()).isEqualTo("Email already in use");

        logger.debug("Test passed: Duplicate resource error message handled");
    }

    // Timestamp Edge Cases

    @Test
    @DisplayName("Should allow setting custom timestamp")
    void testCustomTimestamp() {
        logger.debug("Test: Custom timestamp");

        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);
        LocalDateTime customTimestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        response.setTimestamp(customTimestamp);

        assertThat(response.getTimestamp()).isEqualTo(customTimestamp);

        logger.debug("Test passed: Custom timestamp can be set");
    }

    @Test
    @DisplayName("Should handle null timestamp")
    void testNullTimestamp() {
        logger.debug("Test: Null timestamp");

        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);
        response.setTimestamp(null);

        assertThat(response.getTimestamp()).isNull();

        logger.debug("Test passed: Null timestamp handled");
    }

    @Test
    @DisplayName("Should preserve nanosecond precision in timestamp")
    void testTimestampNanosecondPrecision() {
        logger.debug("Test: Timestamp nanosecond precision");

        ErrorResponse response = new ErrorResponse(TEST_ERROR_MESSAGE);
        LocalDateTime preciseTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 45, 123456789);
        response.setTimestamp(preciseTimestamp);

        assertThat(response.getTimestamp().getNano()).isEqualTo(123456789);

        logger.debug("Test passed: Nanosecond precision preserved");
    }

    @Test
    @DisplayName("Should handle message update after creation")
    void testMessageUpdate() {
        logger.debug("Test: Message update after creation");

        ErrorResponse response = new ErrorResponse("Original message");
        LocalDateTime originalTimestamp = response.getTimestamp();

        response.setMessage("Updated message");

        assertThat(response.getMessage()).isEqualTo("Updated message");
        assertThat(response.getTimestamp()).isEqualTo(originalTimestamp); // Timestamp should not change

        logger.debug("Test passed: Message can be updated without changing timestamp");
    }

    @Test
    @DisplayName("Should create multiple error responses with different timestamps")
    void testMultipleErrorResponses() throws InterruptedException {
        logger.debug("Test: Multiple error responses");

        ErrorResponse response1 = new ErrorResponse("Error 1");
        Thread.sleep(10); // Small delay to ensure different timestamps
        ErrorResponse response2 = new ErrorResponse("Error 2");

        assertThat(response1.getTimestamp()).isBefore(response2.getTimestamp());

        logger.debug("Test passed: Multiple error responses have different timestamps");
    }

    @Test
    @DisplayName("Should serialize null message as null in JSON")
    void testSerializeNullMessage() throws Exception {
        logger.debug("Test: Serialize null message");

        ErrorResponse response = new ErrorResponse(null);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"message\":null");

        logger.debug("Test passed: Null message serialized correctly");
    }
}