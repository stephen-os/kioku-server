package com.kioku.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Tag entity.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Tag creation with deck association</li>
 *   <li>Field validation (deck, name cannot be null/empty)</li>
 *   <li>User inheritance from deck</li>
 *   <li>Editing tag properties (name, deck)</li>
 *   <li>Input trimming (whitespace removal)</li>
 *   <li>Deck-specific tag isolation</li>
 *   <li>Equality and hash code based on ID and name</li>
 *   <li>toString includes deck context</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("Tag Entity Unit Tests")
class TagEntityTest {

    private static final Logger logger = LoggerFactory.getLogger(TagEntityTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String DECK_NAME = "Japanese Vocabulary";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary";
    private static final String TAG_NAME = "verbs";
    private static final String UPDATED_TAG_NAME = "nouns";

    private UserEntity testUserEntity;
    private DeckEntity testDeckEntity;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up Tag test: Creating user and deck");
        testUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        testUserEntity.setId(1L);
        testDeckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        testDeckEntity.setId(1L);
    }

    // Constructor Tests

    /**
     * Tests tag creation with deck.
     */
    @Test
    @DisplayName("Should create tag with deck")
    void testTagCreation() {
        logger.debug("Test: Creating tag with name='{}'", TAG_NAME);

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertEquals(testDeckEntity, tagEntity.getDeck());
        assertEquals(testUserEntity, tagEntity.getUser()); // User inherited from deck
        assertEquals(TAG_NAME, tagEntity.getName());
        assertNull(tagEntity.getId());
        assertNotNull(tagEntity.getCards());
        assertTrue(tagEntity.getCards().isEmpty());

        logger.debug("Test passed: Tag created successfully");
    }

    /**
     * Tests that user is inherited from deck.
     */
    @Test
    @DisplayName("Should inherit user from deck")
    void testUserInheritedFromDeck() {
        logger.debug("Test: User inheritance from deck");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertEquals(testDeckEntity.getUser(), tagEntity.getUser());
        assertEquals(testUserEntity, tagEntity.getUser());

        logger.debug("Test passed: User inherited correctly");
    }

    /**
     * Tests no-args constructor creates valid tag.
     */
    @Test
    @DisplayName("Should create tag with no-args constructor")
    void testNoArgsConstructor() {
        logger.debug("Test: Creating tag with no-args constructor");

        TagEntity tagEntity = new TagEntity();

        assertNotNull(tagEntity);
        assertNull(tagEntity.getDeck());
        assertNull(tagEntity.getUser());
        assertNull(tagEntity.getName());
        assertNotNull(tagEntity.getCards());

        logger.debug("Test passed: No-args constructor works");
    }

    // Validation Tests

    /**
     * Tests that creating tag with null deck throws exception.
     */
    @Test
    @DisplayName("Should throw exception when deck is null")
    void testNullDeck() {
        logger.debug("Test: Creating tag with null deck");

        assertThrows(IllegalArgumentException.class, () -> {
            new TagEntity(null, TAG_NAME);
        });

        logger.debug("Test passed: Exception thrown for null deck");
    }

    /**
     * Tests that creating tag with null name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when name is null")
    void testNullName() {
        logger.debug("Test: Creating tag with null name");

        assertThrows(IllegalArgumentException.class, () -> {
            new TagEntity(testDeckEntity, null);
        });

        logger.debug("Test passed: Exception thrown for null name");
    }

    /**
     * Tests that creating tag with empty name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when name is empty")
    void testEmptyName() {
        logger.debug("Test: Creating tag with empty name");

        assertThrows(IllegalArgumentException.class, () -> {
            new TagEntity(testDeckEntity, "");
        });

        logger.debug("Test passed: Exception thrown for empty name");
    }

    /**
     * Tests that creating tag with whitespace-only name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when name is whitespace only")
    void testWhitespaceName() {
        logger.debug("Test: Creating tag with whitespace-only name");

        assertThrows(IllegalArgumentException.class, () -> {
            new TagEntity(testDeckEntity, "   ");
        });

        logger.debug("Test passed: Exception thrown for whitespace-only name");
    }

    /**
     * Tests that setting null deck throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting null deck")
    void testSetNullDeck() {
        logger.debug("Test: Setting null deck");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertThrows(IllegalArgumentException.class, () -> {
            tagEntity.setDeck(null);
        });

        logger.debug("Test passed: Exception thrown for setting null deck");
    }

    /**
     * Tests that setting null user throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting null user")
    void testSetNullUser() {
        logger.debug("Test: Setting null user");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertThrows(IllegalArgumentException.class, () -> {
            tagEntity.setUser(null);
        });

        logger.debug("Test passed: Exception thrown for setting null user");
    }

    /**
     * Tests that setting null name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting null name")
    void testSetNullName() {
        logger.debug("Test: Setting null name");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertThrows(IllegalArgumentException.class, () -> {
            tagEntity.setName(null);
        });

        logger.debug("Test passed: Exception thrown for setting null name");
    }

    /**
     * Tests that setting empty name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting empty name")
    void testSetEmptyName() {
        logger.debug("Test: Setting empty name");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertThrows(IllegalArgumentException.class, () -> {
            tagEntity.setName("");
        });

        logger.debug("Test passed: Exception thrown for setting empty name");
    }

    /**
     * Tests that setting whitespace-only name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting whitespace name")
    void testSetWhitespaceName() {
        logger.debug("Test: Setting whitespace-only name");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertThrows(IllegalArgumentException.class, () -> {
            tagEntity.setName("   ");
        });

        logger.debug("Test passed: Exception thrown for setting whitespace name");
    }

    // Input Trimming Tests

    /**
     * Tests that name is trimmed in constructor.
     */
    @Test
    @DisplayName("Should trim name in constructor")
    void testNameTrimmedInConstructor() {
        logger.debug("Test: Name trimming in constructor");

        TagEntity tagEntity = new TagEntity(testDeckEntity, "  " + TAG_NAME + "  ");

        assertEquals(TAG_NAME, tagEntity.getName());

        logger.debug("Test passed: Name trimmed successfully");
    }

    /**
     * Tests that name is trimmed in setter.
     */
    @Test
    @DisplayName("Should trim name in setter")
    void testNameTrimmedInSetter() {
        logger.debug("Test: Name trimming in setter");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity.setName("  " + UPDATED_TAG_NAME + "  ");

        assertEquals(UPDATED_TAG_NAME, tagEntity.getName());

        logger.debug("Test passed: Name trimmed in setter");
    }

    // Editing Tests

    /**
     * Tests editing tag name.
     */
    @Test
    @DisplayName("Should allow editing tag name")
    void testEditName() {
        logger.debug("Test: Editing tag name from '{}' to '{}'", TAG_NAME, UPDATED_TAG_NAME);

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity.setName(UPDATED_TAG_NAME);

        assertEquals(UPDATED_TAG_NAME, tagEntity.getName());

        logger.debug("Test passed: Name edited successfully");
    }

    /**
     * Tests changing tag's deck.
     */
    @Test
    @DisplayName("Should allow changing tag's deck")
    void testChangeDeck() {
        logger.debug("Test: Changing tag's deck");

        DeckEntity newDeckEntity = new DeckEntity(testUserEntity, "New Deck", "Description");
        newDeckEntity.setId(2L);

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        assertEquals(testDeckEntity, tagEntity.getDeck());

        tagEntity.setDeck(newDeckEntity);
        assertEquals(newDeckEntity, tagEntity.getDeck());

        logger.debug("Test passed: Deck changed successfully");
    }

    /**
     * Tests changing tag's user.
     */
    @Test
    @DisplayName("Should allow changing tag's user")
    void testChangeUser() {
        logger.debug("Test: Changing tag's user");

        UserEntity newUserEntity = new UserEntity("other@example.com", PASSWORD_HASH);
        newUserEntity.setId(2L);

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        assertEquals(testUserEntity, tagEntity.getUser());

        tagEntity.setUser(newUserEntity);
        assertEquals(newUserEntity, tagEntity.getUser());

        logger.debug("Test passed: User changed successfully");
    }

    // Deck Isolation Tests

    /**
     * Tests that tags with same name in different decks are different.
     */
    @Test
    @DisplayName("Should allow same tag name in different decks")
    void testSameNameDifferentDecks() {
        logger.debug("Test: Same tag name in different decks");

        DeckEntity deckEntity1 = new DeckEntity(testUserEntity, "Japanese", "Description");
        deckEntity1.setId(1L);
        DeckEntity deckEntity2 = new DeckEntity(testUserEntity, "Spanish", "Description");
        deckEntity2.setId(2L);

        TagEntity tagEntity1 = new TagEntity(deckEntity1, "verbs");
        TagEntity tagEntity2 = new TagEntity(deckEntity2, "verbs");

        assertEquals("verbs", tagEntity1.getName());
        assertEquals("verbs", tagEntity2.getName());
        assertNotEquals(tagEntity1.getDeck(), tagEntity2.getDeck());

        logger.debug("Test passed: Same name allowed in different decks");
    }

    // Equality and HashCode Tests

    /**
     * Tests tag equality based on ID and name.
     */
    @Test
    @DisplayName("Should consider tags equal with same ID and name")
    void testTagEquality() {
        logger.debug("Test: Testing tag equality");

        TagEntity tagEntity1 = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity1.setId(1L);

        TagEntity tagEntity2 = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity2.setId(1L);

        TagEntity tagEntity3 = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity3.setId(2L);

        // Same ID and name = equal
        assertEquals(tagEntity1, tagEntity2);

        // Different ID = not equal
        assertNotEquals(tagEntity1, tagEntity3);

        logger.debug("Test passed: Equality based on ID and name works correctly");
    }

    /**
     * Tests reflexive property of equals.
     */
    @Test
    @DisplayName("Should be equal to itself")
    void testEqualityReflexive() {
        logger.debug("Test: Testing reflexive equality");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity.setId(1L);

        assertEquals(tagEntity, tagEntity);

        logger.debug("Test passed: Tag equals itself");
    }

    /**
     * Tests that tag is not equal to null.
     */
    @Test
    @DisplayName("Should not be equal to null")
    void testEqualityNull() {
        logger.debug("Test: Testing equality with null");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity.setId(1L);

        assertNotEquals(null, tagEntity);

        logger.debug("Test passed: Tag not equal to null");
    }

    /**
     * Tests that tag is not equal to different class.
     */
    @Test
    @DisplayName("Should not be equal to different class")
    void testEqualityDifferentClass() {
        logger.debug("Test: Testing equality with different class");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity.setId(1L);

        assertNotEquals(tagEntity, "Not a Tag");

        logger.debug("Test passed: Tag not equal to different class");
    }

    /**
     * Tests that tags without IDs are only equal to themselves.
     */
    @Test
    @DisplayName("Should handle equality for tags without IDs")
    void testEqualityWithoutIds() {
        logger.debug("Test: Testing equality for tags without IDs");

        TagEntity tagEntity1 = new TagEntity(testDeckEntity, TAG_NAME);
        TagEntity tagEntity2 = new TagEntity(testDeckEntity, TAG_NAME);

        // Same instance
        assertEquals(tagEntity1, tagEntity1);

        // Different instances without IDs are not equal
        assertNotEquals(tagEntity1, tagEntity2);

        logger.debug("Test passed: Tags without IDs handled correctly");
    }

    /**
     * Tests hash code consistency with equals.
     */
    @Test
    @DisplayName("Should have consistent hash code with equals")
    void testHashCodeConsistency() {
        logger.debug("Test: Testing hash code consistency");

        TagEntity tagEntity1 = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity1.setId(1L);

        TagEntity tagEntity2 = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity2.setId(1L);

        assertEquals(tagEntity1.hashCode(), tagEntity2.hashCode());

        logger.debug("Test passed: Hash code consistent with equals");
    }

    // toString Tests

    /**
     * Tests that toString includes tag details.
     */
    @Test
    @DisplayName("Should include tag details in toString")
    void testToStringIncludesDetails() {
        logger.debug("Test: Testing toString includes details");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity.setId(1L);

        String tagString = tagEntity.toString();

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

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        String tagString = tagEntity.toString();

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

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        String tagString = tagEntity.toString();

        assertTrue(tagString.contains("cardCount=0"));

        logger.debug("Test passed: toString includes card count");
    }

    // Getter/Setter Tests

    /**
     * Tests ID field getter and setter.
     */
    @Test
    @DisplayName("Should get and set ID")
    void testIdGetterSetter() {
        logger.debug("Test: Testing ID getter and setter");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertNull(tagEntity.getId());

        tagEntity.setId(100L);
        assertEquals(100L, tagEntity.getId());

        logger.debug("Test passed: ID getter and setter work");
    }

    /**
     * Tests cards field getter and setter.
     */
    @Test
    @DisplayName("Should get and set cards")
    void testCardsGetterSetter() {
        logger.debug("Test: Testing cards getter and setter");

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);

        assertNotNull(tagEntity.getCards());
        assertTrue(tagEntity.getCards().isEmpty());

        logger.debug("Test passed: Cards getter works");
    }
}