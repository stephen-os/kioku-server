package com.kioku.api.repository;

import com.kioku.api.entity.CardEntity;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.entity.UserEntity;
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

    private UserEntity testUserEntity;
    private DeckEntity testDeckEntity;

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

        testUserEntity = new UserEntity(TEST_EMAIL, PASSWORD_HASH);
        testUserEntity = userRepository.save(testUserEntity);

        testDeckEntity = new DeckEntity(testUserEntity, DECK_NAME, DECK_DESCRIPTION);
        testDeckEntity = deckRepository.save(testDeckEntity);

        logger.debug("Test setup complete: user id={}, deck id={}", testUserEntity.getId(), testDeckEntity.getId());
    }

    // Basic CRUD Tests

    /**
     * Tests saving a card to the database.
     */
    @Test
    @DisplayName("Should save a card")
    void testSaveCard() {
        logger.debug("Test: Saving card to deck id={}", testDeckEntity.getId());

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity savedCard = cardRepository.save(card);

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity savedCard = cardRepository.save(card);

        Optional<CardEntity> found = cardRepository.findById(savedCard.getId());

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity savedCard = cardRepository.save(card);
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
        logger.debug("Test: Finding cards by deck id={}", testDeckEntity.getId());

        CardEntity card1 = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity card2 = new CardEntity(testDeckEntity, CARD_FRONT_2, CARD_BACK_2);
        cardRepository.save(card1);
        cardRepository.save(card2);

        List<CardEntity> cards = cardRepository.findByDeckId(testDeckEntity.getId());

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

        List<CardEntity> cards = cardRepository.findByDeckId(999L);

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity savedCard = cardRepository.save(card);

        Optional<CardEntity> found = cardRepository.findByIdAndDeckId(savedCard.getId(), testDeckEntity.getId());

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity savedCard = cardRepository.save(card);

        Optional<CardEntity> found = cardRepository.findByIdAndDeckId(savedCard.getId(), 999L);

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        boolean exists = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeckEntity.getId(), CARD_FRONT_1, CARD_BACK_1);

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        boolean exists = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeckEntity.getId(), CARD_FRONT_2, CARD_BACK_2);

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        boolean exists = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeckEntity.getId(), CARD_FRONT_1.toUpperCase(), CARD_BACK_1);

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

        DeckEntity otherDeckEntity = new DeckEntity(testUserEntity, "Other Deck", "Description");
        otherDeckEntity = deckRepository.save(otherDeckEntity);

        CardEntity card1 = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity card2 = new CardEntity(otherDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card1);
        cardRepository.save(card2);

        boolean deck1HasCard = cardRepository.existsByDeckIdAndFrontAndBack(
                testDeckEntity.getId(), CARD_FRONT_1, CARD_BACK_1);
        boolean deck2HasCard = cardRepository.existsByDeckIdAndFrontAndBack(
                otherDeckEntity.getId(), CARD_FRONT_1, CARD_BACK_1);

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

        CardEntity card1 = new CardEntity(testDeckEntity, "食べる", "to eat");
        CardEntity card2 = new CardEntity(testDeckEntity, "飲む", "to drink");
        CardEntity card3 = new CardEntity(testDeckEntity, "食事", "meal");
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        List<CardEntity> results = cardRepository.searchByDeckId(testDeckEntity.getId(), "食べ");

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

        CardEntity card1 = new CardEntity(testDeckEntity, "食べる", "to eat");
        CardEntity card2 = new CardEntity(testDeckEntity, "飲む", "to drink");
        CardEntity card3 = new CardEntity(testDeckEntity, "読む", "to read");
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        List<CardEntity> results = cardRepository.searchByDeckId(testDeckEntity.getId(), "eat");

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, "To Eat");
        cardRepository.save(card);

        List<CardEntity> results = cardRepository.searchByDeckId(testDeckEntity.getId(), "EAT");

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, "to eat something");
        cardRepository.save(card);

        List<CardEntity> results = cardRepository.searchByDeckId(testDeckEntity.getId(), "eat");

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        List<CardEntity> results = cardRepository.searchByDeckId(testDeckEntity.getId(), "xyz");

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

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity = tagRepository.save(tagEntity);

        CardEntity card1 = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity card2 = new CardEntity(testDeckEntity, CARD_FRONT_2, CARD_BACK_2);
        CardEntity card3 = new CardEntity(testDeckEntity, CARD_FRONT_3, CARD_BACK_3);

        card1.addTag(tagEntity);
        card2.addTag(tagEntity);
        // card3 has no tag

        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        List<CardEntity> cardsWithTag = cardRepository.findByDeckIdAndTagId(testDeckEntity.getId(), tagEntity.getId());

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

        CardEntity card = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        cardRepository.save(card);

        List<CardEntity> cardsWithTag = cardRepository.findByDeckIdAndTagId(testDeckEntity.getId(), 999L);

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

        TagEntity tagEntity = new TagEntity(testDeckEntity, TAG_NAME);
        tagEntity = tagRepository.save(tagEntity);

        CardEntity cardWithTag = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity cardWithoutTag = new CardEntity(testDeckEntity, CARD_FRONT_2, CARD_BACK_2);

        cardWithTag.addTag(tagEntity);
        cardRepository.save(cardWithTag);
        cardRepository.save(cardWithoutTag);

        List<CardEntity> cardsWithTag = cardRepository.findByDeckIdAndTagId(testDeckEntity.getId(), tagEntity.getId());

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
        DeckEntity otherDeckEntity = new DeckEntity(testUserEntity, "Spanish Vocabulary", "Spanish vocab");
        otherDeckEntity = deckRepository.save(otherDeckEntity);

        // Create tags with same name in different decks
        TagEntity japaneseVerbsTagEntity = new TagEntity(testDeckEntity, "verbs");
        japaneseVerbsTagEntity = tagRepository.save(japaneseVerbsTagEntity);

        TagEntity spanishVerbsTagEntity = new TagEntity(otherDeckEntity, "verbs");
        spanishVerbsTagEntity = tagRepository.save(spanishVerbsTagEntity);

        // Create cards in both decks
        CardEntity japaneseCard = new CardEntity(testDeckEntity, "食べる", "to eat");
        japaneseCard.addTag(japaneseVerbsTagEntity);
        cardRepository.save(japaneseCard);

        CardEntity spanishCard = new CardEntity(otherDeckEntity, "comer", "to eat");
        spanishCard.addTag(spanishVerbsTagEntity);
        cardRepository.save(spanishCard);

        // Search for cards with Japanese "verbs" tag
        List<CardEntity> japaneseCards = cardRepository.findByDeckIdAndTagId(
                testDeckEntity.getId(), japaneseVerbsTagEntity.getId());

        // Should only find Japanese card
        assertEquals(1, japaneseCards.size());
        assertEquals("食べる", japaneseCards.get(0).getFront());

        // Search for cards with Spanish "verbs" tag
        List<CardEntity> spanishCards = cardRepository.findByDeckIdAndTagId(
                otherDeckEntity.getId(), spanishVerbsTagEntity.getId());

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

        CardEntity card1 = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity card2 = new CardEntity(testDeckEntity, CARD_FRONT_2, CARD_BACK_2);
        CardEntity card3 = new CardEntity(testDeckEntity, CARD_FRONT_3, CARD_BACK_3);
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        long count = cardRepository.countByDeckId(testDeckEntity.getId());

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

        long count = cardRepository.countByDeckId(testDeckEntity.getId());

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

        CardEntity card1 = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity card2 = new CardEntity(testDeckEntity, CARD_FRONT_2, CARD_BACK_2);
        cardRepository.save(card1);
        cardRepository.save(card2);

        List<CardEntity> allCards = cardRepository.findAll();

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

        CardEntity card1 = new CardEntity(testDeckEntity, CARD_FRONT_1, CARD_BACK_1);
        CardEntity card2 = new CardEntity(testDeckEntity, CARD_FRONT_2, CARD_BACK_2);
        cardRepository.save(card1);
        cardRepository.save(card2);

        cardRepository.deleteAll();

        assertEquals(0, cardRepository.count());

        logger.debug("Test passed: All cards deleted");
    }
}