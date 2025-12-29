package com.kioku.api.repository;

import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
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
class TagRepositoryTest {

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

        testUser = new User("test@example.com", "hashedPassword");
        testUser = userRepository.save(testUser);

        otherUser = new User("other@example.com", "hashedPassword");
        otherUser = userRepository.save(otherUser);
    }

    @Test
    void testSaveTag() {
        // Given
        Tag tag = new Tag(testUser, "verbs");

        // When
        Tag savedTag = tagRepository.save(tag);

        // Then
        assertNotNull(savedTag.getId());
        assertEquals("verbs", savedTag.getName());
        assertEquals(testUser.getId(), savedTag.getUser().getId());
    }

    @Test
    void testFindByUserId() {
        // Given
        Tag tag1 = new Tag(testUser, "verbs");
        Tag tag2 = new Tag(testUser, "nouns");
        Tag otherTag = new Tag(otherUser, "adjectives");

        tagRepository.save(tag1);
        tagRepository.save(tag2);
        tagRepository.save(otherTag);

        // When
        List<Tag> userTags = tagRepository.findByUserId(testUser.getId());

        // Then
        assertEquals(2, userTags.size());
        assertTrue(userTags.stream().allMatch(t -> t.getUser().getId().equals(testUser.getId())));
    }

    @Test
    void testFindByUserIdAndName() {
        // Given
        Tag tag = new Tag(testUser, "verbs");
        tagRepository.save(tag);

        // When
        Optional<Tag> found = tagRepository.findByUserIdAndName(testUser.getId(), "verbs");

        // Then
        assertTrue(found.isPresent());
        assertEquals("verbs", found.get().getName());
    }

    @Test
    void testFindByUserIdAndNameNotFound() {
        // When
        Optional<Tag> found = tagRepository.findByUserIdAndName(testUser.getId(), "nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUserIdAndName() {
        // Given
        Tag tag = new Tag(testUser, "verbs");
        tagRepository.save(tag);

        // When & Then
        assertTrue(tagRepository.existsByUserIdAndName(testUser.getId(), "verbs"));
        assertFalse(tagRepository.existsByUserIdAndName(testUser.getId(), "nouns"));
    }

    @Test
    void testDuplicateTagNameForSameUserThrowsException() {
        // Given
        Tag tag1 = new Tag(testUser, "verbs");
        tagRepository.save(tag1);

        // When
        Tag tag2 = new Tag(testUser, "verbs");

        // Then
        assertThrows(Exception.class, () -> {
            tagRepository.save(tag2);
            tagRepository.flush();
        });
    }

    @Test
    void testSameTagNameForDifferentUsersIsAllowed() {
        // Given
        Tag tag1 = new Tag(testUser, "verbs");
        Tag tag2 = new Tag(otherUser, "verbs");

        // When & Then
        assertDoesNotThrow(() -> {
            tagRepository.save(tag1);
            tagRepository.save(tag2);
            tagRepository.flush();
        });

        // Verify both saved
        assertEquals(1, tagRepository.findByUserId(testUser.getId()).size());
        assertEquals(1, tagRepository.findByUserId(otherUser.getId()).size());
    }

    @Test
    void testDeleteTag() {
        // Given
        Tag tag = new Tag(testUser, "verbs");
        tag = tagRepository.save(tag);
        Long tagId = tag.getId();

        // When
        tagRepository.delete(tag);

        // Then
        assertFalse(tagRepository.existsById(tagId));
    }

    @Test
    void testFindByUserIdEmptyList() {
        // When
        List<Tag> tags = tagRepository.findByUserId(testUser.getId());

        // Then
        assertTrue(tags.isEmpty());
    }
}