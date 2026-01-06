package com.kioku.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Card entity.
 *
 * <p>Enhanced tests verify:
 * <ul>
 *   <li>Card creation with required and optional fields</li>
 *   <li>Field validation (front, back, deck cannot be null/empty)</li>
 *   <li>Editing card content (front, back, notes)</li>
 *   <li>Tag management (add, remove, clear)</li>
 *   <li>Bidirectional tag relationships</li>
 *   <li>Deck association and reassignment</li>
 *   <li>Deck-specific tag isolation</li>
 *   <li>Equality and hash code based on ID</li>
 *   <li>toString excludes sensitive data</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 2.0
 * @since 1.0
 */
@DisplayName("Card Entity Comprehensive Tests")
class CardUnitTests {

    private static final Logger logger = LoggerFactory.getLogger(CardUnitTests.class);

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
    private Card card;

    /**
     * Sets up test fixtures before each test.
     * Creates a test user, deck, and card for testing.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up Card test: Creating test user, deck, and card");
        testUser = new User(TEST_EMAIL, PASSWORD_HASH);
        testUser.setId(1L);
        testDeck = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
        testDeck.setId(1L);
        card = new Card(testDeck, CARD_FRONT, CARD_BACK);
        card.setId(1L);
    }

    // ========================================
    // Constructor Tests
    // ========================================

    /**
     * Tests for the Card constructor and initialization.
     */
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        /**
         * Tests the Card constructor with required fields only.
         */
        @Test
        @DisplayName("Should create card with required fields")
        void testCardCreation() {
            logger.debug("Test: Creating card with front='{}', back='{}'", CARD_FRONT, CARD_BACK);

            Card newCard = new Card(testDeck, CARD_FRONT, CARD_BACK);

            assertEquals(CARD_FRONT, newCard.getFront());
            assertEquals(CARD_BACK, newCard.getBack());
            assertEquals(testDeck, newCard.getDeck());
            assertNull(newCard.getId());
            assertNull(newCard.getNotes());
            assertNotNull(newCard.getTags(), "Tags collection should be initialized");
            assertTrue(newCard.getTags().isEmpty(), "Tags should be empty");

            logger.debug("Test passed: Card created successfully");
        }

        /**
         * Tests the Card constructor with optional notes field.
         */
        @Test
        @DisplayName("Should create card with notes")
        void testCardCreationWithNotes() {
            logger.debug("Test: Creating card with notes='{}'", CARD_NOTES);

            Card newCard = new Card(testDeck, CARD_FRONT, CARD_BACK, CARD_NOTES);

            assertEquals(CARD_FRONT, newCard.getFront());
            assertEquals(CARD_BACK, newCard.getBack());
            assertEquals(CARD_NOTES, newCard.getNotes());
            assertEquals(testDeck, newCard.getDeck());

            logger.debug("Test passed: Card created with notes");
        }

        /**
         * Tests the no-args constructor creates a valid card.
         */
        @Test
        @DisplayName("Should create card with no-args constructor")
        void testNoArgsConstructor() {
            logger.debug("Test: Creating card with no-args constructor");

            Card emptyCard = new Card();

            assertNotNull(emptyCard);
            assertNull(emptyCard.getFront());
            assertNull(emptyCard.getBack());
            assertNull(emptyCard.getDeck());
            assertNull(emptyCard.getNotes());
            assertNotNull(emptyCard.getTags());

            logger.debug("Test passed: No-args constructor works");
        }
    }

    // ========================================
    // Validation Tests
    // ========================================

    /**
     * Tests for field validation and null/empty checks.
     */
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        /**
         * Tests that creating a card with null deck throws exception.
         */
        @Test
        @DisplayName("Should throw exception when deck is null")
        void testNullDeck() {
            logger.debug("Test: Creating card with null deck");

            assertThrows(IllegalArgumentException.class, () -> {
                new Card(null, CARD_FRONT, CARD_BACK);
            }, "Should throw exception for null deck");

            logger.debug("Test passed: Exception thrown for null deck");
        }

        /**
         * Tests that creating a card with null front text throws exception.
         */
        @Test
        @DisplayName("Should throw exception when front is null")
        void testNullFront() {
            logger.debug("Test: Creating card with null front");

            assertThrows(IllegalArgumentException.class, () -> {
                new Card(testDeck, null, CARD_BACK);
            }, "Should throw exception for null front");

            logger.debug("Test passed: Exception thrown for null front");
        }

        /**
         * Tests that creating a card with empty front text throws exception.
         */
        @Test
        @DisplayName("Should throw exception when front is empty")
        void testEmptyFront() {
            logger.debug("Test: Creating card with empty front");

            assertThrows(IllegalArgumentException.class, () -> {
                new Card(testDeck, "", CARD_BACK);
            }, "Should throw exception for empty front");

            logger.debug("Test passed: Exception thrown for empty front");
        }

        /**
         * Tests that creating a card with whitespace-only front throws exception.
         */
        @Test
        @DisplayName("Should throw exception when front is whitespace only")
        void testWhitespaceFront() {
            logger.debug("Test: Creating card with whitespace-only front");

            assertThrows(IllegalArgumentException.class, () -> {
                new Card(testDeck, "   ", CARD_BACK);
            }, "Should throw exception for whitespace front");

            logger.debug("Test passed: Exception thrown for whitespace-only front");
        }

        /**
         * Tests that creating a card with null back text throws exception.
         */
        @Test
        @DisplayName("Should throw exception when back is null")
        void testNullBack() {
            logger.debug("Test: Creating card with null back");

            assertThrows(IllegalArgumentException.class, () -> {
                new Card(testDeck, CARD_FRONT, null);
            }, "Should throw exception for null back");

            logger.debug("Test passed: Exception thrown for null back");
        }

        /**
         * Tests that creating a card with empty back text throws exception.
         */
        @Test
        @DisplayName("Should throw exception when back is empty")
        void testEmptyBack() {
            logger.debug("Test: Creating card with empty back");

            assertThrows(IllegalArgumentException.class, () -> {
                new Card(testDeck, CARD_FRONT, "");
            }, "Should throw exception for empty back");

            logger.debug("Test passed: Exception thrown for empty back");
        }

        /**
         * Tests that setting null deck throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting null deck")
        void testSetNullDeck() {
            logger.debug("Test: Setting null deck");

            assertThrows(IllegalArgumentException.class, () -> {
                card.setDeck(null);
            }, "Should throw exception when setting null deck");

            logger.debug("Test passed: Exception thrown for setting null deck");
        }

        /**
         * Tests that setting null front text throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting null front")
        void testSetNullFront() {
            logger.debug("Test: Setting null front");

            assertThrows(IllegalArgumentException.class, () -> {
                card.setFront(null);
            }, "Should throw exception when setting null front");

            logger.debug("Test passed: Exception thrown for setting null front");
        }

        /**
         * Tests that setting empty front text throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting empty front")
        void testSetEmptyFront() {
            logger.debug("Test: Setting empty front");

            assertThrows(IllegalArgumentException.class, () -> {
                card.setFront("");
            }, "Should throw exception when setting empty front");

            logger.debug("Test passed: Exception thrown for setting empty front");
        }

        /**
         * Tests that setting null back text throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting null back")
        void testSetNullBack() {
            logger.debug("Test: Setting null back");

            assertThrows(IllegalArgumentException.class, () -> {
                card.setBack(null);
            }, "Should throw exception when setting null back");

            logger.debug("Test passed: Exception thrown for setting null back");
        }

        /**
         * Tests that setting empty back text throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting empty back")
        void testSetEmptyBack() {
            logger.debug("Test: Setting empty back");

            assertThrows(IllegalArgumentException.class, () -> {
                card.setBack("");
            }, "Should throw exception when setting empty back");

            logger.debug("Test passed: Exception thrown for setting empty back");
        }
    }

    // ========================================
    // Editing Tests
    // ========================================

    /**
     * Tests for editing card content fields.
     */
    @Nested
    @DisplayName("Editing Tests")
    class EditingTests {

        /**
         * Tests editing the card's front text.
         */
        @Test
        @DisplayName("Should allow editing card front")
        void testEditFront() {
            logger.debug("Test: Editing card front from '{}' to '{}'", CARD_FRONT, UPDATED_FRONT);

            card.setFront(UPDATED_FRONT);

            assertEquals(UPDATED_FRONT, card.getFront());
            assertEquals(CARD_BACK, card.getBack(), "Back should remain unchanged");

            logger.debug("Test passed: Front edited successfully");
        }

        /**
         * Tests editing the card's back text.
         */
        @Test
        @DisplayName("Should allow editing card back")
        void testEditBack() {
            logger.debug("Test: Editing card back from '{}' to '{}'", CARD_BACK, UPDATED_BACK);

            card.setBack(UPDATED_BACK);

            assertEquals(CARD_FRONT, card.getFront(), "Front should remain unchanged");
            assertEquals(UPDATED_BACK, card.getBack());

            logger.debug("Test passed: Back edited successfully");
        }

        /**
         * Tests editing the card's notes.
         */
        @Test
        @DisplayName("Should allow editing card notes")
        void testEditNotes() {
            logger.debug("Test: Editing card notes");

            assertNull(card.getNotes());

            card.setNotes(CARD_NOTES);
            assertEquals(CARD_NOTES, card.getNotes());

            card.setNotes("Updated notes");
            assertEquals("Updated notes", card.getNotes());

            logger.debug("Test passed: Notes edited successfully");
        }

        /**
         * Tests clearing the card's notes by setting to null.
         */
        @Test
        @DisplayName("Should allow clearing card notes")
        void testClearNotes() {
            logger.debug("Test: Clearing card notes");

            card.setNotes(CARD_NOTES);
            assertEquals(CARD_NOTES, card.getNotes());

            card.setNotes(null);
            assertNull(card.getNotes());

            logger.debug("Test passed: Notes cleared successfully");
        }

        /**
         * Tests editing all card fields simultaneously.
         */
        @Test
        @DisplayName("Should allow editing all fields")
        void testEditAllFields() {
            logger.debug("Test: Editing all card fields");

            card.setFront(UPDATED_FRONT);
            card.setBack(UPDATED_BACK);
            card.setNotes("New notes");

            assertEquals(UPDATED_FRONT, card.getFront());
            assertEquals(UPDATED_BACK, card.getBack());
            assertEquals("New notes", card.getNotes());

            logger.debug("Test passed: All fields edited successfully");
        }
    }

    // ========================================
    // Deck Association Tests
    // ========================================

    /**
     * Tests for card-deck relationship management.
     */
    @Nested
    @DisplayName("Deck Association Tests")
    class DeckAssociationTests {

        /**
         * Tests reassigning a card to a different deck.
         */
        @Test
        @DisplayName("Should allow reassigning card to different deck")
        void testReassignDeck() {
            logger.debug("Test: Reassigning card to different deck");

            Deck newDeck = new Deck(testUser, "New Deck", "Another deck");
            newDeck.setId(2L);

            assertEquals(testDeck, card.getDeck());

            card.setDeck(newDeck);

            assertEquals(newDeck, card.getDeck());

            logger.debug("Test passed: Deck reassigned successfully");
        }

        /**
         * Tests that deck reference is maintained throughout card lifecycle.
         */
        @Test
        @DisplayName("Should maintain deck reference throughout lifecycle")
        void testDeckReferenceMaintained() {
            logger.debug("Test: Deck reference maintained");

            assertEquals(testDeck, card.getDeck());

            // Edit card properties
            card.setFront("New front");
            card.setBack("New back");
            card.setNotes("New notes");

            assertEquals(testDeck, card.getDeck(),
                    "Deck should remain after property changes");

            logger.debug("Test passed: Deck reference maintained");
        }
    }

    // ========================================
    // Tag Management Tests
    // ========================================

    /**
     * Tests for card-tag relationship management.
     */
    @Nested
    @DisplayName("Tag Management Tests")
    class TagManagementTests {

        /**
         * Tests that new cards have an empty tag collection.
         */
        @Test
        @DisplayName("Should have empty tag collection when created")
        void testNewCardHasEmptyTagCollection() {
            logger.debug("Test: New card has empty tag collection");

            assertNotNull(card.getTags());
            assertTrue(card.getTags().isEmpty());
            assertEquals(0, card.getTags().size());

            logger.debug("Test passed: Empty tag collection initialized");
        }

        /**
         * Tests adding a single tag to a card.
         */
        @Test
        @DisplayName("Should add tag to card")
        void testAddTag() {
            logger.debug("Test: Adding tag='{}' to card", TAG_NAME);

            Tag tag = new Tag(testDeck, TAG_NAME);
            tag.setId(1L);

            card.addTag(tag);

            assertTrue(card.getTags().contains(tag), "Card should contain tag");
            assertTrue(tag.getCards().contains(card), "Tag should contain card");
            assertEquals(1, card.getTags().size());

            logger.debug("Test passed: Tag added successfully");
        }

        /**
         * Tests removing a tag from a card.
         */
        @Test
        @DisplayName("Should remove tag from card")
        void testRemoveTag() {
            logger.debug("Test: Removing tag from card");

            Tag tag = new Tag(testDeck, TAG_NAME);
            tag.setId(1L);

            card.addTag(tag);
            assertTrue(card.getTags().contains(tag));

            card.removeTag(tag);

            assertFalse(card.getTags().contains(tag), "Card should not contain tag");
            assertFalse(tag.getCards().contains(card), "Tag should not contain card");
            assertEquals(0, card.getTags().size());

            logger.debug("Test passed: Tag removed successfully");
        }

        /**
         * Tests adding multiple tags to a card.
         */
        @Test
        @DisplayName("Should add multiple tags to card")
        void testAddMultipleTags() {
            logger.debug("Test: Adding multiple tags to card");

            Tag tag1 = new Tag(testDeck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(testDeck, "N5");
            tag2.setId(2L);
            Tag tag3 = new Tag(testDeck, "food");
            tag3.setId(3L);

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

            assertThrows(IllegalArgumentException.class, () -> {
                card.addTag(null);
            }, "Should throw exception for null tag");

            logger.debug("Test passed: Exception thrown for null tag");
        }

        /**
         * Tests that removing null tag throws exception.
         */
        @Test
        @DisplayName("Should throw exception when removing null tag")
        void testRemoveNullTag() {
            logger.debug("Test: Removing null tag");

            assertThrows(IllegalArgumentException.class, () -> {
                card.removeTag(null);
            }, "Should throw exception for null tag");

            logger.debug("Test passed: Exception thrown for null tag");
        }

        /**
         * Tests clearing all tags from a card.
         */
        @Test
        @DisplayName("Should clear all tags from card")
        void testClearTags() {
            logger.debug("Test: Clearing all tags from card");

            Tag tag1 = new Tag(testDeck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(testDeck, "N5");
            tag2.setId(2L);

            card.addTag(tag1);
            card.addTag(tag2);
            assertEquals(2, card.getTags().size());

            card.clearTags();

            assertEquals(0, card.getTags().size());
            assertFalse(tag1.getCards().contains(card),
                    "Tag1 should not contain card after clear");
            assertFalse(tag2.getCards().contains(card),
                    "Tag2 should not contain card after clear");

            logger.debug("Test passed: All tags cleared successfully");
        }

        /**
         * Tests that attempting to add the same tag twice doesn't create duplicates.
         */
        @Test
        @DisplayName("Should not add duplicate tags")
        void testAddSameTagTwice() {
            logger.debug("Test: Adding same tag twice");

            Tag tag = new Tag(testDeck, TAG_NAME);
            tag.setId(1L);

            card.addTag(tag);
            card.addTag(tag); // Add again

            assertEquals(1, card.getTags().size(), "Should not add duplicate tag");
            assertTrue(card.getTags().contains(tag));

            logger.debug("Test passed: Duplicate tag not added");
        }

        /**
         * Tests bidirectional consistency when adding a tag.
         */
        @Test
        @DisplayName("Should maintain bidirectional consistency when adding tag")
        void testBidirectionalConsistencyAdd() {
            logger.debug("Test: Bidirectional consistency when adding tag");

            Tag tag = new Tag(testDeck, TAG_NAME);
            tag.setId(1L);

            assertFalse(tag.getCards().contains(card), "Initially tag should not contain card");

            card.addTag(tag);

            assertTrue(card.getTags().contains(tag), "Card should contain tag");
            assertTrue(tag.getCards().contains(card), "Tag should contain card");

            logger.debug("Test passed: Bidirectional consistency maintained on add");
        }

        /**
         * Tests bidirectional consistency when removing a tag.
         */
        @Test
        @DisplayName("Should maintain bidirectional consistency when removing tag")
        void testBidirectionalConsistencyRemove() {
            logger.debug("Test: Bidirectional consistency when removing tag");

            Tag tag = new Tag(testDeck, TAG_NAME);
            tag.setId(1L);

            card.addTag(tag);
            assertTrue(card.getTags().contains(tag));
            assertTrue(tag.getCards().contains(card));

            card.removeTag(tag);

            assertFalse(card.getTags().contains(tag), "Card should not contain tag");
            assertFalse(tag.getCards().contains(card), "Tag should not contain card");

            logger.debug("Test passed: Bidirectional consistency maintained on remove");
        }
    }

    // ========================================
    // Deck-Specific Tag Tests
    // ========================================

    /**
     * Tests for deck-specific tag isolation and behavior.
     */
    @Nested
    @DisplayName("Deck-Specific Tag Tests")
    class DeckSpecificTagTests {

        /**
         * Tests that tags from different decks can have the same name.
         */
        @Test
        @DisplayName("Should support tags with same name in different decks")
        void testSameTagNameInDifferentDecks() {
            logger.debug("Test: Same tag name in different decks");

            // Create two decks with same tag name
            Deck japaneseDeck = new Deck(testUser, "Japanese", "Japanese vocab");
            japaneseDeck.setId(1L);
            Deck spanishDeck = new Deck(testUser, "Spanish", "Spanish vocab");
            spanishDeck.setId(2L);

            Tag japaneseVerbsTag = new Tag(japaneseDeck, "verbs");
            japaneseVerbsTag.setId(1L);
            Tag spanishVerbsTag = new Tag(spanishDeck, "verbs");
            spanishVerbsTag.setId(2L);

            Card japaneseCard = new Card(japaneseDeck, "食べる", "to eat");
            japaneseCard.setId(1L);
            Card spanishCard = new Card(spanishDeck, "comer", "to eat");
            spanishCard.setId(2L);

            japaneseCard.addTag(japaneseVerbsTag);
            spanishCard.addTag(spanishVerbsTag);

            // Both cards have "verbs" tag, but from different decks
            assertTrue(japaneseCard.getTags().contains(japaneseVerbsTag));
            assertFalse(japaneseCard.getTags().contains(spanishVerbsTag));

            assertTrue(spanishCard.getTags().contains(spanishVerbsTag));
            assertFalse(spanishCard.getTags().contains(japaneseVerbsTag));

            logger.debug("Test passed: Deck-specific tags work correctly");
        }

        /**
         * Tests that tags must belong to the same deck as the card.
         */
        @Test
        @DisplayName("Should verify tag deck matches card deck")
        void testTagDeckMatchesCardDeck() {
            logger.debug("Test: Tag deck matches card deck");

            Tag tag = new Tag(testDeck, TAG_NAME);
            tag.setId(1L);

            card.addTag(tag);

            assertEquals(testDeck, card.getDeck());
            assertEquals(testDeck, tag.getDeck());

            logger.debug("Test passed: Tag and card belong to same deck");
        }

        /**
         * Tests that a tag from one deck can be used with multiple cards in that deck.
         */
        @Test
        @DisplayName("Should allow same tag on multiple cards in same deck")
        void testSameTagOnMultipleCards() {
            logger.debug("Test: Same tag on multiple cards");

            Tag verbsTag = new Tag(testDeck, "verbs");
            verbsTag.setId(1L);

            Card card1 = new Card(testDeck, "食べる", "to eat");
            card1.setId(1L);
            Card card2 = new Card(testDeck, "飲む", "to drink");
            card2.setId(2L);
            Card card3 = new Card(testDeck, "行く", "to go");
            card3.setId(3L);

            card1.addTag(verbsTag);
            card2.addTag(verbsTag);
            card3.addTag(verbsTag);

            assertEquals(3, verbsTag.getCards().size());
            assertTrue(verbsTag.getCards().contains(card1));
            assertTrue(verbsTag.getCards().contains(card2));
            assertTrue(verbsTag.getCards().contains(card3));

            logger.debug("Test passed: Tag shared across multiple cards");
        }
    }

    // ========================================
    // Equality and HashCode Tests
    // ========================================

    /**
     * Tests for equals() and hashCode() implementation.
     */
    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        /**
         * Tests that cards with the same ID are equal.
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

            assertEquals(card1, card2, "Same ID should be equal");
            assertNotEquals(card1, card3, "Different ID should not be equal");

            logger.debug("Test passed: Equality based on ID works correctly");
        }

        /**
         * Tests reflexive property of equals (x.equals(x) is true).
         */
        @Test
        @DisplayName("Should be equal to itself")
        void testEqualityReflexive() {
            logger.debug("Test: Testing reflexive equality");

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

            assertNotEquals(null, card);

            logger.debug("Test passed: Card not equal to null");
        }

        /**
         * Tests that card is not equal to an object of different class.
         */
        @Test
        @DisplayName("Should not be equal to different class")
        void testEqualityDifferentClass() {
            logger.debug("Test: Testing equality with different class");

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

            assertEquals(card1, card1, "Same instance should equal itself");
            assertNotEquals(card1, card2,
                    "Different instances without IDs should not be equal");

            logger.debug("Test passed: Cards without IDs handled correctly");
        }

        /**
         * Tests hash code consistency with equals.
         */
        @Test
        @DisplayName("Should have consistent hash code with equals")
        void testHashCodeConsistency() {
            logger.debug("Test: Testing hash code consistency");

            Card card1 = new Card(testDeck, CARD_FRONT, CARD_BACK);
            card1.setId(1L);

            Card card2 = new Card(testDeck, "Different", "Content");
            card2.setId(1L);

            assertEquals(card1.hashCode(), card2.hashCode(),
                    "Equal cards should have equal hash codes");

            logger.debug("Test passed: Hash code consistent with equals");
        }
    }

    // ========================================
    // toString Tests
    // ========================================

    /**
     * Tests for toString() method output.
     */
    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        /**
         * Tests that toString includes card content.
         */
        @Test
        @DisplayName("Should include card content in toString")
        void testToStringIncludesContent() {
            logger.debug("Test: Testing toString includes content");

            card.setNotes(CARD_NOTES);
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
            sensitiveUser.setId(1L);
            Deck sensitiveDeck = new Deck(sensitiveUser, "My Deck", "Description");
            sensitiveDeck.setId(1L);
            Card sensitiveCard = new Card(sensitiveDeck, CARD_FRONT, CARD_BACK);
            sensitiveCard.setId(1L);

            String cardString = sensitiveCard.toString();

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

            Tag tag1 = new Tag(testDeck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(testDeck, "N5");
            tag2.setId(2L);

            card.addTag(tag1);
            card.addTag(tag2);

            String cardString = card.toString();

            assertTrue(cardString.contains("tagCount=2"));

            logger.debug("Test passed: toString includes tag count");
        }
    }

    // ========================================
    // Getter/Setter Tests
    // ========================================

    /**
     * Tests for getter and setter methods.
     */
    @Nested
    @DisplayName("Getter/Setter Tests")
    class GetterSetterTests {

        /**
         * Tests version field getter and setter.
         */
        @Test
        @DisplayName("Should get and set version")
        void testVersionGetterSetter() {
            logger.debug("Test: Testing version getter and setter");

            assertNull(card.getVersion());

            card.setVersion(1L);
            assertEquals(1L, card.getVersion());

            card.setVersion(2L);
            assertEquals(2L, card.getVersion());

            logger.debug("Test passed: Version getter and setter work");
        }

        /**
         * Tests ID field getter and setter.
         */
        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            logger.debug("Test: Testing ID getter and setter");

            assertEquals(1L, card.getId());

            card.setId(100L);
            assertEquals(100L, card.getId());

            logger.debug("Test passed: ID getter and setter work");
        }
    }

    // ========================================
    // Integration Tests
    // ========================================

    /**
     * Tests for complex integration scenarios.
     */
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        /**
         * Tests complete card lifecycle with tags and deck changes.
         */
        @Test
        @DisplayName("Should handle complete card lifecycle")
        void testCompleteCardLifecycle() {
            logger.debug("Test: Complete card lifecycle");

            // Create card
            Card newCard = new Card(testDeck, "Initial Front", "Initial Back");
            newCard.setId(10L);

            // Add notes
            newCard.setNotes("Initial notes");

            // Add multiple tags
            Tag tag1 = new Tag(testDeck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(testDeck, "N5");
            tag2.setId(2L);
            Tag tag3 = new Tag(testDeck, "important");
            tag3.setId(3L);

            newCard.addTag(tag1);
            newCard.addTag(tag2);
            newCard.addTag(tag3);

            assertEquals(3, newCard.getTags().size());

            // Modify content
            newCard.setFront("Updated Front");
            newCard.setBack("Updated Back");
            newCard.setNotes("Updated notes");

            // Remove one tag
            newCard.removeTag(tag2);
            assertEquals(2, newCard.getTags().size());

            // Change deck
            Deck newDeck = new Deck(testUser, "New Deck", "Description");
            newDeck.setId(2L);
            newCard.setDeck(newDeck);

            assertEquals(newDeck, newCard.getDeck());
            assertEquals("Updated Front", newCard.getFront());
            assertEquals("Updated Back", newCard.getBack());

            logger.debug("Test passed: Complete lifecycle handled");
        }

        /**
         * Tests card with multiple tags shared across cards.
         */
        @Test
        @DisplayName("Should handle shared tags across multiple cards")
        void testSharedTagsAcrossCards() {
            logger.debug("Test: Shared tags across multiple cards");

            Tag verbsTag = new Tag(testDeck, "verbs");
            verbsTag.setId(1L);
            Tag n5Tag = new Tag(testDeck, "N5");
            n5Tag.setId(2L);

            Card card1 = new Card(testDeck, "食べる", "to eat");
            card1.setId(1L);
            Card card2 = new Card(testDeck, "飲む", "to drink");
            card2.setId(2L);
            Card card3 = new Card(testDeck, "見る", "to see");
            card3.setId(3L);

            // All cards share "verbs" tag
            card1.addTag(verbsTag);
            card1.addTag(n5Tag);
            card2.addTag(verbsTag);
            card2.addTag(n5Tag);
            card3.addTag(verbsTag);

            assertEquals(3, verbsTag.getCards().size());
            assertEquals(2, n5Tag.getCards().size());

            // Remove tag from one card
            card2.removeTag(verbsTag);
            assertEquals(2, verbsTag.getCards().size());
            assertFalse(card2.getTags().contains(verbsTag));

            logger.debug("Test passed: Shared tags handled correctly");
        }

        /**
         * Tests complex scenario with multiple decks and overlapping tag names.
         */
        @Test
        @DisplayName("Should handle complex multi-deck scenario")
        void testComplexMultiDeckScenario() {
            logger.debug("Test: Complex multi-deck scenario");

            // Create two decks
            Deck japaneseDeck = new Deck(testUser, "Japanese", "Japanese vocab");
            japaneseDeck.setId(1L);
            Deck spanishDeck = new Deck(testUser, "Spanish", "Spanish vocab");
            spanishDeck.setId(2L);

            // Create "verbs" tag for each deck
            Tag japaneseVerbs = new Tag(japaneseDeck, "verbs");
            japaneseVerbs.setId(1L);
            Tag spanishVerbs = new Tag(spanishDeck, "verbs");
            spanishVerbs.setId(2L);

            // Create cards
            Card japCard1 = new Card(japaneseDeck, "食べる", "to eat");
            japCard1.setId(1L);
            Card japCard2 = new Card(japaneseDeck, "飲む", "to drink");
            japCard2.setId(2L);
            Card spaCard = new Card(spanishDeck, "comer", "to eat");
            spaCard.setId(3L);

            // Add tags
            japCard1.addTag(japaneseVerbs);
            japCard2.addTag(japaneseVerbs);
            spaCard.addTag(spanishVerbs);

            // Verify isolation
            assertEquals(2, japaneseVerbs.getCards().size());
            assertEquals(1, spanishVerbs.getCards().size());
            assertNotEquals(japaneseVerbs, spanishVerbs);

            // Verify cards only have their deck's tags
            assertTrue(japCard1.getTags().contains(japaneseVerbs));
            assertFalse(japCard1.getTags().contains(spanishVerbs));

            logger.debug("Test passed: Complex multi-deck scenario handled");
        }
    }
}