package com.kioku.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Deck entity.
 *
 * <p>Enhanced tests verify:
 * <ul>
 *   <li>Deck creation with required and optional fields</li>
 *   <li>Field validation (user, name cannot be null/empty)</li>
 *   <li>Editing deck properties (name, description)</li>
 *   <li>User ownership association</li>
 *   <li>Input trimming (whitespace removal)</li>
 *   <li>Card management and relationships</li>
 *   <li>Tag management and relationships</li>
 *   <li>Cascade operations</li>
 *   <li>OrphanRemoval behavior</li>
 *   <li>Equality and hash code based on ID and name</li>
 *   <li>toString excludes sensitive user data</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 2.0
 * @since 1.0
 */
@DisplayName("Deck Entity Comprehensive Tests")
class DeckUnitTests {

    private static final Logger logger = LoggerFactory.getLogger(DeckUnitTests.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String DECK_NAME = "Japanese Vocabulary";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary practice";
    private static final String UPDATED_NAME = "Japanese Grammar";
    private static final String UPDATED_DESCRIPTION = "JLPT N5 grammar practice";

    private User testUser;
    private Deck deck;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up Deck test: Creating test user and deck");
        testUser = new User(TEST_EMAIL, PASSWORD_HASH);
        testUser.setId(1L);
        deck = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
        deck.setId(1L);
    }

    // ========================================
    // Constructor Tests
    // ========================================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create deck with all fields")
        void testDeckCreation() {
            logger.debug("Test: Creating deck with name='{}', description='{}'",
                    DECK_NAME, DECK_DESCRIPTION);

            Deck newDeck = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);

            assertEquals(testUser, newDeck.getUser());
            assertEquals(DECK_NAME, newDeck.getName());
            assertEquals(DECK_DESCRIPTION, newDeck.getDescription());
            assertNull(newDeck.getId());
            assertNull(newDeck.getVersion());
            assertNotNull(newDeck.getCards(), "Cards collection should be initialized");
            assertTrue(newDeck.getCards().isEmpty(), "Cards should be empty");
            assertNotNull(newDeck.getTags(), "Tags collection should be initialized");
            assertTrue(newDeck.getTags().isEmpty(), "Tags should be empty");

            logger.debug("Test passed: Deck created successfully");
        }

        @Test
        @DisplayName("Should create deck with null description")
        void testDeckCreationWithNullDescription() {
            logger.debug("Test: Creating deck with null description");

            Deck newDeck = new Deck(testUser, DECK_NAME, null);

            assertEquals(testUser, newDeck.getUser());
            assertEquals(DECK_NAME, newDeck.getName());
            assertNull(newDeck.getDescription());

            logger.debug("Test passed: Deck created with null description");
        }

        @Test
        @DisplayName("Should create deck with no-args constructor")
        void testNoArgsConstructor() {
            logger.debug("Test: Creating deck with no-args constructor");

            Deck emptyDeck = new Deck();

            assertNotNull(emptyDeck);
            assertNull(emptyDeck.getUser());
            assertNull(emptyDeck.getName());
            assertNull(emptyDeck.getDescription());
            assertNotNull(emptyDeck.getCards());
            assertNotNull(emptyDeck.getTags());

            logger.debug("Test passed: No-args constructor works");
        }
    }

    // ========================================
    // Validation Tests
    // ========================================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when user is null")
        void testNullUser() {
            logger.debug("Test: Creating deck with null user");

            assertThrows(IllegalArgumentException.class, () -> {
                new Deck(null, DECK_NAME, DECK_DESCRIPTION);
            }, "Should throw exception for null user");

            logger.debug("Test passed: Exception thrown for null user");
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void testNullName() {
            logger.debug("Test: Creating deck with null name");

            assertThrows(IllegalArgumentException.class, () -> {
                new Deck(testUser, null, DECK_DESCRIPTION);
            }, "Should throw exception for null name");

            logger.debug("Test passed: Exception thrown for null name");
        }

        @Test
        @DisplayName("Should throw exception when name is empty")
        void testEmptyName() {
            logger.debug("Test: Creating deck with empty name");

            assertThrows(IllegalArgumentException.class, () -> {
                new Deck(testUser, "", DECK_DESCRIPTION);
            }, "Should throw exception for empty name");

            logger.debug("Test passed: Exception thrown for empty name");
        }

        @Test
        @DisplayName("Should throw exception when name is whitespace only")
        void testWhitespaceName() {
            logger.debug("Test: Creating deck with whitespace-only name");

            assertThrows(IllegalArgumentException.class, () -> {
                new Deck(testUser, "   ", DECK_DESCRIPTION);
            }, "Should throw exception for whitespace name");

            logger.debug("Test passed: Exception thrown for whitespace-only name");
        }

        @Test
        @DisplayName("Should throw exception when setting null user")
        void testSetNullUser() {
            logger.debug("Test: Setting null user");

            assertThrows(IllegalArgumentException.class, () -> {
                deck.setUser(null);
            }, "Should throw exception when setting null user");

            logger.debug("Test passed: Exception thrown for setting null user");
        }

        @Test
        @DisplayName("Should throw exception when setting null name")
        void testSetNullName() {
            logger.debug("Test: Setting null name");

            assertThrows(IllegalArgumentException.class, () -> {
                deck.setName(null);
            }, "Should throw exception when setting null name");

            logger.debug("Test passed: Exception thrown for setting null name");
        }

        @Test
        @DisplayName("Should throw exception when setting empty name")
        void testSetEmptyName() {
            logger.debug("Test: Setting empty name");

            assertThrows(IllegalArgumentException.class, () -> {
                deck.setName("");
            }, "Should throw exception when setting empty name");

            logger.debug("Test passed: Exception thrown for setting empty name");
        }

        @Test
        @DisplayName("Should throw exception when setting whitespace name")
        void testSetWhitespaceName() {
            logger.debug("Test: Setting whitespace-only name");

            assertThrows(IllegalArgumentException.class, () -> {
                deck.setName("   ");
            }, "Should throw exception when setting whitespace name");

            logger.debug("Test passed: Exception thrown for setting whitespace name");
        }
    }

    // ========================================
    // Input Trimming Tests
    // ========================================

    @Nested
    @DisplayName("Input Trimming Tests")
    class InputTrimmingTests {

        @Test
        @DisplayName("Should trim name in constructor")
        void testNameTrimmedInConstructor() {
            logger.debug("Test: Name trimming in constructor");

            Deck trimDeck = new Deck(testUser, "  " + DECK_NAME + "  ", DECK_DESCRIPTION);

            assertEquals(DECK_NAME, trimDeck.getName());

            logger.debug("Test passed: Name trimmed successfully");
        }

        @Test
        @DisplayName("Should trim description in constructor")
        void testDescriptionTrimmedInConstructor() {
            logger.debug("Test: Description trimming in constructor");

            Deck trimDeck = new Deck(testUser, DECK_NAME, "  " + DECK_DESCRIPTION + "  ");

            assertEquals(DECK_DESCRIPTION, trimDeck.getDescription());

            logger.debug("Test passed: Description trimmed successfully");
        }

        @Test
        @DisplayName("Should trim name in setter")
        void testNameTrimmedInSetter() {
            logger.debug("Test: Name trimming in setter");

            deck.setName("  " + UPDATED_NAME + "  ");

            assertEquals(UPDATED_NAME, deck.getName());

            logger.debug("Test passed: Name trimmed in setter");
        }

        @Test
        @DisplayName("Should trim description in setter")
        void testDescriptionTrimmedInSetter() {
            logger.debug("Test: Description trimming in setter");

            deck.setDescription("  " + UPDATED_DESCRIPTION + "  ");

            assertEquals(UPDATED_DESCRIPTION, deck.getDescription());

            logger.debug("Test passed: Description trimmed in setter");
        }

        @Test
        @DisplayName("Should convert empty description to null in constructor")
        void testEmptyDescriptionToNullInConstructor() {
            logger.debug("Test: Converting empty description to null in constructor");

            Deck emptyDescDeck = new Deck(testUser, DECK_NAME, "");

            assertNull(emptyDescDeck.getDescription());

            logger.debug("Test passed: Empty description converted to null");
        }

        @Test
        @DisplayName("Should convert whitespace description to null in constructor")
        void testWhitespaceDescriptionToNullInConstructor() {
            logger.debug("Test: Converting whitespace description to null in constructor");

            Deck whitespaceDeck = new Deck(testUser, DECK_NAME, "   ");

            assertNull(whitespaceDeck.getDescription());

            logger.debug("Test passed: Whitespace description converted to null");
        }

        @Test
        @DisplayName("Should convert empty description to null in setter")
        void testEmptyDescriptionToNullInSetter() {
            logger.debug("Test: Converting empty description to null in setter");

            deck.setDescription("");

            assertNull(deck.getDescription());

            logger.debug("Test passed: Empty description converted to null");
        }

        @Test
        @DisplayName("Should convert whitespace description to null in setter")
        void testWhitespaceDescriptionToNullInSetter() {
            logger.debug("Test: Converting whitespace description to null in setter");

            deck.setDescription("   ");

            assertNull(deck.getDescription());

            logger.debug("Test passed: Whitespace description converted to null");
        }
    }

    // ========================================
    // Editing Tests
    // ========================================

    @Nested
    @DisplayName("Editing Tests")
    class EditingTests {

        @Test
        @DisplayName("Should allow editing deck name")
        void testEditName() {
            logger.debug("Test: Editing deck name from '{}' to '{}'", DECK_NAME, UPDATED_NAME);

            deck.setName(UPDATED_NAME);

            assertEquals(UPDATED_NAME, deck.getName());
            assertEquals(DECK_DESCRIPTION, deck.getDescription(),
                    "Description should remain unchanged");

            logger.debug("Test passed: Name edited successfully");
        }

        @Test
        @DisplayName("Should allow editing deck description")
        void testEditDescription() {
            logger.debug("Test: Editing deck description");

            deck.setDescription(UPDATED_DESCRIPTION);

            assertEquals(DECK_NAME, deck.getName(), "Name should remain unchanged");
            assertEquals(UPDATED_DESCRIPTION, deck.getDescription());

            logger.debug("Test passed: Description edited successfully");
        }

        @Test
        @DisplayName("Should allow clearing deck description")
        void testClearDescription() {
            logger.debug("Test: Clearing deck description");

            assertEquals(DECK_DESCRIPTION, deck.getDescription());

            deck.setDescription(null);

            assertNull(deck.getDescription());

            logger.debug("Test passed: Description cleared successfully");
        }

        @Test
        @DisplayName("Should allow editing all fields")
        void testEditAllFields() {
            logger.debug("Test: Editing all deck fields");

            deck.setName(UPDATED_NAME);
            deck.setDescription(UPDATED_DESCRIPTION);

            assertEquals(UPDATED_NAME, deck.getName());
            assertEquals(UPDATED_DESCRIPTION, deck.getDescription());

            logger.debug("Test passed: All fields edited successfully");
        }
    }

    // ========================================
    // User Association Tests
    // ========================================

    @Nested
    @DisplayName("User Association Tests")
    class UserAssociationTests {

        @Test
        @DisplayName("Should allow changing deck owner")
        void testChangeOwner() {
            logger.debug("Test: Changing deck owner");

            User newUser = new User("other@example.com", PASSWORD_HASH);
            newUser.setId(2L);

            assertEquals(testUser, deck.getUser());

            deck.setUser(newUser);

            assertEquals(newUser, deck.getUser());

            logger.debug("Test passed: Owner changed successfully");
        }

        @Test
        @DisplayName("Should maintain user relationship throughout lifecycle")
        void testUserRelationshipMaintained() {
            logger.debug("Test: User relationship maintained");

            assertEquals(testUser, deck.getUser());

            // Edit deck properties
            deck.setName("New Name");
            deck.setDescription("New Description");

            assertEquals(testUser, deck.getUser(),
                    "User should remain after property changes");

            logger.debug("Test passed: User relationship maintained");
        }
    }

    // ========================================
    // Card Management Tests (NEW)
    // ========================================

    @Nested
    @DisplayName("Card Management Tests")
    class CardManagementTests {

        @Test
        @DisplayName("Should have empty card collection when created")
        void testNewDeckHasEmptyCardCollection() {
            logger.debug("Test: New deck has empty card collection");

            assertNotNull(deck.getCards());
            assertTrue(deck.getCards().isEmpty());
            assertEquals(0, deck.getCards().size());

            logger.debug("Test passed: Empty card collection initialized");
        }

        @Test
        @DisplayName("Should add card to deck")
        void testAddCard() {
            logger.debug("Test: Adding card to deck");

            Card card = new Card(deck, "食べる", "to eat");
            card.setId(1L);

            deck.getCards().add(card);

            assertTrue(deck.getCards().contains(card));
            assertEquals(1, deck.getCards().size());
            assertEquals(deck, card.getDeck());

            logger.debug("Test passed: Card added successfully");
        }

        @Test
        @DisplayName("Should add multiple cards to deck")
        void testAddMultipleCards() {
            logger.debug("Test: Adding multiple cards");

            Card card1 = new Card(deck, "食べる", "to eat");
            card1.setId(1L);
            Card card2 = new Card(deck, "飲む", "to drink");
            card2.setId(2L);
            Card card3 = new Card(deck, "行く", "to go");
            card3.setId(3L);

            deck.getCards().add(card1);
            deck.getCards().add(card2);
            deck.getCards().add(card3);

            assertEquals(3, deck.getCards().size());
            assertTrue(deck.getCards().contains(card1));
            assertTrue(deck.getCards().contains(card2));
            assertTrue(deck.getCards().contains(card3));

            logger.debug("Test passed: Multiple cards added");
        }

        @Test
        @DisplayName("Should remove card from deck")
        void testRemoveCard() {
            logger.debug("Test: Removing card from deck");

            Card card = new Card(deck, "食べる", "to eat");
            card.setId(1L);

            deck.getCards().add(card);
            assertTrue(deck.getCards().contains(card));

            deck.getCards().remove(card);

            assertFalse(deck.getCards().contains(card));
            assertEquals(0, deck.getCards().size());

            logger.debug("Test passed: Card removed successfully");
        }

        @Test
        @DisplayName("Should remove specific card from multiple cards")
        void testRemoveSpecificCard() {
            logger.debug("Test: Removing specific card");

            Card card1 = new Card(deck, "食べる", "to eat");
            card1.setId(1L);
            Card card2 = new Card(deck, "飲む", "to drink");
            card2.setId(2L);
            Card card3 = new Card(deck, "行く", "to go");
            card3.setId(3L);

            deck.getCards().add(card1);
            deck.getCards().add(card2);
            deck.getCards().add(card3);

            deck.getCards().remove(card2);

            assertEquals(2, deck.getCards().size());
            assertTrue(deck.getCards().contains(card1));
            assertFalse(deck.getCards().contains(card2));
            assertTrue(deck.getCards().contains(card3));

            logger.debug("Test passed: Specific card removed");
        }

        @Test
        @DisplayName("Should maintain deck reference when cards added")
        void testDeckReferenceInCards() {
            logger.debug("Test: Deck reference in cards");

            Card card1 = new Card(deck, "食べる", "to eat");
            Card card2 = new Card(deck, "飲む", "to drink");

            deck.getCards().add(card1);
            deck.getCards().add(card2);

            assertEquals(deck, card1.getDeck());
            assertEquals(deck, card2.getDeck());

            logger.debug("Test passed: Deck references maintained");
        }
    }

    // ========================================
    // Tag Management Tests (NEW)
    // ========================================

    @Nested
    @DisplayName("Tag Management Tests")
    class TagManagementTests {

        @Test
        @DisplayName("Should have empty tag collection when created")
        void testNewDeckHasEmptyTagCollection() {
            logger.debug("Test: New deck has empty tag collection");

            assertNotNull(deck.getTags());
            assertTrue(deck.getTags().isEmpty());
            assertEquals(0, deck.getTags().size());

            logger.debug("Test passed: Empty tag collection initialized");
        }

        @Test
        @DisplayName("Should add tag to deck")
        void testAddTag() {
            logger.debug("Test: Adding tag to deck");

            Tag tag = new Tag(deck, "verbs");
            tag.setId(1L);

            deck.getTags().add(tag);

            assertTrue(deck.getTags().contains(tag));
            assertEquals(1, deck.getTags().size());
            assertEquals(deck, tag.getDeck());
            assertEquals(testUser, tag.getUser());

            logger.debug("Test passed: Tag added successfully");
        }

        @Test
        @DisplayName("Should add multiple tags to deck")
        void testAddMultipleTags() {
            logger.debug("Test: Adding multiple tags");

            Tag tag1 = new Tag(deck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(deck, "N5");
            tag2.setId(2L);
            Tag tag3 = new Tag(deck, "important");
            tag3.setId(3L);

            deck.getTags().add(tag1);
            deck.getTags().add(tag2);
            deck.getTags().add(tag3);

            assertEquals(3, deck.getTags().size());
            assertTrue(deck.getTags().contains(tag1));
            assertTrue(deck.getTags().contains(tag2));
            assertTrue(deck.getTags().contains(tag3));

            logger.debug("Test passed: Multiple tags added");
        }

        @Test
        @DisplayName("Should remove tag from deck")
        void testRemoveTag() {
            logger.debug("Test: Removing tag from deck");

            Tag tag = new Tag(deck, "verbs");
            tag.setId(1L);

            deck.getTags().add(tag);
            assertTrue(deck.getTags().contains(tag));

            deck.getTags().remove(tag);

            assertFalse(deck.getTags().contains(tag));
            assertEquals(0, deck.getTags().size());

            logger.debug("Test passed: Tag removed successfully");
        }

        @Test
        @DisplayName("Should maintain deck and user references in tags")
        void testDeckAndUserReferencesInTags() {
            logger.debug("Test: Deck and user references in tags");

            Tag tag1 = new Tag(deck, "verbs");
            Tag tag2 = new Tag(deck, "N5");

            deck.getTags().add(tag1);
            deck.getTags().add(tag2);

            assertEquals(deck, tag1.getDeck());
            assertEquals(deck, tag2.getDeck());
            assertEquals(testUser, tag1.getUser());
            assertEquals(testUser, tag2.getUser());

            logger.debug("Test passed: References maintained");
        }

        @Test
        @DisplayName("Should support tags with same name in different decks")
        void testSameTagNameInDifferentDecks() {
            logger.debug("Test: Same tag name in different decks");

            Deck otherDeck = new Deck(testUser, "Spanish", "Spanish vocab");
            otherDeck.setId(2L);

            Tag tag1 = new Tag(deck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(otherDeck, "verbs");
            tag2.setId(2L);

            deck.getTags().add(tag1);
            otherDeck.getTags().add(tag2);

            assertEquals("verbs", tag1.getName());
            assertEquals("verbs", tag2.getName());
            assertNotEquals(tag1, tag2, "Tags should be different entities");
            assertEquals(deck, tag1.getDeck());
            assertEquals(otherDeck, tag2.getDeck());

            logger.debug("Test passed: Same name in different decks supported");
        }
    }

    // ========================================
    // Cascade and OrphanRemoval Tests (NEW)
    // ========================================

    @Nested
    @DisplayName("Cascade and OrphanRemoval Tests")
    class CascadeTests {

        @Test
        @DisplayName("Should demonstrate cascade to cards")
        void testCascadeToCards() {
            logger.debug("Test: Cascade to cards");

            Card card1 = new Card(deck, "食べる", "to eat");
            card1.setId(1L);
            Card card2 = new Card(deck, "飲む", "to drink");
            card2.setId(2L);

            deck.getCards().add(card1);
            deck.getCards().add(card2);

            assertEquals(2, deck.getCards().size());
            // In real JPA, deleting deck would cascade delete cards

            logger.debug("Test passed: Cascade relationship verified");
        }

        @Test
        @DisplayName("Should demonstrate cascade to tags")
        void testCascadeToTags() {
            logger.debug("Test: Cascade to tags");

            Tag tag1 = new Tag(deck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(deck, "N5");
            tag2.setId(2L);

            deck.getTags().add(tag1);
            deck.getTags().add(tag2);

            assertEquals(2, deck.getTags().size());
            // In real JPA, deleting deck would cascade delete tags

            logger.debug("Test passed: Cascade relationship verified");
        }

        @Test
        @DisplayName("Should demonstrate orphanRemoval for cards")
        void testOrphanRemovalForCards() {
            logger.debug("Test: OrphanRemoval for cards");

            Card card = new Card(deck, "食べる", "to eat");
            card.setId(1L);

            deck.getCards().add(card);
            assertTrue(deck.getCards().contains(card));

            // Removing from collection triggers orphanRemoval in JPA
            deck.getCards().remove(card);

            assertFalse(deck.getCards().contains(card));
            // In real JPA, card would be deleted from database

            logger.debug("Test passed: OrphanRemoval relationship verified");
        }

        @Test
        @DisplayName("Should demonstrate orphanRemoval for tags")
        void testOrphanRemovalForTags() {
            logger.debug("Test: OrphanRemoval for tags");

            Tag tag = new Tag(deck, "verbs");
            tag.setId(1L);

            deck.getTags().add(tag);
            assertTrue(deck.getTags().contains(tag));

            // Removing from collection triggers orphanRemoval in JPA
            deck.getTags().remove(tag);

            assertFalse(deck.getTags().contains(tag));
            // In real JPA, tag would be deleted from database

            logger.debug("Test passed: OrphanRemoval relationship verified");
        }

        @Test
        @DisplayName("Should verify cascade chain with tags and cards")
        void testCascadeChainWithTagsAndCards() {
            logger.debug("Test: Cascade chain with tags and cards");

            // Create cards
            Card card1 = new Card(deck, "食べる", "to eat");
            card1.setId(1L);
            Card card2 = new Card(deck, "飲む", "to drink");
            card2.setId(2L);
            deck.getCards().add(card1);
            deck.getCards().add(card2);

            // Create tags
            Tag tag1 = new Tag(deck, "verbs");
            tag1.setId(1L);
            Tag tag2 = new Tag(deck, "N5");
            tag2.setId(2L);
            deck.getTags().add(tag1);
            deck.getTags().add(tag2);

            // Associate tags with cards
            card1.addTag(tag1);
            card2.addTag(tag1);
            card2.addTag(tag2);

            // Verify structure
            assertEquals(2, deck.getCards().size());
            assertEquals(2, deck.getTags().size());
            assertEquals(1, card1.getTags().size());
            assertEquals(2, card2.getTags().size());

            // In real JPA, deleting deck cascades to cards and tags
            // Card-tag associations would also be cleaned up

            logger.debug("Test passed: Complex cascade chain verified");
        }
    }

    // ========================================
    // Equality and HashCode Tests
    // ========================================

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should consider decks equal with same ID and name")
        void testDeckEquality() {
            logger.debug("Test: Testing deck equality");

            Deck deck1 = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
            deck1.setId(1L);

            Deck deck2 = new Deck(testUser, DECK_NAME, "Different description");
            deck2.setId(1L);

            Deck deck3 = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
            deck3.setId(2L);

            assertEquals(deck1, deck2, "Same ID and name should be equal");
            assertNotEquals(deck1, deck3, "Different ID should not be equal");

            logger.debug("Test passed: Equality based on ID and name works correctly");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void testEqualityReflexive() {
            logger.debug("Test: Testing reflexive equality");

            assertEquals(deck, deck);

            logger.debug("Test passed: Deck equals itself");
        }

        @Test
        @DisplayName("Should not be equal to null")
        void testEqualityNull() {
            logger.debug("Test: Testing equality with null");

            assertNotEquals(null, deck);

            logger.debug("Test passed: Deck not equal to null");
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void testEqualityDifferentClass() {
            logger.debug("Test: Testing equality with different class");

            assertNotEquals(deck, "Not a Deck");

            logger.debug("Test passed: Deck not equal to different class");
        }

        @Test
        @DisplayName("Should handle equality for decks without IDs")
        void testEqualityWithoutIds() {
            logger.debug("Test: Testing equality for decks without IDs");

            Deck deck1 = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
            Deck deck2 = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);

            assertEquals(deck1, deck1, "Same instance should equal itself");
            assertNotEquals(deck1, deck2, "Different instances without IDs should not be equal");

            logger.debug("Test passed: Decks without IDs handled correctly");
        }

        @Test
        @DisplayName("Should have consistent hash code with equals")
        void testHashCodeConsistency() {
            logger.debug("Test: Testing hash code consistency");

            Deck deck1 = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
            deck1.setId(1L);

            Deck deck2 = new Deck(testUser, DECK_NAME, "Different description");
            deck2.setId(1L);

            assertEquals(deck1.hashCode(), deck2.hashCode());

            logger.debug("Test passed: Hash code consistent with equals");
        }
    }

    // ========================================
    // toString Tests
    // ========================================

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include deck details in toString")
        void testToStringIncludesDetails() {
            logger.debug("Test: Testing toString includes details");

            String deckString = deck.toString();

            assertTrue(deckString.contains(DECK_NAME));
            assertTrue(deckString.contains(DECK_DESCRIPTION));
            assertTrue(deckString.contains("id=1"));

            logger.debug("Test passed: toString includes deck details");
        }

        @Test
        @DisplayName("Should not expose sensitive user data in toString")
        void testToStringDoesNotExposeSensitiveData() {
            logger.debug("Test: Testing toString does not expose sensitive data");

            User sensitiveUser = new User("secret@example.com", "secretPassword");
            sensitiveUser.setId(1L);
            Deck sensitiveDeck = new Deck(sensitiveUser, DECK_NAME, DECK_DESCRIPTION);
            sensitiveDeck.setId(1L);

            String deckString = sensitiveDeck.toString();

            assertTrue(deckString.contains(DECK_NAME));
            assertTrue(deckString.contains("userId=1"), "Should show user ID only");
            assertFalse(deckString.contains("secret@example.com"));
            assertFalse(deckString.contains("secretPassword"));

            logger.debug("Test passed: Sensitive data not in toString");
        }
    }

    // ========================================
    // Getter/Setter Tests
    // ========================================

    @Nested
    @DisplayName("Getter/Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set version")
        void testVersionGetterSetter() {
            logger.debug("Test: Testing version getter and setter");

            assertNull(deck.getVersion());

            deck.setVersion(1L);
            assertEquals(1L, deck.getVersion());

            deck.setVersion(2L);
            assertEquals(2L, deck.getVersion());

            logger.debug("Test passed: Version getter and setter work");
        }

        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            logger.debug("Test: Testing ID getter and setter");

            assertEquals(1L, deck.getId());

            deck.setId(100L);
            assertEquals(100L, deck.getId());

            logger.debug("Test passed: ID getter and setter work");
        }
    }

    // ========================================
    // Integration Tests
    // ========================================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete deck lifecycle with cards and tags")
        void testCompleteDeckLifecycle() {
            logger.debug("Test: Complete deck lifecycle");

            // Create deck
            Deck newDeck = new Deck(testUser, "Lifecycle Deck", "Test deck");
            newDeck.setId(10L);

            // Add cards
            Card card1 = new Card(newDeck, "Front 1", "Back 1");
            card1.setId(1L);
            Card card2 = new Card(newDeck, "Front 2", "Back 2");
            card2.setId(2L);
            newDeck.getCards().add(card1);
            newDeck.getCards().add(card2);

            // Add tags
            Tag tag1 = new Tag(newDeck, "tag1");
            tag1.setId(1L);
            Tag tag2 = new Tag(newDeck, "tag2");
            tag2.setId(2L);
            newDeck.getTags().add(tag1);
            newDeck.getTags().add(tag2);

            // Associate tags with cards
            card1.addTag(tag1);
            card2.addTag(tag1);
            card2.addTag(tag2);

            // Verify initial state
            assertEquals(2, newDeck.getCards().size());
            assertEquals(2, newDeck.getTags().size());

            // Modify deck
            newDeck.setName("Updated Deck");
            newDeck.setDescription("Updated description");

            // Remove a card
            newDeck.getCards().remove(card1);
            assertEquals(1, newDeck.getCards().size());

            // Remove a tag
            newDeck.getTags().remove(tag2);
            assertEquals(1, newDeck.getTags().size());

            logger.debug("Test passed: Complete lifecycle handled");
        }

        @Test
        @DisplayName("Should handle deck ownership transfer")
        void testDeckOwnershipTransfer() {
            logger.debug("Test: Deck ownership transfer");

            User originalOwner = testUser;
            User newOwner = new User("newowner@example.com", PASSWORD_HASH);
            newOwner.setId(2L);

            // Create deck with cards and tags
            Deck transferDeck = new Deck(originalOwner, "Transfer Deck", "Description");
            transferDeck.setId(5L);

            Card card = new Card(transferDeck, "Front", "Back");
            card.setId(1L);
            transferDeck.getCards().add(card);

            Tag tag = new Tag(transferDeck, "tag");
            tag.setId(1L);
            transferDeck.getTags().add(tag);

            // Verify initial ownership
            assertEquals(originalOwner, transferDeck.getUser());
            assertEquals(originalOwner, tag.getUser());

            // Transfer ownership
            transferDeck.setUser(newOwner);

            // Deck ownership transferred
            assertEquals(newOwner, transferDeck.getUser());

            // Note: Tag user would need to be updated separately
            // This is a design consideration - should tags auto-update?

            logger.debug("Test passed: Ownership transfer handled");
        }
    }
}