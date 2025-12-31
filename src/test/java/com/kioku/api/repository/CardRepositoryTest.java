package com.kioku.api.repository;

import com.kioku.api.TestContainersConfiguration;
import com.kioku.api.entity.Card;
import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
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
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User testUser;
    private Deck testDeck;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        tagRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("test@example.com", "hashedPassword");
        testUser = userRepository.save(testUser);

        testDeck = new Deck(testUser, "Japanese Verbs", "JLPT N5");
        testDeck = deckRepository.save(testDeck);
    }

    @Test
    void testSaveCard() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");

        // When
        Card savedCard = cardRepository.save(card);

        // Then
        assertNotNull(savedCard.getId());
        assertNotNull(savedCard.getCreatedAt());
        assertNotNull(savedCard.getUpdatedAt());
        assertEquals("食べる", savedCard.getFront());
        assertEquals("to eat", savedCard.getBack());
        assertEquals(testDeck.getId(), savedCard.getDeck().getId());
    }

    @Test
    void testFindByDeckId() {
        // Given
        Card card1 = new Card(testDeck, "食べる", "to eat");
        Card card2 = new Card(testDeck, "飲む", "to drink");
        cardRepository.save(card1);
        cardRepository.save(card2);

        // When
        List<Card> cards = cardRepository.findByDeckId(testDeck.getId());

        // Then
        assertEquals(2, cards.size());
        assertTrue(cards.stream().allMatch(c -> c.getDeck().getId().equals(testDeck.getId())));
    }

    @Test
    void testFindByIdAndDeckId() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");
        card = cardRepository.save(card);

        // When
        Optional<Card> found = cardRepository.findByIdAndDeckId(card.getId(), testDeck.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("食べる", found.get().getFront());
    }

    @Test
    void testFindByIdAndDeckIdNotFound() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");
        card = cardRepository.save(card);

        // When - Try with wrong deck ID
        Optional<Card> found = cardRepository.findByIdAndDeckId(card.getId(), 999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByDeckIdAndFrontAndBack() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");
        cardRepository.save(card);

        // When & Then
        assertTrue(cardRepository.existsByDeckIdAndFrontAndBack(testDeck.getId(), "食べる", "to eat"));
        assertFalse(cardRepository.existsByDeckIdAndFrontAndBack(testDeck.getId(), "飲む", "to drink"));
    }

    @Test
    void testDuplicateCardDetection() {
        // Given
        Card card1 = new Card(testDeck, "食べる", "to eat");
        cardRepository.save(card1);

        // When - Check for duplicate before creating
        boolean isDuplicate = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeck.getId(), "食べる", "to eat"
        );

        // Then
        assertTrue(isDuplicate);
    }

    @Test
    void testSearchByDeckId() {
        // Given
        Card card1 = new Card(testDeck, "食べる", "to eat");
        Card card2 = new Card(testDeck, "飲む", "to drink");
        Card card3 = new Card(testDeck, "走る", "to run");
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        // When - Search for cards containing "eat"
        List<Card> results = cardRepository.searchByDeckId(testDeck.getId(), "eat");

        // Then
        assertEquals(1, results.size());
        assertEquals("食べる", results.get(0).getFront());
    }

    @Test
    void testSearchByDeckIdCaseInsensitive() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");
        cardRepository.save(card);

        // When
        List<Card> results1 = cardRepository.searchByDeckId(testDeck.getId(), "EAT");
        List<Card> results2 = cardRepository.searchByDeckId(testDeck.getId(), "eat");

        // Then
        assertEquals(1, results1.size());
        assertEquals(1, results2.size());
    }

    @Test
    void testSearchByDeckIdSearchesBothSides() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");
        cardRepository.save(card);

        // When - Search front
        List<Card> frontResults = cardRepository.searchByDeckId(testDeck.getId(), "食べる");

        // When - Search back
        List<Card> backResults = cardRepository.searchByDeckId(testDeck.getId(), "eat");

        // Then
        assertEquals(1, frontResults.size());
        assertEquals(1, backResults.size());
    }

    @Test
    void testFindByDeckIdAndTagId() {
        // Given
        Tag verbTag = new Tag(testUser, "verbs");
        verbTag = tagRepository.save(verbTag);
        final Tag finalVerbTag = verbTag; // Create final reference for lambda

        Card card1 = new Card(testDeck, "食べる", "to eat");
        Card card2 = new Card(testDeck, "飲む", "to drink");
        Card card3 = new Card(testDeck, "赤い", "red"); // adjective, no tag

        card1.addTag(verbTag);
        card2.addTag(verbTag);

        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        // When
        List<Card> verbCards = cardRepository.findByDeckIdAndTagId(testDeck.getId(), verbTag.getId());

        // Then
        assertEquals(2, verbCards.size());
        assertTrue(verbCards.stream().allMatch(c -> c.getTags().contains(finalVerbTag)));
    }

    @Test
    void testCardWithMultipleTags() {
        // Given
        Tag verbTag = new Tag(testUser, "verbs");
        Tag n5Tag = new Tag(testUser, "n5");
        verbTag = tagRepository.save(verbTag);
        n5Tag = tagRepository.save(n5Tag);

        Card card = new Card(testDeck, "食べる", "to eat");
        card.addTag(verbTag);
        card.addTag(n5Tag);
        card = cardRepository.save(card);

        // When
        Card foundCard = cardRepository.findById(card.getId()).orElseThrow();

        // Then
        assertEquals(2, foundCard.getTags().size());
        assertTrue(foundCard.getTags().contains(verbTag));
        assertTrue(foundCard.getTags().contains(n5Tag));
    }

    @Test
    void testRemoveTagFromCard() {
        // Given
        Tag verbTag = new Tag(testUser, "verbs");
        verbTag = tagRepository.save(verbTag);

        Card card = new Card(testDeck, "食べる", "to eat");
        card.addTag(verbTag);
        card = cardRepository.save(card);

        // When
        card.removeTag(verbTag);
        card = cardRepository.save(card);

        // Then
        Card foundCard = cardRepository.findById(card.getId()).orElseThrow();
        assertTrue(foundCard.getTags().isEmpty());
    }

    @Test
    void testUpdateCard() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");
        card = cardRepository.save(card);
        Long cardId = card.getId();

        // When
        card.setFront("飲む");
        card.setBack("to drink");
        card.setNotes("ru-verb");
        Card updatedCard = cardRepository.save(card);

        // Then
        assertEquals(cardId, updatedCard.getId());
        assertEquals("飲む", updatedCard.getFront());
        assertEquals("to drink", updatedCard.getBack());
        assertEquals("ru-verb", updatedCard.getNotes());
    }

    @Test
    void testDeleteCard() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");
        card = cardRepository.save(card);
        Long cardId = card.getId();

        // When
        cardRepository.delete(card);

        // Then
        assertFalse(cardRepository.existsById(cardId));
    }

    @Test
    void testVersionFieldExists() {
        // Given
        Card card = new Card(testDeck, "食べる", "to eat");
        card = cardRepository.save(card);

        // Then - Version field should be initialized
        assertNotNull(card.getVersion());
    }
}