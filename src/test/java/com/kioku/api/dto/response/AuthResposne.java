package com.kioku.api.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuthResponse DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Constructor initialization</li>
 *   <li>Getter and setter methods</li>
 *   <li>Default values</li>
 *   <li>JSON serialization/deserialization</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("AuthResponse DTO Tests")
class AuthResponseTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthResponseTest.class);

    private ObjectMapper objectMapper;

    // Test data constants
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "user@example.com";
    private static final String DEFAULT_TYPE = "Bearer";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up AuthResponse test");
        objectMapper = new ObjectMapper();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create response with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        AuthResponse response = new AuthResponse();

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNull();
        assertThat(response.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(response.getUserId()).isNull();
        assertThat(response.getEmail()).isNull();

        logger.debug("Test passed: Default constructor works with type='Bearer'");
    }

    @Test
    @DisplayName("Should create response with parameterized constructor")
    void testParameterizedConstructor() {
        logger.debug("Test: Parameterized constructor");

        AuthResponse response = new AuthResponse(TEST_TOKEN, TEST_USER_ID, TEST_EMAIL);

        assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        logger.debug("Test passed: Parameterized constructor works");
    }

    @Test
    @DisplayName("Should set type to Bearer by default in parameterized constructor")
    void testDefaultTypeInConstructor() {
        logger.debug("Test: Default type in parameterized constructor");

        AuthResponse response = new AuthResponse(TEST_TOKEN, TEST_USER_ID, TEST_EMAIL);

        assertThat(response.getType()).isEqualTo("Bearer");

        logger.debug("Test passed: Type defaults to 'Bearer'");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get token correctly")
    void testSetAndGetToken() {
        logger.debug("Test: Set and get token");

        AuthResponse response = new AuthResponse();
        response.setToken(TEST_TOKEN);

        assertThat(response.getToken()).isEqualTo(TEST_TOKEN);

        logger.debug("Test passed: Token setter and getter work");
    }

    @Test
    @DisplayName("Should set and get type correctly")
    void testSetAndGetType() {
        logger.debug("Test: Set and get type");

        AuthResponse response = new AuthResponse();
        response.setType("Custom");

        assertThat(response.getType()).isEqualTo("Custom");

        logger.debug("Test passed: Type setter and getter work");
    }

    @Test
    @DisplayName("Should set and get userId correctly")
    void testSetAndGetUserId() {
        logger.debug("Test: Set and get userId");

        AuthResponse response = new AuthResponse();
        response.setUserId(TEST_USER_ID);

        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);

        logger.debug("Test passed: UserId setter and getter work");
    }

    @Test
    @DisplayName("Should set and get email correctly")
    void testSetAndGetEmail() {
        logger.debug("Test: Set and get email");

        AuthResponse response = new AuthResponse();
        response.setEmail(TEST_EMAIL);

        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        logger.debug("Test passed: Email setter and getter work");
    }

    // Field Value Tests

    @Test
    @DisplayName("Should handle null token")
    void testNullToken() {
        logger.debug("Test: Null token");

        AuthResponse response = new AuthResponse(null, TEST_USER_ID, TEST_EMAIL);

        assertThat(response.getToken()).isNull();
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        logger.debug("Test passed: Null token handled");
    }

    @Test
    @DisplayName("Should handle null userId")
    void testNullUserId() {
        logger.debug("Test: Null userId");

        AuthResponse response = new AuthResponse(TEST_TOKEN, null, TEST_EMAIL);

        assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getUserId()).isNull();
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        logger.debug("Test passed: Null userId handled");
    }

    @Test
    @DisplayName("Should handle null email")
    void testNullEmail() {
        logger.debug("Test: Null email");

        AuthResponse response = new AuthResponse(TEST_TOKEN, TEST_USER_ID, null);

        assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isNull();

        logger.debug("Test passed: Null email handled");
    }

    @Test
    @DisplayName("Should handle all null values")
    void testAllNullValues() {
        logger.debug("Test: All null values");

        AuthResponse response = new AuthResponse(null, null, null);

        assertThat(response.getToken()).isNull();
        assertThat(response.getType()).isEqualTo(DEFAULT_TYPE); // type still defaults to "Bearer"
        assertThat(response.getUserId()).isNull();
        assertThat(response.getEmail()).isNull();

        logger.debug("Test passed: All null values handled");
    }

    // JSON Serialization Tests

    @Test
    @DisplayName("Should serialize to JSON correctly")
    void testSerializeToJson() throws Exception {
        logger.debug("Test: Serialize to JSON");

        AuthResponse response = new AuthResponse(TEST_TOKEN, TEST_USER_ID, TEST_EMAIL);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"token\":\"" + TEST_TOKEN + "\"");
        assertThat(json).contains("\"type\":\"Bearer\"");
        assertThat(json).contains("\"userId\":" + TEST_USER_ID);
        assertThat(json).contains("\"email\":\"" + TEST_EMAIL + "\"");

        logger.debug("Test passed: Serialization works - {}", json);
    }

    @Test
    @DisplayName("Should deserialize from JSON correctly")
    void testDeserializeFromJson() throws Exception {
        logger.debug("Test: Deserialize from JSON");

        String json = String.format(
                "{\"token\":\"%s\",\"type\":\"Bearer\",\"userId\":%d,\"email\":\"%s\"}",
                TEST_TOKEN, TEST_USER_ID, TEST_EMAIL
        );

        AuthResponse response = objectMapper.readValue(json, AuthResponse.class);

        assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        logger.debug("Test passed: Deserialization works");
    }

    @Test
    @DisplayName("Should handle round-trip JSON serialization")
    void testJsonRoundTrip() throws Exception {
        logger.debug("Test: JSON round-trip");

        AuthResponse original = new AuthResponse(TEST_TOKEN, TEST_USER_ID, TEST_EMAIL);

        String json = objectMapper.writeValueAsString(original);
        AuthResponse deserialized = objectMapper.readValue(json, AuthResponse.class);

        assertThat(deserialized.getToken()).isEqualTo(original.getToken());
        assertThat(deserialized.getType()).isEqualTo(original.getType());
        assertThat(deserialized.getUserId()).isEqualTo(original.getUserId());
        assertThat(deserialized.getEmail()).isEqualTo(original.getEmail());

        logger.debug("Test passed: Round-trip serialization works");
    }

    @Test
    @DisplayName("Should deserialize with missing type field and use default")
    void testDeserializeWithMissingType() throws Exception {
        logger.debug("Test: Deserialize with missing type field");

        String json = String.format(
                "{\"token\":\"%s\",\"userId\":%d,\"email\":\"%s\"}",
                TEST_TOKEN, TEST_USER_ID, TEST_EMAIL
        );

        AuthResponse response = objectMapper.readValue(json, AuthResponse.class);

        assertThat(response.getType()).isEqualTo("Bearer");

        logger.debug("Test passed: Missing type field defaults to 'Bearer'");
    }

    @Test
    @DisplayName("Should serialize null fields as null in JSON")
    void testSerializeNullFields() throws Exception {
        logger.debug("Test: Serialize null fields");

        AuthResponse response = new AuthResponse(null, null, null);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"token\":null");
        assertThat(json).contains("\"userId\":null");
        assertThat(json).contains("\"email\":null");
        assertThat(json).contains("\"type\":\"Bearer\""); // type is still "Bearer"

        logger.debug("Test passed: Null fields serialized correctly");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle empty string token")
    void testEmptyStringToken() {
        logger.debug("Test: Empty string token");

        AuthResponse response = new AuthResponse("", TEST_USER_ID, TEST_EMAIL);

        assertThat(response.getToken()).isEmpty();

        logger.debug("Test passed: Empty string token handled");
    }

    @Test
    @DisplayName("Should handle empty string email")
    void testEmptyStringEmail() {
        logger.debug("Test: Empty string email");

        AuthResponse response = new AuthResponse(TEST_TOKEN, TEST_USER_ID, "");

        assertThat(response.getEmail()).isEmpty();

        logger.debug("Test passed: Empty string email handled");
    }

    @Test
    @DisplayName("Should handle very long token")
    void testVeryLongToken() {
        logger.debug("Test: Very long token");

        String longToken = "a".repeat(1000);
        AuthResponse response = new AuthResponse(longToken, TEST_USER_ID, TEST_EMAIL);

        assertThat(response.getToken()).hasSize(1000);
        assertThat(response.getToken()).isEqualTo(longToken);

        logger.debug("Test passed: Very long token handled");
    }

    @Test
    @DisplayName("Should handle zero userId")
    void testZeroUserId() {
        logger.debug("Test: Zero userId");

        AuthResponse response = new AuthResponse(TEST_TOKEN, 0L, TEST_EMAIL);

        assertThat(response.getUserId()).isEqualTo(0L);

        logger.debug("Test passed: Zero userId handled");
    }

    @Test
    @DisplayName("Should handle negative userId")
    void testNegativeUserId() {
        logger.debug("Test: Negative userId");

        AuthResponse response = new AuthResponse(TEST_TOKEN, -1L, TEST_EMAIL);

        assertThat(response.getUserId()).isEqualTo(-1L);

        logger.debug("Test passed: Negative userId handled");
    }

    @Test
    @DisplayName("Should handle large userId")
    void testLargeUserId() {
        logger.debug("Test: Large userId");

        Long largeId = Long.MAX_VALUE;
        AuthResponse response = new AuthResponse(TEST_TOKEN, largeId, TEST_EMAIL);

        assertThat(response.getUserId()).isEqualTo(largeId);

        logger.debug("Test passed: Large userId handled");
    }

    @Test
    @DisplayName("Should preserve type when set to custom value")
    void testCustomType() {
        logger.debug("Test: Custom type value");

        AuthResponse response = new AuthResponse(TEST_TOKEN, TEST_USER_ID, TEST_EMAIL);
        response.setType("CustomType");

        assertThat(response.getType()).isEqualTo("CustomType");

        logger.debug("Test passed: Custom type preserved");
    }

    @Test
    @DisplayName("Should handle special characters in email")
    void testSpecialCharactersInEmail() {
        logger.debug("Test: Special characters in email");

        String specialEmail = "user+test@example.com";
        AuthResponse response = new AuthResponse(TEST_TOKEN, TEST_USER_ID, specialEmail);

        assertThat(response.getEmail()).isEqualTo(specialEmail);

        logger.debug("Test passed: Special characters in email handled");
    }
}