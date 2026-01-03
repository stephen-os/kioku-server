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
 * Unit tests for UpdateCardRequest DTO.
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
@DisplayName("UpdateCardRequest DTO Tests")
class UpdateCardRequestTest {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCardRequestTest.class);

    private Validator validator;

    // Test data constants
    private static final String VALID_FRONT = "Updated front";
    private static final String VALID_BACK = "Updated back";
    private static final String VALID_NOTES = "Updated notes";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up UpdateCardRequest test");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create request with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        UpdateCardRequest request = new UpdateCardRequest();

        assertThat(request).isNotNull();
        assertThat(request.getFront()).isNull();
        assertThat(request.getBack()).isNull();
        assertThat(request.getNotes()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create request with parameterized constructor")
    void testParameterizedConstructor() {
        logger.debug("Test: Parameterized constructor");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, VALID_BACK, VALID_NOTES);

        assertThat(request.getFront()).isEqualTo(VALID_FRONT);
        assertThat(request.getBack()).isEqualTo(VALID_BACK);
        assertThat(request.getNotes()).isEqualTo(VALID_NOTES);

        logger.debug("Test passed: Parameterized constructor works");
    }

    @Test
    @DisplayName("Should create request with null notes")
    void testConstructorWithNullNotes() {
        logger.debug("Test: Constructor with null notes");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, VALID_BACK, null);

        assertThat(request.getFront()).isEqualTo(VALID_FRONT);
        assertThat(request.getBack()).isEqualTo(VALID_BACK);
        assertThat(request.getNotes()).isNull();

        logger.debug("Test passed: Constructor accepts null notes");
    }

    // Validation Tests - Valid Cases

    @Test
    @DisplayName("Should validate successfully with all valid fields")
    void testValidRequest() {
        logger.debug("Test: Valid request");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Valid request has no violations");
    }

    @Test
    @DisplayName("Should validate successfully without notes")
    void testValidRequestWithoutNotes() {
        logger.debug("Test: Valid request without notes");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, VALID_BACK, null);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Notes are optional");
    }

    @Test
    @DisplayName("Should validate successfully with empty notes")
    void testValidRequestWithEmptyNotes() {
        logger.debug("Test: Valid request with empty notes");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, VALID_BACK, "");

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Empty notes are valid");
    }

    // Validation Tests - Front Field

    @Test
    @DisplayName("Should fail validation when front is null")
    void testFrontNull() {
        logger.debug("Test: Front is null");

        UpdateCardRequest request = new UpdateCardRequest(null, VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Front is required");

        logger.debug("Test passed: Null front rejected");
    }

    @Test
    @DisplayName("Should fail validation when front is empty")
    void testFrontEmpty() {
        logger.debug("Test: Front is empty");

        UpdateCardRequest request = new UpdateCardRequest("", VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Front is required");

        logger.debug("Test passed: Empty front rejected");
    }

    @Test
    @DisplayName("Should fail validation when front is blank")
    void testFrontBlank() {
        logger.debug("Test: Front is blank");

        UpdateCardRequest request = new UpdateCardRequest("   ", VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Front is required");

        logger.debug("Test passed: Blank front rejected");
    }

    @Test
    @DisplayName("Should fail validation when front exceeds max length")
    void testFrontTooLong() {
        logger.debug("Test: Front exceeds max length");

        String longFront = "a".repeat(501);
        UpdateCardRequest request = new UpdateCardRequest(longFront, VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Front must not exceed 500 characters");

        logger.debug("Test passed: Front over 500 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when front is exactly max length")
    void testFrontExactlyMaxLength() {
        logger.debug("Test: Front is exactly 500 characters");

        String maxFront = "a".repeat(500);
        UpdateCardRequest request = new UpdateCardRequest(maxFront, VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Front at 500 chars is valid");
    }

    // Validation Tests - Back Field

    @Test
    @DisplayName("Should fail validation when back is null")
    void testBackNull() {
        logger.debug("Test: Back is null");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, null, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Back is required");

        logger.debug("Test passed: Null back rejected");
    }

    @Test
    @DisplayName("Should fail validation when back is empty")
    void testBackEmpty() {
        logger.debug("Test: Back is empty");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, "", VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Back is required");

        logger.debug("Test passed: Empty back rejected");
    }

    @Test
    @DisplayName("Should fail validation when back is blank")
    void testBackBlank() {
        logger.debug("Test: Back is blank");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, "   ", VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Back is required");

        logger.debug("Test passed: Blank back rejected");
    }

    @Test
    @DisplayName("Should fail validation when back exceeds max length")
    void testBackTooLong() {
        logger.debug("Test: Back exceeds max length");

        String longBack = "a".repeat(501);
        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, longBack, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Back must not exceed 500 characters");

        logger.debug("Test passed: Back over 500 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when back is exactly max length")
    void testBackExactlyMaxLength() {
        logger.debug("Test: Back is exactly 500 characters");

        String maxBack = "a".repeat(500);
        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, maxBack, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Back at 500 chars is valid");
    }

    // Validation Tests - Notes Field

    @Test
    @DisplayName("Should fail validation when notes exceed max length")
    void testNotesTooLong() {
        logger.debug("Test: Notes exceed max length");

        String longNotes = "a".repeat(1001);
        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, VALID_BACK, longNotes);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Notes must not exceed 1000 characters");

        logger.debug("Test passed: Notes over 1000 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when notes are exactly max length")
    void testNotesExactlyMaxLength() {
        logger.debug("Test: Notes are exactly 1000 characters");

        String maxNotes = "a".repeat(1000);
        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, VALID_BACK, maxNotes);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Notes at 1000 chars are valid");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get front correctly")
    void testSetAndGetFront() {
        logger.debug("Test: Set and get front");

        UpdateCardRequest request = new UpdateCardRequest();
        request.setFront(VALID_FRONT);

        assertThat(request.getFront()).isEqualTo(VALID_FRONT);

        logger.debug("Test passed: Front setter and getter work");
    }

    @Test
    @DisplayName("Should set and get back correctly")
    void testSetAndGetBack() {
        logger.debug("Test: Set and get back");

        UpdateCardRequest request = new UpdateCardRequest();
        request.setBack(VALID_BACK);

        assertThat(request.getBack()).isEqualTo(VALID_BACK);

        logger.debug("Test passed: Back setter and getter work");
    }

    @Test
    @DisplayName("Should set and get notes correctly")
    void testSetAndGetNotes() {
        logger.debug("Test: Set and get notes");

        UpdateCardRequest request = new UpdateCardRequest();
        request.setNotes(VALID_NOTES);

        assertThat(request.getNotes()).isEqualTo(VALID_NOTES);

        logger.debug("Test passed: Notes setter and getter work");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle special characters in front")
    void testSpecialCharactersInFront() {
        logger.debug("Test: Special characters in front");

        UpdateCardRequest request = new UpdateCardRequest("日本語 & français! 🎌", VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getFront()).isEqualTo("日本語 & français! 🎌");

        logger.debug("Test passed: Special characters in front are valid");
    }

    @Test
    @DisplayName("Should handle special characters in back")
    void testSpecialCharactersInBack() {
        logger.debug("Test: Special characters in back");

        UpdateCardRequest request = new UpdateCardRequest(VALID_FRONT, "Japanese & French! 🎌", VALID_NOTES);

        Set<ConstraintViolation<UpdateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getBack()).isEqualTo("Japanese & French! 🎌");

        logger.debug("Test passed: Special characters in back are valid");
    }
}