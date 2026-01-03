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
 * Unit tests for CreateDeckRequest DTO.
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
@DisplayName("CreateDeckRequest DTO Tests")
class CreateDeckRequestTest {

    private static final Logger logger = LoggerFactory.getLogger(CreateDeckRequestTest.class);

    private Validator validator;

    // Test data constants
    private static final String VALID_NAME = "Japanese Verbs";
    private static final String VALID_DESCRIPTION = "Common Japanese verbs for JLPT N5 level";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up CreateDeckRequest test");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create request with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        CreateDeckRequest request = new CreateDeckRequest();

        assertThat(request).isNotNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getDescription()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create request with parameterized constructor")
    void testParameterizedConstructor() {
        logger.debug("Test: Parameterized constructor");

        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, VALID_DESCRIPTION);

        assertThat(request.getName()).isEqualTo(VALID_NAME);
        assertThat(request.getDescription()).isEqualTo(VALID_DESCRIPTION);

        logger.debug("Test passed: Parameterized constructor works");
    }

    @Test
    @DisplayName("Should create request with null description")
    void testConstructorWithNullDescription() {
        logger.debug("Test: Constructor with null description");

        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, null);

        assertThat(request.getName()).isEqualTo(VALID_NAME);
        assertThat(request.getDescription()).isNull();

        logger.debug("Test passed: Constructor accepts null description");
    }

    // Validation Tests - Valid Cases

    @Test
    @DisplayName("Should validate successfully with all valid fields")
    void testValidRequest() {
        logger.debug("Test: Valid request");

        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Valid request has no violations");
    }

    @Test
    @DisplayName("Should validate successfully without description")
    void testValidRequestWithoutDescription() {
        logger.debug("Test: Valid request without description");

        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, null);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Description is optional");
    }

    @Test
    @DisplayName("Should validate successfully with empty description")
    void testValidRequestWithEmptyDescription() {
        logger.debug("Test: Valid request with empty description");

        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, "");

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Empty description is valid");
    }

    // Validation Tests - Name Field

    @Test
    @DisplayName("Should fail validation when name is null")
    void testNameNull() {
        logger.debug("Test: Name is null");

        CreateDeckRequest request = new CreateDeckRequest(null, VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Null name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name is empty")
    void testNameEmpty() {
        logger.debug("Test: Name is empty");

        CreateDeckRequest request = new CreateDeckRequest("", VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Empty name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void testNameBlank() {
        logger.debug("Test: Name is blank");

        CreateDeckRequest request = new CreateDeckRequest("   ", VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Blank name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name exceeds max length")
    void testNameTooLong() {
        logger.debug("Test: Name exceeds max length");

        String longName = "a".repeat(256);
        CreateDeckRequest request = new CreateDeckRequest(longName, VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name must not exceed 255 characters");

        logger.debug("Test passed: Name over 255 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when name is exactly max length")
    void testNameExactlyMaxLength() {
        logger.debug("Test: Name is exactly 255 characters");

        String maxName = "a".repeat(255);
        CreateDeckRequest request = new CreateDeckRequest(maxName, VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Name at 255 chars is valid");
    }

    @Test
    @DisplayName("Should validate successfully with single character name")
    void testNameSingleCharacter() {
        logger.debug("Test: Name is single character");

        CreateDeckRequest request = new CreateDeckRequest("A", VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Single character name is valid");
    }

    // Validation Tests - Description Field

    @Test
    @DisplayName("Should fail validation when description exceeds max length")
    void testDescriptionTooLong() {
        logger.debug("Test: Description exceeds max length");

        String longDescription = "a".repeat(1001);
        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, longDescription);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description must not exceed 1000 characters");

        logger.debug("Test passed: Description over 1000 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when description is exactly max length")
    void testDescriptionExactlyMaxLength() {
        logger.debug("Test: Description is exactly 1000 characters");

        String maxDescription = "a".repeat(1000);
        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, maxDescription);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Description at 1000 chars is valid");
    }

    @Test
    @DisplayName("Should validate successfully with blank description")
    void testDescriptionBlank() {
        logger.debug("Test: Description is blank (whitespace only)");

        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, "   ");

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Blank description is valid (unlike name)");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        logger.debug("Test: Set and get name");

        CreateDeckRequest request = new CreateDeckRequest();
        request.setName(VALID_NAME);

        assertThat(request.getName()).isEqualTo(VALID_NAME);

        logger.debug("Test passed: Name setter and getter work");
    }

    @Test
    @DisplayName("Should set and get description correctly")
    void testSetAndGetDescription() {
        logger.debug("Test: Set and get description");

        CreateDeckRequest request = new CreateDeckRequest();
        request.setDescription(VALID_DESCRIPTION);

        assertThat(request.getDescription()).isEqualTo(VALID_DESCRIPTION);

        logger.debug("Test passed: Description setter and getter work");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        logger.debug("Test: Special characters in name");

        CreateDeckRequest request = new CreateDeckRequest("日本語 & Français! 🎌", VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("日本語 & Français! 🎌");

        logger.debug("Test passed: Special characters in name are valid");
    }

    @Test
    @DisplayName("Should handle special characters in description")
    void testSpecialCharactersInDescription() {
        logger.debug("Test: Special characters in description");

        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, "日本語 & Français! 🎌 - Study deck for language learning");

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getDescription()).isEqualTo("日本語 & Français! 🎌 - Study deck for language learning");

        logger.debug("Test passed: Special characters in description are valid");
    }

    @Test
    @DisplayName("Should handle name with leading/trailing whitespace")
    void testNameWithWhitespace() {
        logger.debug("Test: Name with leading/trailing whitespace");

        // Note: @NotBlank allows leading/trailing whitespace as long as there's content
        CreateDeckRequest request = new CreateDeckRequest("  Valid Name  ", VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("  Valid Name  ");

        logger.debug("Test passed: Name with whitespace is valid (application should trim)");
    }

    @Test
    @DisplayName("Should handle multiline description")
    void testMultilineDescription() {
        logger.debug("Test: Multiline description");

        String multilineDescription = "Line 1: Introduction\nLine 2: Details\nLine 3: Summary";
        CreateDeckRequest request = new CreateDeckRequest(VALID_NAME, multilineDescription);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getDescription()).contains("\n");

        logger.debug("Test passed: Multiline description is valid");
    }

    @Test
    @DisplayName("Should handle numeric-only name")
    void testNumericName() {
        logger.debug("Test: Numeric-only name");

        CreateDeckRequest request = new CreateDeckRequest("12345", VALID_DESCRIPTION);

        Set<ConstraintViolation<CreateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("12345");

        logger.debug("Test passed: Numeric name is valid");
    }
}