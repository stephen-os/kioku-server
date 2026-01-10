package com.kioku.api.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CreateTagRequest DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Field validation (required fields, max lengths)</li>
 *   <li>Constructor initialization</li>
 *   <li>Getter and setter methods</li>
 *   <li>Edge cases (null, empty, max length strings)</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("CreateTagRequest DTO Tests")
class CreateTagRequestTests {

    private static final Logger logger = LoggerFactory.getLogger(CreateTagRequestTests.class);

    private Validator validator;

    // Test data constants
    private static final String VALID_NAME = "verbs";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up CreateTagRequest test");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create request with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        CreateTagRequest request = new CreateTagRequest();

        assertThat(request).isNotNull();
        assertThat(request.getName()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create request with parameterized constructor")
    void testParameterizedConstructor() {
        logger.debug("Test: Parameterized constructor");

        CreateTagRequest request = new CreateTagRequest(VALID_NAME);

        assertThat(request.getName()).isEqualTo(VALID_NAME);

        logger.debug("Test passed: Parameterized constructor works");
    }

    // Validation Tests - Valid Cases

    @Test
    @DisplayName("Should validate successfully with valid name")
    void testValidRequest() {
        logger.debug("Test: Valid request");

        CreateTagRequest request = new CreateTagRequest(VALID_NAME);

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Valid request has no violations");
    }

    @Test
    @DisplayName("Should validate successfully with single character name")
    void testSingleCharacterName() {
        logger.debug("Test: Single character name");

        CreateTagRequest request = new CreateTagRequest("N");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Single character name is valid");
    }

    @Test
    @DisplayName("Should validate successfully with numeric name")
    void testNumericName() {
        logger.debug("Test: Numeric name");

        CreateTagRequest request = new CreateTagRequest("123");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Numeric name is valid");
    }

    // Validation Tests - Name Field Failures

    @Test
    @DisplayName("Should fail validation when name is null")
    void testNameNull() {
        logger.debug("Test: Name is null");

        CreateTagRequest request = new CreateTagRequest(null);

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Null name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name is empty")
    void testNameEmpty() {
        logger.debug("Test: Name is empty");

        CreateTagRequest request = new CreateTagRequest("");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Empty name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void testNameBlank() {
        logger.debug("Test: Name is blank");

        CreateTagRequest request = new CreateTagRequest("   ");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Blank name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name exceeds max length")
    void testNameTooLong() {
        logger.debug("Test: Name exceeds max length");

        String longName = "a".repeat(101);
        CreateTagRequest request = new CreateTagRequest(longName);

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name must not exceed 100 characters");

        logger.debug("Test passed: Name over 100 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when name is exactly max length")
    void testNameExactlyMaxLength() {
        logger.debug("Test: Name is exactly 100 characters");

        String maxName = "a".repeat(100);
        CreateTagRequest request = new CreateTagRequest(maxName);

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Name at 100 chars is valid");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        logger.debug("Test: Set and get name");

        CreateTagRequest request = new CreateTagRequest();
        request.setName(VALID_NAME);

        assertThat(request.getName()).isEqualTo(VALID_NAME);

        logger.debug("Test passed: Name setter and getter work");
    }

    @Test
    @DisplayName("Should allow setting name to null via setter")
    void testSetNameToNull() {
        logger.debug("Test: Set name to null via setter");

        CreateTagRequest request = new CreateTagRequest(VALID_NAME);
        request.setName(null);

        assertThat(request.getName()).isNull();

        logger.debug("Test passed: Setter allows null (validation will catch it)");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        logger.debug("Test: Special characters in name");

        CreateTagRequest request = new CreateTagRequest("N5-verbs");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("N5-verbs");

        logger.debug("Test passed: Special characters in name are valid");
    }

    @Test
    @DisplayName("Should handle unicode characters in name")
    void testUnicodeCharactersInName() {
        logger.debug("Test: Unicode characters in name");

        CreateTagRequest request = new CreateTagRequest("動詞");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("動詞");

        logger.debug("Test passed: Unicode characters in name are valid");
    }

    @Test
    @DisplayName("Should handle emojis in name")
    void testEmojisInName() {
        logger.debug("Test: Emojis in name");

        CreateTagRequest request = new CreateTagRequest("important⭐");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("important⭐");

        logger.debug("Test passed: Emojis in name are valid");
    }

    @Test
    @DisplayName("Should handle name with spaces")
    void testNameWithSpaces() {
        logger.debug("Test: Name with spaces");

        CreateTagRequest request = new CreateTagRequest("ru verbs");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("ru verbs");

        logger.debug("Test passed: Name with spaces is valid");
    }

    @Test
    @DisplayName("Should handle name with leading/trailing whitespace")
    void testNameWithWhitespace() {
        logger.debug("Test: Name with leading/trailing whitespace");

        // Note: @NotBlank allows leading/trailing whitespace as long as there's content
        CreateTagRequest request = new CreateTagRequest("  verbs  ");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("  verbs  ");

        logger.debug("Test passed: Name with whitespace is valid (application should trim)");
    }

    @Test
    @DisplayName("Should handle mixed case name")
    void testMixedCaseName() {
        logger.debug("Test: Mixed case name");

        CreateTagRequest request = new CreateTagRequest("VeRbS");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("VeRbS");

        logger.debug("Test passed: Mixed case name is valid (tags are case-sensitive)");
    }

    @Test
    @DisplayName("Should handle name with underscores")
    void testNameWithUnderscores() {
        logger.debug("Test: Name with underscores");

        CreateTagRequest request = new CreateTagRequest("ru_verbs");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("ru_verbs");

        logger.debug("Test passed: Name with underscores is valid");
    }

    @Test
    @DisplayName("Should handle name with hyphens")
    void testNameWithHyphens() {
        logger.debug("Test: Name with hyphens");

        CreateTagRequest request = new CreateTagRequest("JLPT-N5");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("JLPT-N5");

        logger.debug("Test passed: Name with hyphens is valid");
    }

    @Test
    @DisplayName("Should handle name with dots")
    void testNameWithDots() {
        logger.debug("Test: Name with dots");

        CreateTagRequest request = new CreateTagRequest("level.1");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("level.1");

        logger.debug("Test passed: Name with dots is valid");
    }

    @Test
    @DisplayName("Should handle alphanumeric name")
    void testAlphanumericName() {
        logger.debug("Test: Alphanumeric name");

        CreateTagRequest request = new CreateTagRequest("N5verbs2024");

        Set<ConstraintViolation<CreateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("N5verbs2024");

        logger.debug("Test passed: Alphanumeric name is valid");
    }
}