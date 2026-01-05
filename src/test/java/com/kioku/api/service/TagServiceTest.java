package com.kioku.api.service;

import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import com.kioku.api.repository.DeckRepository;
import com.kioku.api.repository.TagRepository;
import com.kioku.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;  // ✅ ADD THIS
import org.springframework.test.context.ActiveProfiles;  // ✅ ADD THIS
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
 * <p><strong>Test Infrastructure:</strong>
 * <ul>
 *   <li>Uses H2 in-memory database for fast testing</li>
 *   <li>Loads full Spring application context</li>
 *   <li>Transactional - each test is rolled back</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TagService Tests")
class TagServiceTest {

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

    private User testUser;
    private User otherUser;
    private Deck japaneseDeck;
    private Deck spanishDeck;
    private Deck otherUserDeck;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up TagService test: Creating users and decks");

        tagRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(TEST_EMAIL, PASSWORD_HASH);
        testUser = userRepository.save(testUser);

        otherUser = new User(OTHER_EMAIL, PASSWORD_HASH);
        otherUser = userRepository.save(otherUser);

        japaneseDeck = new Deck(testUser, DECK_NAME_JAPANESE, DECK_DESCRIPTION);
        japaneseDeck = deckRepository.save(japaneseDeck);

        spanishDeck = new Deck(testUser, DECK_NAME_SPANISH, DECK_DESCRIPTION);
        spanishDeck = deckRepository.save(spanishDeck);

        otherUserDeck = new Deck(otherUser, "Other Deck", "Description");
        otherUserDeck = deckRepository.save(otherUserDeck);

        logger.debug("Test setup complete: user id={}, japanese deck id={}, spanish deck id={}",
                testUser.getId(), japaneseDeck.getId(), spanishDeck.getId());
    }

    // Tag Creation Tests

    /**
     * Tests successful tag creation.
     */
    @Test
    @DisplayName("Should create tag in deck")
    void testCreateTag() {
        logger.debug("Test: Creating tag in deck");

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        assertNotNull(tag.getId());
        assertEquals(TAG_VERBS, tag.getName());
        assertEquals(japaneseDeck.getId(), tag.getDeck().getId());
        assertEquals(testUser.getId(), tag.getUser().getId());

        logger.debug("Test passed: Tag created with id={}", tag.getId());
    }

    /**
     * Tests that duplicate tag names in same deck throw exception.
     */
    @Test
    @DisplayName("Should throw exception for duplicate tag name in same deck")
    void testCreateTagWithDuplicateNameThrowsException() {
        logger.debug("Test: Creating duplicate tag in same deck");

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
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

        Tag japaneseTag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        Tag spanishTag = tagService.createTag(testUser.getId(), spanishDeck.getId(), TAG_VERBS);

        assertNotEquals(japaneseTag.getId(), spanishTag.getId());
        assertEquals(TAG_VERBS, japaneseTag.getName());
        assertEquals(TAG_VERBS, spanishTag.getName());

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
            tagService.createTag(testUser.getId(), 999L, TAG_VERBS);
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
            tagService.createTag(testUser.getId(), otherUserDeck.getId(), TAG_VERBS);
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

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_NOUNS);
        tagService.createTag(testUser.getId(), spanishDeck.getId(), TAG_ADJECTIVES);

        List<Tag> japaneseTagEntities = tagService.getDeckTags(testUser.getId(), japaneseDeck.getId());

        assertEquals(2, japaneseTagEntities.size());
        assertTrue(japaneseTagEntities.stream()
                .allMatch(t -> t.getDeck().getId().equals(japaneseDeck.getId())));

        logger.debug("Test passed: Found {} tags in deck", japaneseTagEntities.size());
    }

    /**
     * Tests getting all user's tags across all decks.
     */
    @Test
    @DisplayName("Should get all user's tags across all decks")
    void testGetAllUserTags() {
        logger.debug("Test: Getting all user's tags");

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_NOUNS);
        tagService.createTag(testUser.getId(), spanishDeck.getId(), TAG_ADJECTIVES);
        tagService.createTag(otherUser.getId(), otherUserDeck.getId(), TAG_VERBS);

        List<Tag> userTagEntities = tagService.getAllUserTags(testUser.getId());

        assertEquals(3, userTagEntities.size());
        assertTrue(userTagEntities.stream()
                .allMatch(t -> t.getUser().getId().equals(testUser.getId())));

        logger.debug("Test passed: Found {} tags for user", userTagEntities.size());
    }

    /**
     * Tests getting a specific tag.
     */
    @Test
    @DisplayName("Should get specific tag")
    void testGetTag() {
        logger.debug("Test: Getting specific tag");

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        Optional<Tag> found = tagService.getTag(testUser.getId(), japaneseDeck.getId(), tag.getId());

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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        Optional<Tag> found = tagService.getTag(testUser.getId(), spanishDeck.getId(), tag.getId());

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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        Optional<Tag> found = tagService.getTag(otherUser.getId(), japaneseDeck.getId(), tag.getId());

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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        Tag found = tagService.getTagOrThrow(testUser.getId(), japaneseDeck.getId(), tag.getId());

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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.getTagOrThrow(otherUser.getId(), japaneseDeck.getId(), tag.getId());
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

        Tag tag1 = tagService.getOrCreateTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        Tag tag2 = tagService.getOrCreateTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        assertEquals(tag1.getId(), tag2.getId());
        assertEquals(1, tagService.getDeckTags(testUser.getId(), japaneseDeck.getId()).size());

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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        Tag updated = tagService.updateTag(testUser.getId(), japaneseDeck.getId(), tag.getId(), TAG_UPDATED);

        assertEquals(tag.getId(), updated.getId());
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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(otherUser.getId(), japaneseDeck.getId(), tag.getId(), TAG_UPDATED);
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

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        Tag tag2 = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_NOUNS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(testUser.getId(), japaneseDeck.getId(), tag2.getId(), TAG_VERBS);
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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        Tag updated = tagService.updateTag(testUser.getId(), japaneseDeck.getId(), tag.getId(), TAG_VERBS);

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

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        Tag spanishTag = tagService.createTag(testUser.getId(), spanishDeck.getId(), TAG_NOUNS);

        // Should succeed because "verbs" exists in Japanese deck, not Spanish
        Tag updated = tagService.updateTag(testUser.getId(), spanishDeck.getId(), spanishTag.getId(), TAG_VERBS);

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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        Long tagId = tag.getId();

        tagService.deleteTag(testUser.getId(), japaneseDeck.getId(), tagId);

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

        Tag tag = tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.deleteTag(otherUser.getId(), japaneseDeck.getId(), tag.getId());
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

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        Optional<Tag> found = tagService.findTagByName(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

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

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);

        Optional<Tag> found = tagService.findTagByName(testUser.getId(), spanishDeck.getId(), TAG_VERBS);

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

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_NOUNS);
        tagService.createTag(testUser.getId(), spanishDeck.getId(), TAG_ADJECTIVES);

        long count = tagService.countTags(testUser.getId(), japaneseDeck.getId());

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

        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_VERBS);
        tagService.createTag(testUser.getId(), japaneseDeck.getId(), TAG_NOUNS);
        tagService.createTag(testUser.getId(), spanishDeck.getId(), TAG_ADJECTIVES);

        tagService.deleteAllTagsInDeck(testUser.getId(), japaneseDeck.getId());

        assertEquals(0, tagService.countTags(testUser.getId(), japaneseDeck.getId()));
        assertEquals(1, tagService.countTags(testUser.getId(), spanishDeck.getId()));

        logger.debug("Test passed: All tags in deck deleted");
    }
}