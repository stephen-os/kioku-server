package com.kioku.api.service;

import com.kioku.api.entity.Card;
import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import com.kioku.api.repository.CardRepository;
import com.kioku.api.repository.DeckRepository;
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
class CardServiceTest {

    @Autowired
    private CardService cardService;

    @Autowired
    private DeckService deckService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;
    private Deck testDeck;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        tagRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userService.createUser("test@example.com", "hashedPassword");
        otherUser = userService.createUser("other@example.com", "hashedPassword");
        testDeck = deckService.createDeck(testUser.getId(), "Japanese Verbs", "JLPT N5");
    }

    @Test
    void testCreateCard() {
        // When
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", "ru-verb");

        // Then
        assertNotNull(card.getId());
        assertEquals("食べる", card.getFront());
        assertEquals("to eat", card.getBack());
        assertEquals("ru-verb", card.getNotes());
    }

    @Test
    void testCreateCardWithoutOwnershipThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(otherUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        });
    }

    @Test
    void testCreateDuplicateCardThrowsException() {
        // Given
        cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", "different notes");
        });
    }

    @Test
    void testGetDeckCards() {
        // Given
        cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        cardService.createCard(testUser.getId(), testDeck.getId(), "飲む", "to drink", null);

        // When
        List<Card> cards = cardService.getDeckCards(testUser.getId(), testDeck.getId());

        // Then
        assertEquals(2, cards.size());
    }

    @Test
    void testGetDeckCardsWithoutOwnershipThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getDeckCards(otherUser.getId(), testDeck.getId());
        });
    }

    @Test
    void testGetCard() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When
        Optional<Card> found = cardService.getCard(testUser.getId(), testDeck.getId(), card.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("食べる", found.get().getFront());
    }

    @Test
    void testGetCardWithWrongUserReturnsEmpty() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When
        Optional<Card> found = cardService.getCard(otherUser.getId(), testDeck.getId(), card.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testGetCardOrThrow() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When
        Card found = cardService.getCardOrThrow(testUser.getId(), testDeck.getId(), card.getId());

        // Then
        assertNotNull(found);
        assertEquals("食べる", found.getFront());
    }

    @Test
    void testGetCardOrThrowWithWrongUserThrowsException() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getCardOrThrow(otherUser.getId(), testDeck.getId(), card.getId());
        });
    }

    @Test
    void testUpdateCard() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When
        Card updated = cardService.updateCard(
                testUser.getId(),
                testDeck.getId(),
                card.getId(),
                "飲む",
                "to drink",
                "u-verb"
        );

        // Then
        assertEquals(card.getId(), updated.getId());
        assertEquals("飲む", updated.getFront());
        assertEquals("to drink", updated.getBack());
        assertEquals("u-verb", updated.getNotes());
    }

    @Test
    void testUpdateCardWithWrongUserThrowsException() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.updateCard(otherUser.getId(), testDeck.getId(), card.getId(), "飲む", "to drink", null);
        });
    }

    @Test
    void testUpdateCardToDuplicateThrowsException() {
        // Given
        cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        Card card2 = cardService.createCard(testUser.getId(), testDeck.getId(), "飲む", "to drink", null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.updateCard(testUser.getId(), testDeck.getId(), card2.getId(), "食べる", "to eat", null);
        });
    }

    @Test
    void testDeleteCard() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        Long cardId = card.getId();

        // When
        cardService.deleteCard(testUser.getId(), testDeck.getId(), cardId);

        // Then
        assertFalse(cardRepository.existsById(cardId));
    }

    @Test
    void testDeleteCardWithWrongUserThrowsException() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.deleteCard(otherUser.getId(), testDeck.getId(), card.getId());
        });
    }

    @Test
    void testSearchCards() {
        // Given
        cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        cardService.createCard(testUser.getId(), testDeck.getId(), "飲む", "to drink", null);

        // When
        List<Card> results = cardService.searchCards(testUser.getId(), testDeck.getId(), "eat");

        // Then
        assertEquals(1, results.size());
        assertEquals("食べる", results.get(0).getFront());
    }

    @Test
    void testSearchCardsWithoutOwnershipThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.searchCards(otherUser.getId(), testDeck.getId(), "eat");
        });
    }

    @Test
    void testAddTagToCard() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When
        Card updated = cardService.addTagToCard(testUser.getId(), testDeck.getId(), card.getId(), tag.getId());

        // Then
        assertTrue(updated.getTags().contains(tag));
    }

    @Test
    void testAddTagToCardWithWrongUserThrowsException() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        Tag tag = tagService.createTag(testUser.getId(), "verbs");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.addTagToCard(otherUser.getId(), testDeck.getId(), card.getId(), tag.getId());
        });
    }

    @Test
    void testRemoveTagFromCard() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        Tag tag = tagService.createTag(testUser.getId(), "verbs");
        card = cardService.addTagToCard(testUser.getId(), testDeck.getId(), card.getId(), tag.getId());

        // When
        Card updated = cardService.removeTagFromCard(testUser.getId(), testDeck.getId(), card.getId(), tag.getId());

        // Then
        assertFalse(updated.getTags().contains(tag));
    }

    @Test
    void testGetCardsByTag() {
        // Given
        Tag verbTag = tagService.createTag(testUser.getId(), "verbs");
        Card card1 = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);
        Card card2 = cardService.createCard(testUser.getId(), testDeck.getId(), "飲む", "to drink", null);
        cardService.createCard(testUser.getId(), testDeck.getId(), "赤い", "red", null); // no tag

        cardService.addTagToCard(testUser.getId(), testDeck.getId(), card1.getId(), verbTag.getId());
        cardService.addTagToCard(testUser.getId(), testDeck.getId(), card2.getId(), verbTag.getId());

        // When
        List<Card> verbCards = cardService.getCardsByTag(testUser.getId(), testDeck.getId(), verbTag.getId());

        // Then
        assertEquals(2, verbCards.size());
    }

    @Test
    void testSetCardAudio() {
        // Given
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When
        Card updated = cardService.setCardAudio(
                testUser.getId(),
                testDeck.getId(),
                card.getId(),
                "https://example.com/front.mp3",
                "https://example.com/back.mp3"
        );

        // Then
        assertEquals("https://example.com/front.mp3", updated.getFrontAudioUrl());
        assertEquals("https://example.com/back.mp3", updated.getBackAudioUrl());
    }

    @Test
    void testIsDuplicate() {
        // Given
        cardService.createCard(testUser.getId(), testDeck.getId(), "食べる", "to eat", null);

        // When & Then
        assertTrue(cardService.isDuplicate(testDeck.getId(), "食べる", "to eat"));
        assertFalse(cardService.isDuplicate(testDeck.getId(), "飲む", "to drink"));
    }
}