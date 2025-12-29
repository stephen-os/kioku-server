package com.kioku.api.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testCardCreation() {
        User user = new User("user@example.com", "hashedPassword");
        Deck deck = new Deck(user, "Test Deck", "Description");
        Card card = new Card(deck, "食べる", "to eat");

        assertEquals("食べる", card.getFront());
        assertEquals("to eat", card.getBack());
        assertEquals(deck, card.getDeck());
        assertNull(card.getId());
        assertNull(card.getNotes());
    }

    @Test
    void testCardCreationWithNotes() {
        User user = new User("user@example.com", "hashedPassword");
        Deck deck = new Deck(user, "Test Deck", "Description");
        Card card = new Card(deck, "食べる", "to eat", "ru-verb");

        assertEquals("食べる", card.getFront());
        assertEquals("to eat", card.getBack());
        assertEquals("ru-verb", card.getNotes());
    }

    @Test
    void testCardEquality() {
        User user = new User("user@example.com", "hash");
        Deck deck = new Deck(user, "Deck", "Description");

        Card card1 = new Card(deck, "Front", "Back");
        card1.setId(1L);

        Card card2 = new Card(deck, "Different", "Content");
        card2.setId(1L);

        Card card3 = new Card(deck, "Front", "Back");
        card3.setId(2L);

        // Same ID = equal
        assertEquals(card1, card2);

        // Different ID = not equal
        assertNotEquals(card1, card3);
    }

    @Test
    void testCardHashCode() {
        User user = new User("user@example.com", "hash");
        Deck deck = new Deck(user, "Deck", "Description");

        Card card1 = new Card(deck, "Front", "Back");
        card1.setId(1L);

        Card card2 = new Card(deck, "Different", "Content");
        card2.setId(1L);

        assertEquals(card1.hashCode(), card2.hashCode());
    }

    @Test
    void testAddTag() {
        User user = new User("user@example.com", "hash");
        Deck deck = new Deck(user, "Deck", "Description");
        Card card = new Card(deck, "Front", "Back");
        Tag tag = new Tag(user, "verbs");

        card.addTag(tag);

        assertTrue(card.getTags().contains(tag));
        assertTrue(tag.getCards().contains(card));
    }

    @Test
    void testRemoveTag() {
        User user = new User("user@example.com", "hash");
        Deck deck = new Deck(user, "Deck", "Description");
        Card card = new Card(deck, "Front", "Back");
        Tag tag = new Tag(user, "verbs");

        card.addTag(tag);
        card.removeTag(tag);

        assertFalse(card.getTags().contains(tag));
        assertFalse(tag.getCards().contains(card));
    }

    @Test
    void testToStringDoesNotExposeSensitiveData() {
        User user = new User("secret@example.com", "secretPassword");
        Deck deck = new Deck(user, "My Deck", "Description");
        Card card = new Card(deck, "食べる", "to eat");

        String cardString = card.toString();

        assertTrue(cardString.contains("食べる"));
        assertTrue(cardString.contains("to eat"));
        assertFalse(cardString.contains("secret@example.com"));
    }

    @Test
    void testNoArgsConstructor() {
        Card card = new Card();

        assertNotNull(card);
        assertNull(card.getFront());
        assertNull(card.getBack());
        assertNull(card.getDeck());
    }

    @Test
    void testAudioUrlFields() {
        User user = new User("user@example.com", "hash");
        Deck deck = new Deck(user, "Deck", "Description");
        Card card = new Card(deck, "Front", "Back");

        assertNull(card.getFrontAudioUrl());
        assertNull(card.getBackAudioUrl());

        card.setFrontAudioUrl("https://example.com/audio/front.mp3");
        card.setBackAudioUrl("https://example.com/audio/back.mp3");

        assertEquals("https://example.com/audio/front.mp3", card.getFrontAudioUrl());
        assertEquals("https://example.com/audio/back.mp3", card.getBackAudioUrl());
    }
}