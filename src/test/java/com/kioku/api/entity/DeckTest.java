package com.kioku.api.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void testDeckCreation() {
        User user = new User("user@example.com", "hashedPassword");
        Deck deck = new Deck(user, "Japanese Verbs", "JLPT N5 verb conjugations");

        assertEquals("Japanese Verbs", deck.getName());
        assertEquals("JLPT N5 verb conjugations", deck.getDescription());
        assertEquals(user, deck.getUser());
        assertNull(deck.getId()); // Not persisted yet
        assertNull(deck.getCreatedAt());
        assertNull(deck.getUpdatedAt());
    }

    @Test
    void testDeckEquality() {
        User user = new User("user@example.com", "hash");

        Deck deck1 = new Deck(user, "Deck A", "Description");
        deck1.setId(1L);

        Deck deck2 = new Deck(user, "Deck A", "Different description");
        deck2.setId(1L);

        Deck deck3 = new Deck(user, "Deck B", "Description");
        deck3.setId(2L);

        // Same ID and name = equal
        assertEquals(deck1, deck2);

        // Different ID = not equal
        assertNotEquals(deck1, deck3);
    }

    @Test
    void testDeckHashCode() {
        User user = new User("user@example.com", "hash");

        Deck deck1 = new Deck(user, "Test Deck", "Description");
        deck1.setId(1L);

        Deck deck2 = new Deck(user, "Test Deck", "Different");
        deck2.setId(1L);

        assertEquals(deck1.hashCode(), deck2.hashCode());
    }

    @Test
    void testToStringDoesNotExposeUser() {
        User user = new User("secret@example.com", "secretPassword");
        Deck deck = new Deck(user, "My Deck", "Description");

        String deckString = deck.toString();

        // Verify user details aren't in toString
        assertFalse(deckString.contains("secret@example.com"));
        assertFalse(deckString.contains("secretPassword"));
        assertTrue(deckString.contains("My Deck"));
    }

    @Test
    void testNoArgsConstructor() {
        Deck deck = new Deck();

        assertNotNull(deck);
        assertNull(deck.getName());
        assertNull(deck.getDescription());
        assertNull(deck.getUser());
    }
}