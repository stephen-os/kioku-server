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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeckImportRequest.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Validation rules for deck import fields</li>
 *   <li>Nested validation of cards and tags</li>
 *   <li>Constructor behavior</li>
 *   <li>Getter and setter functionality</li>
 *   <li>Helper methods (hasTags, getCardCount, etc.)</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("DeckImportRequest Tests")
class DeckImportRequestTests {

    private static final Logger logger = LoggerFactory.getLogger(DeckImportRequestTests.class);

    private Validator validator;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up validator");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create DeckImportRequest with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        DeckImportRequest request = new DeckImportRequest();

        assertNotNull(request.getCards());
        assertTrue(request.getCards().isEmpty());
        assertNotNull(request.getTags());
        assertTrue(request.getTags().isEmpty());

        logger.debug("Test passed: Default constructor initializes lists");
    }

    @Test
    @DisplayName("Should create DeckImportRequest with full constructor")
    void testFullConstructor() {
        logger.debug("Test: Full constructor");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat"),
                new CardImportDto("飲む", "to drink")
        );
        List<TagImportDto> tags = Arrays.asList(
                new TagImportDto("verbs"),
                new TagImportDto("food")
        );

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "JLPT N5 vocab", cards, tags);

        assertEquals("Japanese N5", request.getName());
        assertEquals("JLPT N5 vocab", request.getDescription());
        assertEquals(2, request.getCards().size());
        assertEquals(2, request.getTags().size());

        logger.debug("Test passed: Full constructor sets all fields");
    }

    @Test
    @DisplayName("Should create DeckImportRequest without tags")
    void testConstructorWithoutTags() {
        logger.debug("Test: Constructor without tags");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        assertEquals("Japanese N5", request.getName());
        assertEquals(1, request.getCards().size());
        assertNotNull(request.getTags());
        assertTrue(request.getTags().isEmpty());

        logger.debug("Test passed: Constructor without tags initializes empty tag list");
    }

    @Test
    @DisplayName("Should handle null cards in constructor")
    void testConstructorWithNullCards() {
        logger.debug("Test: Constructor with null cards");

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", null, null);

        assertNotNull(request.getCards());
        assertTrue(request.getCards().isEmpty());
        assertNotNull(request.getTags());
        assertTrue(request.getTags().isEmpty());

        logger.debug("Test passed: Null cards/tags converted to empty lists");
    }

    // Validation Tests - Deck Name

    @Test
    @DisplayName("Should pass validation with valid deck import")
    void testValidDeckImport() {
        logger.debug("Test: Valid deck import");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Valid deck import passes validation");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void testBlankName() {
        logger.debug("Test: Blank name");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest("", "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Deck name cannot be blank"));

        logger.debug("Test passed: Blank name fails validation");
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void testNullName() {
        logger.debug("Test: Null name");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest();
        request.setCards(cards);
        request.setName(null);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Deck name cannot be blank"));

        logger.debug("Test passed: Null name fails validation");
    }

    @Test
    @DisplayName("Should fail validation when name exceeds 255 characters")
    void testNameTooLong() {
        logger.debug("Test: Name too long");

        String longName = "a".repeat(256);
        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest(longName, "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Deck name cannot exceed 255 characters"));

        logger.debug("Test passed: Name exceeding 255 characters fails validation");
    }

    @Test
    @DisplayName("Should pass validation when name is exactly 255 characters")
    void testNameExactly255Characters() {
        logger.debug("Test: Name exactly 255 characters");

        String exactName = "a".repeat(255);
        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest(exactName, "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Name with 255 characters passes validation");
    }

    // Validation Tests - Description

    @Test
    @DisplayName("Should pass validation with null description")
    void testNullDescription() {
        logger.debug("Test: Null description");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest("Japanese N5", null, cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Null description passes validation");
    }

    @Test
    @DisplayName("Should pass validation with empty description")
    void testEmptyDescription() {
        logger.debug("Test: Empty description");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest("Japanese N5", "", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Empty description passes validation");
    }

    @Test
    @DisplayName("Should fail validation when description exceeds 1000 characters")
    void testDescriptionTooLong() {
        logger.debug("Test: Description too long");

        String longDescription = "a".repeat(1001);
        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest("Japanese N5", longDescription, cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Deck description cannot exceed 1000 characters"));

        logger.debug("Test passed: Description exceeding 1000 characters fails validation");
    }

    @Test
    @DisplayName("Should pass validation when description is exactly 1000 characters")
    void testDescriptionExactly1000Characters() {
        logger.debug("Test: Description exactly 1000 characters");

        String exactDescription = "a".repeat(1000);
        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest("Japanese N5", exactDescription, cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Description with 1000 characters passes validation");
    }

    // Validation Tests - Cards

    @Test
    @DisplayName("Should convert null cards to empty list and fail validation")
    void testNullCardsConvertedToEmpty() {
        logger.debug("Test: Null cards converted to empty");

        DeckImportRequest request = new DeckImportRequest();
        request.setName("Japanese N5");
        request.setCards(null);

        // Verify setter behavior
        assertNotNull(request.getCards());
        assertTrue(request.getCards().isEmpty());

        // Validation should fail because empty list
        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("At least one card is required"));

        logger.debug("Test passed: Null cards converted to empty and fails validation");
    }

    @Test
    @DisplayName("Should fail validation when cards is empty")
    void testEmptyCards() {
        logger.debug("Test: Empty cards");

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", new ArrayList<>());

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("At least one card is required"));

        logger.debug("Test passed: Empty cards fails validation");
    }

    @Test
    @DisplayName("Should pass validation with single card")
    void testSingleCard() {
        logger.debug("Test: Single card");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Single card passes validation");
    }

    @Test
    @DisplayName("Should pass validation with multiple cards")
    void testMultipleCards() {
        logger.debug("Test: Multiple cards");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat"),
                new CardImportDto("飲む", "to drink"),
                new CardImportDto("読む", "to read")
        );
        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());

        logger.debug("Test passed: Multiple cards pass validation");
    }

    // Nested Validation Tests - Invalid Cards

    @Test
    @DisplayName("Should fail validation when card has invalid front")
    void testInvalidCardFront() {
        logger.debug("Test: Invalid card front");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("", "to eat"));
        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card front cannot be blank"));

        logger.debug("Test passed: Invalid card front fails validation");
    }

    @Test
    @DisplayName("Should fail validation when card has invalid back")
    void testInvalidCardBack() {
        logger.debug("Test: Invalid card back");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", ""));
        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Card back cannot be blank"));

        logger.debug("Test passed: Invalid card back fails validation");
    }

    @Test
    @DisplayName("Should fail validation when multiple cards are invalid")
    void testMultipleInvalidCards() {
        logger.debug("Test: Multiple invalid cards");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("", "to eat"),  // Invalid front
                new CardImportDto("飲む", "")     // Invalid back
        );
        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations.size()).isGreaterThanOrEqualTo(2);

        logger.debug("Test passed: Multiple invalid cards produce multiple violations");
    }

    // Nested Validation Tests - Invalid Tags

    @Test
    @DisplayName("Should fail validation when tag has blank name")
    void testInvalidTag() {
        logger.debug("Test: Invalid tag");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        List<TagImportDto> tags = Arrays.asList(new TagImportDto(""));

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards, tags);

        Set<ConstraintViolation<DeckImportRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Tag name cannot be blank"));

        logger.debug("Test passed: Invalid tag fails validation");
    }

    // Helper Method Tests

    @Test
    @DisplayName("Should correctly report hasTags when tags are present")
    void testHasTagsTrue() {
        logger.debug("Test: hasTags with tags present");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        List<TagImportDto> tags = Arrays.asList(new TagImportDto("verbs"));

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards, tags);

        assertTrue(request.hasTags());

        logger.debug("Test passed: hasTags returns true");
    }

    @Test
    @DisplayName("Should correctly report hasTags when tags are empty")
    void testHasTagsFalse() {
        logger.debug("Test: hasTags with empty tags");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        assertFalse(request.hasTags());

        logger.debug("Test passed: hasTags returns false");
    }

    @Test
    @DisplayName("Should correctly count cards")
    void testGetCardCount() {
        logger.debug("Test: getCardCount");

        List<CardImportDto> cards = Arrays.asList(
                new CardImportDto("食べる", "to eat"),
                new CardImportDto("飲む", "to drink"),
                new CardImportDto("読む", "to read")
        );

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        assertEquals(3, request.getCardCount());

        logger.debug("Test passed: Card count is correct");
    }

    @Test
    @DisplayName("Should return zero card count for empty list")
    void testGetCardCountEmpty() {
        logger.debug("Test: getCardCount with empty list");

        DeckImportRequest request = new DeckImportRequest();

        assertEquals(0, request.getCardCount());

        logger.debug("Test passed: Empty card count returns zero");
    }

    @Test
    @DisplayName("Should correctly count tags")
    void testGetTagCount() {
        logger.debug("Test: getTagCount");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));
        List<TagImportDto> tags = Arrays.asList(
                new TagImportDto("verbs"),
                new TagImportDto("food")
        );

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards, tags);

        assertEquals(2, request.getTagCount());

        logger.debug("Test passed: Tag count is correct");
    }

    @Test
    @DisplayName("Should return zero tag count for empty list")
    void testGetTagCountEmpty() {
        logger.debug("Test: getTagCount with empty list");

        List<CardImportDto> cards = Arrays.asList(new CardImportDto("食べる", "to eat"));

        DeckImportRequest request = new DeckImportRequest("Japanese N5", "Description", cards);

        assertEquals(0, request.getTagCount());

        logger.debug("Test passed: Empty tag count returns zero");
    }

    // Getter/Setter Tests

    @Test
    @DisplayName("Should handle setting null cards")
    void testSetNullCards() {
        logger.debug("Test: Setting null cards");

        DeckImportRequest request = new DeckImportRequest();
        request.setCards(null);

        assertNotNull(request.getCards());
        assertTrue(request.getCards().isEmpty());

        logger.debug("Test passed: Setting null cards creates empty list");
    }

    @Test
    @DisplayName("Should handle setting null tags")
    void testSetNullTags() {
        logger.debug("Test: Setting null tags");

        DeckImportRequest request = new DeckImportRequest();
        request.setTags(null);

        assertNotNull(request.getTags());
        assertTrue(request.getTags().isEmpty());

        logger.debug("Test passed: Setting null tags creates empty list");
    }
}