package com.kioku.api.repository;

import com.kioku.api.entity.Card;
import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CardRepository.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Finding cards by deck</li>
 *   <li>Finding cards by ID and deck</li>
 *   <li>Duplicate detection</li>
 *   <li>Card search functionality</li>
 *   <li>Finding cards by tag</li>
 *   <li>Card counting</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CardRepository Tests")
class CardRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(CardRepositoryTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword123";
    private static final String DECK_NAME = "Japanese Vocabulary";
    private static final String DECK_DESCRIPTION = "JLPT N5 vocabulary";
    private static final String CARD_FRONT_1 = "食べる";
    private static final String CARD_BACK_1 = "to eat";
    private static final String CARD_FRONT_2 = "飲む";
    private static final String CARD_BACK_2 = "to drink";
    private static final String CARD_FRONT_3 = "読む";
    private static final String CARD_BACK_3 = "to read";
    private static final String TAG_NAME = "verbs";

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

    /**
     * Sets up test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        logger.debug("Setting up CardRepository test: Creating user and deck");

        cardRepository.deleteAll();
        tagRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(TEST_EMAIL, PASSWORD_HASH);
        testUser = userRepository.save(testUser);

        testDeck = new Deck(testUser, DECK_NAME, DECK_DESCRIPTION);
        testDeck = deckRepository.save(testDeck);

        logger.debug("Test setup complete: user id={}, deck id={}", testUser.getId(), testDeck.getId());
    }

    // Basic CRUD Tests

    /**
     * Tests saving a card to the database.
     */
    @Test
    @DisplayName("Should save a card")
    void testSaveCard() {
        logger.debug("Test: Saving card to deck id={}", testDeck.getId());

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card savedCard = cardRepository.save(card);

        assertNotNull(savedCard.getId());
        assertEquals(CARD_FRONT_1, savedCard.getFront());
        assertEquals(CARD_BACK_1, savedCard.getBack());
        assertNotNull(savedCard.getCreatedAt());
        assertNotNull(savedCard.getUpdatedAt());

        logger.debug("Test passed: Card saved with id={}", savedCard.getId());
    }

    /**
     * Tests finding a card by ID.
     */
    @Test
    @DisplayName("Should find card by ID")
    void testFindById() {
        logger.debug("Test: Finding card by ID");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card savedCard = cardRepository.save(card);

        Optional<Card> found = cardRepository.findById(savedCard.getId());

        assertTrue(found.isPresent());
        assertEquals(CARD_FRONT_1, found.get().getFront());

        logger.debug("Test passed: Card found by id={}", savedCard.getId());
    }

    /**
     * Tests deleting a card.
     */
    @Test
    @DisplayName("Should delete a card")
    void testDeleteCard() {
        logger.debug("Test: Deleting card");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card savedCard = cardRepository.save(card);
        Long cardId = savedCard.getId();

        cardRepository.delete(savedCard);

        assertFalse(cardRepository.existsById(cardId));

        logger.debug("Test passed: Card deleted with id={}", cardId);
    }

    // Find by Deck Tests

    /**
     * Tests finding all cards in a deck.
     */
    @Test
    @DisplayName("Should find cards by deck ID")
    void testFindByDeckId() {
        logger.debug("Test: Finding cards by deck id={}", testDeck.getId());

        Card card1 = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card card2 = new Card(testDeck, CARD_FRONT_2, CARD_BACK_2);
        cardRepository.save(card1);
        cardRepository.save(card2);

        List<Card> cards = cardRepository.findByDeckId(testDeck.getId());

        assertEquals(2, cards.size());

        logger.debug("Test passed: Found {} cards in deck", cards.size());
    }

    /**
     * Tests that finding cards by non-existent deck returns empty list.
     */
    @Test
    @DisplayName("Should return empty list for non-existent deck")
    void testFindByNonExistentDeck() {
        logger.debug("Test: Finding cards in non-existent deck");

        List<Card> cards = cardRepository.findByDeckId(999L);

        assertTrue(cards.isEmpty());

        logger.debug("Test passed: Empty list returned");
    }

    /**
     * Tests finding card by ID and deck ID.
     */
    @Test
    @DisplayName("Should find card by ID and deck ID")
    void testFindByIdAndDeckId() {
        logger.debug("Test: Finding card by ID and deck ID");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card savedCard = cardRepository.save(card);

        Optional<Card> found = cardRepository.findByIdAndDeckId(savedCard.getId(), testDeck.getId());

        assertTrue(found.isPresent());
        assertEquals(CARD_FRONT_1, found.get().getFront());

        logger.debug("Test passed: Card found");
    }

    /**
     * Tests that finding card with wrong deck ID returns empty.
     */
    @Test
    @DisplayName("Should return empty when card ID exists but deck ID is wrong")
    void testFindByIdAndWrongDeckId() {
        logger.debug("Test: Finding card with wrong deck ID");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card savedCard = cardRepository.save(card);

        Optional<Card> found = cardRepository.findByIdAndDeckId(savedCard.getId(), 999L);

        assertFalse(found.isPresent());

        logger.debug("Test passed: Empty result for wrong deck ID");
    }

    // Duplicate Detection Tests

    /**
     * Tests duplicate detection.
     */
    @Test
    @DisplayName("Should detect duplicate cards")
    void testExistsByDeckIdAndFrontAndBack() {
        logger.debug("Test: Detecting duplicate cards");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        boolean exists = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeck.getId(), CARD_FRONT_1, CARD_BACK_1);

        assertTrue(exists);

        logger.debug("Test passed: Duplicate detected");
    }

    /**
     * Tests that non-duplicate returns false.
     */
    @Test
    @DisplayName("Should return false for non-duplicate")
    void testNoDuplicate() {
        logger.debug("Test: Checking non-duplicate");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        boolean exists = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeck.getId(), CARD_FRONT_2, CARD_BACK_2);

        assertFalse(exists);

        logger.debug("Test passed: No duplicate found");
    }

    /**
     * Tests that duplicate detection is case-sensitive.
     */
    @Test
    @DisplayName("Should be case-sensitive for duplicate detection")
    void testDuplicateDetectionCaseSensitive() {
        logger.debug("Test: Testing case-sensitive duplicate detection");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        boolean exists = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeck.getId(), CARD_FRONT_1.toUpperCase(), CARD_BACK_1);

        assertTrue(exists);

        logger.debug("Test passed: Case-sensitive check works");
    }

    /**
     * Tests that same front/back in different decks is not a duplicate.
     */
    @Test
    @DisplayName("Should allow same card in different decks")
    void testSameCardDifferentDecks() {
        logger.debug("Test: Same card in different decks");

        Deck otherDeck = new Deck(testUser, "Other Deck", "Description");
        otherDeck = deckRepository.save(otherDeck);

        Card card1 = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card card2 = new Card(otherDeck, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card1);
        cardRepository.save(card2);

        boolean deck1HasCard = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeck.getId(), CARD_FRONT_1, CARD_BACK_1);
        boolean deck2HasCard = cardRepository.existsByDeckIdAndFrontAndBack(
                otherDeck.getId(), CARD_FRONT_1, CARD_BACK_1);

        assertTrue(deck1HasCard);
        assertTrue(deck2HasCard);

        logger.debug("Test passed: Same card allowed in different decks");
    }

    // Search Tests

    /**
     * Tests searching cards by front text.
     */
    @Test
    @DisplayName("Should search cards by front text")
    void testSearchByFront() {
        logger.debug("Test: Searching cards by front text");

        Card card1 = new Card(testDeck, "食べる", "to eat");
        Card card2 = new Card(testDeck, "飲む", "to drink");
        Card card3 = new Card(testDeck, "食事", "meal");
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        List<Card> results = cardRepository.searchByDeckId(testDeck.getId(), "食べ");

        assertEquals(1, results.size());
        assertEquals("食べる", results.get(0).getFront());

        logger.debug("Test passed: Found {} cards matching search", results.size());
    }

    /**
     * Tests searching cards by back text.
     */
    @Test
    @DisplayName("Should search cards by back text")
    void testSearchByBack() {
        logger.debug("Test: Searching cards by back text");

        Card card1 = new Card(testDeck, "食べる", "to eat");
        Card card2 = new Card(testDeck, "飲む", "to drink");
        Card card3 = new Card(testDeck, "読む", "to read");
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        List<Card> results = cardRepository.searchByDeckId(testDeck.getId(), "eat");

        assertEquals(1, results.size());
        assertEquals("to eat", results.get(0).getBack());

        logger.debug("Test passed: Found {} cards matching search", results.size());
    }

    /**
     * Tests that search is case-insensitive.
     */
    @Test
    @DisplayName("Should perform case-insensitive search")
    void testSearchCaseInsensitive() {
        logger.debug("Test: Case-insensitive search");

        Card card = new Card(testDeck, CARD_FRONT_1, "To Eat");
        cardRepository.save(card);

        List<Card> results = cardRepository.searchByDeckId(testDeck.getId(), "EAT");

        assertEquals(1, results.size());

        logger.debug("Test passed: Case-insensitive search works");
    }

    /**
     * Tests that search matches partial text.
     */
    @Test
    @DisplayName("Should match partial text in search")
    void testSearchPartialMatch() {
        logger.debug("Test: Partial text search");

        Card card = new Card(testDeck, CARD_FRONT_1, "to eat something");
        cardRepository.save(card);

        List<Card> results = cardRepository.searchByDeckId(testDeck.getId(), "eat");

        assertEquals(1, results.size());

        logger.debug("Test passed: Partial match works");
    }

    /**
     * Tests that search returns empty for no matches.
     */
    @Test
    @DisplayName("Should return empty list for no search matches")
    void testSearchNoMatches() {
        logger.debug("Test: Search with no matches");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        List<Card> results = cardRepository.searchByDeckId(testDeck.getId(), "xyz");

        assertTrue(results.isEmpty());

        logger.debug("Test passed: Empty list for no matches");
    }

    // Tag Tests

    /**
     * Tests finding cards by tag.
     */
    @Test
    @DisplayName("Should find cards by tag")
    void testFindByDeckIdAndTagId() {
        logger.debug("Test: Finding cards by tag");

        Tag tag = new Tag(testDeck, TAG_NAME);
        tag = tagRepository.save(tag);

        Card card1 = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card card2 = new Card(testDeck, CARD_FRONT_2, CARD_BACK_2);
        Card card3 = new Card(testDeck, CARD_FRONT_3, CARD_BACK_3);

        card1.addTag(tag);
        card2.addTag(tag);
        // card3 has no tag

        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        List<Card> cardsWithTag = cardRepository.findByDeckIdAndTagId(testDeck.getId(), tag.getId());

        assertEquals(2, cardsWithTag.size());

        logger.debug("Test passed: Found {} cards with tag", cardsWithTag.size());
    }

    /**
     * Tests that finding cards by non-existent tag returns empty list.
     */
    @Test
    @DisplayName("Should return empty list for non-existent tag")
    void testFindByNonExistentTag() {
        logger.debug("Test: Finding cards by non-existent tag");

        Card card = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        List<Card> cardsWithTag = cardRepository.findByDeckIdAndTagId(testDeck.getId(), 999L);

        assertTrue(cardsWithTag.isEmpty());

        logger.debug("Test passed: Empty list returned");
    }

    /**
     * Tests that cards without tags are not returned.
     */
    @Test
    @DisplayName("Should not return cards without the specified tag")
    void testFindByTagExcludesUntagged() {
        logger.debug("Test: Ensuring untagged cards are excluded");

        Tag tag = new Tag(testDeck, TAG_NAME);
        tag = tagRepository.save(tag);

        Card cardWithTag = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card cardWithoutTag = new Card(testDeck, CARD_FRONT_2, CARD_BACK_2);

        cardWithTag.addTag(tag);
        cardRepository.save(cardWithTag);
        cardRepository.save(cardWithoutTag);

        List<Card> cardsWithTag = cardRepository.findByDeckIdAndTagId(testDeck.getId(), tag.getId());

        assertEquals(1, cardsWithTag.size());
        assertEquals(CARD_FRONT_1, cardsWithTag.get(0).getFront());

        logger.debug("Test passed: Only tagged cards returned");
    }

    /**
     * Tests that tags are deck-specific.
     */
    @Test
    @DisplayName("Should only find cards with tags from same deck")
    void testFindByTagDeckIsolation() {
        logger.debug("Test: Testing deck-specific tag isolation");

        // Create second deck
        Deck otherDeck = new Deck(testUser, "Spanish Vocabulary", "Spanish vocab");
        otherDeck = deckRepository.save(otherDeck);

        // Create tags with same name in different decks
        Tag japaneseVerbsTag = new Tag(testDeck, "verbs");
        japaneseVerbsTag = tagRepository.save(japaneseVerbsTag);

        Tag spanishVerbsTag = new Tag(otherDeck, "verbs");
        spanishVerbsTag = tagRepository.save(spanishVerbsTag);

        // Create cards in both decks
        Card japaneseCard = new Card(testDeck, "食べる", "to eat");
        japaneseCard.addTag(japaneseVerbsTag);
        cardRepository.save(japaneseCard);

        Card spanishCard = new Card(otherDeck, "comer", "to eat");
        spanishCard.addTag(spanishVerbsTag);
        cardRepository.save(spanishCard);

        // Search for cards with Japanese "verbs" tag
        List<Card> japaneseCards = cardRepository.findByDeckIdAndTagId(
                testDeck.getId(), japaneseVerbsTag.getId());

        // Should only find Japanese card
        assertEquals(1, japaneseCards.size());
        assertEquals("食べる", japaneseCards.get(0).getFront());

        // Search for cards with Spanish "verbs" tag
        List<Card> spanishCards = cardRepository.findByDeckIdAndTagId(
                otherDeck.getId(), spanishVerbsTag.getId());

        // Should only find Spanish card
        assertEquals(1, spanishCards.size());
        assertEquals("comer", spanishCards.get(0).getFront());

        logger.debug("Test passed: Tags are properly isolated by deck");
    }

    // Count Tests

    /**
     * Tests counting cards in a deck.
     */
    @Test
    @DisplayName("Should count cards in deck")
    void testCountByDeckId() {
        logger.debug("Test: Counting cards in deck");

        Card card1 = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card card2 = new Card(testDeck, CARD_FRONT_2, CARD_BACK_2);
        Card card3 = new Card(testDeck, CARD_FRONT_3, CARD_BACK_3);
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        long count = cardRepository.countByDeckId(testDeck.getId());

        assertEquals(3, count);

        logger.debug("Test passed: Counted {} cards", count);
    }

    /**
     * Tests that count returns zero for empty deck.
     */
    @Test
    @DisplayName("Should return zero count for empty deck")
    void testCountEmptyDeck() {
        logger.debug("Test: Counting cards in empty deck");

        long count = cardRepository.countByDeckId(testDeck.getId());

        assertEquals(0, count);

        logger.debug("Test passed: Zero count for empty deck");
    }

    /**
     * Tests that count returns zero for non-existent deck.
     */
    @Test
    @DisplayName("Should return zero count for non-existent deck")
    void testCountNonExistentDeck() {
        logger.debug("Test: Counting cards in non-existent deck");

        long count = cardRepository.countByDeckId(999L);

        assertEquals(0, count);

        logger.debug("Test passed: Zero count for non-existent deck");
    }

    // Batch Operations Tests

    /**
     * Tests finding all cards.
     */
    @Test
    @DisplayName("Should find all cards")
    void testFindAll() {
        logger.debug("Test: Finding all cards");

        Card card1 = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card card2 = new Card(testDeck, CARD_FRONT_2, CARD_BACK_2);
        cardRepository.save(card1);
        cardRepository.save(card2);

        List<Card> allCards = cardRepository.findAll();

        assertEquals(2, allCards.size());

        logger.debug("Test passed: Found {} cards", allCards.size());
    }

    /**
     * Tests deleting all cards.
     */
    @Test
    @DisplayName("Should delete all cards")
    void testDeleteAll() {
        logger.debug("Test: Deleting all cards");

        Card card1 = new Card(testDeck, CARD_FRONT_1, CARD_BACK_1);
        Card card2 = new Card(testDeck, CARD_FRONT_2, CARD_BACK_2);
        cardRepository.save(card1);
        cardRepository.save(card2);

        cardRepository.deleteAll();

        assertEquals(0, cardRepository.count());

        logger.debug("Test passed: All cards deleted");
    }
}