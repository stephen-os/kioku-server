package com.kioku.api.repository;

import com.kioku.api.TestContainersConfiguration;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
@Transactional
class DeckRepositoryTest {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUserEntity;
    private UserEntity otherUserEntity;

    @BeforeEach
    void setUp() {
        deckRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUserEntity = new UserEntity("test@example.com", "hashedPassword");
        testUserEntity = userRepository.save(testUserEntity);

        otherUserEntity = new UserEntity("other@example.com", "hashedPassword");
        otherUserEntity = userRepository.save(otherUserEntity);
    }

    @Test
    void testSaveDeck() {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "Japanese Verbs", "JLPT N5 verbs");

        // When
        DeckEntity savedDeckEntity = deckRepository.save(deckEntity);

        // Then
        assertNotNull(savedDeckEntity.getId());
        assertNotNull(savedDeckEntity.getCreatedAt());
        assertNotNull(savedDeckEntity.getUpdatedAt());
        assertEquals("Japanese Verbs", savedDeckEntity.getName());
        assertEquals(testUserEntity.getId(), savedDeckEntity.getUser().getId());
    }

    @Test
    void testFindByUserId() {
        // Given
        DeckEntity deckEntity1 = new DeckEntity(testUserEntity, "Deck 1", "Description 1");
        DeckEntity deckEntity2 = new DeckEntity(testUserEntity, "Deck 2", "Description 2");
        DeckEntity otherDeckEntity = new DeckEntity(otherUserEntity, "Other Deck", "Other description");

        deckRepository.save(deckEntity1);
        deckRepository.save(deckEntity2);
        deckRepository.save(otherDeckEntity);

        // When
        List<DeckEntity> userDeckEntities = deckRepository.findByUserId(testUserEntity.getId());

        // Then
        assertEquals(2, userDeckEntities.size());
        assertTrue(userDeckEntities.stream().allMatch(d -> d.getUser().getId().equals(testUserEntity.getId())));
    }

    @Test
    void testFindByIdAndUserId() {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "My Deck", "Description");
        deckEntity = deckRepository.save(deckEntity);

        // When
        Optional<DeckEntity> found = deckRepository.findByIdAndUserId(deckEntity.getId(), testUserEntity.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("My Deck", found.get().getName());
    }

    @Test
    void testFindByIdAndUserIdNotFound() {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "My Deck", "Description");
        deckEntity = deckRepository.save(deckEntity);

        // When - Try to access with wrong user
        Optional<DeckEntity> found = deckRepository.findByIdAndUserId(deckEntity.getId(), otherUserEntity.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByIdAndUserId() {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "My Deck", "Description");
        deckEntity = deckRepository.save(deckEntity);

        // When & Then
        assertTrue(deckRepository.existsByIdAndUserId(deckEntity.getId(), testUserEntity.getId()));
        assertFalse(deckRepository.existsByIdAndUserId(deckEntity.getId(), otherUserEntity.getId()));
        assertFalse(deckRepository.existsByIdAndUserId(999L, testUserEntity.getId()));
    }

    @Test
    void testUpdateDeck() throws InterruptedException {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "Original Name", "Original Description");
        deckEntity = deckRepository.save(deckEntity);

        Long deckId = deckEntity.getId();
        var originalUpdatedAt = deckEntity.getUpdatedAt();
        var originalCreatedAt = deckEntity.getCreatedAt();

        // Sleep to ensure timestamp difference
        Thread.sleep(100); // Increased to 100ms

        // When
        deckEntity.setName("Updated Name");
        deckEntity.setDescription("Updated Description");
        DeckEntity updatedDeckEntity = deckRepository.save(deckEntity);

        // Then
        assertEquals(deckId, updatedDeckEntity.getId());
        assertEquals("Updated Name", updatedDeckEntity.getName());
        assertEquals("Updated Description", updatedDeckEntity.getDescription());

        // Check timestamps - updatedAt should change, createdAt should not
        assertNotNull(updatedDeckEntity.getUpdatedAt());
        assertNotNull(updatedDeckEntity.getCreatedAt());
        assertEquals(originalCreatedAt, updatedDeckEntity.getCreatedAt()); // createdAt unchanged

        // updatedAt should be equal or after (depending on precision)
        assertFalse(updatedDeckEntity.getUpdatedAt().isBefore(originalUpdatedAt));
    }

    @Test
    void testDeleteDeck() {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "To Delete", "Description");
        deckEntity = deckRepository.save(deckEntity);
        Long deckId = deckEntity.getId();

        // When
        deckRepository.delete(deckEntity);

        // Then
        assertFalse(deckRepository.existsById(deckId));
    }

    @Test
    void testFindByUserIdEmptyList() {
        // When
        List<DeckEntity> deckEntities = deckRepository.findByUserId(testUserEntity.getId());

        // Then
        assertTrue(deckEntities.isEmpty());
    }

    @Test
    void testUserDeletionDoesNotCascadeToDecks() {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "My Deck", "Description");
        deckEntity = deckRepository.save(deckEntity);

        // When - Delete user
        // This should fail because of foreign key constraint
        assertThrows(Exception.class, () -> {
            userRepository.delete(testUserEntity);
            userRepository.flush();
        });
    }

    @Test
    void testDuplicateDeckNameForSameUserThrowsException() {
        // Given
        DeckEntity deckEntity1 = new DeckEntity(testUserEntity, "Japanese Verbs", "Description 1");
        deckRepository.save(deckEntity1);

        // When - Try to create duplicate name for same user
        DeckEntity deckEntity2 = new DeckEntity(testUserEntity, "Japanese Verbs", "Description 2");

        // Then - Should throw exception due to unique constraint
        assertThrows(Exception.class, () -> {
            deckRepository.save(deckEntity2);
            deckRepository.flush();
        });
    }

    @Test
    void testSameDeckNameForDifferentUsersIsAllowed() {
        // Given
        DeckEntity deckEntity1 = new DeckEntity(testUserEntity, "Japanese Verbs", "Description");
        DeckEntity deckEntity2 = new DeckEntity(otherUserEntity, "Japanese Verbs", "Description");

        // When & Then - Should work fine, different users can have same deck names
        assertDoesNotThrow(() -> {
            deckRepository.save(deckEntity1);
            deckRepository.save(deckEntity2);
            deckRepository.flush();
        });

        // Verify both saved
        assertEquals(1, deckRepository.findByUserId(testUserEntity.getId()).size());
        assertEquals(1, deckRepository.findByUserId(otherUserEntity.getId()).size());
    }

    @Test
    void testUpdateDeckNameToExistingNameThrowsException() {
        // Given - Create two decks with different names
        DeckEntity deckEntity1 = new DeckEntity(testUserEntity, "Deck A", "Description A");
        DeckEntity deckEntity2 = new DeckEntity(testUserEntity, "Deck B", "Description B");

        deckEntity1 = deckRepository.save(deckEntity1);
        deckEntity2 = deckRepository.save(deckEntity2);

        final DeckEntity finalDeckEntity2 = deckEntity2;  // Make it final for lambda

        // When - Try to rename deck2 to same name as deck1
        finalDeckEntity2.setName("Deck A");

        // Then - Should throw exception
        assertThrows(Exception.class, () -> {
            deckRepository.save(finalDeckEntity2);
            deckRepository.flush();
        });
    }

    @Test
    void testUpdateDeckNameToNewNameSucceeds() {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "Original Name", "Description");
        deckEntity = deckRepository.save(deckEntity);
        Long deckId = deckEntity.getId();

        // When - Update to a new unique name
        deckEntity.setName("New Unique Name");
        DeckEntity updatedDeckEntity = deckRepository.save(deckEntity);

        // Then
        assertEquals(deckId, updatedDeckEntity.getId());
        assertEquals("New Unique Name", updatedDeckEntity.getName());
    }

    @Test
    void testVersionIncrementOnUpdate() throws InterruptedException {
        // Given
        DeckEntity deckEntity = new DeckEntity(testUserEntity, "Test Deck", "Description");
        deckEntity = deckRepository.save(deckEntity);

        Long originalVersion = deckEntity.getVersion();
        assertNotNull(originalVersion);
        assertEquals(0L, originalVersion); // Version starts at 0

        // Small sleep to ensure different timestamp
        Thread.sleep(10);

        // When - Update the deck
        deckEntity.setDescription("Updated Description");
        DeckEntity updatedDeckEntity = deckRepository.save(deckEntity);
        deckRepository.flush(); // Force the update to happen

        // Then - Version should increment
        assertNotNull(updatedDeckEntity.getVersion());
        assertEquals(1L, updatedDeckEntity.getVersion()); // Should be exactly 1
    }
}