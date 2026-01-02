package com.kioku.api.service;

import com.kioku.api.BaseIntegrationTest;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.entity.UserEntity;
import com.kioku.api.repository.DeckRepository;
import com.kioku.api.repository.TagRepository;
import com.kioku.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TagService.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Tag creation within decks with ownership verification</li>
 *   <li>Tag retrieval with ownership checks</li>
 *   <li>Tag updates with duplicate prevention</li>
 *   <li>Tag deletion</li>
 *   <li>Deck-specific tag isolation</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("TagService Integration Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
@Transactional
class TagServiceTest extends BaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(TagServiceTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String DECK_NAME_JAPANESE = "Japanese Vocabulary";
    private static final String DECK_NAME_SPANISH = "Spanish Vocabulary";
    private static final String DECK_DESCRIPTION = "Language learning deck";
    private static final String TAG_VERBS = "verbs";
    private static final String TAG_NOUNS = "nouns";
    private static final String TAG_ADJECTIVES = "adjectives";
    private static final String TAG_UPDATED = "all-verbs";

    @Autowired
    private TagService tagService;

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
    private DeckEntity otherUserDeckEntity;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up TagService test: Creating users and decks");

        tagRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        testUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        testUserEntity = userRepository.save(testUserEntity);

        otherUserEntity = new UserEntity(OTHER_EMAIL, PASSWORD_HASH);
        otherUserEntity = userRepository.save(otherUserEntity);

        japaneseDeckEntity = new DeckEntity(testUserEntity, DECK_NAME_JAPANESE, DECK_DESCRIPTION);
        japaneseDeckEntity = deckRepository.save(japaneseDeckEntity);

        spanishDeckEntity = new DeckEntity(testUserEntity, DECK_NAME_SPANISH, DECK_DESCRIPTION);
        spanishDeckEntity = deckRepository.save(spanishDeckEntity);

        otherUserDeckEntity = new DeckEntity(otherUserEntity, "Other Deck", "Description");
        otherUserDeckEntity = deckRepository.save(otherUserDeckEntity);

        logger.debug("Test setup complete: user id={}, japanese deck id={}, spanish deck id={}",
                testUserEntity.getId(), japaneseDeckEntity.getId(), spanishDeckEntity.getId());
    }

    // Tag Creation Tests

    /**
     * Tests successful tag creation.
     */
    @Test
    @DisplayName("Should create tag in deck")
    void testCreateTag() {
        logger.debug("Test: Creating tag in deck");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        assertNotNull(tagEntity.getId());
        assertEquals(TAG_VERBS, tagEntity.getName());
        assertEquals(japaneseDeckEntity.getId(), tagEntity.getDeck().getId());
        assertEquals(testUserEntity.getId(), tagEntity.getUser().getId());

        logger.debug("Test passed: Tag created with id={}", tagEntity.getId());
    }

    /**
     * Tests that duplicate tag names in same deck throw exception.
     */
    @Test
    @DisplayName("Should throw exception for duplicate tag name in same deck")
    void testCreateTagWithDuplicateNameThrowsException() {
        logger.debug("Test: Creating duplicate tag in same deck");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        });

        logger.debug("Test passed: Exception thrown for duplicate tag");
    }

    /**
     * Tests that same tag name in different decks is allowed.
     */
    @Test
    @DisplayName("Should allow same tag name in different decks")
    void testCreateTagWithSameNameInDifferentDecks() {
        logger.debug("Test: Creating same tag name in different decks");

        TagEntity japaneseTagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        TagEntity spanishTagEntity = tagService.createTag(testUserEntity.getId(), spanishDeckEntity.getId(), TAG_VERBS);

        assertNotEquals(japaneseTagEntity.getId(), spanishTagEntity.getId());
        assertEquals(TAG_VERBS, japaneseTagEntity.getName());
        assertEquals(TAG_VERBS, spanishTagEntity.getName());

        logger.debug("Test passed: Same tag name allowed in different decks");
    }

    /**
     * Tests that creating tag in non-existent deck throws exception.
     */
    @Test
    @DisplayName("Should throw exception when deck not found")
    void testCreateTagWithNonExistentDeckThrowsException() {
        logger.debug("Test: Creating tag in non-existent deck");

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(testUserEntity.getId(), 999L, TAG_VERBS);
        });

        logger.debug("Test passed: Exception thrown for non-existent deck");
    }

    /**
     * Tests that creating tag in other user's deck throws exception.
     */
    @Test
    @DisplayName("Should throw exception when user doesn't own deck")
    void testCreateTagWithUnownedDeckThrowsException() {
        logger.debug("Test: Creating tag in unowned deck");

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(testUserEntity.getId(), otherUserDeckEntity.getId(), TAG_VERBS);
        });

        logger.debug("Test passed: Exception thrown for unowned deck");
    }

    // Tag Retrieval Tests

    /**
     * Tests getting all tags in a deck.
     */
    @Test
    @DisplayName("Should get all tags in deck")
    void testGetDeckTags() {
        logger.debug("Test: Getting all tags in deck");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_NOUNS);
        tagService.createTag(testUserEntity.getId(), spanishDeckEntity.getId(), TAG_ADJECTIVES);

        List<TagEntity> japaneseTagEntities = tagService.getDeckTags(testUserEntity.getId(), japaneseDeckEntity.getId());

        assertEquals(2, japaneseTagEntities.size());
        assertTrue(japaneseTagEntities.stream()
                .allMatch(t -> t.getDeck().getId().equals(japaneseDeckEntity.getId())));

        logger.debug("Test passed: Found {} tags in deck", japaneseTagEntities.size());
    }

    /**
     * Tests getting all user's tags across all decks.
     */
    @Test
    @DisplayName("Should get all user's tags across all decks")
    void testGetAllUserTags() {
        logger.debug("Test: Getting all user's tags");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_NOUNS);
        tagService.createTag(testUserEntity.getId(), spanishDeckEntity.getId(), TAG_ADJECTIVES);
        tagService.createTag(otherUserEntity.getId(), otherUserDeckEntity.getId(), TAG_VERBS);

        List<TagEntity> userTagEntities = tagService.getAllUserTags(testUserEntity.getId());

        assertEquals(3, userTagEntities.size());
        assertTrue(userTagEntities.stream()
                .allMatch(t -> t.getUser().getId().equals(testUserEntity.getId())));

        logger.debug("Test passed: Found {} tags for user", userTagEntities.size());
    }

    /**
     * Tests getting a specific tag.
     */
    @Test
    @DisplayName("Should get specific tag")
    void testGetTag() {
        logger.debug("Test: Getting specific tag");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        Optional<TagEntity> found = tagService.getTag(testUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity.getId());

        assertTrue(found.isPresent());
        assertEquals(TAG_VERBS, found.get().getName());

        logger.debug("Test passed: Tag found");
    }

    /**
     * Tests that getting tag with wrong deck returns empty.
     */
    @Test
    @DisplayName("Should return empty when tag not in specified deck")
    void testGetTagWithWrongDeckReturnsEmpty() {
        logger.debug("Test: Getting tag with wrong deck");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        Optional<TagEntity> found = tagService.getTag(testUserEntity.getId(), spanishDeckEntity.getId(), tagEntity.getId());

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty returned for wrong deck");
    }

    /**
     * Tests that getting tag with wrong user returns empty.
     */
    @Test
    @DisplayName("Should return empty when user doesn't own deck")
    void testGetTagWithWrongUserReturnsEmpty() {
        logger.debug("Test: Getting tag with wrong user");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        Optional<TagEntity> found = tagService.getTag(otherUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity.getId());

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty returned for wrong user");
    }

    /**
     * Tests getTagOrThrow success.
     */
    @Test
    @DisplayName("Should get tag with getTagOrThrow")
    void testGetTagOrThrow() {
        logger.debug("Test: Getting tag with getTagOrThrow");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        TagEntity found = tagService.getTagOrThrow(testUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity.getId());

        assertNotNull(found);
        assertEquals(TAG_VERBS, found.getName());

        logger.debug("Test passed: Tag retrieved");
    }

    /**
     * Tests getTagOrThrow throws exception for wrong user.
     */
    @Test
    @DisplayName("Should throw exception when user doesn't own tag")
    void testGetTagOrThrowWithWrongUserThrowsException() {
        logger.debug("Test: Getting tag with getTagOrThrow for wrong user");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.getTagOrThrow(otherUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity.getId());
        });

        logger.debug("Test passed: Exception thrown for wrong user");
    }

    // Get or Create Tests

    /**
     * Tests get or create tag.
     */
    @Test
    @DisplayName("Should get or create tag")
    void testGetOrCreateTag() {
        logger.debug("Test: Get or create tag");

        TagEntity tagEntity1 = tagService.getOrCreateTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        TagEntity tagEntity2 = tagService.getOrCreateTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        assertEquals(tagEntity1.getId(), tagEntity2.getId());
        assertEquals(1, tagService.getDeckTags(testUserEntity.getId(), japaneseDeckEntity.getId()).size());

        logger.debug("Test passed: Get or create works");
    }

    // Tag Update Tests

    /**
     * Tests updating tag name.
     */
    @Test
    @DisplayName("Should update tag name")
    void testUpdateTag() {
        logger.debug("Test: Updating tag name");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        TagEntity updated = tagService.updateTag(testUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity.getId(), TAG_UPDATED);

        assertEquals(tagEntity.getId(), updated.getId());
        assertEquals(TAG_UPDATED, updated.getName());

        logger.debug("Test passed: Tag updated");
    }

    /**
     * Tests that updating tag with wrong user throws exception.
     */
    @Test
    @DisplayName("Should throw exception when updating tag with wrong user")
    void testUpdateTagWithWrongUserThrowsException() {
        logger.debug("Test: Updating tag with wrong user");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(otherUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity.getId(), TAG_UPDATED);
        });

        logger.debug("Test passed: Exception thrown for wrong user");
    }

    /**
     * Tests that updating to duplicate name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when updating to duplicate name")
    void testUpdateTagToExistingNameThrowsException() {
        logger.debug("Test: Updating tag to existing name");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        TagEntity tagEntity2 = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_NOUNS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(testUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity2.getId(), TAG_VERBS);
        });

        logger.debug("Test passed: Exception thrown for duplicate name");
    }

    /**
     * Tests that updating to same name succeeds.
     */
    @Test
    @DisplayName("Should allow updating tag to same name")
    void testUpdateTagKeepingSameNameSucceeds() {
        logger.debug("Test: Updating tag to same name");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        TagEntity updated = tagService.updateTag(testUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity.getId(), TAG_VERBS);

        assertEquals(TAG_VERBS, updated.getName());

        logger.debug("Test passed: Update to same name allowed");
    }

    /**
     * Tests that same tag name in different decks doesn't conflict.
     */
    @Test
    @DisplayName("Should allow updating to name that exists in different deck")
    void testUpdateTagToNameInDifferentDeck() {
        logger.debug("Test: Updating tag to name that exists in different deck");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        TagEntity spanishTagEntity = tagService.createTag(testUserEntity.getId(), spanishDeckEntity.getId(), TAG_NOUNS);

        // Should succeed because "verbs" exists in Japanese deck, not Spanish
        TagEntity updated = tagService.updateTag(testUserEntity.getId(), spanishDeckEntity.getId(), spanishTagEntity.getId(), TAG_VERBS);

        assertEquals(TAG_VERBS, updated.getName());

        logger.debug("Test passed: Update to name in different deck allowed");
    }

    // Tag Deletion Tests

    /**
     * Tests deleting a tag.
     */
    @Test
    @DisplayName("Should delete tag")
    void testDeleteTag() {
        logger.debug("Test: Deleting tag");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        Long tagId = tagEntity.getId();

        tagService.deleteTag(testUserEntity.getId(), japaneseDeckEntity.getId(), tagId);

        assertFalse(tagRepository.existsById(tagId));

        logger.debug("Test passed: Tag deleted");
    }

    /**
     * Tests that deleting tag with wrong user throws exception.
     */
    @Test
    @DisplayName("Should throw exception when deleting tag with wrong user")
    void testDeleteTagWithWrongUserThrowsException() {
        logger.debug("Test: Deleting tag with wrong user");

        TagEntity tagEntity = tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.deleteTag(otherUserEntity.getId(), japaneseDeckEntity.getId(), tagEntity.getId());
        });

        logger.debug("Test passed: Exception thrown for wrong user");
    }

    // Find by Name Tests

    /**
     * Tests finding tag by name.
     */
    @Test
    @DisplayName("Should find tag by name in deck")
    void testFindTagByName() {
        logger.debug("Test: Finding tag by name");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        Optional<TagEntity> found = tagService.findTagByName(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        assertTrue(found.isPresent());
        assertEquals(TAG_VERBS, found.get().getName());

        logger.debug("Test passed: Tag found by name");
    }

    /**
     * Tests that finding tag by name in wrong deck returns empty.
     */
    @Test
    @DisplayName("Should return empty when tag name not in deck")
    void testFindTagByNameNotFound() {
        logger.debug("Test: Finding non-existent tag by name");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);

        Optional<TagEntity> found = tagService.findTagByName(testUserEntity.getId(), spanishDeckEntity.getId(), TAG_VERBS);

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty returned for tag not in deck");
    }

    // Count Tests

    /**
     * Tests counting tags in deck.
     */
    @Test
    @DisplayName("Should count tags in deck")
    void testCountTags() {
        logger.debug("Test: Counting tags in deck");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_NOUNS);
        tagService.createTag(testUserEntity.getId(), spanishDeckEntity.getId(), TAG_ADJECTIVES);

        long count = tagService.countTags(testUserEntity.getId(), japaneseDeckEntity.getId());

        assertEquals(2, count);

        logger.debug("Test passed: Counted {} tags", count);
    }

    // Bulk Deletion Tests

    /**
     * Tests deleting all tags in deck.
     */
    @Test
    @DisplayName("Should delete all tags in deck")
    void testDeleteAllTagsInDeck() {
        logger.debug("Test: Deleting all tags in deck");

        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_VERBS);
        tagService.createTag(testUserEntity.getId(), japaneseDeckEntity.getId(), TAG_NOUNS);
        tagService.createTag(testUserEntity.getId(), spanishDeckEntity.getId(), TAG_ADJECTIVES);

        tagService.deleteAllTagsInDeck(testUserEntity.getId(), japaneseDeckEntity.getId());

        assertEquals(0, tagService.countTags(testUserEntity.getId(), japaneseDeckEntity.getId()));
        assertEquals(1, tagService.countTags(testUserEntity.getId(), spanishDeckEntity.getId()));

        logger.debug("Test passed: All tags in deck deleted");
    }
}