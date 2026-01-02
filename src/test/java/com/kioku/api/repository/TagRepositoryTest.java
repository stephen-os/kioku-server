package com.kioku.api.repository;

import com.kioku.api.BaseIntegrationTest;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
@DisplayName("TagRepository Integration Tests")
@Transactional
class TagRepositoryTest extends BaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(TagRepositoryTest.class);

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

    private UserEntity testUserEntity;
    private UserEntity otherUserEntity;
    private DeckEntity japaneseDeckEntity;
    private DeckEntity spanishDeckEntity;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up TagRepository test: Creating users and decks");

        tagRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        testUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        testUserEntity = userRepository.save(testUserEntity);

        otherUserEntity = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        otherUserEntity = userRepository.save(otherUserEntity);

        japaneseDeckEntity = new DeckEntity(testUserEntity, DECK_NAME_1, DECK_DESCRIPTION);
        japaneseDeckEntity = deckRepository.save(japaneseDeckEntity);

        spanishDeckEntity = new DeckEntity(testUserEntity, DECK_NAME_2, DECK_DESCRIPTION);
        spanishDeckEntity = deckRepository.save(spanishDeckEntity);

        logger.debug("Test setup complete: user id={}, japanese deck id={}, spanish deck id={}",
                testUserEntity.getId(), japaneseDeckEntity.getId(), spanishDeckEntity.getId());
    }

    // Basic CRUD Tests

    /**
     * Tests saving a tag to the database.
     */
    @Test
    @DisplayName("Should save a tag")
    void testSaveTag() {
        logger.debug("Test: Saving tag to deck id={}", japaneseDeckEntity.getId());

        TagEntity tagEntity = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity savedTagEntity = tagRepository.save(tagEntity);

        assertNotNull(savedTagEntity.getId());
        assertEquals(TAG_VERBS, savedTagEntity.getName());
        assertEquals(japaneseDeckEntity.getId(), savedTagEntity.getDeck().getId());
        assertEquals(testUserEntity.getId(), savedTagEntity.getUser().getId());

        logger.debug("Test passed: Tag saved with id={}", savedTagEntity.getId());
    }

    /**
     * Tests finding a tag by ID.
     */
    @Test
    @DisplayName("Should find tag by ID")
    void testFindById() {
        logger.debug("Test: Finding tag by ID");

        TagEntity tagEntity = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity savedTagEntity = tagRepository.save(tagEntity);

        Optional<TagEntity> found = tagRepository.findById(savedTagEntity.getId());

        assertTrue(found.isPresent());
        assertEquals(TAG_VERBS, found.get().getName());

        logger.debug("Test passed: Tag found by id={}", savedTagEntity.getId());
    }

    /**
     * Tests deleting a tag.
     */
    @Test
    @DisplayName("Should delete a tag")
    void testDeleteTag() {
        logger.debug("Test: Deleting tag");

        TagEntity tagEntity = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity savedTagEntity = tagRepository.save(tagEntity);
        Long tagId = savedTagEntity.getId();

        tagRepository.delete(savedTagEntity);

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
        logger.debug("Test: Finding tags by deck id={}", japaneseDeckEntity.getId());

        TagEntity tagEntity1 = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity tagEntity2 = new TagEntity(japaneseDeckEntity, TAG_NOUNS);
        TagEntity tagEntity3 = new TagEntity(spanishDeckEntity, TAG_ADJECTIVES); // Different deck

        tagRepository.save(tagEntity1);
        tagRepository.save(tagEntity2);
        tagRepository.save(tagEntity3);

        List<TagEntity> japaneseTagEntities = tagRepository.findByDeckId(japaneseDeckEntity.getId());

        assertEquals(2, japaneseTagEntities.size());
        assertTrue(japaneseTagEntities.stream()
                .allMatch(t -> t.getDeck().getId().equals(japaneseDeckEntity.getId())));

        logger.debug("Test passed: Found {} tags in deck", japaneseTagEntities.size());
    }

    /**
     * Tests that finding tags by non-existent deck returns empty list.
     */
    @Test
    @DisplayName("Should return empty list for non-existent deck")
    void testFindByNonExistentDeck() {
        logger.debug("Test: Finding tags in non-existent deck");

        List<TagEntity> tagEntities = tagRepository.findByDeckId(999L);

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

        List<TagEntity> tagEntities = tagRepository.findByDeckId(japaneseDeckEntity.getId());

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

        TagEntity tagEntity = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity savedTagEntity = tagRepository.save(tagEntity);

        Optional<TagEntity> found = tagRepository.findByIdAndDeckId(
                savedTagEntity.getId(), japaneseDeckEntity.getId());

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

        TagEntity tagEntity = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity savedTagEntity = tagRepository.save(tagEntity);

        Optional<TagEntity> found = tagRepository.findByIdAndDeckId(
                savedTagEntity.getId(), spanishDeckEntity.getId()); // Wrong deck

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

        TagEntity tagEntity = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        tagRepository.save(tagEntity);

        assertTrue(tagRepository.existsByDeckIdAndName(japaneseDeckEntity.getId(), TAG_VERBS));
        assertFalse(tagRepository.existsByDeckIdAndName(japaneseDeckEntity.getId(), TAG_NOUNS));

        logger.debug("Test passed: Duplicate detection works");
    }

    /**
     * Tests that duplicate tag names throw exception.
     */
    @Test
    @DisplayName("Should throw exception when duplicate tag name in same deck")
    void testDuplicateTagNameInSameDeckThrowsException() {
        logger.debug("Test: Creating duplicate tag in same deck");

        TagEntity tagEntity1 = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        tagRepository.save(tagEntity1);

        TagEntity tagEntity2 = new TagEntity(japaneseDeckEntity, TAG_VERBS); // Same deck, same name

        assertThrows(Exception.class, () -> {
            tagRepository.save(tagEntity2);
            tagRepository.flush();
        });

        logger.debug("Test passed: Exception thrown for duplicate tag");
    }

    /**
     * Tests that same tag name in different decks is allowed.
     */
    @Test
    @DisplayName("Should allow same tag name in different decks")
    void testSameTagNameInDifferentDecksIsAllowed() {
        logger.debug("Test: Same tag name in different decks");

        TagEntity japaneseVerbsTagEntity = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity spanishVerbsTagEntity = new TagEntity(spanishDeckEntity, TAG_VERBS); // Same name, different deck

        assertDoesNotThrow(() -> {
            tagRepository.save(japaneseVerbsTagEntity);
            tagRepository.save(spanishVerbsTagEntity);
            tagRepository.flush();
        });

        // Verify both saved
        assertEquals(1, tagRepository.findByDeckId(japaneseDeckEntity.getId()).size());
        assertEquals(1, tagRepository.findByDeckId(spanishDeckEntity.getId()).size());

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
        TagEntity japaneseVerbsTagEntity = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        japaneseVerbsTagEntity = tagRepository.save(japaneseVerbsTagEntity);

        TagEntity spanishVerbsTagEntity = new TagEntity(spanishDeckEntity, TAG_VERBS);
        spanishVerbsTagEntity = tagRepository.save(spanishVerbsTagEntity);

        // Verify they're different tags
        assertNotEquals(japaneseVerbsTagEntity.getId(), spanishVerbsTagEntity.getId());

        // Verify deck isolation
        List<TagEntity> japaneseTagEntities = tagRepository.findByDeckId(japaneseDeckEntity.getId());
        assertEquals(1, japaneseTagEntities.size());
        assertEquals(japaneseVerbsTagEntity.getId(), japaneseTagEntities.get(0).getId());

        List<TagEntity> spanishTagEntities = tagRepository.findByDeckId(spanishDeckEntity.getId());
        assertEquals(1, spanishTagEntities.size());
        assertEquals(spanishVerbsTagEntity.getId(), spanishTagEntities.get(0).getId());

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

        TagEntity japaneseTagEntity1 = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity japaneseTagEntity2 = new TagEntity(japaneseDeckEntity, TAG_NOUNS);
        TagEntity spanishTagEntity = new TagEntity(spanishDeckEntity, TAG_ADJECTIVES);

        DeckEntity otherUserDeckEntity = new DeckEntity(otherUserEntity, "Other Deck", "Description");
        otherUserDeckEntity = deckRepository.save(otherUserDeckEntity);
        TagEntity otherUserTagEntity = new TagEntity(otherUserDeckEntity, TAG_VERBS);

        tagRepository.save(japaneseTagEntity1);
        tagRepository.save(japaneseTagEntity2);
        tagRepository.save(spanishTagEntity);
        tagRepository.save(otherUserTagEntity);

        List<TagEntity> testUserTagEntities = tagRepository.findByUserId(testUserEntity.getId());

        assertEquals(3, testUserTagEntities.size());
        assertTrue(testUserTagEntities.stream()
                .allMatch(t -> t.getUser().getId().equals(testUserEntity.getId())));

        logger.debug("Test passed: Found {} tags for user", testUserTagEntities.size());
    }

    /**
     * Tests that finding tags by user with no tags returns empty list.
     */
    @Test
    @DisplayName("Should return empty list when user has no tags")
    void testFindByUserIdEmptyList() {
        logger.debug("Test: Finding tags for user with no tags");

        List<TagEntity> tagEntities = tagRepository.findByUserId(testUserEntity.getId());

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

        TagEntity tagEntity1 = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity tagEntity2 = new TagEntity(japaneseDeckEntity, TAG_NOUNS);
        TagEntity tagEntity3 = new TagEntity(japaneseDeckEntity, TAG_ADJECTIVES);

        tagRepository.save(tagEntity1);
        tagRepository.save(tagEntity2);
        tagRepository.save(tagEntity3);

        long count = tagRepository.countByDeckId(japaneseDeckEntity.getId());

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

        long count = tagRepository.countByDeckId(japaneseDeckEntity.getId());

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

        TagEntity tagEntity1 = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity tagEntity2 = new TagEntity(japaneseDeckEntity, TAG_NOUNS);
        TagEntity spanishTagEntity = new TagEntity(spanishDeckEntity, TAG_ADJECTIVES);

        tagRepository.save(tagEntity1);
        tagRepository.save(tagEntity2);
        tagRepository.save(spanishTagEntity);

        tagRepository.deleteByDeckId(japaneseDeckEntity.getId());

        assertEquals(0, tagRepository.countByDeckId(japaneseDeckEntity.getId()));
        assertEquals(1, tagRepository.countByDeckId(spanishDeckEntity.getId())); // Spanish tag unaffected

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

        TagEntity tagEntity1 = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity tagEntity2 = new TagEntity(spanishDeckEntity, TAG_NOUNS);

        tagRepository.save(tagEntity1);
        tagRepository.save(tagEntity2);

        List<TagEntity> allTagEntities = tagRepository.findAll();

        assertEquals(2, allTagEntities.size());

        logger.debug("Test passed: Found {} tags", allTagEntities.size());
    }

    /**
     * Tests deleting all tags.
     */
    @Test
    @DisplayName("Should delete all tags")
    void testDeleteAll() {
        logger.debug("Test: Deleting all tags");

        TagEntity tagEntity1 = new TagEntity(japaneseDeckEntity, TAG_VERBS);
        TagEntity tagEntity2 = new TagEntity(spanishDeckEntity, TAG_NOUNS);

        tagRepository.save(tagEntity1);
        tagRepository.save(tagEntity2);

        tagRepository.deleteAll();

        assertEquals(0, tagRepository.count());

        logger.debug("Test passed: All tags deleted");
    }
}