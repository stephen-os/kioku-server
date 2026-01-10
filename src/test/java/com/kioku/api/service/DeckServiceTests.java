package com.kioku.api.service;

import com.kioku.api.dto.request.CardImportDto;
import com.kioku.api.dto.request.DeckImportRequest;
import com.kioku.api.dto.request.TagImportDto;
import com.kioku.api.dto.response.DeckExportResponse;
import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import com.kioku.api.model.User;
import com.kioku.api.repository.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeckService.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Deck creation with duplicate detection</li>
 *   <li>Deck retrieval with ownership verification</li>
 *   <li>Deck updates with duplicate prevention</li>
 *   <li>Deck deletion with ownership checks</li>
 *   <li>User ownership verification</li>
 *   <li>Deck import with cards and tags</li>
 *   <li>Deck export to JSON-serializable response</li>
 *   <li>Error handling for invalid operations</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DeckService Unit Tests")
class DeckServiceTests {

    private static final Logger logger = LoggerFactory.getLogger(DeckServiceTests.class);

    // Test data constants
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long DECK_ID = 1L;
    private static final Long TAG_ID = 200L;
    private static final Long CARD_ID = 300L;
    private static final Long NON_EXISTENT_ID = 999L;
    private static final String DECK_NAME = "Japanese Verbs";
    private static final String DECK_DESCRIPTION = "JLPT N5";
    private static final String UPDATED_NAME = "New Name";
    private static final String UPDATED_DESCRIPTION = "New Description";

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardService cardService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private DeckService deckService;

    private User testUser;
    private User otherUser;
    private Deck testDeck;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up DeckService test");

        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(USER_ID);
        when(testUser.getDecks()).thenReturn(new HashSet<>());

        otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(OTHER_USER_ID);
        when(otherUser.getDecks()).thenReturn(new HashSet<>());

        testDeck = mock(Deck.class);
        when(testDeck.getId()).thenReturn(DECK_ID);
        when(testDeck.getName()).thenReturn(DECK_NAME);
        when(testDeck.getDescription()).thenReturn(DECK_DESCRIPTION);

        logger.debug("Test setup complete");
    }

    // Deck Creation Tests

    @Test
    @DisplayName("Should create deck successfully")
    void testCreateDeck() {
        logger.debug("Test: Successful deck creation");

        Set<Deck> userDecks = new HashSet<>();
        when(testUser.getDecks()).thenReturn(userDecks);
        when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());

        // After save, the deck should be in the user's collection
        doAnswer(invocation -> {
            Deck deck = invocation.getArgument(0);
            userDecks.add(deck);
            return null;
        }).when(testUser).addDeck(any(Deck.class));

        when(userService.save(testUser)).thenReturn(testUser);

        Deck created = deckService.createDeck(USER_ID, DECK_NAME, DECK_DESCRIPTION);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo(DECK_NAME);
        verify(userService).findById(USER_ID);
        verify(deckRepository).findByUserId(USER_ID);
        verify(testUser).addDeck(any(Deck.class));
        verify(userService).save(testUser);

        logger.debug("Test passed: Deck created successfully");
    }

    @Test
    @DisplayName("Should create deck without description")
    void testCreateDeckWithoutDescription() {
        logger.debug("Test: Create deck without description");

        Set<Deck> userDecks = new HashSet<>();
        when(testUser.getDecks()).thenReturn(userDecks);
        when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());

        doAnswer(invocation -> {
            Deck deck = invocation.getArgument(0);
            userDecks.add(deck);
            return null;
        }).when(testUser).addDeck(any(Deck.class));

        when(userService.save(testUser)).thenReturn(testUser);

        Deck created = deckService.createDeck(USER_ID, DECK_NAME, null);

        assertThat(created).isNotNull();
        verify(userService).save(testUser);

        logger.debug("Test passed: Deck created without description");
    }

    @Test
    @DisplayName("Should throw exception when creating deck with duplicate name")
    void testCreateDeckWithDuplicateNameThrowsException() {
        logger.debug("Test: Create deck with duplicate name");

        when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(deckRepository.findByUserId(USER_ID)).thenReturn(List.of(testDeck));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deckService.createDeck(USER_ID, DECK_NAME, "Different description");
        });

        assertThat(exception.getMessage()).contains("already exists");
        verify(userService, never()).save(any(User.class));

        logger.debug("Test passed: Duplicate name rejected");
    }

    @Test
    @DisplayName("Should throw exception when creating deck for non-existent user")
    void testCreateDeckWithNonExistentUserThrowsException() {
        logger.debug("Test: Create deck for non-existent user");

        when(userService.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deckService.createDeck(NON_EXISTENT_ID, DECK_NAME, DECK_DESCRIPTION);
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(deckRepository, never()).findByUserId(anyLong());

        logger.debug("Test passed: Non-existent user rejected");
    }

    // Deck Retrieval Tests

    @Test
    @DisplayName("Should get all user decks")
    void testGetUserDecks() {
        logger.debug("Test: Get all user decks");

        Deck deck2 = mock(Deck.class);
        when(deck2.getName()).thenReturn("Deck 2");

        List<Deck> expectedDecks = List.of(testDeck, deck2);
        when(deckRepository.findByUserId(USER_ID)).thenReturn(expectedDecks);

        List<Deck> userDecks = deckService.getUserDecks(USER_ID);

        assertThat(userDecks).hasSize(2);
        verify(deckRepository).findByUserId(USER_ID);

        logger.debug("Test passed: Retrieved {} decks for user", userDecks.size());
    }

    @Test
    @DisplayName("Should return empty list when user has no decks")
    void testGetUserDecksEmpty() {
        logger.debug("Test: Get decks for user with no decks");

        when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());

        List<Deck> userDecks = deckService.getUserDecks(USER_ID);

        assertThat(userDecks).isEmpty();

        logger.debug("Test passed: Empty list returned");
    }

    @Test
    @DisplayName("Should get deck by ID and user ID")
    void testGetDeck() {
        logger.debug("Test: Get deck by ID");

        when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));

        Optional<Deck> found = deckService.getDeck(DECK_ID, USER_ID);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(DECK_NAME);
        verify(deckRepository).findByIdAndUserId(DECK_ID, USER_ID);

        logger.debug("Test passed: Deck retrieved successfully");
    }

    @Test
    @DisplayName("Should return empty when getting deck with wrong user ID")
    void testGetDeckWithWrongUserReturnsEmpty() {
        logger.debug("Test: Get deck with wrong user");

        when(deckRepository.findByIdAndUserId(DECK_ID, OTHER_USER_ID)).thenReturn(Optional.empty());

        Optional<Deck> found = deckService.getDeck(DECK_ID, OTHER_USER_ID);

        assertThat(found).isEmpty();

        logger.debug("Test passed: Wrong user returns empty");
    }

    @Test
    @DisplayName("Should return empty when deck doesn't exist")
    void testGetDeckNonExistent() {
        logger.debug("Test: Get non-existent deck");

        when(deckRepository.findByIdAndUserId(NON_EXISTENT_ID, USER_ID)).thenReturn(Optional.empty());

        Optional<Deck> found = deckService.getDeck(NON_EXISTENT_ID, USER_ID);

        assertThat(found).isEmpty();

        logger.debug("Test passed: Non-existent deck returns empty");
    }

    @Test
    @DisplayName("Should get deck or throw exception")
    void testGetDeckOrThrow() {
        logger.debug("Test: Get deck or throw");

        when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));

        Deck found = deckService.getDeckOrThrow(DECK_ID, USER_ID);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(DECK_NAME);

        logger.debug("Test passed: Deck retrieved via getDeckOrThrow");
    }

    @Test
    @DisplayName("Should throw exception when getting deck with wrong user")
    void testGetDeckOrThrowWithWrongUserThrowsException() {
        logger.debug("Test: GetDeckOrThrow with wrong user");

        when(deckRepository.findByIdAndUserId(DECK_ID, OTHER_USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deckService.getDeckOrThrow(DECK_ID, OTHER_USER_ID);
        });

        assertThat(exception.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Wrong user throws exception");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent deck")
    void testGetDeckOrThrowNonExistent() {
        logger.debug("Test: GetDeckOrThrow for non-existent deck");

        when(deckRepository.findByIdAndUserId(NON_EXISTENT_ID, USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deckService.getDeckOrThrow(NON_EXISTENT_ID, USER_ID);
        });

        assertThat(exception.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Non-existent deck throws exception");
    }

    // Deck Update Tests

    @Test
    @DisplayName("Should update deck successfully")
    void testUpdateDeck() {
        logger.debug("Test: Successful deck update");

        when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
        when(deckRepository.findByUserId(USER_ID)).thenReturn(List.of(testDeck));
        when(deckRepository.save(testDeck)).thenReturn(testDeck);

        Deck updated = deckService.updateDeck(DECK_ID, USER_ID, UPDATED_NAME, UPDATED_DESCRIPTION);

        assertThat(updated).isNotNull();
        verify(testDeck).setName(UPDATED_NAME);
        verify(testDeck).setDescription(UPDATED_DESCRIPTION);
        verify(deckRepository).save(testDeck);

        logger.debug("Test passed: Deck updated successfully");
    }

    @Test
    @DisplayName("Should throw exception when updating deck with wrong user")
    void testUpdateDeckWithWrongUserThrowsException() {
        logger.debug("Test: Update deck with wrong user");

        when(deckRepository.findByIdAndUserId(DECK_ID, OTHER_USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deckService.updateDeck(DECK_ID, OTHER_USER_ID, UPDATED_NAME, UPDATED_DESCRIPTION);
        });

        assertThat(exception.getMessage()).contains("not found or access denied");
        verify(deckRepository, never()).save(any(Deck.class));

        logger.debug("Test passed: Wrong user update rejected");
    }

    @Test
    @DisplayName("Should throw exception when updating to existing name")
    void testUpdateDeckToExistingNameThrowsException() {
        logger.debug("Test: Update deck to existing name");

        Deck otherDeck = mock(Deck.class);
        when(otherDeck.getId()).thenReturn(2L);
        when(otherDeck.getName()).thenReturn("Existing Name");

        when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
        when(deckRepository.findByUserId(USER_ID)).thenReturn(List.of(testDeck, otherDeck));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deckService.updateDeck(DECK_ID, USER_ID, "Existing Name", UPDATED_DESCRIPTION);
        });

        assertThat(exception.getMessage()).contains("already exists");
        verify(deckRepository, never()).save(any(Deck.class));

        logger.debug("Test passed: Duplicate name update rejected");
    }

    @Test
    @DisplayName("Should update deck keeping same name")
    void testUpdateDeckKeepingSameNameSucceeds() {
        logger.debug("Test: Update deck keeping same name");

        when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
        when(deckRepository.save(testDeck)).thenReturn(testDeck);

        Deck updated = deckService.updateDeck(DECK_ID, USER_ID, DECK_NAME, UPDATED_DESCRIPTION);

        assertThat(updated).isNotNull();
        verify(testDeck).setDescription(UPDATED_DESCRIPTION);
        verify(deckRepository).save(testDeck);

        logger.debug("Test passed: Same name update allowed");
    }

    @Test
    @DisplayName("Should update deck to null description")
    void testUpdateDeckToNullDescription() {
        logger.debug("Test: Update deck to null description");

        when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
        when(deckRepository.save(testDeck)).thenReturn(testDeck);

        Deck updated = deckService.updateDeck(DECK_ID, USER_ID, DECK_NAME, null);

        assertThat(updated).isNotNull();
        verify(testDeck).setDescription(null);
        verify(deckRepository).save(testDeck);

        logger.debug("Test passed: Description updated to null");
    }

    // Deck Deletion Tests

    @Test
    @DisplayName("Should delete deck successfully")
    void testDeleteDeck() {
        logger.debug("Test: Successful deck deletion");

        Set<Deck> userDecks = new HashSet<>();
        userDecks.add(testDeck);
        when(testUser.getDecks()).thenReturn(userDecks);

        when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
        when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(userService.save(testUser)).thenReturn(testUser);

        deckService.deleteDeck(DECK_ID, USER_ID);

        verify(testUser).removeDeck(testDeck);
        verify(userService).save(testUser);

        logger.debug("Test passed: Deck deleted successfully");
    }

    @Test
    @DisplayName("Should throw exception when deleting deck with wrong user")
    void testDeleteDeckWithWrongUserThrowsException() {
        logger.debug("Test: Delete deck with wrong user");

        when(deckRepository.findByIdAndUserId(DECK_ID, OTHER_USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deckService.deleteDeck(DECK_ID, OTHER_USER_ID);
        });

        assertThat(exception.getMessage()).contains("not found or access denied");
        verify(userService, never()).save(any(User.class));

        logger.debug("Test passed: Wrong user delete rejected");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent deck")
    void testDeleteDeckNonExistent() {
        logger.debug("Test: Delete non-existent deck");

        when(deckRepository.findByIdAndUserId(NON_EXISTENT_ID, USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deckService.deleteDeck(NON_EXISTENT_ID, USER_ID);
        });

        assertThat(exception.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Non-existent deck delete rejected");
    }

    // Ownership Verification Tests

    @Test
    @DisplayName("Should verify user owns deck")
    void testUserOwnsDeck() {
        logger.debug("Test: User owns deck verification");

        when(deckRepository.existsByIdAndUserId(DECK_ID, USER_ID)).thenReturn(true);
        when(deckRepository.existsByIdAndUserId(DECK_ID, OTHER_USER_ID)).thenReturn(false);
        when(deckRepository.existsByIdAndUserId(NON_EXISTENT_ID, USER_ID)).thenReturn(false);

        assertThat(deckService.userOwnsDeck(DECK_ID, USER_ID)).isTrue();
        assertThat(deckService.userOwnsDeck(DECK_ID, OTHER_USER_ID)).isFalse();
        assertThat(deckService.userOwnsDeck(NON_EXISTENT_ID, USER_ID)).isFalse();

        logger.debug("Test passed: Ownership verification works correctly");
    }

    @Test
    @DisplayName("Should return true when deck name is duplicate")
    void testIsDuplicateNameTrue() {
        logger.debug("Test: Duplicate deck name");

        when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(true);

        boolean isDuplicate = deckService.isDuplicateName(USER_ID, DECK_NAME);

        assertTrue(isDuplicate);
        verify(deckRepository).existsByUserIdAndName(USER_ID, DECK_NAME);

        logger.debug("Test passed: Duplicate name detected");
    }

    @Test
    @DisplayName("Should return false when deck name is not duplicate")
    void testIsDuplicateNameFalse() {
        logger.debug("Test: Non-duplicate deck name");

        when(deckRepository.existsByUserIdAndName(USER_ID, "Non-existent Deck")).thenReturn(false);

        boolean isDuplicate = deckService.isDuplicateName(USER_ID, "Non-existent Deck");

        assertFalse(isDuplicate);

        logger.debug("Test passed: Non-duplicate name confirmed");
    }

    @Test
    @DisplayName("Should be case-sensitive for duplicate name check")
    void testIsDuplicateNameCaseSensitive() {
        logger.debug("Test: Case-sensitive duplicate name check");

        when(deckRepository.existsByUserIdAndName(USER_ID, "MyDeck")).thenReturn(true);
        when(deckRepository.existsByUserIdAndName(USER_ID, "mydeck")).thenReturn(false);

        boolean existsExact = deckService.isDuplicateName(USER_ID, "MyDeck");
        boolean existsLower = deckService.isDuplicateName(USER_ID, "mydeck");

        assertTrue(existsExact);
        assertFalse(existsLower);

        logger.debug("Test passed: Case-sensitive check works correctly");
    }

    @Test
    @DisplayName("Should isolate deck names by user")
    void testIsDuplicateNameUserIsolation() {
        logger.debug("Test: Deck name isolation by user");

        when(deckRepository.existsByUserIdAndName(USER_ID, "Shared Deck Name")).thenReturn(true);
        when(deckRepository.existsByUserIdAndName(OTHER_USER_ID, "Shared Deck Name")).thenReturn(false);

        boolean existsForTestUser = deckService.isDuplicateName(USER_ID, "Shared Deck Name");
        boolean existsForOtherUser = deckService.isDuplicateName(OTHER_USER_ID, "Shared Deck Name");

        assertTrue(existsForTestUser);
        assertFalse(existsForOtherUser);

        logger.debug("Test passed: Deck names isolated by user");
    }

    // ==================== Import Tests ====================

    @Nested
    @DisplayName("Deck Import Tests")
    class DeckImportTests {

        @Test
        @DisplayName("Should import deck with cards only (no tags)")
        void testImportDeckWithCardsOnly() {
            logger.debug("Test: Import deck with cards only");

            List<CardImportDto> cards = Arrays.asList(
                    new CardImportDto("食べる", "to eat"),
                    new CardImportDto("飲む", "to drink")
            );
            DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

            // Setup mocks for deck creation
            Set<Deck> userDecks = new HashSet<>();
            when(testUser.getDecks()).thenReturn(userDecks);
            when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());
            when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(false);

            doAnswer(invocation -> {
                Deck deck = invocation.getArgument(0);
                userDecks.add(deck);
                return null;
            }).when(testUser).addDeck(any(Deck.class));

            when(userService.save(testUser)).thenReturn(testUser);
            when(cardService.createCard(eq(USER_ID), any(), anyString(), anyString(), any()))
                    .thenReturn(mock(Card.class));

            Deck result = deckService.importDeck(USER_ID, request);

            assertNotNull(result);
            verify(cardService, times(2)).createCard(eq(USER_ID), any(), anyString(), anyString(), any());
            verify(tagService, never()).createTag(anyLong(), anyLong(), anyString());

            logger.debug("Test passed: Deck imported with cards only");
        }

        @Test
        @DisplayName("Should import deck with explicit tags")
        void testImportDeckWithExplicitTags() {
            logger.debug("Test: Import deck with explicit tags");

            List<CardImportDto> cards = Arrays.asList(
                    new CardImportDto("食べる", "to eat")
            );
            List<TagImportDto> tags = Arrays.asList(
                    new TagImportDto("verbs"),
                    new TagImportDto("food")
            );
            DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards, tags);

            // Setup mocks for deck creation
            Set<Deck> userDecks = new HashSet<>();
            when(testUser.getDecks()).thenReturn(userDecks);
            when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());
            when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(false);

            doAnswer(invocation -> {
                Deck deck = invocation.getArgument(0);
                userDecks.add(deck);
                return null;
            }).when(testUser).addDeck(any(Deck.class));

            when(userService.save(testUser)).thenReturn(testUser);

            Tag verbsTag = mock(Tag.class);
            when(verbsTag.getId()).thenReturn(TAG_ID);
            Tag foodTag = mock(Tag.class);
            when(foodTag.getId()).thenReturn(TAG_ID + 1);

            when(tagService.createTag(eq(USER_ID), any(), eq("verbs"))).thenReturn(verbsTag);
            when(tagService.createTag(eq(USER_ID), any(), eq("food"))).thenReturn(foodTag);
            when(cardService.createCard(eq(USER_ID), any(), anyString(), anyString(), any()))
                    .thenReturn(mock(Card.class));

            Deck result = deckService.importDeck(USER_ID, request);

            assertNotNull(result);
            verify(tagService).createTag(eq(USER_ID), any(), eq("verbs"));
            verify(tagService).createTag(eq(USER_ID), any(), eq("food"));

            logger.debug("Test passed: Deck imported with explicit tags");
        }

        @Test
        @DisplayName("Should import deck with cards containing tag references")
        void testImportDeckWithCardTags() {
            logger.debug("Test: Import deck with card tag references");

            List<CardImportDto> cards = Arrays.asList(
                    new CardImportDto("食べる", "to eat", null, Arrays.asList("verbs", "food")),
                    new CardImportDto("飲む", "to drink", null, Arrays.asList("verbs"))
            );
            DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

            // Setup mocks for deck creation
            Set<Deck> userDecks = new HashSet<>();
            when(testUser.getDecks()).thenReturn(userDecks);
            when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());
            when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(false);

            doAnswer(invocation -> {
                Deck deck = invocation.getArgument(0);
                userDecks.add(deck);
                return null;
            }).when(testUser).addDeck(any(Deck.class));

            when(userService.save(testUser)).thenReturn(testUser);

            Tag verbsTag = mock(Tag.class);
            when(verbsTag.getId()).thenReturn(TAG_ID);
            Tag foodTag = mock(Tag.class);
            when(foodTag.getId()).thenReturn(TAG_ID + 1);

            when(tagService.createTag(eq(USER_ID), any(), eq("verbs"))).thenReturn(verbsTag);
            when(tagService.createTag(eq(USER_ID), any(), eq("food"))).thenReturn(foodTag);

            Card card1 = mock(Card.class);
            when(card1.getCardId()).thenReturn(CARD_ID);
            when(card1.getFront()).thenReturn("食べる");
            Card card2 = mock(Card.class);
            when(card2.getCardId()).thenReturn(CARD_ID + 1);
            when(card2.getFront()).thenReturn("飲む");

            when(cardService.createCard(eq(USER_ID), any(), eq("食べる"), eq("to eat"), any())).thenReturn(card1);
            when(cardService.createCard(eq(USER_ID), any(), eq("飲む"), eq("to drink"), any())).thenReturn(card2);

            Deck result = deckService.importDeck(USER_ID, request);

            assertNotNull(result);
            verify(tagService).createTag(eq(USER_ID), any(), eq("verbs"));
            verify(tagService).createTag(eq(USER_ID), any(), eq("food"));
            verify(card1).addTag(verbsTag);
            verify(card1).addTag(foodTag);
            verify(card2).addTag(verbsTag);

            logger.debug("Test passed: Deck imported with card tag references");
        }

        @Test
        @DisplayName("Should deduplicate tags by name")
        void testTagDeduplication() {
            logger.debug("Test: Tag deduplication");

            List<CardImportDto> cards = Arrays.asList(
                    new CardImportDto("食べる", "to eat", null, Arrays.asList("verbs")),
                    new CardImportDto("飲む", "to drink", null, Arrays.asList("verbs"))
            );
            List<TagImportDto> tags = Arrays.asList(
                    new TagImportDto("verbs")
            );
            DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards, tags);

            // Setup mocks for deck creation
            Set<Deck> userDecks = new HashSet<>();
            when(testUser.getDecks()).thenReturn(userDecks);
            when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());
            when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(false);

            doAnswer(invocation -> {
                Deck deck = invocation.getArgument(0);
                userDecks.add(deck);
                return null;
            }).when(testUser).addDeck(any(Deck.class));

            when(userService.save(testUser)).thenReturn(testUser);

            Tag verbsTag = mock(Tag.class);
            when(verbsTag.getId()).thenReturn(TAG_ID);
            when(tagService.createTag(eq(USER_ID), any(), eq("verbs"))).thenReturn(verbsTag);

            Card card1 = mock(Card.class);
            when(card1.getCardId()).thenReturn(CARD_ID);
            Card card2 = mock(Card.class);
            when(card2.getCardId()).thenReturn(CARD_ID + 1);

            when(cardService.createCard(eq(USER_ID), any(), anyString(), anyString(), any()))
                    .thenReturn(card1, card2);

            Deck result = deckService.importDeck(USER_ID, request);

            assertNotNull(result);
            // Tag should only be created once
            verify(tagService, times(1)).createTag(eq(USER_ID), any(), eq("verbs"));

            logger.debug("Test passed: Tags deduplicated correctly");
        }

        @Test
        @DisplayName("Should throw exception when deck name already exists")
        void testImportDeckDuplicateName() {
            logger.debug("Test: Import deck with duplicate name");

            List<CardImportDto> cards = Arrays.asList(
                    new CardImportDto("食べる", "to eat")
            );
            DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

            when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> {
                deckService.importDeck(USER_ID, request);
            });

            verify(cardService, never()).createCard(anyLong(), anyLong(), anyString(), anyString(), any());

            logger.debug("Test passed: Duplicate deck name rejected");
        }

        @Test
        @DisplayName("Should throw exception when duplicate cards in import")
        void testImportDeckDuplicateCards() {
            logger.debug("Test: Import deck with duplicate cards");

            List<CardImportDto> cards = Arrays.asList(
                    new CardImportDto("食べる", "to eat"),
                    new CardImportDto("食べる", "to eat")  // Duplicate
            );
            DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

            // Setup mocks for deck creation
            Set<Deck> userDecks = new HashSet<>();
            when(testUser.getDecks()).thenReturn(userDecks);
            when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());
            when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(false);

            doAnswer(invocation -> {
                Deck deck = invocation.getArgument(0);
                userDecks.add(deck);
                return null;
            }).when(testUser).addDeck(any(Deck.class));

            when(userService.save(testUser)).thenReturn(testUser);
            when(cardService.createCard(eq(USER_ID), any(), anyString(), anyString(), any()))
                    .thenReturn(mock(Card.class));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                deckService.importDeck(USER_ID, request);
            });

            assertTrue(exception.getMessage().contains("Duplicate card"));
            assertTrue(exception.getMessage().contains("食べる"));

            logger.debug("Test passed: Duplicate cards rejected");
        }

        @Test
        @DisplayName("Should import deck with many cards")
        void testImportDeckManyCards() {
            logger.debug("Test: Import deck with many cards");

            List<CardImportDto> cards = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                cards.add(new CardImportDto("Front " + i, "Back " + i));
            }
            DeckImportRequest request = new DeckImportRequest(DECK_NAME, DECK_DESCRIPTION, cards);

            // Setup mocks for deck creation
            Set<Deck> userDecks = new HashSet<>();
            when(testUser.getDecks()).thenReturn(userDecks);
            when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());
            when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(false);

            doAnswer(invocation -> {
                Deck deck = invocation.getArgument(0);
                userDecks.add(deck);
                return null;
            }).when(testUser).addDeck(any(Deck.class));

            when(userService.save(testUser)).thenReturn(testUser);
            when(cardService.createCard(eq(USER_ID), any(), anyString(), anyString(), any()))
                    .thenReturn(mock(Card.class));

            Deck result = deckService.importDeck(USER_ID, request);

            assertNotNull(result);
            verify(cardService, times(100)).createCard(eq(USER_ID), any(), anyString(), anyString(), any());

            logger.debug("Test passed: Large import handled correctly");
        }

        @Test
        @DisplayName("Should import deck with null description")
        void testImportDeckNullDescription() {
            logger.debug("Test: Import deck with null description");

            List<CardImportDto> cards = Arrays.asList(
                    new CardImportDto("食べる", "to eat")
            );
            DeckImportRequest request = new DeckImportRequest(DECK_NAME, null, cards);

            // Setup mocks for deck creation
            Set<Deck> userDecks = new HashSet<>();
            when(testUser.getDecks()).thenReturn(userDecks);
            when(userService.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(deckRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());
            when(deckRepository.existsByUserIdAndName(USER_ID, DECK_NAME)).thenReturn(false);

            doAnswer(invocation -> {
                Deck deck = invocation.getArgument(0);
                userDecks.add(deck);
                return null;
            }).when(testUser).addDeck(any(Deck.class));

            when(userService.save(testUser)).thenReturn(testUser);
            when(cardService.createCard(eq(USER_ID), any(), anyString(), anyString(), any()))
                    .thenReturn(mock(Card.class));

            Deck result = deckService.importDeck(USER_ID, request);

            assertNotNull(result);

            logger.debug("Test passed: Null description handled correctly");
        }
    }

    // ==================== Export Tests ====================

    @Nested
    @DisplayName("Deck Export Tests")
    class DeckExportTests {

        @Test
        @DisplayName("Should export deck with cards and tags")
        void testExportDeckWithCardsAndTags() {
            logger.debug("Test: Export deck with cards and tags");

            when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));

            // Setup cards
            Card card1 = mock(Card.class);
            when(card1.getCardId()).thenReturn(1L);
            when(card1.getFront()).thenReturn("食べる");
            when(card1.getBack()).thenReturn("to eat");
            when(card1.getTags()).thenReturn(Collections.emptySet());

            Card card2 = mock(Card.class);
            when(card2.getCardId()).thenReturn(2L);
            when(card2.getFront()).thenReturn("飲む");
            when(card2.getBack()).thenReturn("to drink");
            when(card2.getTags()).thenReturn(Collections.emptySet());

            List<Card> cards = Arrays.asList(card1, card2);
            when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(cards);

            // Setup tags
            Tag tag1 = mock(Tag.class);
            when(tag1.getId()).thenReturn(1L);
            when(tag1.getName()).thenReturn("verbs");

            Tag tag2 = mock(Tag.class);
            when(tag2.getId()).thenReturn(2L);
            when(tag2.getName()).thenReturn("food");

            List<Tag> tags = Arrays.asList(tag1, tag2);
            when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(tags);

            DeckExportResponse response = deckService.exportDeck(USER_ID, DECK_ID);

            assertNotNull(response);
            assertEquals(DECK_ID, response.getId());
            assertEquals(DECK_NAME, response.getName());
            assertEquals(DECK_DESCRIPTION, response.getDescription());
            assertEquals(2, response.getCards().size());
            assertEquals(2, response.getTags().size());
            assertNotNull(response.getMetadata());
            assertEquals(2, response.getMetadata().getCardCount());
            assertEquals(2, response.getMetadata().getTagCount());

            verify(deckRepository).findByIdAndUserId(DECK_ID, USER_ID);
            verify(cardService).getDeckCards(USER_ID, DECK_ID);
            verify(tagService).getDeckTags(USER_ID, DECK_ID);

            logger.debug("Test passed: Deck exported with cards and tags");
        }

        @Test
        @DisplayName("Should export empty deck")
        void testExportEmptyDeck() {
            logger.debug("Test: Export empty deck");

            when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
            when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());
            when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

            DeckExportResponse response = deckService.exportDeck(USER_ID, DECK_ID);

            assertNotNull(response);
            assertEquals(DECK_ID, response.getId());
            assertEquals(DECK_NAME, response.getName());
            assertTrue(response.getCards().isEmpty());
            assertTrue(response.getTags().isEmpty());
            assertEquals(0, response.getMetadata().getCardCount());
            assertEquals(0, response.getMetadata().getTagCount());

            logger.debug("Test passed: Empty deck exported");
        }

        @Test
        @DisplayName("Should export deck with cards only (no tags)")
        void testExportDeckWithCardsOnly() {
            logger.debug("Test: Export deck with cards only");

            when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));

            Card card = mock(Card.class);
            when(card.getCardId()).thenReturn(1L);
            when(card.getFront()).thenReturn("食べる");
            when(card.getBack()).thenReturn("to eat");
            when(card.getTags()).thenReturn(Collections.emptySet());

            when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Arrays.asList(card));
            when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

            DeckExportResponse response = deckService.exportDeck(USER_ID, DECK_ID);

            assertNotNull(response);
            assertEquals(1, response.getCards().size());
            assertEquals(0, response.getTags().size());
            assertEquals(1, response.getMetadata().getCardCount());
            assertEquals(0, response.getMetadata().getTagCount());

            logger.debug("Test passed: Deck with cards only exported");
        }

        @Test
        @DisplayName("Should export deck with tags only (no cards)")
        void testExportDeckWithTagsOnly() {
            logger.debug("Test: Export deck with tags only");

            when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
            when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

            Tag tag = mock(Tag.class);
            when(tag.getId()).thenReturn(1L);
            when(tag.getName()).thenReturn("verbs");

            when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Arrays.asList(tag));

            DeckExportResponse response = deckService.exportDeck(USER_ID, DECK_ID);

            assertNotNull(response);
            assertEquals(0, response.getCards().size());
            assertEquals(1, response.getTags().size());
            assertEquals(0, response.getMetadata().getCardCount());
            assertEquals(1, response.getMetadata().getTagCount());

            logger.debug("Test passed: Deck with tags only exported");
        }

        @Test
        @DisplayName("Should throw exception when deck not found")
        void testExportDeckNotFound() {
            logger.debug("Test: Export deck not found");

            when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> {
                deckService.exportDeck(USER_ID, DECK_ID);
            });

            verify(deckRepository).findByIdAndUserId(DECK_ID, USER_ID);
            verify(cardService, never()).getDeckCards(anyLong(), anyLong());
            verify(tagService, never()).getDeckTags(anyLong(), anyLong());

            logger.debug("Test passed: Exception thrown for deck not found");
        }

        @Test
        @DisplayName("Should export deck with many cards")
        void testExportDeckManyCards() {
            logger.debug("Test: Export deck with many cards");

            when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));

            // Create 100 mock cards
            List<Card> cards = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Card card = mock(Card.class);
                when(card.getCardId()).thenReturn((long) i);
                when(card.getFront()).thenReturn("Front " + i);
                when(card.getBack()).thenReturn("Back " + i);
                when(card.getTags()).thenReturn(Collections.emptySet());
                cards.add(card);
            }

            when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(cards);
            when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

            DeckExportResponse response = deckService.exportDeck(USER_ID, DECK_ID);

            assertNotNull(response);
            assertEquals(100, response.getCards().size());
            assertEquals(100, response.getMetadata().getCardCount());

            logger.debug("Test passed: Large deck exported");
        }

        @Test
        @DisplayName("Should include export metadata")
        void testExportMetadata() {
            logger.debug("Test: Export metadata");

            when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
            when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());
            when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

            DeckExportResponse response = deckService.exportDeck(USER_ID, DECK_ID);

            assertNotNull(response.getMetadata());
            assertNotNull(response.getMetadata().getExportDate());
            assertEquals("1.0", response.getMetadata().getVersion());
            assertEquals(0, response.getMetadata().getCardCount());
            assertEquals(0, response.getMetadata().getTagCount());

            logger.debug("Test passed: Export metadata included");
        }

        @Test
        @DisplayName("Should verify service call order")
        void testServiceCallOrder() {
            logger.debug("Test: Service call order");

            when(deckRepository.findByIdAndUserId(DECK_ID, USER_ID)).thenReturn(Optional.of(testDeck));
            when(cardService.getDeckCards(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());
            when(tagService.getDeckTags(USER_ID, DECK_ID)).thenReturn(Collections.emptyList());

            deckService.exportDeck(USER_ID, DECK_ID);

            // Verify services called in correct order
            var inOrder = inOrder(deckRepository, cardService, tagService);
            inOrder.verify(deckRepository).findByIdAndUserId(DECK_ID, USER_ID);
            inOrder.verify(cardService).getDeckCards(USER_ID, DECK_ID);
            inOrder.verify(tagService).getDeckTags(USER_ID, DECK_ID);

            logger.debug("Test passed: Services called in correct order");
        }
    }
}
