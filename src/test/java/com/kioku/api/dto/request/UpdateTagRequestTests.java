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
 * Unit tests for UpdateTagRequest DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Field validation for updates (required field, max length)</li>
 *   <li>Constructor initialization</li>
 *   <li>Getter and setter methods</li>
 *   <li>Edge cases (null, empty, max length strings)</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("UpdateTagRequest DTO Tests")
class UpdateTagRequestTests {

    private static final Logger logger = LoggerFactory.getLogger(UpdateTagRequestTests.class);

    private Validator validator;

    // Test data constants
    private static final String VALID_NAME = "verbs-updated";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up UpdateTagRequest test");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create request with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        UpdateTagRequest request = new UpdateTagRequest();

        assertThat(request).isNotNull();
        assertThat(request.getName()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create request with parameterized constructor")
    void testParameterizedConstructor() {
        logger.debug("Test: Parameterized constructor");

        UpdateTagRequest request = new UpdateTagRequest(VALID_NAME);

        assertThat(request.getName()).isEqualTo(VALID_NAME);

        logger.debug("Test passed: Parameterized constructor works");
    }

    // Validation Tests - Valid Cases

    @Test
    @DisplayName("Should validate successfully with valid name")
    void testValidRequest() {
        logger.debug("Test: Valid request");

        UpdateTagRequest request = new UpdateTagRequest(VALID_NAME);

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Valid request has no violations");
    }

    // Validation Tests - Name Field

    @Test
    @DisplayName("Should fail validation when name is null")
    void testNameNull() {
        logger.debug("Test: Name is null");

        UpdateTagRequest request = new UpdateTagRequest(null);

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Null name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name is empty")
    void testNameEmpty() {
        logger.debug("Test: Name is empty");

        UpdateTagRequest request = new UpdateTagRequest("");

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Empty name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void testNameBlank() {
        logger.debug("Test: Name is blank");

        UpdateTagRequest request = new UpdateTagRequest("   ");

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name is required");

        logger.debug("Test passed: Blank name rejected");
    }

    @Test
    @DisplayName("Should fail validation when name exceeds max length")
    void testNameTooLong() {
        logger.debug("Test: Name exceeds max length");

        String longName = "a".repeat(101);
        UpdateTagRequest request = new UpdateTagRequest(longName);

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name must not exceed 100 characters");

        logger.debug("Test passed: Name over 100 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when name is exactly max length")
    void testNameExactlyMaxLength() {
        logger.debug("Test: Name is exactly 100 characters");

        String maxName = "a".repeat(100);
        UpdateTagRequest request = new UpdateTagRequest(maxName);

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Name at 100 chars is valid");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        logger.debug("Test: Set and get name");

        UpdateTagRequest request = new UpdateTagRequest();
        request.setName(VALID_NAME);

        assertThat(request.getName()).isEqualTo(VALID_NAME);

        logger.debug("Test passed: Name setter and getter work");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        logger.debug("Test: Special characters in name");

        UpdateTagRequest request = new UpdateTagRequest("verbs-日本語!");

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getName()).isEqualTo("verbs-日本語!");

        logger.debug("Test passed: Special characters in name are valid");
    }

    @Test
    @DisplayName("Should handle lowercase name")
    void testLowercaseName() {
        logger.debug("Test: Lowercase name");

        UpdateTagRequest request = new UpdateTagRequest("verbs");

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Lowercase name is valid");
    }

    @Test
    @DisplayName("Should handle uppercase name")
    void testUppercaseName() {
        logger.debug("Test: Uppercase name");

        UpdateTagRequest request = new UpdateTagRequest("VERBS");

        Set<ConstraintViolation<UpdateTagRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Uppercase name is valid");
    }
}