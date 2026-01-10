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
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TagImportDto.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Validation rules for tag name</li>
 *   <li>Constructor behavior</li>
 *   <li>Getter and setter functionality</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("TagImportDto Tests")
class TagImportDtoTests {

    private static final Logger logger = LoggerFactory.getLogger(TagImportDtoTests.class);

    private Validator validator;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up validator");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create TagImportDto with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        TagImportDto dto = new TagImportDto();

        assertNotNull(dto);
        assertNull(dto.getName());

        logger.debug("Test passed: Default constructor creates instance");
    }

    @Test
    @DisplayName("Should create TagImportDto with name constructor")
    void testNameConstructor() {
        logger.debug("Test: Name constructor");

        TagImportDto dto = new TagImportDto("verbs");

        assertEquals("verbs", dto.getName());

        logger.debug("Test passed: Name constructor sets name");
    }

    // Validation Tests - Name Field

    @Test
    @DisplayName("Should pass validation with valid tag name")
    void testValidTagName() {
        logger.debug("Test: Valid tag name");

        TagImportDto dto = new TagImportDto("verbs");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Valid tag name passes validation");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void testBlankName() {
        logger.debug("Test: Blank name");

        TagImportDto dto = new TagImportDto("");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Tag name cannot be blank"));

        logger.debug("Test passed: Blank name fails validation");
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void testNullName() {
        logger.debug("Test: Null name");

        TagImportDto dto = new TagImportDto();
        dto.setName(null);

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Tag name cannot be blank"));

        logger.debug("Test passed: Null name fails validation");
    }

    @Test
    @DisplayName("Should fail validation when name is whitespace only")
    void testWhitespaceOnlyName() {
        logger.debug("Test: Whitespace only name");

        TagImportDto dto = new TagImportDto("   ");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Tag name cannot be blank"));

        logger.debug("Test passed: Whitespace only name fails validation");
    }

    @Test
    @DisplayName("Should fail validation when name exceeds 50 characters")
    void testNameTooLong() {
        logger.debug("Test: Name too long");

        String longName = "a".repeat(51);
        TagImportDto dto = new TagImportDto(longName);

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Tag name cannot exceed 50 characters"));

        logger.debug("Test passed: Name exceeding 50 characters fails validation");
    }

    @Test
    @DisplayName("Should pass validation when name is exactly 50 characters")
    void testNameExactly50Characters() {
        logger.debug("Test: Name exactly 50 characters");

        String exactName = "a".repeat(50);
        TagImportDto dto = new TagImportDto(exactName);

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Name with 50 characters passes validation");
    }

    @Test
    @DisplayName("Should pass validation with single character name")
    void testSingleCharacterName() {
        logger.debug("Test: Single character name");

        TagImportDto dto = new TagImportDto("N");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Single character name passes validation");
    }

    // Tag Name Characteristics Tests

    @Test
    @DisplayName("Should handle tag names with hyphens")
    void testTagNameWithHyphens() {
        logger.debug("Test: Tag name with hyphens");

        TagImportDto dto = new TagImportDto("jlpt-n5");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("jlpt-n5", dto.getName());

        logger.debug("Test passed: Tag name with hyphens is valid");
    }

    @Test
    @DisplayName("Should handle tag names with underscores")
    void testTagNameWithUnderscores() {
        logger.debug("Test: Tag name with underscores");

        TagImportDto dto = new TagImportDto("common_verbs");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("common_verbs", dto.getName());

        logger.debug("Test passed: Tag name with underscores is valid");
    }

    @Test
    @DisplayName("Should handle tag names with numbers")
    void testTagNameWithNumbers() {
        logger.debug("Test: Tag name with numbers");

        TagImportDto dto = new TagImportDto("chapter1");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("chapter1", dto.getName());

        logger.debug("Test passed: Tag name with numbers is valid");
    }

    @Test
    @DisplayName("Should handle tag names with mixed case")
    void testTagNameWithMixedCase() {
        logger.debug("Test: Tag name with mixed case");

        TagImportDto dto = new TagImportDto("ImportantVerbs");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("ImportantVerbs", dto.getName());

        logger.debug("Test passed: Tag name with mixed case is valid");
    }

    @Test
    @DisplayName("Should handle tag names with spaces")
    void testTagNameWithSpaces() {
        logger.debug("Test: Tag name with spaces");

        TagImportDto dto = new TagImportDto("common verbs");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("common verbs", dto.getName());

        logger.debug("Test passed: Tag name with spaces is valid");
    }

    @Test
    @DisplayName("Should handle tag names with unicode characters")
    void testTagNameWithUnicode() {
        logger.debug("Test: Tag name with unicode characters");

        TagImportDto dto = new TagImportDto("動詞");

        Set<ConstraintViolation<TagImportDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("動詞", dto.getName());

        logger.debug("Test passed: Tag name with unicode is valid");
    }

    // Getter/Setter Tests

    @Test
    @DisplayName("Should get and set name correctly")
    void testGetSetName() {
        logger.debug("Test: Get/set name");

        TagImportDto dto = new TagImportDto();
        dto.setName("adjectives");

        assertEquals("adjectives", dto.getName());

        logger.debug("Test passed: Name getter/setter work correctly");
    }

    @Test
    @DisplayName("Should allow overwriting name")
    void testOverwriteName() {
        logger.debug("Test: Overwriting name");

        TagImportDto dto = new TagImportDto("verbs");
        assertEquals("verbs", dto.getName());

        dto.setName("nouns");
        assertEquals("nouns", dto.getName());

        logger.debug("Test passed: Name can be overwritten");
    }
}