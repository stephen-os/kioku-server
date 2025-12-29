package com.kioku.api.service;

import com.kioku.api.entity.Deck;
import com.kioku.api.entity.User;
import com.kioku.api.repository.DeckRepository;
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
class DeckServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private UserService userService;

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

        testUser = userService.createUser("test@example.com", "hashedPassword");
        otherUser = userService.createUser("other@example.com", "hashedPassword");
    }

    @Test
    void testCreateDeck() {
        // When
        Deck deck = deckService.createDeck(testUser.getId(), "Japanese Verbs", "JLPT N5");

        // Then
        assertNotNull(deck.getId());
        assertEquals("Japanese Verbs", deck.getName());
        assertEquals("JLPT N5", deck.getDescription());
        assertEquals(testUser.getId(), deck.getUser().getId());
    }

    @Test
    void testCreateDeckWithDuplicateNameThrowsException() {
        // Given
        deckService.createDeck(testUser.getId(), "Japanese Verbs", "Description 1");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.createDeck(testUser.getId(), "Japanese Verbs", "Description 2");
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
        deckService.createDeck(testUser.getId(), "Deck 1", "Description 1");
        deckService.createDeck(testUser.getId(), "Deck 2", "Description 2");
        deckService.createDeck(otherUser.getId(), "Other Deck", "Other description");

        // When
        List<Deck> userDecks = deckService.getUserDecks(testUser.getId());

        // Then
        assertEquals(2, userDecks.size());
        assertTrue(userDecks.stream().allMatch(d -> d.getUser().getId().equals(testUser.getId())));
    }

    @Test
    void testGetDeck() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");

        // When
        Optional<Deck> found = deckService.getDeck(deck.getId(), testUser.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("My Deck", found.get().getName());
    }

    @Test
    void testGetDeckWithWrongUserReturnsEmpty() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");

        // When
        Optional<Deck> found = deckService.getDeck(deck.getId(), otherUser.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testGetDeckOrThrow() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");

        // When
        Deck found = deckService.getDeckOrThrow(deck.getId(), testUser.getId());

        // Then
        assertNotNull(found);
        assertEquals("My Deck", found.getName());
    }

    @Test
    void testGetDeckOrThrowWithWrongUserThrowsException() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.getDeckOrThrow(deck.getId(), otherUser.getId());
        });
    }

    @Test
    void testUpdateDeck() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "Original Name", "Original Description");

        // When
        Deck updated = deckService.updateDeck(
                deck.getId(),
                testUser.getId(),
                "New Name",
                "New Description"
        );

        // Then
        assertEquals(deck.getId(), updated.getId());
        assertEquals("New Name", updated.getName());
        assertEquals("New Description", updated.getDescription());
    }

    @Test
    void testUpdateDeckWithWrongUserThrowsException() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.updateDeck(deck.getId(), otherUser.getId(), "New Name", "New Description");
        });
    }

    @Test
    void testUpdateDeckToExistingNameThrowsException() {
        // Given
        deckService.createDeck(testUser.getId(), "Deck A", "Description A");
        Deck deckB = deckService.createDeck(testUser.getId(), "Deck B", "Description B");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.updateDeck(deckB.getId(), testUser.getId(), "Deck A", "New Description");
        });
    }

    @Test
    void testUpdateDeckKeepingSameNameSucceeds() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Original Description");

        // When - Update description but keep same name
        Deck updated = deckService.updateDeck(
                deck.getId(),
                testUser.getId(),
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
        Deck deck = deckService.createDeck(testUser.getId(), "To Delete", "Description");
        Long deckId = deck.getId();

        // When
        deckService.deleteDeck(deckId, testUser.getId());

        // Then
        assertFalse(deckRepository.existsById(deckId));
    }

    @Test
    void testDeleteDeckWithWrongUserThrowsException() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.deleteDeck(deck.getId(), otherUser.getId());
        });
    }

    @Test
    void testUserOwnsDeck() {
        // Given
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");

        // When & Then
        assertTrue(deckService.userOwnsDeck(deck.getId(), testUser.getId()));
        assertFalse(deckService.userOwnsDeck(deck.getId(), otherUser.getId()));
        assertFalse(deckService.userOwnsDeck(999L, testUser.getId()));
    }
}