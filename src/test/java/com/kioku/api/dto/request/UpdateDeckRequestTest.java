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
 * Unit tests for UpdateDeckRequest DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Field validation for updates (required fields, max lengths)</li>
 *   <li>Constructor initialization</li>
 *   <li>Getter and setter methods</li>
 *   <li>Edge cases (null, empty, max length strings)</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("UpdateDeckRequest DTO Tests")
class UpdateDeckRequestTest {

    private static final Logger logger = LoggerFactory.getLogger(UpdateDeckRequestTest.class);

    private Validator validator;

    // Test data constants
    private static final String VALID_NAME = "Updated Japanese Verbs";
    private static final String VALID_DESCRIPTION = "Updated description for JLPT N4 level";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up UpdateDeckRequest test");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create request with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        UpdateDeckRequest request = new UpdateDeckRequest();

        assertThat(request).isNotNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getDescription()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create request with parameterized constructor")
    void testParameterizedConstructor() {
        logger.debug("Test: Parameterized constructor");

        UpdateDeckRequest request = new UpdateDeckRequest(VALID_NAME, VALID_DESCRIPTION);

        assertThat(request.getName()).isEqualTo(VALID_NAME);
        assertThat(request.getDescription()).isEqualTo(VALID_DESCRIPTION);

        logger.debug("Test passed: Parameterized constructor works");
    }

    @Test
    @DisplayName("Should create request with null description")
    void testConstructorWithNullDescription() {
        logger.debug("Test: Constructor with null description");

        UpdateDeckRequest request = new UpdateDeckRequest(VALID_NAME, null);

        assertThat(request.getName()).isEqualTo(VALID_NAME);
        assertThat(request.getDescription()).isNull();

        logger.debug("Test passed: Constructor accepts null description");
    }

    // Validation Tests - Valid Cases

    @Test
    @DisplayName("Should validate successfully with all valid fields")
    void testValidRequest() {
        logger.debug("Test: Valid request");

        UpdateDeckRequest request = new UpdateDeckRequest(VALID_NAME, VALID_DESCRIPTION);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Valid request has no violations");
    }

    @Test
    @DisplayName("Should validate successfully without description")
    void testValidRequestWithoutDescription() {
        logger.debug("Test: Valid request without description");

        UpdateDeckRequest request = new UpdateDeckRequest(VALID_NAME, null);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Description is optional");
    }

    @Test
    @DisplayName("Should validate successfully with empty description")
    void testValidRequestWithEmptyDescription() {
        logger.debug("Test: Valid request with empty description");

        UpdateDeckRequest request = new UpdateDeckRequest(VALID_NAME, "");

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Empty description is valid");
    }

    // Validation Tests - Name Field

    @Test
    @DisplayName("Should fail validation when name is null")
    void testNameNull() {
        logger.debug("Test: Name is null");

        UpdateDeckRequest request = new UpdateDeckRequest(null, VALID_DESCRIPTION);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Null name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name is empty")
    void testNameEmpty() {
        logger.debug("Test: Name is empty");

        UpdateDeckRequest request = new UpdateDeckRequest("", VALID_DESCRIPTION);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Empty name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void testNameBlank() {
        logger.debug("Test: Name is blank");

        UpdateDeckRequest request = new UpdateDeckRequest("   ", VALID_DESCRIPTION);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Blank name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name exceeds max length")
    void testNameTooLong() {
        logger.debug("Test: Name exceeds max length");

        String longName = "a".repeat(256);
        UpdateDeckRequest request = new UpdateDeckRequest(longName, VALID_DESCRIPTION);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name must not exceed 255 characters");

        logger.debug("Test passed: Name over 255 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when name is exactly max length")
    void testNameExactlyMaxLength() {
        logger.debug("Test: Name is exactly 255 characters");

        String maxName = "a".repeat(255);
        UpdateDeckRequest request = new UpdateDeckRequest(maxName, VALID_DESCRIPTION);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Name at 255 chars is valid");
    }

    // Validation Tests - Description Field

    @Test
    @DisplayName("Should fail validation when description exceeds max length")
    void testDescriptionTooLong() {
        logger.debug("Test: Description exceeds max length");

        String longDescription = "a".repeat(1001);
        UpdateDeckRequest request = new UpdateDeckRequest(VALID_NAME, longDescription);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description must not exceed 1000 characters");

        logger.debug("Test passed: Description over 1000 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when description is exactly max length")
    void testDescriptionExactlyMaxLength() {
        logger.debug("Test: Description is exactly 1000 characters");

        String maxDescription = "a".repeat(1000);
        UpdateDeckRequest request = new UpdateDeckRequest(VALID_NAME, maxDescription);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Description at 1000 chars is valid");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        logger.debug("Test: Set and get name");

        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setName(VALID_NAME);

        assertThat(request.getName()).isEqualTo(VALID_NAME);

        logger.debug("Test passed: Name setter and getter work");
    }

    @Test
    @DisplayName("Should set and get description correctly")
    void testSetAndGetDescription() {
        logger.debug("Test: Set and get description");

        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setDescription(VALID_DESCRIPTION);

        assertThat(request.getDescription()).isEqualTo(VALID_DESCRIPTION);

        logger.debug("Test passed: Description setter and getter work");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        logger.debug("Test: Special characters in name");

        UpdateDeckRequest request = new UpdateDeckRequest("日本語 & français! 🎌", VALID_DESCRIPTION);

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("日本語 & français! 🎌");

        logger.debug("Test passed: Special characters in name are valid");
    }

    @Test
    @DisplayName("Should handle special characters in description")
    void testSpecialCharactersInDescription() {
        logger.debug("Test: Special characters in description");

        UpdateDeckRequest request = new UpdateDeckRequest(VALID_NAME, "Updated: 日本語 & français! 🎌");

        Set<ConstraintViolation<UpdateDeckRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getDescription()).isEqualTo("Updated: 日本語 & français! 🎌");

        logger.debug("Test passed: Special characters in description are valid");
    }
}