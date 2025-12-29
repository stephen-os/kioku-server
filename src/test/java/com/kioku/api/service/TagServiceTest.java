package com.kioku.api.service;

import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import com.kioku.api.repository.TagRepository;
import com.kioku.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TagServiceTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userService.createUser("test@example.com", "hashedPassword");
        otherUser = userService.createUser("other@example.com", "hashedPassword");
    }

    @Test
    void testCreateTag() {
        // When
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // Then
        assertNotNull(tag.getId());
        assertEquals("verbs", tag.getName());
        assertEquals(testUser.getId(), tag.getUser().getId());
    }

    @Test
    void testCreateTagWithDuplicateNameThrowsException() {
        // Given
        tagService.createTag(testUser.getId(), "verbs");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(testUser.getId(), "verbs");
        });
    }

    @Test
    void testCreateTagWithNonExistentUserThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(999L, "verbs");
        });
    }

    @Test
    void testGetUserTags() {
        // Given
        tagService.createTag(testUser.getId(), "verbs");
        tagService.createTag(testUser.getId(), "nouns");
        tagService.createTag(otherUser.getId(), "adjectives");

        // When
        List<Tag> userTags = tagService.getUserTags(testUser.getId());

        // Then
        assertEquals(2, userTags.size());
        assertTrue(userTags.stream().allMatch(t -> t.getUser().getId().equals(testUser.getId())));
    }

    @Test
    void testGetTag() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When
        Optional<Tag> found = tagService.getTag(testUser.getId(), tag.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("verbs", found.get().getName());
    }

    @Test
    void testGetTagWithWrongUserReturnsEmpty() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When
        Optional<Tag> found = tagService.getTag(otherUser.getId(), tag.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testGetTagOrThrow() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When
        Tag found = tagService.getTagOrThrow(testUser.getId(), tag.getId());

        // Then
        assertNotNull(found);
        assertEquals("verbs", found.getName());
    }

    @Test
    void testGetTagOrThrowWithWrongUserThrowsException() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tagService.getTagOrThrow(otherUser.getId(), tag.getId());
        });
    }

    @Test
    void testGetOrCreateTag() {
        // When - Create new
        Tag tag1 = tagService.getOrCreateTag(testUser.getId(), "verbs");

        // When - Get existing
        Tag tag2 = tagService.getOrCreateTag(testUser.getId(), "verbs");

        // Then
        assertEquals(tag1.getId(), tag2.getId());
        assertEquals(1, tagService.getUserTags(testUser.getId()).size());
    }

    @Test
    void testUpdateTag() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When
        Tag updated = tagService.updateTag(testUser.getId(), tag.getId(), "all-verbs");

        // Then
        assertEquals(tag.getId(), updated.getId());
        assertEquals("all-verbs", updated.getName());
    }

    @Test
    void testUpdateTagWithWrongUserThrowsException() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(otherUser.getId(), tag.getId(), "new-name");
        });
    }

    @Test
    void testUpdateTagToExistingNameThrowsException() {
        // Given
        tagService.createTag(testUser.getId(), "verbs");
        Tag tag2 = tagService.createTag(testUser.getId(), "nouns");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(testUser.getId(), tag2.getId(), "verbs");
        });
    }

    @Test
    void testUpdateTagKeepingSameNameSucceeds() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When - Update to same name (should succeed)
        Tag updated = tagService.updateTag(testUser.getId(), tag.getId(), "verbs");

        // Then
        assertEquals("verbs", updated.getName());
    }

    @Test
    void testDeleteTag() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");
        Long tagId = tag.getId();

        // When
        tagService.deleteTag(testUser.getId(), tagId);

        // Then
        assertFalse(tagRepository.existsById(tagId));
    }

    @Test
    void testDeleteTagWithWrongUserThrowsException() {
        // Given
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tagService.deleteTag(otherUser.getId(), tag.getId());
        });
    }

    @Test
    void testFindTagByName() {
        // Given
        tagService.createTag(testUser.getId(), "verbs");

        // When
        Optional<Tag> found = tagService.findTagByName(testUser.getId(), "verbs");

        // Then
        assertTrue(found.isPresent());
        assertEquals("verbs", found.get().getName());
    }

    @Test
    void testFindTagByNameNotFound() {
        // When
        Optional<Tag> found = tagService.findTagByName(testUser.getId(), "nonexistent");

        // Then
        assertFalse(found.isPresent());
    }
}