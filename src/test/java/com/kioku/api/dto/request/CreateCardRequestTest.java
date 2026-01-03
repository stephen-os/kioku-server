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
 * Unit tests for CreateCardRequest DTO.
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
@DisplayName("CreateCardRequest DTO Tests")
class CreateCardRequestTest {

    private static final Logger logger = LoggerFactory.getLogger(CreateCardRequestTest.class);

    private Validator validator;

    // Test data constants
    private static final String VALID_FRONT = "食べる";
    private static final String VALID_BACK = "to eat";
    private static final String VALID_NOTES = "ru-verb, ichidan verb";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up CreateCardRequest test");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create request with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        CreateCardRequest request = new CreateCardRequest();

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

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, VALID_BACK, VALID_NOTES);

        assertThat(request.getFront()).isEqualTo(VALID_FRONT);
        assertThat(request.getBack()).isEqualTo(VALID_BACK);
        assertThat(request.getNotes()).isEqualTo(VALID_NOTES);

        logger.debug("Test passed: Parameterized constructor works");
    }

    @Test
    @DisplayName("Should create request with null notes")
    void testConstructorWithNullNotes() {
        logger.debug("Test: Constructor with null notes");

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, VALID_BACK, null);

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

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Valid request has no violations");
    }

    @Test
    @DisplayName("Should validate successfully without notes")
    void testValidRequestWithoutNotes() {
        logger.debug("Test: Valid request without notes");

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, VALID_BACK, null);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Notes are optional");
    }

    @Test
    @DisplayName("Should validate successfully with empty notes")
    void testValidRequestWithEmptyNotes() {
        logger.debug("Test: Valid request with empty notes");

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, VALID_BACK, "");

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Empty notes are valid");
    }

    // Validation Tests - Front Field

    @Test
    @DisplayName("Should fail validation when front is null")
    void testFrontNull() {
        logger.debug("Test: Front is null");

        CreateCardRequest request = new CreateCardRequest(null, VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Front is required");

        logger.debug("Test passed: Null front rejected");
    }

    @Test
    @DisplayName("Should fail validation when front is empty")
    void testFrontEmpty() {
        logger.debug("Test: Front is empty");

        CreateCardRequest request = new CreateCardRequest("", VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Front is required");

        logger.debug("Test passed: Empty front rejected");
    }

    @Test
    @DisplayName("Should fail validation when front is blank")
    void testFrontBlank() {
        logger.debug("Test: Front is blank");

        CreateCardRequest request = new CreateCardRequest("   ", VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Front is required");

        logger.debug("Test passed: Blank front rejected");
    }

    @Test
    @DisplayName("Should fail validation when front exceeds max length")
    void testFrontTooLong() {
        logger.debug("Test: Front exceeds max length");

        String longFront = "a".repeat(501);
        CreateCardRequest request = new CreateCardRequest(longFront, VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Front must not exceed 500 characters");

        logger.debug("Test passed: Front over 500 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when front is exactly max length")
    void testFrontExactlyMaxLength() {
        logger.debug("Test: Front is exactly 500 characters");

        String maxFront = "a".repeat(500);
        CreateCardRequest request = new CreateCardRequest(maxFront, VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Front at 500 chars is valid");
    }

    // Validation Tests - Back Field

    @Test
    @DisplayName("Should fail validation when back is null")
    void testBackNull() {
        logger.debug("Test: Back is null");

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, null, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Back is required");

        logger.debug("Test passed: Null back rejected");
    }

    @Test
    @DisplayName("Should fail validation when back is empty")
    void testBackEmpty() {
        logger.debug("Test: Back is empty");

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, "", VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Back is required");

        logger.debug("Test passed: Empty back rejected");
    }

    @Test
    @DisplayName("Should fail validation when back is blank")
    void testBackBlank() {
        logger.debug("Test: Back is blank");

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, "   ", VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Back is required");

        logger.debug("Test passed: Blank back rejected");
    }

    @Test
    @DisplayName("Should fail validation when back exceeds max length")
    void testBackTooLong() {
        logger.debug("Test: Back exceeds max length");

        String longBack = "a".repeat(501);
        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, longBack, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Back must not exceed 500 characters");

        logger.debug("Test passed: Back over 500 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when back is exactly max length")
    void testBackExactlyMaxLength() {
        logger.debug("Test: Back is exactly 500 characters");

        String maxBack = "a".repeat(500);
        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, maxBack, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Back at 500 chars is valid");
    }

    // Validation Tests - Notes Field

    @Test
    @DisplayName("Should fail validation when notes exceed max length")
    void testNotesTooLong() {
        logger.debug("Test: Notes exceed max length");

        String longNotes = "a".repeat(1001);
        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, VALID_BACK, longNotes);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Notes must not exceed 1000 characters");

        logger.debug("Test passed: Notes over 1000 chars rejected");
    }

    @Test
    @DisplayName("Should validate successfully when notes are exactly max length")
    void testNotesExactlyMaxLength() {
        logger.debug("Test: Notes are exactly 1000 characters");

        String maxNotes = "a".repeat(1000);
        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, VALID_BACK, maxNotes);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Notes at 1000 chars are valid");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get front correctly")
    void testSetAndGetFront() {
        logger.debug("Test: Set and get front");

        CreateCardRequest request = new CreateCardRequest();
        request.setFront(VALID_FRONT);

        assertThat(request.getFront()).isEqualTo(VALID_FRONT);

        logger.debug("Test passed: Front setter and getter work");
    }

    @Test
    @DisplayName("Should set and get back correctly")
    void testSetAndGetBack() {
        logger.debug("Test: Set and get back");

        CreateCardRequest request = new CreateCardRequest();
        request.setBack(VALID_BACK);

        assertThat(request.getBack()).isEqualTo(VALID_BACK);

        logger.debug("Test passed: Back setter and getter work");
    }

    @Test
    @DisplayName("Should set and get notes correctly")
    void testSetAndGetNotes() {
        logger.debug("Test: Set and get notes");

        CreateCardRequest request = new CreateCardRequest();
        request.setNotes(VALID_NOTES);

        assertThat(request.getNotes()).isEqualTo(VALID_NOTES);

        logger.debug("Test passed: Notes setter and getter work");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle special characters in front")
    void testSpecialCharactersInFront() {
        logger.debug("Test: Special characters in front");

        CreateCardRequest request = new CreateCardRequest("日本語 & français! 🎌", VALID_BACK, VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getFront()).isEqualTo("日本語 & français! 🎌");

        logger.debug("Test passed: Special characters in front are valid");
    }

    @Test
    @DisplayName("Should handle special characters in back")
    void testSpecialCharactersInBack() {
        logger.debug("Test: Special characters in back");

        CreateCardRequest request = new CreateCardRequest(VALID_FRONT, "Japanese & French! 🎌", VALID_NOTES);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getBack()).isEqualTo("Japanese & French! 🎌");

        logger.debug("Test passed: Special characters in back are valid");
    }
}