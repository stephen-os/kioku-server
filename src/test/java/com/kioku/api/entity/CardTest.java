package com.kioku.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Card entity.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Card creation with required and optional fields</li>
 *   <li>Field validation (front, back, deck cannot be null/empty)</li>
 *   <li>Editing card content (front, back, notes)</li>
 *   <li>Tag management (add, remove, clear)</li>
 *   <li>Deck association and reassignment</li>
 *   <li>Equality and hash code based on ID</li>
 *   <li>toString excludes sensitive data</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("Card Entity Unit Tests")
class CardTest {

    private static final Logger logger = LoggerFactory.getLogger(CardTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String DECK_NAME = "Japanese Vocabulary";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary practice";
    private static final String CARD_FRONT = "食べる";
    private static final String CARD_BACK = "to eat";
    private static final String CARD_NOTES = "ru-verb, ichidan verb";
    private static final String UPDATED_FRONT = "飲む";
    private static final String UPDATED_BACK = "to drink";
    private static final String TAG_NAME = "verbs";

    private User testUser;
    private Deck testDeck;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up Card test: Creating test user and deck");
        testUser = new User(TEST_EMAIL, PASSWORD_HASH);
        testUser.setId(1L);
        testDeck = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
        testDeck.setId(1L);
    }

    // Constructor Tests

    /**
     * Tests card creation with required fields only.
     */
    @Test
    @DisplayName("Should create card with required fields")
    void testCardCreation() {
        logger.debug("Test: Creating card with front='{}', back='{}'", CARD_FRONT, CARD_BACK);

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertEquals(CARD_FRONT, card.getFront());
        assertEquals(CARD_BACK, card.getBack());
        assertEquals(testDeck, card.getDeck());
        assertNull(card.getId());
        assertNull(card.getNotes());
        assertNotNull(card.getTags());
        assertTrue(card.getTags().isEmpty());

        logger.debug("Test passed: Card created successfully");
    }

    /**
     * Tests card creation with optional notes.
     */
    @Test
    @DisplayName("Should create card with notes")
    void testCardCreationWithNotes() {
        logger.debug("Test: Creating card with notes='{}'", CARD_NOTES);

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK, CARD_NOTES);

        assertEquals(CARD_FRONT, card.getFront());
        assertEquals(CARD_BACK, card.getBack());
        assertEquals(CARD_NOTES, card.getNotes());
        assertEquals(testDeck, card.getDeck());

        logger.debug("Test passed: Card created with notes");
    }

    /**
     * Tests no-args constructor creates valid card.
     */
    @Test
    @DisplayName("Should create card with no-args constructor")
    void testNoArgsConstructor() {
        logger.debug("Test: Creating card with no-args constructor");

        Card card = new Card();

        assertNotNull(card);
        assertNull(card.getFront());
        assertNull(card.getBack());
        assertNull(card.getDeck());
        assertNull(card.getNotes());
        assertNotNull(card.getTags());

        logger.debug("Test passed: No-args constructor works");
    }

    // Validation Tests

    /**
     * Tests that creating card with null deck throws exception.
     */
    @Test
    @DisplayName("Should throw exception when deck is null")
    void testNullDeck() {
        logger.debug("Test: Creating card with null deck");

        assertThrows(IllegalArgumentException.class, () -> {
            new Card(null, CARD_FRONT, CARD_BACK);
        });

        logger.debug("Test passed: Exception thrown for null deck");
    }

    /**
     * Tests that creating card with null front throws exception.
     */
    @Test
    @DisplayName("Should throw exception when front is null")
    void testNullFront() {
        logger.debug("Test: Creating card with null front");

        assertThrows(IllegalArgumentException.class, () -> {
            new Card(testDeck, null, CARD_BACK);
        });

        logger.debug("Test passed: Exception thrown for null front");
    }

    /**
     * Tests that creating card with empty front throws exception.
     */
    @Test
    @DisplayName("Should throw exception when front is empty")
    void testEmptyFront() {
        logger.debug("Test: Creating card with empty front");

        assertThrows(IllegalArgumentException.class, () -> {
            new Card(testDeck, "", CARD_BACK);
        });

        logger.debug("Test passed: Exception thrown for empty front");
    }

    /**
     * Tests that creating card with whitespace-only front throws exception.
     */
    @Test
    @DisplayName("Should throw exception when front is whitespace only")
    void testWhitespaceFront() {
        logger.debug("Test: Creating card with whitespace-only front");

        assertThrows(IllegalArgumentException.class, () -> {
            new Card(testDeck, "   ", CARD_BACK);
        });

        logger.debug("Test passed: Exception thrown for whitespace-only front");
    }

    /**
     * Tests that creating card with null back throws exception.
     */
    @Test
    @DisplayName("Should throw exception when back is null")
    void testNullBack() {
        logger.debug("Test: Creating card with null back");

        assertThrows(IllegalArgumentException.class, () -> {
            new Card(testDeck, CARD_FRONT, null);
        });

        logger.debug("Test passed: Exception thrown for null back");
    }

    /**
     * Tests that creating card with empty back throws exception.
     */
    @Test
    @DisplayName("Should throw exception when back is empty")
    void testEmptyBack() {
        logger.debug("Test: Creating card with empty back");

        assertThrows(IllegalArgumentException.class, () -> {
            new Card(testDeck, CARD_FRONT, "");
        });

        logger.debug("Test passed: Exception thrown for empty back");
    }

    /**
     * Tests that setting null deck throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting null deck")
    void testSetNullDeck() {
        logger.debug("Test: Setting null deck");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertThrows(IllegalArgumentException.class, () -> {
            card.setDeck(null);
        });

        logger.debug("Test passed: Exception thrown for setting null deck");
    }

    /**
     * Tests that setting null front throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting null front")
    void testSetNullFront() {
        logger.debug("Test: Setting null front");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertThrows(IllegalArgumentException.class, () -> {
            card.setFront(null);
        });

        logger.debug("Test passed: Exception thrown for setting null front");
    }

    /**
     * Tests that setting empty front throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting empty front")
    void testSetEmptyFront() {
        logger.debug("Test: Setting empty front");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertThrows(IllegalArgumentException.class, () -> {
            card.setFront("");
        });

        logger.debug("Test passed: Exception thrown for setting empty front");
    }

    /**
     * Tests that setting null back throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting null back")
    void testSetNullBack() {
        logger.debug("Test: Setting null back");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertThrows(IllegalArgumentException.class, () -> {
            card.setBack(null);
        });

        logger.debug("Test passed: Exception thrown for setting null back");
    }

    /**
     * Tests that setting empty back throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting empty back")
    void testSetEmptyBack() {
        logger.debug("Test: Setting empty back");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertThrows(IllegalArgumentException.class, () -> {
            card.setBack("");
        });

        logger.debug("Test passed: Exception thrown for setting empty back");
    }

    // Editing Tests

    /**
     * Tests editing card front content.
     */
    @Test
    @DisplayName("Should allow editing card front")
    void testEditFront() {
        logger.debug("Test: Editing card front from '{}' to '{}'", CARD_FRONT, UPDATED_FRONT);

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card.setFront(UPDATED_FRONT);

        assertEquals(UPDATED_FRONT, card.getFront());
        assertEquals(CARD_BACK, card.getBack()); // Back unchanged

        logger.debug("Test passed: Front edited successfully");
    }

    /**
     * Tests editing card back content.
     */
    @Test
    @DisplayName("Should allow editing card back")
    void testEditBack() {
        logger.debug("Test: Editing card back from '{}' to '{}'", CARD_BACK, UPDATED_BACK);

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card.setBack(UPDATED_BACK);

        assertEquals(CARD_FRONT, card.getFront()); // Front unchanged
        assertEquals(UPDATED_BACK, card.getBack());

        logger.debug("Test passed: Back edited successfully");
    }

    /**
     * Tests editing card notes.
     */
    @Test
    @DisplayName("Should allow editing card notes")
    void testEditNotes() {
        logger.debug("Test: Editing card notes");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        assertNull(card.getNotes());

        card.setNotes(CARD_NOTES);
        assertEquals(CARD_NOTES, card.getNotes());

        card.setNotes("Updated notes");
        assertEquals("Updated notes", card.getNotes());

        logger.debug("Test passed: Notes edited successfully");
    }

    /**
     * Tests clearing card notes by setting to null.
     */
    @Test
    @DisplayName("Should allow clearing card notes")
    void testClearNotes() {
        logger.debug("Test: Clearing card notes");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK, CARD_NOTES);
        assertEquals(CARD_NOTES, card.getNotes());

        card.setNotes(null);
        assertNull(card.getNotes());

        logger.debug("Test passed: Notes cleared successfully");
    }

    /**
     * Tests editing all card fields at once.
     */
    @Test
    @DisplayName("Should allow editing all fields")
    void testEditAllFields() {
        logger.debug("Test: Editing all card fields");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        card.setFront(UPDATED_FRONT);
        card.setBack(UPDATED_BACK);
        card.setNotes("New notes");

        assertEquals(UPDATED_FRONT, card.getFront());
        assertEquals(UPDATED_BACK, card.getBack());
        assertEquals("New notes", card.getNotes());

        logger.debug("Test passed: All fields edited successfully");
    }

    // Deck Association Tests

    /**
     * Tests reassigning card to different deck.
     */
    @Test
    @DisplayName("Should allow reassigning card to different deck")
    void testReassignDeck() {
        logger.debug("Test: Reassigning card to different deck");

        Deck newDeck = new Deck(testUser, "New Deck", "Another deck");
        newDeck.setId(2L);

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        assertEquals(testDeck, card.getDeck());

        card.setDeck(newDeck);
        assertEquals(newDeck, card.getDeck());

        logger.debug("Test passed: Deck reassigned successfully");
    }

    // Tag Management Tests

    /**
     * Tests adding a tag to a card.
     */
    @Test
    @DisplayName("Should add tag to card")
    void testAddTag() {
        logger.debug("Test: Adding tag='{}' to card", TAG_NAME);

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        Tag tag = new Tag(testUser, TAG_NAME);

        card.addTag(tag);

        assertTrue(card.getTags().contains(tag));
        assertTrue(tag.getCards().contains(card));

        logger.debug("Test passed: Tag added successfully");
    }

    /**
     * Tests removing a tag from a card.
     */
    @Test
    @DisplayName("Should remove tag from card")
    void testRemoveTag() {
        logger.debug("Test: Removing tag from card");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        Tag tag = new Tag(testUser, TAG_NAME);

        card.addTag(tag);
        assertTrue(card.getTags().contains(tag));

        card.removeTag(tag);
        assertFalse(card.getTags().contains(tag));
        assertFalse(tag.getCards().contains(card));

        logger.debug("Test passed: Tag removed successfully");
    }

    /**
     * Tests adding multiple tags to a card.
     */
    @Test
    @DisplayName("Should add multiple tags to card")
    void testAddMultipleTags() {
        logger.debug("Test: Adding multiple tags to card");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        Tag tag1 = new Tag(testUser, "verbs");
        Tag tag2 = new Tag(testUser, "N5");
        Tag tag3 = new Tag(testUser, "food");

        card.addTag(tag1);
        card.addTag(tag2);
        card.addTag(tag3);

        assertEquals(3, card.getTags().size());
        assertTrue(card.getTags().contains(tag1));
        assertTrue(card.getTags().contains(tag2));
        assertTrue(card.getTags().contains(tag3));

        logger.debug("Test passed: Multiple tags added successfully");
    }

    /**
     * Tests that adding null tag throws exception.
     */
    @Test
    @DisplayName("Should throw exception when adding null tag")
    void testAddNullTag() {
        logger.debug("Test: Adding null tag");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertThrows(IllegalArgumentException.class, () -> {
            card.addTag(null);
        });

        logger.debug("Test passed: Exception thrown for null tag");
    }

    /**
     * Tests that removing null tag throws exception.
     */
    @Test
    @DisplayName("Should throw exception when removing null tag")
    void testRemoveNullTag() {
        logger.debug("Test: Removing null tag");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertThrows(IllegalArgumentException.class, () -> {
            card.removeTag(null);
        });

        logger.debug("Test passed: Exception thrown for null tag");
    }

    /**
     * Tests clearing all tags from a card.
     */
    @Test
    @DisplayName("Should clear all tags from card")
    void testClearTags() {
        logger.debug("Test: Clearing all tags from card");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        Tag tag1 = new Tag(testUser, "verbs");
        Tag tag2 = new Tag(testUser, "N5");

        card.addTag(tag1);
        card.addTag(tag2);
        assertEquals(2, card.getTags().size());

        card.clearTags();

        assertEquals(0, card.getTags().size());
        assertFalse(tag1.getCards().contains(card));
        assertFalse(tag2.getCards().contains(card));

        logger.debug("Test passed: All tags cleared successfully");
    }

    // Equality and HashCode Tests

    /**
     * Tests card equality based on ID.
     */
    @Test
    @DisplayName("Should consider cards equal with same ID")
    void testCardEquality() {
        logger.debug("Test: Testing card equality");

        Card card1 = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card1.setId(1L);

        Card card2 = new Card(testDeck, "Different", "Content");
        card2.setId(1L);

        Card card3 = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card3.setId(2L);

        // Same ID = equal
        assertEquals(card1, card2);

        // Different ID = not equal
        assertNotEquals(card1, card3);

        logger.debug("Test passed: Equality based on ID works correctly");
    }

    /**
     * Tests reflexive property of equals.
     */
    @Test
    @DisplayName("Should be equal to itself")
    void testEqualityReflexive() {
        logger.debug("Test: Testing reflexive equality");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card.setId(1L);

        assertEquals(card, card);

        logger.debug("Test passed: Card equals itself");
    }

    /**
     * Tests that card is not equal to null.
     */
    @Test
    @DisplayName("Should not be equal to null")
    void testEqualityNull() {
        logger.debug("Test: Testing equality with null");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card.setId(1L);

        assertNotEquals(null, card);

        logger.debug("Test passed: Card not equal to null");
    }

    /**
     * Tests that card is not equal to different class.
     */
    @Test
    @DisplayName("Should not be equal to different class")
    void testEqualityDifferentClass() {
        logger.debug("Test: Testing equality with different class");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card.setId(1L);

        assertNotEquals(card, "Not a Card");

        logger.debug("Test passed: Card not equal to different class");
    }

    /**
     * Tests that cards without IDs are only equal to themselves.
     */
    @Test
    @DisplayName("Should handle equality for cards without IDs")
    void testEqualityWithoutIds() {
        logger.debug("Test: Testing equality for cards without IDs");

        Card card1 = new Card(testDeck, CARD_FRONT, CARD_BACK);
        Card card2 = new Card(testDeck, CARD_FRONT, CARD_BACK);

        // Same instance
        assertEquals(card1, card1);

        // Different instances without IDs are not equal
        assertNotEquals(card1, card2);

        logger.debug("Test passed: Cards without IDs handled correctly");
    }

    /**
     * Tests hash code consistency with equals.
     */
    @Test
    @DisplayName("Should have consistent hash code with equals")
    void testCardHashCode() {
        logger.debug("Test: Testing hash code consistency");

        Card card1 = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card1.setId(1L);

        Card card2 = new Card(testDeck, "Different", "Content");
        card2.setId(1L);

        assertEquals(card1.hashCode(), card2.hashCode());

        logger.debug("Test passed: Hash code consistent with equals");
    }

    // toString Tests

    /**
     * Tests that toString includes card content.
     */
    @Test
    @DisplayName("Should include card content in toString")
    void testToStringIncludesContent() {
        logger.debug("Test: Testing toString includes content");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK, CARD_NOTES);

        String cardString = card.toString();

        assertTrue(cardString.contains(CARD_FRONT));
        assertTrue(cardString.contains(CARD_BACK));
        assertTrue(cardString.contains(CARD_NOTES));

        logger.debug("Test passed: toString includes card content");
    }

    /**
     * Tests that toString does not expose sensitive user data.
     */
    @Test
    @DisplayName("Should not expose sensitive data in toString")
    void testToStringDoesNotExposeSensitiveData() {
        logger.debug("Test: Testing toString does not expose sensitive data");

        User sensitiveUser = new User("secret@example.com", "secretPassword");
        Deck sensitiveDeck = new Deck(sensitiveUser, "My Deck", "Description");
        Card card = new Card(sensitiveDeck, CARD_FRONT, CARD_BACK);

        String cardString = card.toString();

        assertTrue(cardString.contains(CARD_FRONT));
        assertTrue(cardString.contains(CARD_BACK));
        assertFalse(cardString.contains("secret@example.com"));
        assertFalse(cardString.contains("secretPassword"));

        logger.debug("Test passed: Sensitive data not in toString");
    }

    /**
     * Tests that toString includes tag count.
     */
    @Test
    @DisplayName("Should include tag count in toString")
    void testToStringIncludesTagCount() {
        logger.debug("Test: Testing toString includes tag count");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        Tag tag1 = new Tag(testUser, "verbs");
        Tag tag2 = new Tag(testUser, "N5");

        card.addTag(tag1);
        card.addTag(tag2);

        String cardString = card.toString();

        assertTrue(cardString.contains("tagCount=2"));

        logger.debug("Test passed: toString includes tag count");
    }

    // Getter/Setter Tests

    /**
     * Tests version field getter and setter.
     */
    @Test
    @DisplayName("Should get and set version")
    void testVersionGetterSetter() {
        logger.debug("Test: Testing version getter and setter");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertNull(card.getVersion());

        card.setVersion(1L);
        assertEquals(1L, card.getVersion());

        logger.debug("Test passed: Version getter and setter work");
    }

    /**
     * Tests ID field getter and setter.
     */
    @Test
    @DisplayName("Should get and set ID")
    void testIdGetterSetter() {
        logger.debug("Test: Testing ID getter and setter");

        Card card = new Card(testDeck, CARD_FRONT, CARD_BACK);

        assertNull(card.getId());

        card.setId(100L);
        assertEquals(100L, card.getId());

        logger.debug("Test passed: ID getter and setter work");
    }
}