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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CardImportDto.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Validation rules for card fields</li>
 *   <li>Constructor behavior</li>
 *   <li>Getter and setter functionality</li>
 *   <li>Tag handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("CardImportDto Tests")
class CardImportDtoTest {

    private static final Logger logger = LoggerFactory.getLogger(CardImportDtoTest.class);

    private Validator validator;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up validator");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create CardImportDto with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        CardImportDto dto = new CardImportDto();

        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());

        logger.debug("Test passed: Default constructor initializes tags list");
    }

    @Test
    @DisplayName("Should create CardImportDto with all fields")
    void testFullConstructor() {
        logger.debug("Test: Full constructor");

        List<String> tags = Arrays.asList("verbs", "food");
        CardImportDto dto = new CardImportDto("食べる", "to eat", "ru-verb", tags);

        assertEquals("食べる", dto.getFront());
        assertEquals("to eat", dto.getBack());
        assertEquals("ru-verb", dto.getNotes());
        assertEquals(2, dto.getTags().size());
        assertTrue(dto.getTags().contains("verbs"));
        assertTrue(dto.getTags().contains("food"));

        logger.debug("Test passed: Full constructor sets all fields");
    }

    @Test
    @DisplayName("Should create CardImportDto with simple constructor")
    void testSimpleConstructor() {
        logger.debug("Test: Simple constructor");

        CardImportDto dto = new CardImportDto("食べる", "to eat");

        assertEquals("食べる", dto.getFront());
        assertEquals("to eat", dto.getBack());
        assertNull(dto.getNotes());
        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());

        logger.debug("Test passed: Simple constructor sets front and back");
    }

    @Test
    @DisplayName("Should handle null tags in constructor")
    void testConstructorWithNullTags() {
        logger.debug("Test: Constructor with null tags");

        CardImportDto dto = new CardImportDto("食べる", "to eat", "notes", null);

        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());

        logger.debug("Test passed: Null tags converted to empty list");
    }

    // Validation Tests - Front Field

    @Test
    @DisplayName("Should pass validation with valid card")
    void testValidCard() {
        logger.debug("Test: Valid card");

        CardImportDto dto = new CardImportDto("食べる", "to eat", "ru-verb", Arrays.asList("verbs"));

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Valid card passes validation");
    }

    @Test
    @DisplayName("Should fail validation when front is blank")
    void testBlankFront() {
        logger.debug("Test: Blank front");

        CardImportDto dto = new CardImportDto("", "to eat");

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card front cannot be blank"));

        logger.debug("Test passed: Blank front fails validation");
    }

    @Test
    @DisplayName("Should fail validation when front is null")
    void testNullFront() {
        logger.debug("Test: Null front");

        CardImportDto dto = new CardImportDto();
        dto.setFront(null);
        dto.setBack("to eat");

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card front cannot be blank"));

        logger.debug("Test passed: Null front fails validation");
    }

    @Test
    @DisplayName("Should fail validation when front exceeds 1000 characters")
    void testFrontTooLong() {
        logger.debug("Test: Front too long");

        String longText = "a".repeat(1001);
        CardImportDto dto = new CardImportDto(longText, "to eat");

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card front cannot exceed 1000 characters"));

        logger.debug("Test passed: Front exceeding 1000 characters fails validation");
    }

    @Test
    @DisplayName("Should pass validation when front is exactly 1000 characters")
    void testFrontExactly1000Characters() {
        logger.debug("Test: Front exactly 1000 characters");

        String exactText = "a".repeat(1000);
        CardImportDto dto = new CardImportDto(exactText, "to eat");

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Front with 1000 characters passes validation");
    }

    // Validation Tests - Back Field

    @Test
    @DisplayName("Should fail validation when back is blank")
    void testBlankBack() {
        logger.debug("Test: Blank back");

        CardImportDto dto = new CardImportDto("食べる", "");

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card back cannot be blank"));

        logger.debug("Test passed: Blank back fails validation");
    }

    @Test
    @DisplayName("Should fail validation when back is null")
    void testNullBack() {
        logger.debug("Test: Null back");

        CardImportDto dto = new CardImportDto();
        dto.setFront("食べる");
        dto.setBack(null);

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card back cannot be blank"));

        logger.debug("Test passed: Null back fails validation");
    }

    @Test
    @DisplayName("Should fail validation when back exceeds 1000 characters")
    void testBackTooLong() {
        logger.debug("Test: Back too long");

        String longText = "a".repeat(1001);
        CardImportDto dto = new CardImportDto("食べる", longText);

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card back cannot exceed 1000 characters"));

        logger.debug("Test passed: Back exceeding 1000 characters fails validation");
    }

    @Test
    @DisplayName("Should pass validation when back is exactly 1000 characters")
    void testBackExactly1000Characters() {
        logger.debug("Test: Back exactly 1000 characters");

        String exactText = "a".repeat(1000);
        CardImportDto dto = new CardImportDto("食べる", exactText);

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Back with 1000 characters passes validation");
    }

    // Validation Tests - Notes Field

    @Test
    @DisplayName("Should pass validation with null notes")
    void testNullNotes() {
        logger.debug("Test: Null notes");

        CardImportDto dto = new CardImportDto("食べる", "to eat", null, null);

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Null notes pass validation");
    }

    @Test
    @DisplayName("Should pass validation with empty notes")
    void testEmptyNotes() {
        logger.debug("Test: Empty notes");

        CardImportDto dto = new CardImportDto("食べる", "to eat", "", null);

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Empty notes pass validation");
    }

    @Test
    @DisplayName("Should fail validation when notes exceed 2000 characters")
    void testNotesTooLong() {
        logger.debug("Test: Notes too long");

        String longNotes = "a".repeat(2001);
        CardImportDto dto = new CardImportDto("食べる", "to eat", longNotes, null);

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card notes cannot exceed 2000 characters"));

        logger.debug("Test passed: Notes exceeding 2000 characters fail validation");
    }

    @Test
    @DisplayName("Should pass validation when notes are exactly 2000 characters")
    void testNotesExactly2000Characters() {
        logger.debug("Test: Notes exactly 2000 characters");

        String exactNotes = "a".repeat(2000);
        CardImportDto dto = new CardImportDto("食べる", "to eat", exactNotes, null);

        Set<ConstraintViolation<CardImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Notes with 2000 characters pass validation");
    }

    // Tag Tests

    @Test
    @DisplayName("Should handle tags correctly")
    void testTags() {
        logger.debug("Test: Tags handling");

        List<String> tags = Arrays.asList("verbs", "food", "jlpt-n5");
        CardImportDto dto = new CardImportDto("食べる", "to eat", null, tags);

        assertEquals(3, dto.getTags().size());
        assertTrue(dto.hasTags());
        assertTrue(dto.getTags().contains("verbs"));
        assertTrue(dto.getTags().contains("food"));
        assertTrue(dto.getTags().contains("jlpt-n5"));

        logger.debug("Test passed: Tags handled correctly");
    }

    @Test
    @DisplayName("Should return false for hasTags when tags are empty")
    void testHasTagsWithEmptyList() {
        logger.debug("Test: hasTags with empty list");

        CardImportDto dto = new CardImportDto("食べる", "to eat");

        assertFalse(dto.hasTags());

        logger.debug("Test passed: hasTags returns false for empty list");
    }

    @Test
    @DisplayName("Should handle setting null tags")
    void testSetNullTags() {
        logger.debug("Test: Setting null tags");

        CardImportDto dto = new CardImportDto("食べる", "to eat");
        dto.setTags(null);

        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());
        assertFalse(dto.hasTags());

        logger.debug("Test passed: Setting null tags creates empty list");
    }

    // Getter/Setter Tests

    @Test
    @DisplayName("Should get and set front correctly")
    void testGetSetFront() {
        logger.debug("Test: Get/set front");

        CardImportDto dto = new CardImportDto();
        dto.setFront("飲む");

        assertEquals("飲む", dto.getFront());

        logger.debug("Test passed: Front getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set back correctly")
    void testGetSetBack() {
        logger.debug("Test: Get/set back");

        CardImportDto dto = new CardImportDto();
        dto.setBack("to drink");

        assertEquals("to drink", dto.getBack());

        logger.debug("Test passed: Back getter/setter work correctly");
    }

    @Test
    @DisplayName("Should get and set notes correctly")
    void testGetSetNotes() {
        logger.debug("Test: Get/set notes");

        CardImportDto dto = new CardImportDto();
        dto.setNotes("u-verb");

        assertEquals("u-verb", dto.getNotes());

        logger.debug("Test passed: Notes getter/setter work correctly");
    }
}