package com.kioku.api.service;

import com.kioku.api.TestContainersConfiguration;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.UserEntity;
import com.kioku.api.repository.DeckRepository;
import com.kioku.api.repository.UserRepository;
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
class DeckServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private UserService userService;

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

        testUserEntity = userService.createUser("test@example.com", "hashedPassword");
        otherUserEntity = userService.createUser("other@example.com", "hashedPassword");
    }

    @Test
    void testCreateDeck() {
        // When
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "Japanese Verbs", "JLPT N5");

        // Then
        assertNotNull(deckEntity.getId());
        assertEquals("Japanese Verbs", deckEntity.getName());
        assertEquals("JLPT N5", deckEntity.getDescription());
        assertEquals(testUserEntity.getId(), deckEntity.getUser().getId());
    }

    @Test
    void testCreateDeckWithDuplicateNameThrowsException() {
        // Given
        deckService.createDeck(testUserEntity.getId(), "Japanese Verbs", "Description 1");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.createDeck(testUserEntity.getId(), "Japanese Verbs", "Description 2");
        });
    }

    @Test
    void testCreateDeckWithNonExistentUserThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.createDeck(999L, "Deck Name", "Description");
        });
    }

    @Test
    void testGetUserDecks() {
        // Given
        deckService.createDeck(testUserEntity.getId(), "Deck 1", "Description 1");
        deckService.createDeck(testUserEntity.getId(), "Deck 2", "Description 2");
        deckService.createDeck(otherUserEntity.getId(), "Other Deck", "Other description");

        // When
        List<DeckEntity> userDeckEntities = deckService.getUserDecks(testUserEntity.getId());

        // Then
        assertEquals(2, userDeckEntities.size());
        assertTrue(userDeckEntities.stream().allMatch(d -> d.getUser().getId().equals(testUserEntity.getId())));
    }

    @Test
    void testGetDeck() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "My Deck", "Description");

        // When
        Optional<DeckEntity> found = deckService.getDeck(deckEntity.getId(), testUserEntity.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("My Deck", found.get().getName());
    }

    @Test
    void testGetDeckWithWrongUserReturnsEmpty() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "My Deck", "Description");

        // When
        Optional<DeckEntity> found = deckService.getDeck(deckEntity.getId(), otherUserEntity.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testGetDeckOrThrow() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "My Deck", "Description");

        // When
        DeckEntity found = deckService.getDeckOrThrow(deckEntity.getId(), testUserEntity.getId());

        // Then
        assertNotNull(found);
        assertEquals("My Deck", found.getName());
    }

    @Test
    void testGetDeckOrThrowWithWrongUserThrowsException() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "My Deck", "Description");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.getDeckOrThrow(deckEntity.getId(), otherUserEntity.getId());
        });
    }

    @Test
    void testUpdateDeck() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "Original Name", "Original Description");

        // When
        DeckEntity updated = deckService.updateDeck(
                deckEntity.getId(),
                testUserEntity.getId(),
                "New Name",
                "New Description"
        );

        // Then
        assertEquals(deckEntity.getId(), updated.getId());
        assertEquals("New Name", updated.getName());
        assertEquals("New Description", updated.getDescription());
    }

    @Test
    void testUpdateDeckWithWrongUserThrowsException() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "My Deck", "Description");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.updateDeck(deckEntity.getId(), otherUserEntity.getId(), "New Name", "New Description");
        });
    }

    @Test
    void testUpdateDeckToExistingNameThrowsException() {
        // Given
        deckService.createDeck(testUserEntity.getId(), "Deck A", "Description A");
        DeckEntity deckEntityB = deckService.createDeck(testUserEntity.getId(), "Deck B", "Description B");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.updateDeck(deckEntityB.getId(), testUserEntity.getId(), "Deck A", "New Description");
        });
    }

    @Test
    void testUpdateDeckKeepingSameNameSucceeds() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "My Deck", "Original Description");

        // When - Update description but keep same name
        DeckEntity updated = deckService.updateDeck(
                deckEntity.getId(),
                testUserEntity.getId(),
                "My Deck", // Same name
                "New Description"
        );

        // Then
        assertEquals("My Deck", updated.getName());
        assertEquals("New Description", updated.getDescription());
    }

    @Test
    void testDeleteDeck() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "To Delete", "Description");
        Long deckId = deckEntity.getId();

        // When
        deckService.deleteDeck(deckId, testUserEntity.getId());

        // Then
        assertFalse(deckRepository.existsById(deckId));
    }

    @Test
    void testDeleteDeckWithWrongUserThrowsException() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "My Deck", "Description");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.deleteDeck(deckEntity.getId(), otherUserEntity.getId());
        });
    }

    @Test
    void testUserOwnsDeck() {
        // Given
        DeckEntity deckEntity = deckService.createDeck(testUserEntity.getId(), "My Deck", "Description");

        // When & Then
        assertTrue(deckService.userOwnsDeck(deckEntity.getId(), testUserEntity.getId()));
        assertFalse(deckService.userOwnsDeck(deckEntity.getId(), otherUserEntity.getId()));
        assertFalse(deckService.userOwnsDeck(999L, testUserEntity.getId()));
    }
}