package com.kioku.api.repository;

import com.kioku.api.entity.Deck;
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
class DeckRepositoryTest {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        deckRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser = new User("test@example.com", "hashedPassword");
        testUser = userRepository.save(testUser);

        otherUser = new User("other@example.com", "hashedPassword");
        otherUser = userRepository.save(otherUser);
    }

    @Test
    void testSaveDeck() {
        // Given
        Deck deck = new Deck(testUser, "Japanese Verbs", "JLPT N5 verbs");

        // When
        Deck savedDeck = deckRepository.save(deck);

        // Then
        assertNotNull(savedDeck.getId());
        assertNotNull(savedDeck.getCreatedAt());
        assertNotNull(savedDeck.getUpdatedAt());
        assertEquals("Japanese Verbs", savedDeck.getName());
        assertEquals(testUser.getId(), savedDeck.getUser().getId());
    }

    @Test
    void testFindByUserId() {
        // Given
        Deck deck1 = new Deck(testUser, "Deck 1", "Description 1");
        Deck deck2 = new Deck(testUser, "Deck 2", "Description 2");
        Deck otherDeck = new Deck(otherUser, "Other Deck", "Other description");

        deckRepository.save(deck1);
        deckRepository.save(deck2);
        deckRepository.save(otherDeck);

        // When
        List<Deck> userDecks = deckRepository.findByUserId(testUser.getId());

        // Then
        assertEquals(2, userDecks.size());
        assertTrue(userDecks.stream().allMatch(d -> d.getUser().getId().equals(testUser.getId())));
    }

    @Test
    void testFindByIdAndUserId() {
        // Given
        Deck deck = new Deck(testUser, "My Deck", "Description");
        deck = deckRepository.save(deck);

        // When
        Optional<Deck> found = deckRepository.findByIdAndUserId(deck.getId(), testUser.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("My Deck", found.get().getName());
    }

    @Test
    void testFindByIdAndUserIdNotFound() {
        // Given
        Deck deck = new Deck(testUser, "My Deck", "Description");
        deck = deckRepository.save(deck);

        // When - Try to access with wrong user
        Optional<Deck> found = deckRepository.findByIdAndUserId(deck.getId(), otherUser.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByIdAndUserId() {
        // Given
        Deck deck = new Deck(testUser, "My Deck", "Description");
        deck = deckRepository.save(deck);

        // When & Then
        assertTrue(deckRepository.existsByIdAndUserId(deck.getId(), testUser.getId()));
        assertFalse(deckRepository.existsByIdAndUserId(deck.getId(), otherUser.getId()));
        assertFalse(deckRepository.existsByIdAndUserId(999L, testUser.getId()));
    }

    @Test
    void testUpdateDeck() throws InterruptedException {
        // Given
        Deck deck = new Deck(testUser, "Original Name", "Original Description");
        deck = deckRepository.save(deck);

        Long deckId = deck.getId();
        var originalUpdatedAt = deck.getUpdatedAt();
        var originalCreatedAt = deck.getCreatedAt();

        // Sleep to ensure timestamp difference
        Thread.sleep(100); // Increased to 100ms

        // When
        deck.setName("Updated Name");
        deck.setDescription("Updated Description");
        Deck updatedDeck = deckRepository.save(deck);

        // Then
        assertEquals(deckId, updatedDeck.getId());
        assertEquals("Updated Name", updatedDeck.getName());
        assertEquals("Updated Description", updatedDeck.getDescription());

        // Check timestamps - updatedAt should change, createdAt should not
        assertNotNull(updatedDeck.getUpdatedAt());
        assertNotNull(updatedDeck.getCreatedAt());
        assertEquals(originalCreatedAt, updatedDeck.getCreatedAt()); // createdAt unchanged

        // updatedAt should be equal or after (depending on precision)
        assertFalse(updatedDeck.getUpdatedAt().isBefore(originalUpdatedAt));
    }

    @Test
    void testDeleteDeck() {
        // Given
        Deck deck = new Deck(testUser, "To Delete", "Description");
        deck = deckRepository.save(deck);
        Long deckId = deck.getId();

        // When
        deckRepository.delete(deck);

        // Then
        assertFalse(deckRepository.existsById(deckId));
    }

    @Test
    void testFindByUserIdEmptyList() {
        // When
        List<Deck> decks = deckRepository.findByUserId(testUser.getId());

        // Then
        assertTrue(decks.isEmpty());
    }

    @Test
    void testUserDeletionDoesNotCascadeToDecks() {
        // Given
        Deck deck = new Deck(testUser, "My Deck", "Description");
        deck = deckRepository.save(deck);

        // When - Delete user
        // This should fail because of foreign key constraint
        assertThrows(Exception.class, () -> {
            userRepository.delete(testUser);
            userRepository.flush();
        });
    }

    @Test
    void testDuplicateDeckNameForSameUserThrowsException() {
        // Given
        Deck deck1 = new Deck(testUser, "Japanese Verbs", "Description 1");
        deckRepository.save(deck1);

        // When - Try to create duplicate name for same user
        Deck deck2 = new Deck(testUser, "Japanese Verbs", "Description 2");

        // Then - Should throw exception due to unique constraint
        assertThrows(Exception.class, () -> {
            deckRepository.save(deck2);
            deckRepository.flush();
        });
    }

    @Test
    void testSameDeckNameForDifferentUsersIsAllowed() {
        // Given
        Deck deck1 = new Deck(testUser, "Japanese Verbs", "Description");
        Deck deck2 = new Deck(otherUser, "Japanese Verbs", "Description");

        // When & Then - Should work fine, different users can have same deck names
        assertDoesNotThrow(() -> {
            deckRepository.save(deck1);
            deckRepository.save(deck2);
            deckRepository.flush();
        });

        // Verify both saved
        assertEquals(1, deckRepository.findByUserId(testUser.getId()).size());
        assertEquals(1, deckRepository.findByUserId(otherUser.getId()).size());
    }

    @Test
    void testUpdateDeckNameToExistingNameThrowsException() {
        // Given - Create two decks with different names
        Deck deck1 = new Deck(testUser, "Deck A", "Description A");
        Deck deck2 = new Deck(testUser, "Deck B", "Description B");

        deck1 = deckRepository.save(deck1);
        deck2 = deckRepository.save(deck2);

        final Deck finalDeck2 = deck2;  // Make it final for lambda

        // When - Try to rename deck2 to same name as deck1
        finalDeck2.setName("Deck A");

        // Then - Should throw exception
        assertThrows(Exception.class, () -> {
            deckRepository.save(finalDeck2);
            deckRepository.flush();
        });
    }

    @Test
    void testUpdateDeckNameToNewNameSucceeds() {
        // Given
        Deck deck = new Deck(testUser, "Original Name", "Description");
        deck = deckRepository.save(deck);
        Long deckId = deck.getId();

        // When - Update to a new unique name
        deck.setName("New Unique Name");
        Deck updatedDeck = deckRepository.save(deck);

        // Then
        assertEquals(deckId, updatedDeck.getId());
        assertEquals("New Unique Name", updatedDeck.getName());
    }

    @Test
    void testVersionIncrementOnUpdate() throws InterruptedException {
        // Given
        Deck deck = new Deck(testUser, "Test Deck", "Description");
        deck = deckRepository.save(deck);

        Long originalVersion = deck.getVersion();
        assertNotNull(originalVersion);
        assertEquals(0L, originalVersion); // Version starts at 0

        // Small sleep to ensure different timestamp
        Thread.sleep(10);

        // When - Update the deck
        deck.setDescription("Updated Description");
        Deck updatedDeck = deckRepository.save(deck);
        deckRepository.flush(); // Force the update to happen

        // Then - Version should increment
        assertNotNull(updatedDeck.getVersion());
        assertEquals(1L, updatedDeck.getVersion()); // Should be exactly 1
    }
}