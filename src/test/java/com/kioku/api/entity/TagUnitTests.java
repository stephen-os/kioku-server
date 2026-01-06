package com.kioku.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Tag entity.
 *
 * <p>Enhanced tests verify:
 * <ul>
 *   <li>Tag creation with deck association</li>
 *   <li>Field validation (deck, name cannot be null/empty)</li>
 *   <li>User inheritance from deck</li>
 *   <li>Editing tag properties (name, deck)</li>
 *   <li>Input trimming (whitespace removal)</li>
 *   <li>Deck-specific tag isolation</li>
 *   <li>Card associations and bidirectional relationships</li>
 *   <li>Equality and hash code based on ID and name</li>
 *   <li>toString includes deck context</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 2.0
 * @since 1.0
 */
@DisplayName("Tag Entity Comprehensive Tests")
class TagUnitTests {

    private static final Logger logger = LoggerFactory.getLogger(TagUnitTests.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String DECK_NAME = "Japanese Vocabulary";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary";
    private static final String TAG_NAME = "verbs";
    private static final String UPDATED_TAG_NAME = "nouns";

    private User testUser;
    private Deck testDeck;
    private Tag tag;

    /**
     * Sets up test fixtures before each test.
     * Creates a test user, deck, and tag for testing.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up Tag test: Creating user, deck, and tag");
        testUser = new User(TEST_EMAIL, PASSWORD_HASH);
        testUser.setId(1L);
        testDeck = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
        testDeck.setId(1L);
        tag = new Tag(testDeck, TAG_NAME);
        tag.setId(1L);
    }

    // ========================================
    // Constructor Tests
    // ========================================

    /**
     * Tests for the Tag constructor and initialization.
     */
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        /**
         * Tests the Tag constructor with deck and name.
         */
        @Test
        @DisplayName("Should create tag with deck")
        void testTagCreation() {
            logger.debug("Test: Creating tag with name='{}'", TAG_NAME);

            Tag newTag = new Tag(testDeck, TAG_NAME);

            assertEquals(testDeck, newTag.getDeck());
            assertEquals(testUser, newTag.getUser(), "User should be inherited from deck");
            assertEquals(TAG_NAME, newTag.getName());
            assertNull(newTag.getId());
            assertNotNull(newTag.getCards(), "Cards collection should be initialized");
            assertTrue(newTag.getCards().isEmpty(), "Cards should be empty");

            logger.debug("Test passed: Tag created successfully");
        }

        /**
         * Tests that user is correctly inherited from deck on creation.
         */
        @Test
        @DisplayName("Should inherit user from deck")
        void testUserInheritedFromDeck() {
            logger.debug("Test: User inheritance from deck");

            Tag newTag = new Tag(testDeck, TAG_NAME);

            assertEquals(testDeck.getUser(), newTag.getUser());
            assertEquals(testUser, newTag.getUser());

            logger.debug("Test passed: User inherited correctly");
        }

        /**
         * Tests the no-args constructor creates a valid tag.
         */
        @Test
        @DisplayName("Should create tag with no-args constructor")
        void testNoArgsConstructor() {
            logger.debug("Test: Creating tag with no-args constructor");

            Tag emptyTag = new Tag();

            assertNotNull(emptyTag);
            assertNull(emptyTag.getDeck());
            assertNull(emptyTag.getUser());
            assertNull(emptyTag.getName());
            assertNotNull(emptyTag.getCards());

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
         * Tests that creating a tag with null deck throws exception.
         */
        @Test
        @DisplayName("Should throw exception when deck is null")
        void testNullDeck() {
            logger.debug("Test: Creating tag with null deck");

            assertThrows(IllegalArgumentException.class, () -> {
                new Tag(null, TAG_NAME);
            }, "Should throw exception for null deck");

            logger.debug("Test passed: Exception thrown for null deck");
        }

        /**
         * Tests that creating a tag with null name throws exception.
         */
        @Test
        @DisplayName("Should throw exception when name is null")
        void testNullName() {
            logger.debug("Test: Creating tag with null name");

            assertThrows(IllegalArgumentException.class, () -> {
                new Tag(testDeck, null);
            }, "Should throw exception for null name");

            logger.debug("Test passed: Exception thrown for null name");
        }

        /**
         * Tests that creating a tag with empty name throws exception.
         */
        @Test
        @DisplayName("Should throw exception when name is empty")
        void testEmptyName() {
            logger.debug("Test: Creating tag with empty name");

            assertThrows(IllegalArgumentException.class, () -> {
                new Tag(testDeck, "");
            }, "Should throw exception for empty name");

            logger.debug("Test passed: Exception thrown for empty name");
        }

        /**
         * Tests that creating a tag with whitespace-only name throws exception.
         */
        @Test
        @DisplayName("Should throw exception when name is whitespace only")
        void testWhitespaceName() {
            logger.debug("Test: Creating tag with whitespace-only name");

            assertThrows(IllegalArgumentException.class, () -> {
                new Tag(testDeck, "   ");
            }, "Should throw exception for whitespace name");

            logger.debug("Test passed: Exception thrown for whitespace-only name");
        }

        /**
         * Tests that setting null deck throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting null deck")
        void testSetNullDeck() {
            logger.debug("Test: Setting null deck");

            assertThrows(IllegalArgumentException.class, () -> {
                tag.setDeck(null);
            }, "Should throw exception when setting null deck");

            logger.debug("Test passed: Exception thrown for setting null deck");
        }

        /**
         * Tests that setting null user throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting null user")
        void testSetNullUser() {
            logger.debug("Test: Setting null user");

            assertThrows(IllegalArgumentException.class, () -> {
                tag.setUser(null);
            }, "Should throw exception when setting null user");

            logger.debug("Test passed: Exception thrown for setting null user");
        }

        /**
         * Tests that setting null name throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting null name")
        void testSetNullName() {
            logger.debug("Test: Setting null name");

            assertThrows(IllegalArgumentException.class, () -> {
                tag.setName(null);
            }, "Should throw exception when setting null name");

            logger.debug("Test passed: Exception thrown for setting null name");
        }

        /**
         * Tests that setting empty name throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting empty name")
        void testSetEmptyName() {
            logger.debug("Test: Setting empty name");

            assertThrows(IllegalArgumentException.class, () -> {
                tag.setName("");
            }, "Should throw exception when setting empty name");

            logger.debug("Test passed: Exception thrown for setting empty name");
        }

        /**
         * Tests that setting whitespace-only name throws exception.
         */
        @Test
        @DisplayName("Should throw exception when setting whitespace name")
        void testSetWhitespaceName() {
            logger.debug("Test: Setting whitespace-only name");

            assertThrows(IllegalArgumentException.class, () -> {
                tag.setName("   ");
            }, "Should throw exception when setting whitespace name");

            logger.debug("Test passed: Exception thrown for setting whitespace name");
        }
    }

    // ========================================
    // Input Trimming Tests
    // ========================================

    /**
     * Tests for input trimming and whitespace handling.
     */
    @Nested
    @DisplayName("Input Trimming Tests")
    class InputTrimmingTests {

        /**
         * Tests that name is trimmed in constructor.
         */
        @Test
        @DisplayName("Should trim name in constructor")
        void testNameTrimmedInConstructor() {
            logger.debug("Test: Name trimming in constructor");

            Tag trimmedTag = new Tag(testDeck, "  " + TAG_NAME + "  ");

            assertEquals(TAG_NAME, trimmedTag.getName());

            logger.debug("Test passed: Name trimmed successfully");
        }

        /**
         * Tests that name is trimmed in setter.
         */
        @Test
        @DisplayName("Should trim name in setter")
        void testNameTrimmedInSetter() {
            logger.debug("Test: Name trimming in setter");

            tag.setName("  " + UPDATED_TAG_NAME + "  ");

            assertEquals(UPDATED_TAG_NAME, tag.getName());

            logger.debug("Test passed: Name trimmed in setter");
        }
    }

    // ========================================
    // Editing Tests
    // ========================================

    /**
     * Tests for editing tag properties.
     */
    @Nested
    @DisplayName("Editing Tests")
    class EditingTests {

        /**
         * Tests editing tag name.
         */
        @Test
        @DisplayName("Should allow editing tag name")
        void testEditName() {
            logger.debug("Test: Editing tag name from '{}' to '{}'", TAG_NAME, UPDATED_TAG_NAME);

            tag.setName(UPDATED_TAG_NAME);

            assertEquals(UPDATED_TAG_NAME, tag.getName());

            logger.debug("Test passed: Name edited successfully");
        }

        /**
         * Tests changing tag's deck.
         */
        @Test
        @DisplayName("Should allow changing tag's deck")
        void testChangeDeck() {
            logger.debug("Test: Changing tag's deck");

            Deck newDeck = new Deck(testUser, "New Deck", "Description");
            newDeck.setId(2L);

            assertEquals(testDeck, tag.getDeck());

            tag.setDeck(newDeck);

            assertEquals(newDeck, tag.getDeck());

            logger.debug("Test passed: Deck changed successfully");
        }

        /**
         * Tests changing tag's user.
         */
        @Test
        @DisplayName("Should allow changing tag's user")
        void testChangeUser() {
            logger.debug("Test: Changing tag's user");

            User newUser = new User("other@example.com", PASSWORD_HASH);
            newUser.setId(2L);

            assertEquals(testUser, tag.getUser());

            tag.setUser(newUser);

            assertEquals(newUser, tag.getUser());

            logger.debug("Test passed: User changed successfully");
        }
    }

    // ========================================
    // User Inheritance Tests
    // ========================================

    /**
     * Tests for user inheritance from deck.
     */
    @Nested
    @DisplayName("User Inheritance Tests")
    class UserInheritanceTests {

        /**
         * Tests that user is inherited from deck at creation.
         */
        @Test
        @DisplayName("Should inherit user from deck at creation")
        void testUserInheritanceAtCreation() {
            logger.debug("Test: User inheritance at creation");

            User specificUser = new User("specific@example.com", PASSWORD_HASH);
            specificUser.setId(5L);
            Deck specificDeck = new Deck(specificUser, "Deck", "Description");
            specificDeck.setId(10L);

            Tag newTag = new Tag(specificDeck, "tag");

            assertEquals(specificUser, newTag.getUser(),
                    "Tag should inherit user from deck");
            assertEquals(specificDeck.getUser(), newTag.getUser());

            logger.debug("Test passed: User inherited at creation");
        }

        /**
         * Tests that user remains consistent throughout tag lifecycle.
         */
        @Test
        @DisplayName("Should maintain user consistency throughout lifecycle")
        void testUserConsistencyThroughoutLifecycle() {
            logger.debug("Test: User consistency throughout lifecycle");

            assertEquals(testUser, tag.getUser());

            // Edit tag name
            tag.setName("new-name");
            assertEquals(testUser, tag.getUser(),
                    "User should remain after name change");

            logger.debug("Test passed: User consistency maintained");
        }

        /**
         * Tests that changing deck requires manual user update.
         */
        @Test
        @DisplayName("Should require manual user update when changing deck")
        void testManualUserUpdateWhenChangingDeck() {
            logger.debug("Test: Manual user update when changing deck");

            User otherUser = new User("other@example.com", PASSWORD_HASH);
            otherUser.setId(2L);
            Deck otherDeck = new Deck(otherUser, "Other Deck", "Description");
            otherDeck.setId(2L);

            assertEquals(testUser, tag.getUser());

            // Change deck but not user
            tag.setDeck(otherDeck);

            assertEquals(otherDeck, tag.getDeck());
            assertEquals(testUser, tag.getUser(),
                    "User doesn't auto-update when deck changes");

            // Must manually update user to match new deck
            tag.setUser(otherUser);
            assertEquals(otherUser, tag.getUser());

            logger.debug("Test passed: Manual user update required");
        }
    }

    // ========================================
    // Deck Isolation Tests
    // ========================================

    /**
     * Tests for deck-specific tag isolation.
     */
    @Nested
    @DisplayName("Deck Isolation Tests")
    class DeckIsolationTests {

        /**
         * Tests that tags with same name in different decks are different entities.
         */
        @Test
        @DisplayName("Should allow same tag name in different decks")
        void testSameNameDifferentDecks() {
            logger.debug("Test: Same tag name in different decks");

            Deck deck1 = new Deck(testUser, "Japanese", "Description");
            deck1.setId(1L);
            Deck deck2 = new Deck(testUser, "Spanish", "Description");
            deck2.setId(2L);

            Tag tag1 = new Tag(deck1, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(deck2, "verbs");
            tag2.setId(2L);

            assertEquals("verbs", tag1.getName());
            assertEquals("verbs", tag2.getName());
            assertNotEquals(tag1.getDeck(), tag2.getDeck());
            assertNotEquals(tag1, tag2, "Tags should be different entities");

            logger.debug("Test passed: Same name allowed in different decks");
        }

        /**
         * Tests that tags are isolated to their decks.
         */
        @Test
        @DisplayName("Should isolate tags to their decks")
        void testTagIsolationToDecks() {
            logger.debug("Test: Tag isolation to decks");

            Deck japaneseDeck = new Deck(testUser, "Japanese", "Description");
            japaneseDeck.setId(1L);
            Deck spanishDeck = new Deck(testUser, "Spanish", "Description");
            spanishDeck.setId(2L);

            Tag japaneseTag = new Tag(japaneseDeck, "vocabulary");
            japaneseTag.setId(1L);
            Tag spanishTag = new Tag(spanishDeck, "vocabulary");
            spanishTag.setId(2L);

            // Create cards
            Card japaneseCard = new Card(japaneseDeck, "食べる", "to eat");
            japaneseCard.setId(1L);
            Card spanishCard = new Card(spanishDeck, "comer", "to eat");
            spanishCard.setId(2L);

            // Add deck-specific tags
            japaneseCard.addTag(japaneseTag);
            spanishCard.addTag(spanishTag);

            // Verify isolation
            assertTrue(japaneseTag.getCards().contains(japaneseCard));
            assertFalse(japaneseTag.getCards().contains(spanishCard));

            assertTrue(spanishTag.getCards().contains(spanishCard));
            assertFalse(spanishTag.getCards().contains(japaneseCard));

            logger.debug("Test passed: Tags isolated to their decks");
        }
    }

    // ========================================
    // Card Association Tests
    // ========================================

    /**
     * Tests for tag-card bidirectional relationships.
     */
    @Nested
    @DisplayName("Card Association Tests")
    class CardAssociationTests {

        /**
         * Tests that new tags have empty card collection.
         */
        @Test
        @DisplayName("Should have empty card collection when created")
        void testNewTagHasEmptyCardCollection() {
            logger.debug("Test: New tag has empty card collection");

            assertNotNull(tag.getCards());
            assertTrue(tag.getCards().isEmpty());
            assertEquals(0, tag.getCards().size());

            logger.debug("Test passed: Empty card collection initialized");
        }

        /**
         * Tests bidirectional relationship when card adds tag.
         */
        @Test
        @DisplayName("Should maintain bidirectional relationship with card")
        void testBidirectionalRelationshipWithCard() {
            logger.debug("Test: Bidirectional relationship with card");

            Card card = new Card(testDeck, "Front", "Back");
            card.setId(1L);

            assertFalse(tag.getCards().contains(card));

            card.addTag(tag);

            assertTrue(tag.getCards().contains(card), "Tag should contain card");
            assertTrue(card.getTags().contains(tag), "Card should contain tag");

            logger.debug("Test passed: Bidirectional relationship maintained");
        }

        /**
         * Tests that tag can be associated with multiple cards.
         */
        @Test
        @DisplayName("Should allow tag on multiple cards")
        void testTagOnMultipleCards() {
            logger.debug("Test: Tag on multiple cards");

            Card card1 = new Card(testDeck, "Front1", "Back1");
            card1.setId(1L);
            Card card2 = new Card(testDeck, "Front2", "Back2");
            card2.setId(2L);
            Card card3 = new Card(testDeck, "Front3", "Back3");
            card3.setId(3L);

            card1.addTag(tag);
            card2.addTag(tag);
            card3.addTag(tag);

            assertEquals(3, tag.getCards().size());
            assertTrue(tag.getCards().contains(card1));
            assertTrue(tag.getCards().contains(card2));
            assertTrue(tag.getCards().contains(card3));

            logger.debug("Test passed: Tag associated with multiple cards");
        }

        /**
         * Tests removing tag from card updates bidirectional relationship.
         */
        @Test
        @DisplayName("Should update relationship when tag removed from card")
        void testRelationshipWhenTagRemovedFromCard() {
            logger.debug("Test: Relationship when tag removed from card");

            Card card = new Card(testDeck, "Front", "Back");
            card.setId(1L);

            card.addTag(tag);
            assertTrue(tag.getCards().contains(card));

            card.removeTag(tag);

            assertFalse(tag.getCards().contains(card), "Tag should not contain card");
            assertFalse(card.getTags().contains(tag), "Card should not contain tag");

            logger.debug("Test passed: Relationship updated on removal");
        }

        /**
         * Tests setting cards collection directly.
         */
        @Test
        @DisplayName("Should allow setting cards collection")
        void testSetCardsCollection() {
            logger.debug("Test: Setting cards collection");

            Card card1 = new Card(testDeck, "Front1", "Back1");
            card1.setId(1L);
            Card card2 = new Card(testDeck, "Front2", "Back2");
            card2.setId(2L);

            java.util.Set<Card> cardSet = new java.util.HashSet<>();
            cardSet.add(card1);
            cardSet.add(card2);

            tag.setCards(cardSet);

            assertEquals(2, tag.getCards().size());
            assertTrue(tag.getCards().contains(card1));
            assertTrue(tag.getCards().contains(card2));

            logger.debug("Test passed: Cards collection set successfully");
        }

        /**
         * Tests setting null cards collection initializes empty set.
         */
        @Test
        @DisplayName("Should initialize empty set when setting null cards")
        void testSetNullCardsInitializesEmptySet() {
            logger.debug("Test: Setting null cards initializes empty set");

            tag.setCards(null);

            assertNotNull(tag.getCards(), "Cards should not be null");
            assertTrue(tag.getCards().isEmpty(), "Cards should be empty");

            logger.debug("Test passed: Null cards handled with empty set");
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
         * Tests that tags with same ID and name are equal.
         */
        @Test
        @DisplayName("Should consider tags equal with same ID and name")
        void testTagEquality() {
            logger.debug("Test: Testing tag equality");

            Tag tag1 = new Tag(testDeck, TAG_NAME);
            tag1.setId(1L);

            Tag tag2 = new Tag(testDeck, TAG_NAME);
            tag2.setId(1L);

            Tag tag3 = new Tag(testDeck, TAG_NAME);
            tag3.setId(2L);

            assertEquals(tag1, tag2, "Same ID and name should be equal");
            assertNotEquals(tag1, tag3, "Different ID should not be equal");

            logger.debug("Test passed: Equality based on ID and name works correctly");
        }

        /**
         * Tests reflexive property of equals (x.equals(x) is true).
         */
        @Test
        @DisplayName("Should be equal to itself")
        void testEqualityReflexive() {
            logger.debug("Test: Testing reflexive equality");

            assertEquals(tag, tag);

            logger.debug("Test passed: Tag equals itself");
        }

        /**
         * Tests that tag is not equal to null.
         */
        @Test
        @DisplayName("Should not be equal to null")
        void testEqualityNull() {
            logger.debug("Test: Testing equality with null");

            assertNotEquals(null, tag);

            logger.debug("Test passed: Tag not equal to null");
        }

        /**
         * Tests that tag is not equal to an object of different class.
         */
        @Test
        @DisplayName("Should not be equal to different class")
        void testEqualityDifferentClass() {
            logger.debug("Test: Testing equality with different class");

            assertNotEquals(tag, "Not a Tag");

            logger.debug("Test passed: Tag not equal to different class");
        }

        /**
         * Tests that tags without IDs are only equal to themselves.
         */
        @Test
        @DisplayName("Should handle equality for tags without IDs")
        void testEqualityWithoutIds() {
            logger.debug("Test: Testing equality for tags without IDs");

            Tag tag1 = new Tag(testDeck, TAG_NAME);
            Tag tag2 = new Tag(testDeck, TAG_NAME);

            assertEquals(tag1, tag1, "Same instance should equal itself");
            assertNotEquals(tag1, tag2,
                    "Different instances without IDs should not be equal");

            logger.debug("Test passed: Tags without IDs handled correctly");
        }

        /**
         * Tests hash code consistency with equals.
         */
        @Test
        @DisplayName("Should have consistent hash code with equals")
        void testHashCodeConsistency() {
            logger.debug("Test: Testing hash code consistency");

            Tag tag1 = new Tag(testDeck, TAG_NAME);
            tag1.setId(1L);

            Tag tag2 = new Tag(testDeck, TAG_NAME);
            tag2.setId(1L);

            assertEquals(tag1.hashCode(), tag2.hashCode(),
                    "Equal tags should have equal hash codes");

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
         * Tests that toString includes tag details.
         */
        @Test
        @DisplayName("Should include tag details in toString")
        void testToStringIncludesDetails() {
            logger.debug("Test: Testing toString includes details");

            String tagString = tag.toString();

            assertTrue(tagString.contains(TAG_NAME));
            assertTrue(tagString.contains("id=1"));
            assertTrue(tagString.contains("deckId=1"));

            logger.debug("Test passed: toString includes tag details");
        }

        /**
         * Tests that toString includes deck ID for context.
         */
        @Test
        @DisplayName("Should include deck ID in toString")
        void testToStringIncludesDeckId() {
            logger.debug("Test: Testing toString includes deck ID");

            String tagString = tag.toString();

            assertTrue(tagString.contains("deckId=1"));

            logger.debug("Test passed: toString includes deck ID");
        }

        /**
         * Tests that toString includes card count.
         */
        @Test
        @DisplayName("Should include card count in toString")
        void testToStringIncludesCardCount() {
            logger.debug("Test: Testing toString includes card count");

            String tagString = tag.toString();

            assertTrue(tagString.contains("cardCount=0"));

            logger.debug("Test passed: toString includes card count");
        }

        /**
         * Tests that toString updates card count dynamically.
         */
        @Test
        @DisplayName("Should update card count in toString dynamically")
        void testToStringCardCountUpdates() {
            logger.debug("Test: toString card count updates");

            Card card1 = new Card(testDeck, "Front1", "Back1");
            card1.setId(1L);
            Card card2 = new Card(testDeck, "Front2", "Back2");
            card2.setId(2L);

            card1.addTag(tag);
            card2.addTag(tag);

            String tagString = tag.toString();

            assertTrue(tagString.contains("cardCount=2"));

            logger.debug("Test passed: Card count updates in toString");
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
         * Tests ID field getter and setter.
         */
        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            logger.debug("Test: Testing ID getter and setter");

            assertEquals(1L, tag.getId());

            tag.setId(100L);
            assertEquals(100L, tag.getId());

            logger.debug("Test passed: ID getter and setter work");
        }

        /**
         * Tests cards field getter.
         */
        @Test
        @DisplayName("Should get cards collection")
        void testCardsGetter() {
            logger.debug("Test: Testing cards getter");

            assertNotNull(tag.getCards());
            assertTrue(tag.getCards().isEmpty());

            logger.debug("Test passed: Cards getter works");
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
         * Tests complete tag lifecycle with multiple cards.
         */
        @Test
        @DisplayName("Should handle complete tag lifecycle")
        void testCompleteTagLifecycle() {
            logger.debug("Test: Complete tag lifecycle");

            // Create tag
            Tag newTag = new Tag(testDeck, "lifecycle-tag");
            newTag.setId(10L);

            assertEquals(testUser, newTag.getUser());
            assertEquals(testDeck, newTag.getDeck());

            // Add to multiple cards
            Card card1 = new Card(testDeck, "Front1", "Back1");
            card1.setId(1L);
            Card card2 = new Card(testDeck, "Front2", "Back2");
            card2.setId(2L);
            Card card3 = new Card(testDeck, "Front3", "Back3");
            card3.setId(3L);

            card1.addTag(newTag);
            card2.addTag(newTag);
            card3.addTag(newTag);

            assertEquals(3, newTag.getCards().size());

            // Rename tag
            newTag.setName("updated-tag");
            assertEquals("updated-tag", newTag.getName());

            // Remove from one card
            card2.removeTag(newTag);
            assertEquals(2, newTag.getCards().size());

            // Change deck
            Deck newDeck = new Deck(testUser, "New Deck", "Description");
            newDeck.setId(2L);
            newTag.setDeck(newDeck);

            assertEquals(newDeck, newTag.getDeck());

            logger.debug("Test passed: Complete lifecycle handled");
        }

        /**
         * Tests tag shared across many cards.
         */
        @Test
        @DisplayName("Should handle tag shared across many cards")
        void testTagSharedAcrossManyCards() {
            logger.debug("Test: Tag shared across many cards");

            Tag sharedTag = new Tag(testDeck, "shared");
            sharedTag.setId(5L);

            // Create 10 cards with the shared tag
            for (int i = 0; i < 10; i++) {
                Card card = new Card(testDeck, "Front" + i, "Back" + i);
                card.setId((long) i);
                card.addTag(sharedTag);
            }

            assertEquals(10, sharedTag.getCards().size());

            logger.debug("Test passed: Tag shared across many cards");
        }

        /**
         * Tests complex multi-deck scenario with overlapping tag names.
         */
        @Test
        @DisplayName("Should handle complex multi-deck scenario")
        void testComplexMultiDeckScenario() {
            logger.debug("Test: Complex multi-deck scenario");

            // Create three decks
            Deck deck1 = new Deck(testUser, "Deck1", "Description");
            deck1.setId(1L);
            Deck deck2 = new Deck(testUser, "Deck2", "Description");
            deck2.setId(2L);
            Deck deck3 = new Deck(testUser, "Deck3", "Description");
            deck3.setId(3L);

            // Create "important" tag for each deck
            Tag tag1 = new Tag(deck1, "important");
            tag1.setId(1L);
            Tag tag2 = new Tag(deck2, "important");
            tag2.setId(2L);
            Tag tag3 = new Tag(deck3, "important");
            tag3.setId(3L);

            // Create cards in each deck
            Card card1 = new Card(deck1, "Front1", "Back1");
            card1.setId(1L);
            Card card2 = new Card(deck2, "Front2", "Back2");
            card2.setId(2L);
            Card card3 = new Card(deck3, "Front3", "Back3");
            card3.setId(3L);

            // Add deck-specific tags
            card1.addTag(tag1);
            card2.addTag(tag2);
            card3.addTag(tag3);

            // Verify complete isolation
            assertEquals(1, tag1.getCards().size());
            assertEquals(1, tag2.getCards().size());
            assertEquals(1, tag3.getCards().size());

            assertTrue(tag1.getCards().contains(card1));
            assertFalse(tag1.getCards().contains(card2));
            assertFalse(tag1.getCards().contains(card3));

            assertNotEquals(tag1, tag2);
            assertNotEquals(tag2, tag3);
            assertNotEquals(tag1, tag3);

            logger.debug("Test passed: Complex multi-deck scenario handled");
        }
    }
}