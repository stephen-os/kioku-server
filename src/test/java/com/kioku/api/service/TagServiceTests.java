package com.kioku.api.service;

import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import com.kioku.api.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TagService.
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
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TagService Unit Tests")
class TagServiceTests {

    private static final Logger logger = LoggerFactory.getLogger(TagServiceTests.class);

    // Test data constants
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long DECK_ID = 100L;
    private static final Long OTHER_DECK_ID = 200L;
    private static final Long TAG_ID = 1000L;
    private static final String TAG_VERBS = "verbs";
    private static final String TAG_NOUNS = "nouns";
    private static final String TAG_ADJECTIVES = "adjectives";
    private static final String TAG_UPDATED = "all-verbs";

    @Mock
    private TagRepository tagRepository;

    @Mock
    private DeckService deckService;

    @InjectMocks
    private TagService tagService;

    private Deck mockDeck;
    private Deck mockOtherDeck;
    private Tag mockTag;

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up TagService test");

        mockDeck = mock(Deck.class);
        when(mockDeck.getId()).thenReturn(DECK_ID);

        mockOtherDeck = mock(Deck.class);
        when(mockOtherDeck.getId()).thenReturn(OTHER_DECK_ID);

        mockTag = mock(Tag.class);
        when(mockTag.getId()).thenReturn(TAG_ID);
        when(mockTag.getName()).thenReturn(TAG_VERBS);
    }

    // Tag Creation Tests

    /**
     * Tests successful tag creation.
     */
    @Test
    @DisplayName("Should create tag in deck")
    void testCreateTag() {
        logger.debug("Test: Creating tag in deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);
        when(tagRepository.existsByDeckIdAndName(DECK_ID, TAG_VERBS)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(mockTag);

        Tag tag = tagService.createTag(USER_ID, DECK_ID, TAG_VERBS);

        assertNotNull(tag);
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(tagRepository).existsByDeckIdAndName(DECK_ID, TAG_VERBS);
        verify(mockDeck).addTag(any(Tag.class));
        verify(tagRepository).save(any(Tag.class));

        logger.debug("Test passed: Tag created with id={}", tag.getId());
    }

    /**
     * Tests that duplicate tag names in same deck throw exception.
     */
    @Test
    @DisplayName("Should throw exception for duplicate tag name in same deck")
    void testCreateTagWithDuplicateNameThrowsException() {
        logger.debug("Test: Creating duplicate tag in same deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);
        when(tagRepository.existsByDeckIdAndName(DECK_ID, TAG_VERBS)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(USER_ID, DECK_ID, TAG_VERBS);
        });

        verify(tagRepository, never()).save(any(Tag.class));

        logger.debug("Test passed: Exception thrown for duplicate tag");
    }

    /**
     * Tests that creating tag in non-existent deck throws exception.
     */
    @Test
    @DisplayName("Should throw exception when deck not found")
    void testCreateTagWithNonExistentDeckThrowsException() {
        logger.debug("Test: Creating tag in non-existent deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(USER_ID, DECK_ID, TAG_VERBS);
        });

        verify(tagRepository, never()).save(any(Tag.class));

        logger.debug("Test passed: Exception thrown for non-existent deck");
    }

    /**
     * Tests that creating tag in other user's deck throws exception.
     */
    @Test
    @DisplayName("Should throw exception when user doesn't own deck")
    void testCreateTagWithUnownedDeckThrowsException() {
        logger.debug("Test: Creating tag in unowned deck");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(USER_ID, DECK_ID, TAG_VERBS);
        });

        verify(tagRepository, never()).save(any(Tag.class));

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

        Tag tag1 = mock(Tag.class);
        when(tag1.getId()).thenReturn(TAG_ID);
        when(tag1.getName()).thenReturn(TAG_VERBS);

        Tag tag2 = mock(Tag.class);
        when(tag2.getId()).thenReturn(TAG_ID + 1);
        when(tag2.getName()).thenReturn(TAG_NOUNS);

        List<Tag> tags = Arrays.asList(tag1, tag2);

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);
        when(tagRepository.findByDeckId(DECK_ID)).thenReturn(tags);

        List<Tag> result = tagService.getDeckTags(USER_ID, DECK_ID);

        assertEquals(2, result.size());
        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(tagRepository).findByDeckId(DECK_ID);

        logger.debug("Test passed: Found {} tags in deck", result.size());
    }

    /**
     * Tests getting all user's tags across all decks.
     */
    @Test
    @DisplayName("Should get all user's tags across all decks")
    void testGetAllUserTags() {
        logger.debug("Test: Getting all user's tags");

        Tag tag1 = mock(Tag.class);
        Tag tag2 = mock(Tag.class);
        Tag tag3 = mock(Tag.class);
        List<Tag> tags = Arrays.asList(tag1, tag2, tag3);

        when(tagRepository.findByUserId(USER_ID)).thenReturn(tags);

        List<Tag> result = tagService.getAllUserTags(USER_ID);

        assertEquals(3, result.size());
        verify(tagRepository).findByUserId(USER_ID);

        logger.debug("Test passed: Found {} tags for user", result.size());
    }

    /**
     * Tests getting a specific tag.
     */
    @Test
    @DisplayName("Should get specific tag")
    void testGetTag() {
        logger.debug("Test: Getting specific tag");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(tagRepository.findByIdAndDeckId(TAG_ID, DECK_ID)).thenReturn(Optional.of(mockTag));

        Optional<Tag> result = tagService.getTag(USER_ID, DECK_ID, TAG_ID);

        assertTrue(result.isPresent());
        assertEquals(TAG_VERBS, result.get().getName());
        verify(deckService).userOwnsDeck(DECK_ID, USER_ID);
        verify(tagRepository).findByIdAndDeckId(TAG_ID, DECK_ID);

        logger.debug("Test passed: Tag found");
    }

    /**
     * Tests that getting tag with wrong deck returns empty.
     */
    @Test
    @DisplayName("Should return empty when tag not in specified deck")
    void testGetTagWithWrongDeckReturnsEmpty() {
        logger.debug("Test: Getting tag with wrong deck");

        when(deckService.userOwnsDeck(OTHER_DECK_ID, USER_ID)).thenReturn(true);
        when(tagRepository.findByIdAndDeckId(TAG_ID, OTHER_DECK_ID)).thenReturn(Optional.empty());

        Optional<Tag> result = tagService.getTag(USER_ID, OTHER_DECK_ID, TAG_ID);

        assertFalse(result.isPresent());

        logger.debug("Test passed: Empty returned for wrong deck");
    }

    /**
     * Tests that getting tag with wrong user returns empty.
     */
    @Test
    @DisplayName("Should return empty when user doesn't own deck")
    void testGetTagWithWrongUserReturnsEmpty() {
        logger.debug("Test: Getting tag with wrong user");

        when(deckService.userOwnsDeck(DECK_ID, OTHER_USER_ID)).thenReturn(false);

        Optional<Tag> result = tagService.getTag(OTHER_USER_ID, DECK_ID, TAG_ID);

        assertFalse(result.isPresent());
        verify(tagRepository, never()).findByIdAndDeckId(anyLong(), anyLong());

        logger.debug("Test passed: Empty returned for wrong user");
    }

    /**
     * Tests getTagOrThrow success.
     */
    @Test
    @DisplayName("Should get tag with getTagOrThrow")
    void testGetTagOrThrow() {
        logger.debug("Test: Getting tag with getTagOrThrow");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(tagRepository.findByIdAndDeckId(TAG_ID, DECK_ID)).thenReturn(Optional.of(mockTag));

        Tag result = tagService.getTagOrThrow(USER_ID, DECK_ID, TAG_ID);

        assertNotNull(result);
        assertEquals(TAG_VERBS, result.getName());

        logger.debug("Test passed: Tag retrieved");
    }

    /**
     * Tests getTagOrThrow throws exception for wrong user.
     */
    @Test
    @DisplayName("Should throw exception when user doesn't own tag")
    void testGetTagOrThrowWithWrongUserThrowsException() {
        logger.debug("Test: Getting tag with getTagOrThrow for wrong user");

        when(deckService.userOwnsDeck(DECK_ID, OTHER_USER_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.getTagOrThrow(OTHER_USER_ID, DECK_ID, TAG_ID);
        });

        logger.debug("Test passed: Exception thrown for wrong user");
    }

    // Get or Create Tests

    /**
     * Tests get or create tag - returns existing.
     */
    @Test
    @DisplayName("Should get existing tag with getOrCreateTag")
    void testGetOrCreateTagReturnsExisting() {
        logger.debug("Test: Get or create tag - returns existing");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);
        when(tagRepository.findByDeckId(DECK_ID)).thenReturn(Arrays.asList(mockTag));

        Tag result = tagService.getOrCreateTag(USER_ID, DECK_ID, TAG_VERBS);

        assertEquals(mockTag, result);
        verify(tagRepository, never()).save(any(Tag.class));

        logger.debug("Test passed: Existing tag returned");
    }

    /**
     * Tests get or create tag - creates new.
     */
    @Test
    @DisplayName("Should create new tag with getOrCreateTag")
    void testGetOrCreateTagCreatesNew() {
        logger.debug("Test: Get or create tag - creates new");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);
        when(tagRepository.findByDeckId(DECK_ID)).thenReturn(Arrays.asList());
        when(tagRepository.existsByDeckIdAndName(DECK_ID, TAG_NOUNS)).thenReturn(false);

        Tag newTag = mock(Tag.class);
        when(newTag.getId()).thenReturn(TAG_ID + 1);
        when(newTag.getName()).thenReturn(TAG_NOUNS);
        when(tagRepository.save(any(Tag.class))).thenReturn(newTag);

        Tag result = tagService.getOrCreateTag(USER_ID, DECK_ID, TAG_NOUNS);

        assertNotNull(result);
        verify(tagRepository).save(any(Tag.class));

        logger.debug("Test passed: New tag created");
    }

    // Tag Update Tests

    /**
     * Tests updating tag name.
     */
    @Test
    @DisplayName("Should update tag name")
    void testUpdateTag() {
        logger.debug("Test: Updating tag name");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(tagRepository.findByIdAndDeckId(TAG_ID, DECK_ID)).thenReturn(Optional.of(mockTag));
        when(tagRepository.existsByDeckIdAndName(DECK_ID, TAG_UPDATED)).thenReturn(false);
        when(tagRepository.save(mockTag)).thenReturn(mockTag);

        Tag result = tagService.updateTag(USER_ID, DECK_ID, TAG_ID, TAG_UPDATED);

        assertNotNull(result);
        verify(mockTag).setName(TAG_UPDATED);
        verify(tagRepository).save(mockTag);

        logger.debug("Test passed: Tag updated");
    }

    /**
     * Tests that updating tag with wrong user throws exception.
     */
    @Test
    @DisplayName("Should throw exception when updating tag with wrong user")
    void testUpdateTagWithWrongUserThrowsException() {
        logger.debug("Test: Updating tag with wrong user");

        when(deckService.userOwnsDeck(DECK_ID, OTHER_USER_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(OTHER_USER_ID, DECK_ID, TAG_ID, TAG_UPDATED);
        });

        verify(tagRepository, never()).save(any(Tag.class));

        logger.debug("Test passed: Exception thrown for wrong user");
    }

    /**
     * Tests that updating to duplicate name throws exception.
     */
    @Test
    @DisplayName("Should throw exception when updating to duplicate name")
    void testUpdateTagToExistingNameThrowsException() {
        logger.debug("Test: Updating tag to existing name");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(tagRepository.findByIdAndDeckId(TAG_ID, DECK_ID)).thenReturn(Optional.of(mockTag));
        when(tagRepository.existsByDeckIdAndName(DECK_ID, TAG_NOUNS)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(USER_ID, DECK_ID, TAG_ID, TAG_NOUNS);
        });

        verify(tagRepository, never()).save(any(Tag.class));

        logger.debug("Test passed: Exception thrown for duplicate name");
    }

    /**
     * Tests that updating to same name succeeds.
     */
    @Test
    @DisplayName("Should allow updating tag to same name")
    void testUpdateTagKeepingSameNameSucceeds() {
        logger.debug("Test: Updating tag to same name");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(tagRepository.findByIdAndDeckId(TAG_ID, DECK_ID)).thenReturn(Optional.of(mockTag));
        when(tagRepository.save(mockTag)).thenReturn(mockTag);

        Tag result = tagService.updateTag(USER_ID, DECK_ID, TAG_ID, TAG_VERBS);

        assertNotNull(result);
        // existsByDeckIdAndName should NOT be called when name is same
        verify(tagRepository, never()).existsByDeckIdAndName(anyLong(), anyString());
        verify(tagRepository).save(mockTag);

        logger.debug("Test passed: Update to same name allowed");
    }

    // Tag Deletion Tests

    /**
     * Tests deleting a tag.
     */
    @Test
    @DisplayName("Should delete tag")
    void testDeleteTag() {
        logger.debug("Test: Deleting tag");

        when(deckService.userOwnsDeck(DECK_ID, USER_ID)).thenReturn(true);
        when(tagRepository.findByIdAndDeckId(TAG_ID, DECK_ID)).thenReturn(Optional.of(mockTag));

        tagService.deleteTag(USER_ID, DECK_ID, TAG_ID);

        verify(tagRepository).delete(mockTag);

        logger.debug("Test passed: Tag deleted");
    }

    /**
     * Tests that deleting tag with wrong user throws exception.
     */
    @Test
    @DisplayName("Should throw exception when deleting tag with wrong user")
    void testDeleteTagWithWrongUserThrowsException() {
        logger.debug("Test: Deleting tag with wrong user");

        when(deckService.userOwnsDeck(DECK_ID, OTHER_USER_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            tagService.deleteTag(OTHER_USER_ID, DECK_ID, TAG_ID);
        });

        verify(tagRepository, never()).delete(any(Tag.class));

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

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);
        when(tagRepository.findByDeckId(DECK_ID)).thenReturn(Arrays.asList(mockTag));

        Optional<Tag> result = tagService.findTagByName(USER_ID, DECK_ID, TAG_VERBS);

        assertTrue(result.isPresent());
        assertEquals(TAG_VERBS, result.get().getName());

        logger.debug("Test passed: Tag found by name");
    }

    /**
     * Tests that finding tag by name not in deck returns empty.
     */
    @Test
    @DisplayName("Should return empty when tag name not in deck")
    void testFindTagByNameNotFound() {
        logger.debug("Test: Finding non-existent tag by name");

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);
        when(tagRepository.findByDeckId(DECK_ID)).thenReturn(Arrays.asList(mockTag));

        Optional<Tag> result = tagService.findTagByName(USER_ID, DECK_ID, TAG_NOUNS);

        assertFalse(result.isPresent());

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

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);
        when(tagRepository.countByDeckId(DECK_ID)).thenReturn(5L);

        long count = tagService.countTags(USER_ID, DECK_ID);

        assertEquals(5, count);
        verify(tagRepository).countByDeckId(DECK_ID);

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

        when(deckService.getDeckOrThrow(DECK_ID, USER_ID)).thenReturn(mockDeck);

        tagService.deleteAllTagsInDeck(USER_ID, DECK_ID);

        verify(deckService).getDeckOrThrow(DECK_ID, USER_ID);
        verify(tagRepository).deleteByDeckId(DECK_ID);

        logger.debug("Test passed: All tags in deck deleted");
    }
}
