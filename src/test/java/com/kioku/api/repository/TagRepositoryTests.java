package com.kioku.api.repository;

import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import com.kioku.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TagRepository.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Finding tags by deck (deck-specific isolation)</li>
 *   <li>Finding tags by ID within a deck</li>
 *   <li>Duplicate tag name detection within decks</li>
 *   <li>Tag name uniqueness per deck (not per user)</li>
 *   <li>Finding all tags across user's decks</li>
 *   <li>Tag counting and deletion</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TagRepository Tests")
class TagRepositoryTests {

    private static final Logger logger = LoggerFactory.getLogger(TagRepositoryTests.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String DECK_NAME_1 = "Japanese Vocabulary";
    private static final String DECK_NAME_2 = "Spanish Vocabulary";
    private static final String DECK_DESCRIPTION = "Language learning deck";
    private static final String TAG_VERBS = "verbs";
    private static final String TAG_NOUNS = "nouns";
    private static final String TAG_ADJECTIVES = "adjectives";

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private User otherUser;
    private Deck japaneseDeck;
    private Deck spanishDeck;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up TagRepository test: Creating users and decks");

        tagRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(TEST_EMAIL, PASSWORD_HASH);
        testUser = userRepository.save(testUser);

        otherUser = new User(OTHER_EMAIL, PASSWORD_HASH);
        otherUser = userRepository.save(otherUser);

        japaneseDeck = new Deck(DECK_NAME_1, DECK_DESCRIPTION);
        testUser.addDeck(japaneseDeck);
        japaneseDeck = deckRepository.save(japaneseDeck);

        spanishDeck = new Deck(DECK_NAME_2, DECK_DESCRIPTION);
        testUser.addDeck(spanishDeck);
        spanishDeck = deckRepository.save(spanishDeck);

        logger.debug("Test setup complete: user id={}, japanese deck id={}, spanish deck id={}",
                testUser.getId(), japaneseDeck.getId(), spanishDeck.getId());
    }

    // Basic CRUD Tests

    /**
     * Tests saving a tag to the database.
     */
    @Test
    @DisplayName("Should save a tag")
    void testSaveTag() {
        logger.debug("Test: Saving tag to deck id={}", japaneseDeck.getId());

        Tag tag = new Tag(TAG_VERBS);
        japaneseDeck.addTag(tag);
        Tag savedTag = tagRepository.save(tag);

        assertNotNull(savedTag.getId());
        assertEquals(TAG_VERBS, savedTag.getName());
        // Deck and User relationships are now unidirectional - Tag doesn't have getDeck()/getUser()

        logger.debug("Test passed: Tag saved with id={}", savedTag.getId());
    }

    /**
     * Tests finding a tag by ID.
     */
    @Test
    @DisplayName("Should find tag by ID")
    void testFindById() {
        logger.debug("Test: Finding tag by ID");

        Tag tag = new Tag(TAG_VERBS);
        japaneseDeck.addTag(tag);
        Tag savedTag = tagRepository.save(tag);

        Optional<Tag> found = tagRepository.findById(savedTag.getId());

        assertTrue(found.isPresent());
        assertEquals(TAG_VERBS, found.get().getName());

        logger.debug("Test passed: Tag found by id={}", savedTag.getId());
    }

    /**
     * Tests deleting a tag by removing it from the deck.
     * With orphanRemoval=true, removing from deck triggers deletion.
     */
    @Test
    @DisplayName("Should delete a tag")
    void testDeleteTag() {
        logger.debug("Test: Deleting tag");

        Tag tag = new Tag(TAG_VERBS);
        japaneseDeck.addTag(tag);
        deckRepository.save(japaneseDeck);
        entityManager.flush();
        entityManager.clear();

        // Reload deck to get fresh entity with proper hash codes
        japaneseDeck = deckRepository.findById(japaneseDeck.getId()).orElseThrow();
        Tag savedTag = japaneseDeck.getTags().iterator().next();
        Long tagId = savedTag.getId();
        assertNotNull(tagId, "Tag ID should be assigned after save");

        // Remove from deck - orphanRemoval will delete the tag
        japaneseDeck.removeTag(savedTag);
        deckRepository.save(japaneseDeck);
        entityManager.flush();
        entityManager.clear();

        assertFalse(tagRepository.existsById(tagId));

        logger.debug("Test passed: Tag deleted with id={}", tagId);
    }

    // Find by Deck Tests

    /**
     * Tests finding all tags in a deck.
     */
    @Test
    @DisplayName("Should find tags by deck ID")
    void testFindByDeckId() {
        logger.debug("Test: Finding tags by deck id={}", japaneseDeck.getId());

        Tag tag1 = new Tag(TAG_VERBS);
        Tag tag2 = new Tag(TAG_NOUNS);
        Tag tag3 = new Tag(TAG_ADJECTIVES); // Different deck
        japaneseDeck.addTag(tag1);
        japaneseDeck.addTag(tag2);
        spanishDeck.addTag(tag3);

        tagRepository.save(tag1);
        tagRepository.save(tag2);
        tagRepository.save(tag3);

        List<Tag> japaneseTagEntities = tagRepository.findByDeckId(japaneseDeck.getId());

        assertEquals(2, japaneseTagEntities.size());
        // Deck relationship is unidirectional - verify by tag names
        assertTrue(japaneseTagEntities.stream().anyMatch(t -> t.getName().equals(TAG_VERBS)));
        assertTrue(japaneseTagEntities.stream().anyMatch(t -> t.getName().equals(TAG_NOUNS)));

        logger.debug("Test passed: Found {} tags in deck", japaneseTagEntities.size());
    }

    /**
     * Tests that finding tags by non-existent deck returns empty list.
     */
    @Test
    @DisplayName("Should return empty list for non-existent deck")
    void testFindByNonExistentDeck() {
        logger.debug("Test: Finding tags in non-existent deck");

        List<Tag> tagEntities = tagRepository.findByDeckId(999L);

        assertTrue(tagEntities.isEmpty());

        logger.debug("Test passed: Empty list returned");
    }

    /**
     * Tests that finding tags by empty deck returns empty list.
     */
    @Test
    @DisplayName("Should return empty list for deck with no tags")
    void testFindByDeckIdEmptyList() {
        logger.debug("Test: Finding tags in deck with no tags");

        List<Tag> tagEntities = tagRepository.findByDeckId(japaneseDeck.getId());

        assertTrue(tagEntities.isEmpty());

        logger.debug("Test passed: Empty list returned");
    }

    // Find by ID and Deck Tests

    /**
     * Tests finding tag by ID and deck ID.
     */
    @Test
    @DisplayName("Should find tag by ID and deck ID")
    void testFindByIdAndDeckId() {
        logger.debug("Test: Finding tag by ID and deck ID");

        Tag tag = new Tag(TAG_VERBS);
        japaneseDeck.addTag(tag);
        Tag savedTag = tagRepository.save(tag);

        Optional<Tag> found = tagRepository.findByIdAndDeckId(
                savedTag.getId(), japaneseDeck.getId());

        assertTrue(found.isPresent());
        assertEquals(TAG_VERBS, found.get().getName());

        logger.debug("Test passed: Tag found");
    }

    /**
     * Tests that finding tag with wrong deck ID returns empty.
     */
    @Test
    @DisplayName("Should return empty when tag ID exists but deck ID is wrong")
    void testFindByIdAndWrongDeckId() {
        logger.debug("Test: Finding tag with wrong deck ID");

        Tag tag = new Tag(TAG_VERBS);
        japaneseDeck.addTag(tag);
        Tag savedTag = tagRepository.save(tag);

        Optional<Tag> found = tagRepository.findByIdAndDeckId(
                savedTag.getId(), spanishDeck.getId()); // Wrong deck

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty result for wrong deck ID");
    }

    // Duplicate Detection Tests

    /**
     * Tests duplicate tag name detection within a deck.
     */
    @Test
    @DisplayName("Should detect duplicate tag names in same deck")
    void testExistsByDeckIdAndName() {
        logger.debug("Test: Detecting duplicate tag names");

        Tag tag = new Tag(TAG_VERBS);
        japaneseDeck.addTag(tag);
        tagRepository.save(tag);

        assertTrue(tagRepository.existsByDeckIdAndName(japaneseDeck.getId(), TAG_VERBS));
        assertFalse(tagRepository.existsByDeckIdAndName(japaneseDeck.getId(), TAG_NOUNS));

        logger.debug("Test passed: Duplicate detection works");
    }

    /**
     * Tests that duplicate tag names are allowed at the database level.
     * Note: Application logic may enforce uniqueness, but DB has no constraint.
     */
    @Test
    @DisplayName("Should allow duplicate tag names at database level")
    void testDuplicateTagNameInSameDeckIsAllowedAtDbLevel() {
        logger.debug("Test: Creating duplicate tag in same deck (no DB constraint)");

        Tag tag1 = new Tag(TAG_VERBS);
        japaneseDeck.addTag(tag1);
        tagRepository.save(tag1);

        Tag tag2 = new Tag(TAG_VERBS); // Same deck, same name
        japaneseDeck.addTag(tag2);

        // No unique constraint at DB level, so this succeeds
        assertDoesNotThrow(() -> {
            tagRepository.save(tag2);
            tagRepository.flush();
        });

        // Verify both tags exist (application should enforce uniqueness if needed)
        assertEquals(2, tagRepository.findByDeckId(japaneseDeck.getId()).size());

        logger.debug("Test passed: Duplicate tag allowed at DB level");
    }

    /**
     * Tests that same tag name in different decks is allowed.
     */
    @Test
    @DisplayName("Should allow same tag name in different decks")
    void testSameTagNameInDifferentDecksIsAllowed() {
        logger.debug("Test: Same tag name in different decks");

        Tag japaneseVerbsTag = new Tag(TAG_VERBS);
        Tag spanishVerbsTag = new Tag(TAG_VERBS); // Same name, different deck
        japaneseDeck.addTag(japaneseVerbsTag);
        spanishDeck.addTag(spanishVerbsTag);

        assertDoesNotThrow(() -> {
            tagRepository.save(japaneseVerbsTag);
            tagRepository.save(spanishVerbsTag);
            tagRepository.flush();
        });

        // Verify both saved
        assertEquals(1, tagRepository.findByDeckId(japaneseDeck.getId()).size());
        assertEquals(1, tagRepository.findByDeckId(spanishDeck.getId()).size());

        logger.debug("Test passed: Same tag name allowed in different decks");
    }

    /**
     * Tests deck-specific tag isolation.
     */
    @Test
    @DisplayName("Should isolate tags by deck")
    void testDeckSpecificTagIsolation() {
        logger.debug("Test: Deck-specific tag isolation");

        // Create "verbs" tag in both decks
        Tag japaneseVerbsTag = new Tag(TAG_VERBS);
        japaneseDeck.addTag(japaneseVerbsTag);
        japaneseVerbsTag = tagRepository.save(japaneseVerbsTag);

        Tag spanishVerbsTag = new Tag(TAG_VERBS);
        spanishDeck.addTag(spanishVerbsTag);
        spanishVerbsTag = tagRepository.save(spanishVerbsTag);

        // Verify they're different tags
        assertNotEquals(japaneseVerbsTag.getId(), spanishVerbsTag.getId());

        // Verify deck isolation
        List<Tag> japaneseTagEntities = tagRepository.findByDeckId(japaneseDeck.getId());
        assertEquals(1, japaneseTagEntities.size());
        assertEquals(japaneseVerbsTag.getId(), japaneseTagEntities.get(0).getId());

        List<Tag> spanishTagEntities = tagRepository.findByDeckId(spanishDeck.getId());
        assertEquals(1, spanishTagEntities.size());
        assertEquals(spanishVerbsTag.getId(), spanishTagEntities.get(0).getId());

        logger.debug("Test passed: Tags properly isolated by deck");
    }

    // Find by User Tests

    /**
     * Tests finding all tags across user's decks.
     */
    @Test
    @DisplayName("Should find all tags across user's decks")
    void testFindByUserId() {
        logger.debug("Test: Finding all user's tags");

        Tag japaneseTag1 = new Tag(TAG_VERBS);
        Tag japaneseTag2 = new Tag(TAG_NOUNS);
        Tag spanishTag = new Tag(TAG_ADJECTIVES);
        japaneseDeck.addTag(japaneseTag1);
        japaneseDeck.addTag(japaneseTag2);
        spanishDeck.addTag(spanishTag);

        Deck otherUserDeck = new Deck("Other Deck", "Description");
        otherUser.addDeck(otherUserDeck);
        otherUserDeck = deckRepository.save(otherUserDeck);
        Tag otherUserTag = new Tag(TAG_VERBS);
        otherUserDeck.addTag(otherUserTag);

        tagRepository.save(japaneseTag1);
        tagRepository.save(japaneseTag2);
        tagRepository.save(spanishTag);
        tagRepository.save(otherUserTag);

        List<Tag> testUserTagEntities = tagRepository.findByUserId(testUser.getId());

        assertEquals(3, testUserTagEntities.size());
        // User relationship is unidirectional - verify by tag names
        assertTrue(testUserTagEntities.stream().anyMatch(t -> t.getName().equals(TAG_VERBS)));
        assertTrue(testUserTagEntities.stream().anyMatch(t -> t.getName().equals(TAG_NOUNS)));
        assertTrue(testUserTagEntities.stream().anyMatch(t -> t.getName().equals(TAG_ADJECTIVES)));

        logger.debug("Test passed: Found {} tags for user", testUserTagEntities.size());
    }

    /**
     * Tests that finding tags by user with no tags returns empty list.
     */
    @Test
    @DisplayName("Should return empty list when user has no tags")
    void testFindByUserIdEmptyList() {
        logger.debug("Test: Finding tags for user with no tags");

        List<Tag> tagEntities = tagRepository.findByUserId(testUser.getId());

        assertTrue(tagEntities.isEmpty());

        logger.debug("Test passed: Empty list returned");
    }

    // Count Tests

    /**
     * Tests counting tags in a deck.
     */
    @Test
    @DisplayName("Should count tags in deck")
    void testCountByDeckId() {
        logger.debug("Test: Counting tags in deck");

        Tag tag1 = new Tag(TAG_VERBS);
        Tag tag2 = new Tag(TAG_NOUNS);
        Tag tag3 = new Tag(TAG_ADJECTIVES);
        japaneseDeck.addTag(tag1);
        japaneseDeck.addTag(tag2);
        japaneseDeck.addTag(tag3);

        tagRepository.save(tag1);
        tagRepository.save(tag2);
        tagRepository.save(tag3);

        long count = tagRepository.countByDeckId(japaneseDeck.getId());

        assertEquals(3, count);

        logger.debug("Test passed: Counted {} tags", count);
    }

    /**
     * Tests that count returns zero for empty deck.
     */
    @Test
    @DisplayName("Should return zero count for deck with no tags")
    void testCountEmptyDeck() {
        logger.debug("Test: Counting tags in empty deck");

        long count = tagRepository.countByDeckId(japaneseDeck.getId());

        assertEquals(0, count);

        logger.debug("Test passed: Zero count for empty deck");
    }

    /**
     * Tests that count returns zero for non-existent deck.
     */
    @Test
    @DisplayName("Should return zero count for non-existent deck")
    void testCountNonExistentDeck() {
        logger.debug("Test: Counting tags in non-existent deck");

        long count = tagRepository.countByDeckId(999L);

        assertEquals(0, count);

        logger.debug("Test passed: Zero count for non-existent deck");
    }

    // Delete Tests

    /**
     * Tests deleting all tags in a deck.
     */
    @Test
    @DisplayName("Should delete all tags in deck")
    void testDeleteByDeckId() {
        logger.debug("Test: Deleting all tags in deck");

        Tag tag1 = new Tag(TAG_VERBS);
        Tag tag2 = new Tag(TAG_NOUNS);
        Tag spanishTag = new Tag(TAG_ADJECTIVES);
        japaneseDeck.addTag(tag1);
        japaneseDeck.addTag(tag2);
        spanishDeck.addTag(spanishTag);

        tagRepository.save(tag1);
        tagRepository.save(tag2);
        tagRepository.save(spanishTag);

        tagRepository.deleteByDeckId(japaneseDeck.getId());

        assertEquals(0, tagRepository.countByDeckId(japaneseDeck.getId()));
        assertEquals(1, tagRepository.countByDeckId(spanishDeck.getId())); // Spanish tag unaffected

        logger.debug("Test passed: All tags in deck deleted");
    }

    // Batch Operations Tests

    /**
     * Tests finding all tags.
     */
    @Test
    @DisplayName("Should find all tags")
    void testFindAll() {
        logger.debug("Test: Finding all tags");

        Tag tag1 = new Tag(TAG_VERBS);
        Tag tag2 = new Tag(TAG_NOUNS);
        japaneseDeck.addTag(tag1);
        spanishDeck.addTag(tag2);

        tagRepository.save(tag1);
        tagRepository.save(tag2);

        List<Tag> allTagEntities = tagRepository.findAll();

        assertEquals(2, allTagEntities.size());

        logger.debug("Test passed: Found {} tags", allTagEntities.size());
    }

    /**
     * Tests deleting all tags by clearing them from decks.
     * With orphanRemoval=true, clearing deck tag collections deletes tags.
     */
    @Test
    @DisplayName("Should delete all tags")
    void testDeleteAll() {
        logger.debug("Test: Deleting all tags");

        Tag tag1 = new Tag(TAG_VERBS);
        Tag tag2 = new Tag(TAG_NOUNS);
        japaneseDeck.addTag(tag1);
        spanishDeck.addTag(tag2);

        deckRepository.save(japaneseDeck);
        deckRepository.save(spanishDeck);
        entityManager.flush();
        entityManager.clear();

        assertEquals(2, tagRepository.count());

        // Reload and clear tags from all decks (orphanRemoval deletes them)
        japaneseDeck = deckRepository.findById(japaneseDeck.getId()).orElseThrow();
        spanishDeck = deckRepository.findById(spanishDeck.getId()).orElseThrow();
        japaneseDeck.getTags().clear();
        spanishDeck.getTags().clear();
        deckRepository.save(japaneseDeck);
        deckRepository.save(spanishDeck);
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, tagRepository.count());

        logger.debug("Test passed: All tags deleted");
    }
}