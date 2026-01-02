package com.kioku.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Deck entity.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Deck creation with required and optional fields</li>
 *   <li>Field validation (user, name cannot be null/empty)</li>
 *   <li>Editing deck properties (name, description)</li>
 *   <li>User ownership association</li>
 *   <li>Input trimming (whitespace removal)</li>
 *   <li>Equality and hash code based on ID and name</li>
 *   <li>toString excludes sensitive user data</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("Deck Entity Unit Tests")
class DeckEntityTest {

    private static final Logger logger = LoggerFactory.getLogger(DeckEntityTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String DECK_NAME = "Japanese Vocabulary";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary practice";
    private static final String UPDATED_NAME = "Japanese Grammar";
    private static final String UPDATED_DESCRIPTION = "JLPT N5 grammar practice";

    private UserEntity testUserEntity;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up Deck test: Creating test user");
        testUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        testUserEntity.setId(1L);
    }

    // Constructor Tests

    /**
     * Tests deck creation with all fields.
     */
    @Test
    @DisplayName("Should create deck with all fields")
    void testDeckCreation() {
        logger.debug("Test: Creating deck with name='{}', description='{}'", DECK_NAME, DECK_DESCRIPTION);

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        assertEquals(testUserEntity, deckEntity.getUser());
        assertEquals(DECK_NAME, deckEntity.getName());
        assertEquals(DECK_DESCRIPTION, deckEntity.getDescription());
        assertNull(deckEntity.getId());
        assertNull(deckEntity.getVersion());

        logger.debug("Test passed: Deck created successfully");
    }

    /**
     * Tests deck creation with null description.
     */
    @Test
    @DisplayName("Should create deck with null description")
    void testDeckCreationWithNullDescription() {
        logger.debug("Test: Creating deck with null description");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, null);

        assertEquals(testUserEntity, deckEntity.getUser());
        assertEquals(DECK_NAME, deckEntity.getName());
        assertNull(deckEntity.getDescription());

        logger.debug("Test passed: Deck created with null description");
    }

    /**
     * Tests no-args constructor creates valid deck.
     */
    @Test
    @DisplayName("Should create deck with no-args constructor")
    void testNoArgsConstructor() {
        logger.debug("Test: Creating deck with no-args constructor");

        DeckEntity deckEntity = new DeckEntity();

        assertNotNull(deckEntity);
        assertNull(deckEntity.getUser());
        assertNull(deckEntity.getName());
        assertNull(deckEntity.getDescription());

        logger.debug("Test passed: No-args constructor works");
    }

    // Validation Tests

    /**
     * Tests that creating deck with null user throws exception.
     */
    @Test
    @DisplayName("Should throw exception when user is null")
    void testNullUser() {
        logger.debug("Test: Creating deck with null user");

        assertThrows(IllegalArgumentException.class, () -> {
            new DeckEntity(null, DECK_NAME, DECK_DESCRIPTION);
        });

        logger.debug("Test passed: Exception thrown for null user");
    }

    /**
     * Tests that creating deck with null name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when name is null")
    void testNullName() {
        logger.debug("Test: Creating deck with null name");

        assertThrows(IllegalArgumentException.class, () -> {
            new DeckEntity(testUserEntity, null, DECK_DESCRIPTION);
        });

        logger.debug("Test passed: Exception thrown for null name");
    }

    /**
     * Tests that creating deck with empty name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when name is empty")
    void testEmptyName() {
        logger.debug("Test: Creating deck with empty name");

        assertThrows(IllegalArgumentException.class, () -> {
            new DeckEntity(testUserEntity, "", DECK_DESCRIPTION);
        });

        logger.debug("Test passed: Exception thrown for empty name");
    }

    /**
     * Tests that creating deck with whitespace-only name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when name is whitespace only")
    void testWhitespaceName() {
        logger.debug("Test: Creating deck with whitespace-only name");

        assertThrows(IllegalArgumentException.class, () -> {
            new DeckEntity(testUserEntity, "   ", DECK_DESCRIPTION);
        });

        logger.debug("Test passed: Exception thrown for whitespace-only name");
    }

    /**
     * Tests that setting null user throws exception.
     */
    @Test
    @DisplayName("Should throw exception when setting null user")
    void testSetNullUser() {
        logger.debug("Test: Setting null user");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        assertThrows(IllegalArgumentException.class, () -> {
            deckEntity.setUser(null);
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

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        assertThrows(IllegalArgumentException.class, () -> {
            deckEntity.setName(null);
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

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        assertThrows(IllegalArgumentException.class, () -> {
            deckEntity.setName("");
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

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        assertThrows(IllegalArgumentException.class, () -> {
            deckEntity.setName("   ");
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

        DeckEntity deckEntity = new DeckEntity(testUserEntity, "  " + DECK_NAME + "  ", DECK_DESCRIPTION);

        assertEquals(DECK_NAME, deckEntity.getName());

        logger.debug("Test passed: Name trimmed successfully");
    }

    /**
     * Tests that description is trimmed in constructor.
     */
    @Test
    @DisplayName("Should trim description in constructor")
    void testDescriptionTrimmedInConstructor() {
        logger.debug("Test: Description trimming in constructor");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, "  " + DECK_DESCRIPTION + "  ");

        assertEquals(DECK_DESCRIPTION, deckEntity.getDescription());

        logger.debug("Test passed: Description trimmed successfully");
    }

    /**
     * Tests that name is trimmed in setter.
     */
    @Test
    @DisplayName("Should trim name in setter")
    void testNameTrimmedInSetter() {
        logger.debug("Test: Name trimming in setter");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setName("  " + UPDATED_NAME + "  ");

        assertEquals(UPDATED_NAME, deckEntity.getName());

        logger.debug("Test passed: Name trimmed in setter");
    }

    /**
     * Tests that description is trimmed in setter.
     */
    @Test
    @DisplayName("Should trim description in setter")
    void testDescriptionTrimmedInSetter() {
        logger.debug("Test: Description trimming in setter");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setDescription("  " + UPDATED_DESCRIPTION + "  ");

        assertEquals(UPDATED_DESCRIPTION, deckEntity.getDescription());

        logger.debug("Test passed: Description trimmed in setter");
    }

    /**
     * Tests that empty description is converted to null.
     */
    @Test
    @DisplayName("Should convert empty description to null")
    void testEmptyDescriptionToNull() {
        logger.debug("Test: Converting empty description to null");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, "");

        assertNull(deckEntity.getDescription());

        logger.debug("Test passed: Empty description converted to null");
    }

    /**
     * Tests that whitespace-only description is converted to null.
     */
    @Test
    @DisplayName("Should convert whitespace description to null")
    void testWhitespaceDescriptionToNull() {
        logger.debug("Test: Converting whitespace description to null");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setDescription("   ");

        assertNull(deckEntity.getDescription());

        logger.debug("Test passed: Whitespace description converted to null");
    }

    // Editing Tests

    /**
     * Tests editing deck name.
     */
    @Test
    @DisplayName("Should allow editing deck name")
    void testEditName() {
        logger.debug("Test: Editing deck name from '{}' to '{}'", DECK_NAME, UPDATED_NAME);

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setName(UPDATED_NAME);

        assertEquals(UPDATED_NAME, deckEntity.getName());
        assertEquals(DECK_DESCRIPTION, deckEntity.getDescription()); // Description unchanged

        logger.debug("Test passed: Name edited successfully");
    }

    /**
     * Tests editing deck description.
     */
    @Test
    @DisplayName("Should allow editing deck description")
    void testEditDescription() {
        logger.debug("Test: Editing deck description");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setDescription(UPDATED_DESCRIPTION);

        assertEquals(DECK_NAME, deckEntity.getName()); // Name unchanged
        assertEquals(UPDATED_DESCRIPTION, deckEntity.getDescription());

        logger.debug("Test passed: Description edited successfully");
    }

    /**
     * Tests clearing deck description by setting to null.
     */
    @Test
    @DisplayName("Should allow clearing deck description")
    void testClearDescription() {
        logger.debug("Test: Clearing deck description");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        assertEquals(DECK_DESCRIPTION, deckEntity.getDescription());

        deckEntity.setDescription(null);
        assertNull(deckEntity.getDescription());

        logger.debug("Test passed: Description cleared successfully");
    }

    /**
     * Tests editing all deck fields at once.
     */
    @Test
    @DisplayName("Should allow editing all fields")
    void testEditAllFields() {
        logger.debug("Test: Editing all deck fields");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        deckEntity.setName(UPDATED_NAME);
        deckEntity.setDescription(UPDATED_DESCRIPTION);

        assertEquals(UPDATED_NAME, deckEntity.getName());
        assertEquals(UPDATED_DESCRIPTION, deckEntity.getDescription());

        logger.debug("Test passed: All fields edited successfully");
    }

    // User Association Tests

    /**
     * Tests changing deck owner.
     */
    @Test
    @DisplayName("Should allow changing deck owner")
    void testChangeOwner() {
        logger.debug("Test: Changing deck owner");

        UserEntity newUserEntity = new UserEntity("other@example.com", PASSWORD_HASH);
        newUserEntity.setId(2L);

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        assertEquals(testUserEntity, deckEntity.getUser());

        deckEntity.setUser(newUserEntity);
        assertEquals(newUserEntity, deckEntity.getUser());

        logger.debug("Test passed: Owner changed successfully");
    }

    // Equality and HashCode Tests

    /**
     * Tests deck equality based on ID and name.
     */
    @Test
    @DisplayName("Should consider decks equal with same ID and name")
    void testDeckEquality() {
        logger.debug("Test: Testing deck equality");

        DeckEntity deckEntity1 = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity1.setId(1L);

        DeckEntity deckEntity2 = new DeckEntity(testUserEntity, DECK_NAME, "Different description");
        deckEntity2.setId(1L);

        DeckEntity deckEntity3 = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity3.setId(2L);

        // Same ID and name = equal
        assertEquals(deckEntity1, deckEntity2);

        // Different ID = not equal
        assertNotEquals(deckEntity1, deckEntity3);

        logger.debug("Test passed: Equality based on ID and name works correctly");
    }

    /**
     * Tests reflexive property of equals.
     */
    @Test
    @DisplayName("Should be equal to itself")
    void testEqualityReflexive() {
        logger.debug("Test: Testing reflexive equality");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setId(1L);

        assertEquals(deckEntity, deckEntity);

        logger.debug("Test passed: Deck equals itself");
    }

    /**
     * Tests that deck is not equal to null.
     */
    @Test
    @DisplayName("Should not be equal to null")
    void testEqualityNull() {
        logger.debug("Test: Testing equality with null");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setId(1L);

        assertNotEquals(null, deckEntity);

        logger.debug("Test passed: Deck not equal to null");
    }

    /**
     * Tests that deck is not equal to different class.
     */
    @Test
    @DisplayName("Should not be equal to different class")
    void testEqualityDifferentClass() {
        logger.debug("Test: Testing equality with different class");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setId(1L);

        assertNotEquals(deckEntity, "Not a Deck");

        logger.debug("Test passed: Deck not equal to different class");
    }

    /**
     * Tests that decks without IDs are only equal to themselves.
     */
    @Test
    @DisplayName("Should handle equality for decks without IDs")
    void testEqualityWithoutIds() {
        logger.debug("Test: Testing equality for decks without IDs");

        DeckEntity deckEntity1 = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        DeckEntity deckEntity2 = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        // Same instance
        assertEquals(deckEntity1, deckEntity1);

        // Different instances without IDs are not equal
        assertNotEquals(deckEntity1, deckEntity2);

        logger.debug("Test passed: Decks without IDs handled correctly");
    }

    /**
     * Tests hash code consistency with equals.
     */
    @Test
    @DisplayName("Should have consistent hash code with equals")
    void testHashCodeConsistency() {
        logger.debug("Test: Testing hash code consistency");

        DeckEntity deckEntity1 = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity1.setId(1L);

        DeckEntity deckEntity2 = new DeckEntity(testUserEntity, DECK_NAME, "Different description");
        deckEntity2.setId(1L);

        assertEquals(deckEntity1.hashCode(), deckEntity2.hashCode());

        logger.debug("Test passed: Hash code consistent with equals");
    }

    // toString Tests

    /**
     * Tests that toString includes deck details.
     */
    @Test
    @DisplayName("Should include deck details in toString")
    void testToStringIncludesDetails() {
        logger.debug("Test: Testing toString includes details");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        deckEntity.setId(1L);

        String deckString = deckEntity.toString();

        assertTrue(deckString.contains(DECK_NAME));
        assertTrue(deckString.contains(DECK_DESCRIPTION));
        assertTrue(deckString.contains("id=1"));

        logger.debug("Test passed: toString includes deck details");
    }

    /**
     * Tests that toString does not expose sensitive user data.
     */
    @Test
    @DisplayName("Should not expose sensitive user data in toString")
    void testToStringDoesNotExposeSensitiveData() {
        logger.debug("Test: Testing toString does not expose sensitive data");

        UserEntity sensitiveUserEntity = new UserEntity("secret@example.com", "secretPassword");
        sensitiveUserEntity.setId(1L);
        DeckEntity deckEntity = new DeckEntity(sensitiveUserEntity, DECK_NAME, DECK_DESCRIPTION);

        String deckString = deckEntity.toString();

        assertTrue(deckString.contains(DECK_NAME));
        assertTrue(deckString.contains("userId=1")); // Shows ID only
        assertFalse(deckString.contains("secret@example.com"));
        assertFalse(deckString.contains("secretPassword"));

        logger.debug("Test passed: Sensitive data not in toString");
    }

    // Getter/Setter Tests

    /**
     * Tests version field getter and setter.
     */
    @Test
    @DisplayName("Should get and set version")
    void testVersionGetterSetter() {
        logger.debug("Test: Testing version getter and setter");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        assertNull(deckEntity.getVersion());

        deckEntity.setVersion(1L);
        assertEquals(1L, deckEntity.getVersion());

        logger.debug("Test passed: Version getter and setter work");
    }

    /**
     * Tests ID field getter and setter.
     */
    @Test
    @DisplayName("Should get and set ID")
    void testIdGetterSetter() {
        logger.debug("Test: Testing ID getter and setter");

        DeckEntity deckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);

        assertNull(deckEntity.getId());

        deckEntity.setId(100L);
        assertEquals(100L, deckEntity.getId());

        logger.debug("Test passed: ID getter and setter work");
    }
}