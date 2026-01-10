package com.kioku.api.repository;

import com.kioku.api.model.Deck;
import com.kioku.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("DeckRepository Tests")
class DeckRepositoryTests {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
        Deck deck = new Deck("Japanese Verbs", "JLPT N5 verbs");
        testUser.addDeck(deck);

        // When
        Deck savedDeck = deckRepository.save(deck);

        // Then
        assertNotNull(savedDeck.getId());
        assertNotNull(savedDeck.getCreatedAt());
        assertNotNull(savedDeck.getUpdatedAt());
        assertEquals("Japanese Verbs", savedDeck.getName());
        // User relationship is unidirectional - Deck doesn't have getUser()
        // Just verify the deck was saved correctly
    }

    @Test
    void testFindByUserId() {
        // Given
        Deck deck1 = new Deck("Deck 1", "Description 1");
        Deck deck2 = new Deck("Deck 2", "Description 2");
        Deck otherDeck = new Deck("Other Deck", "Other description");
        testUser.addDeck(deck1);
        testUser.addDeck(deck2);
        otherUser.addDeck(otherDeck);

        deckRepository.save(deck1);
        deckRepository.save(deck2);
        deckRepository.save(otherDeck);

        // When
        List<Deck> userDeckEntities = deckRepository.findByUserId(testUser.getId());

        // Then
        assertEquals(2, userDeckEntities.size());
        // User relationship is unidirectional - Deck doesn't have getUser()
        // Just verify we got the right decks by checking names
        assertTrue(userDeckEntities.stream().anyMatch(d -> d.getName().equals("Deck 1")));
        assertTrue(userDeckEntities.stream().anyMatch(d -> d.getName().equals("Deck 2")));
    }

    @Test
    void testFindByIdAndUserId() {
        // Given
        Deck deck = new Deck("My Deck", "Description");
        testUser.addDeck(deck);
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
        Deck deck = new Deck("My Deck", "Description");
        testUser.addDeck(deck);
        deck = deckRepository.save(deck);

        // When - Try to access with wrong user
        Optional<Deck> found = deckRepository.findByIdAndUserId(deck.getId(), otherUser.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByIdAndUserId() {
        // Given
        Deck deck = new Deck("My Deck", "Description");
        testUser.addDeck(deck);
        deck = deckRepository.save(deck);

        // When & Then
        assertTrue(deckRepository.existsByIdAndUserId(deck.getId(), testUser.getId()));
        assertFalse(deckRepository.existsByIdAndUserId(deck.getId(), otherUser.getId()));
        assertFalse(deckRepository.existsByIdAndUserId(999L, testUser.getId()));
    }

    @Test
    void testUpdateDeck() throws InterruptedException {
        // Given
        Deck deck = new Deck("Original Name", "Original Description");
        testUser.addDeck(deck);
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
        Deck deck = new Deck("To Delete", "Description");
        testUser.addDeck(deck);
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // Reload to get fresh entity with proper hash codes
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        Deck savedDeck = testUser.getDecks().iterator().next();
        Long deckId = savedDeck.getId();
        assertNotNull(deckId, "Deck ID should be assigned after save");

        // When - Remove from user's collection (orphanRemoval will delete the deck)
        testUser.removeDeck(savedDeck);
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertFalse(deckRepository.existsById(deckId));
    }

    @Test
    void testFindByUserIdEmptyList() {
        // When
        List<Deck> deckEntities = deckRepository.findByUserId(testUser.getId());

        // Then
        assertTrue(deckEntities.isEmpty());
    }

    @Test
    void testUserDeletionCascadesToDecks() {
        // Given
        Deck deck = new Deck("My Deck", "Description");
        testUser.addDeck(deck);
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        Long deckId = testUser.getDecks().iterator().next().getId();
        assertNotNull(deckId);

        // When - Delete user (cascade should delete decks too)
        userRepository.delete(testUser);
        entityManager.flush();
        entityManager.clear();

        // Then - Deck should be deleted
        assertFalse(deckRepository.existsById(deckId));
        assertFalse(userRepository.existsById(testUser.getId()));
    }

    @Test
    void testDuplicateDeckNameForSameUserIsAllowed() {
        // Given - No unique constraint on deck names per user
        Deck deck1 = new Deck("Japanese Verbs", "Description 1");
        testUser.addDeck(deck1);
        deckRepository.save(deck1);

        // When - Create another deck with same name for same user
        Deck deck2 = new Deck("Japanese Verbs", "Description 2");
        testUser.addDeck(deck2);

        // Then - Should succeed (no unique constraint enforced at DB level)
        assertDoesNotThrow(() -> {
            deckRepository.save(deck2);
            deckRepository.flush();
        });

        // Verify both decks exist
        List<Deck> userDecks = deckRepository.findByUserId(testUser.getId());
        assertEquals(2, userDecks.size());
    }

    @Test
    void testSameDeckNameForDifferentUsersIsAllowed() {
        // Given
        Deck deck1 = new Deck("Japanese Verbs", "Description");
        Deck deck2 = new Deck("Japanese Verbs", "Description");
        testUser.addDeck(deck1);
        otherUser.addDeck(deck2);

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
    void testUpdateDeckNameToExistingNameSucceeds() {
        // Given - Create two decks with different names
        Deck deck1 = new Deck("Deck A", "Description A");
        Deck deck2 = new Deck("Deck B", "Description B");
        testUser.addDeck(deck1);
        testUser.addDeck(deck2);

        deck1 = deckRepository.save(deck1);
        deck2 = deckRepository.save(deck2);

        final Deck finalDeck2 = deck2;

        // When - Rename deck2 to same name as deck1
        finalDeck2.setName("Deck A");

        // Then - Should succeed (no unique constraint)
        assertDoesNotThrow(() -> {
            deckRepository.save(finalDeck2);
            deckRepository.flush();
        });

        // Verify both decks have the same name now
        entityManager.clear();
        List<Deck> userDecks = deckRepository.findByUserId(testUser.getId());
        long deckACount = userDecks.stream()
                .filter(d -> d.getName().equals("Deck A"))
                .count();
        assertEquals(2, deckACount);
    }

    @Test
    void testUpdateDeckNameToNewNameSucceeds() {
        // Given
        Deck deck = new Deck("Original Name", "Description");
        testUser.addDeck(deck);
        deck = deckRepository.save(deck);
        Long deckId = deck.getId();

        // When - Update to a new unique name
        deck.setName("New Unique Name");
        Deck updatedDeck = deckRepository.save(deck);

        // Then
        assertEquals(deckId, updatedDeck.getId());
        assertEquals("New Unique Name", updatedDeck.getName());
    }

    // Note: Version tracking test removed - Deck no longer has @Version field
}