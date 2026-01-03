package com.kioku.api.controller;

import com.kioku.api.dto.request.CreateCardRequest;
import com.kioku.api.dto.request.UpdateCardRequest;
import com.kioku.api.dto.response.CardResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.entity.CardEntity;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.entity.UserEntity;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.security.CurrentUserArgumentResolver;
import com.kioku.api.security.JwtAuthenticationFilter;
import com.kioku.api.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.util.List;

/**
 * Integration tests for CardController using RestTestClient (Spring Boot 4.0).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Card creation endpoint</li>
 *   <li>Card retrieval endpoints (all cards, single card)</li>
 *   <li>Card search functionality</li>
 *   <li>Card filtering by tag</li>
 *   <li>Card update endpoint</li>
 *   <li>Card deletion endpoint</li>
 *   <li>Tag management (add/remove tags)</li>
 *   <li>Request validation</li>
 *   <li>Authorization checks</li>
 *   <li>Error handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(
        controllers = CardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@DisplayName("CardController Integration Tests")
class CardControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(CardControllerTest.class);

    // Test data constants
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_DECK_ID = 1L;
    private static final Long TEST_CARD_ID = 1L;
    private static final Long TEST_TAG_ID = 1L;
    private static final String TEST_FRONT = "What is Java?";
    private static final String TEST_BACK = "A programming language";
    private static final String TEST_NOTES = "Object-oriented";
    private static final String UPDATED_FRONT = "What is Spring?";
    private static final String UPDATED_BACK = "A Java framework";
    private static final String UPDATED_NOTES = "For building applications";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private CurrentUserArgumentResolver currentUserArgumentResolver;

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            // Create a mock resolver that always returns TEST_USER_ID
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterAnnotation(CurrentUser.class) != null
                            && parameter.getParameterType().equals(Long.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {
                    return TEST_USER_ID; // Always return 1L for tests
                }
            });
        }
    }

    RestTestClient client;
    private UserEntity testUser;
    private DeckEntity testDeck;
    private CardEntity testCard;
    private TagEntity testTag;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up CardController test");

        testUser = new UserEntity("test@example.com", "hashedPassword");
        testUser.setId(TEST_USER_ID);

        testDeck = new DeckEntity(testUser, "Test Deck", "Test Description");
        testDeck.setId(TEST_DECK_ID);

        testCard = new CardEntity(testDeck, TEST_FRONT, TEST_BACK, TEST_NOTES);
        testCard.setId(TEST_CARD_ID);

        testTag = new TagEntity(testDeck, "Test Tag");
        testTag.setId(TEST_TAG_ID);

        client = RestTestClient.bindTo(mockMvc).build();
    }

    // Card Creation Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should create card successfully")
    void testCreateCardSuccess() {
        logger.debug("Test: Successful card creation");

        CreateCardRequest request = new CreateCardRequest(TEST_FRONT, TEST_BACK, TEST_NOTES);

        when(cardService.createCard(TEST_USER_ID, TEST_DECK_ID, TEST_FRONT, TEST_BACK, TEST_NOTES))
                .thenReturn(testCard);

        CardResponse response = client.post()
                .uri("/api/decks/{deckId}/cards", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CardResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);
        assertThat(response.getFront()).isEqualTo(TEST_FRONT);
        assertThat(response.getBack()).isEqualTo(TEST_BACK);
        assertThat(response.getNotes()).isEqualTo(TEST_NOTES);

        verify(cardService).createCard(TEST_USER_ID, TEST_DECK_ID, TEST_FRONT, TEST_BACK, TEST_NOTES);

        logger.debug("Test passed: Card created successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when creating duplicate card")
    void testCreateCardDuplicate() {
        logger.debug("Test: Card creation with duplicate");

        CreateCardRequest request = new CreateCardRequest(TEST_FRONT, TEST_BACK, TEST_NOTES);

        when(cardService.createCard(TEST_USER_ID, TEST_DECK_ID, TEST_FRONT, TEST_BACK, TEST_NOTES))
                .thenThrow(new IllegalArgumentException("Card with same front and back already exists in this deck"));

        ErrorResponse response = client.post()
                .uri("/api/decks/{deckId}/cards", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("already exists");

        verify(cardService).createCard(TEST_USER_ID, TEST_DECK_ID, TEST_FRONT, TEST_BACK, TEST_NOTES);

        logger.debug("Test passed: Duplicate card rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when deck not owned by user")
    void testCreateCardDeckNotOwned() {
        logger.debug("Test: Card creation in deck not owned by user");

        CreateCardRequest request = new CreateCardRequest(TEST_FRONT, TEST_BACK, TEST_NOTES);

        when(cardService.createCard(TEST_USER_ID, TEST_DECK_ID, TEST_FRONT, TEST_BACK, TEST_NOTES))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        ErrorResponse response = client.post()
                .uri("/api/decks/{deckId}/cards", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("Deck not found or access denied");

        logger.debug("Test passed: Deck access denied");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when front is missing")
    void testCreateCardMissingFront() {
        logger.debug("Test: Card creation with missing front");

        String requestJson = "{\"back\":\"" + TEST_BACK + "\",\"notes\":\"" + TEST_NOTES + "\"}";

        client.post()
                .uri("/api/decks/{deckId}/cards", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(cardService, never()).createCard(anyLong(), anyLong(), anyString(), anyString(), anyString());

        logger.debug("Test passed: Missing front rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when back is missing")
    void testCreateCardMissingBack() {
        logger.debug("Test: Card creation with missing back");

        String requestJson = "{\"front\":\"" + TEST_FRONT + "\",\"notes\":\"" + TEST_NOTES + "\"}";

        client.post()
                .uri("/api/decks/{deckId}/cards", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(cardService, never()).createCard(anyLong(), anyLong(), anyString(), anyString(), anyString());

        logger.debug("Test passed: Missing back rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when front exceeds max length")
    void testCreateCardFrontTooLong() {
        logger.debug("Test: Card creation with front exceeding max length");

        String longFront = "a".repeat(501);
        CreateCardRequest request = new CreateCardRequest(longFront, TEST_BACK, TEST_NOTES);

        client.post()
                .uri("/api/decks/{deckId}/cards", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(cardService, never()).createCard(anyLong(), anyLong(), anyString(), anyString(), anyString());

        logger.debug("Test passed: Front too long rejected");
    }

    // Card Retrieval Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve all cards in deck successfully")
    void testGetDeckCardsSuccess() {
        logger.debug("Test: Successful retrieval of all cards");

        CardEntity card2 = new CardEntity(testDeck, "Front 2", "Back 2", "Notes 2");
        card2.setId(2L);

        List<CardEntity> cards = Arrays.asList(testCard, card2);

        when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(cards);

        List<CardResponse> response = client.get()
                .uri("/api/decks/{deckId}/cards", TEST_DECK_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<CardResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getId()).isEqualTo(TEST_CARD_ID);
        assertThat(response.get(1).getId()).isEqualTo(2L);

        verify(cardService).getDeckCards(TEST_USER_ID, TEST_DECK_ID);
        verify(cardService, never()).searchCards(anyLong(), anyLong(), anyString());
        verify(cardService, never()).getCardsByTag(anyLong(), anyLong(), anyLong());

        logger.debug("Test passed: All cards retrieved successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should search cards by text successfully")
    void testSearchCardsSuccess() {
        logger.debug("Test: Search cards by text");

        String searchTerm = "Java";
        List<CardEntity> cards = Arrays.asList(testCard);

        when(cardService.searchCards(TEST_USER_ID, TEST_DECK_ID, searchTerm)).thenReturn(cards);

        List<CardResponse> response = client.get()
                .uri("/api/decks/{deckId}/cards?search={search}", TEST_DECK_ID, searchTerm)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<CardResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getFront()).isEqualTo(TEST_FRONT);

        verify(cardService).searchCards(TEST_USER_ID, TEST_DECK_ID, searchTerm);
        verify(cardService, never()).getDeckCards(anyLong(), anyLong());

        logger.debug("Test passed: Cards searched successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should filter cards by tag successfully")
    void testFilterCardsByTagSuccess() {
        logger.debug("Test: Filter cards by tag");

        List<CardEntity> cards = Arrays.asList(testCard);

        when(cardService.getCardsByTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID)).thenReturn(cards);

        List<CardResponse> response = client.get()
                .uri("/api/decks/{deckId}/cards?tagId={tagId}", TEST_DECK_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<CardResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        verify(cardService).getCardsByTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID);
        verify(cardService, never()).getDeckCards(anyLong(), anyLong());

        logger.debug("Test passed: Cards filtered by tag successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should prioritize search over tag filter")
    void testSearchPriorityOverTagFilter() {
        logger.debug("Test: Search takes priority over tag filter");

        String searchTerm = "Java";
        List<CardEntity> cards = Arrays.asList(testCard);

        when(cardService.searchCards(TEST_USER_ID, TEST_DECK_ID, searchTerm)).thenReturn(cards);

        client.get()
                .uri("/api/decks/{deckId}/cards?search={search}&tagId={tagId}",
                        TEST_DECK_ID, searchTerm, TEST_TAG_ID)
                .exchange()
                .expectStatus().isOk();

        verify(cardService).searchCards(TEST_USER_ID, TEST_DECK_ID, searchTerm);
        verify(cardService, never()).getCardsByTag(anyLong(), anyLong(), anyLong());

        logger.debug("Test passed: Search priority works correctly");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should ignore blank search term")
    void testBlankSearchTerm() {
        logger.debug("Test: Blank search term is ignored");

        List<CardEntity> cards = Arrays.asList(testCard);

        when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(cards);

        client.get()
                .uri("/api/decks/{deckId}/cards?search=   ", TEST_DECK_ID)
                .exchange()
                .expectStatus().isOk();

        verify(cardService).getDeckCards(TEST_USER_ID, TEST_DECK_ID);
        verify(cardService, never()).searchCards(anyLong(), anyLong(), anyString());

        logger.debug("Test passed: Blank search term ignored");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve card by ID successfully")
    void testGetCardSuccess() {
        logger.debug("Test: Successful retrieval of card by ID");

        when(cardService.getCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID))
                .thenReturn(Optional.of(testCard));

        CardResponse response = client.get()
                .uri("/api/decks/{deckId}/cards/{cardId}", TEST_DECK_ID, TEST_CARD_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);
        assertThat(response.getFront()).isEqualTo(TEST_FRONT);

        verify(cardService).getCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID);

        logger.debug("Test passed: Card retrieved successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 404 when card not found")
    void testGetCardNotFound() {
        logger.debug("Test: Card retrieval with non-existent card");

        when(cardService.getCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID))
                .thenReturn(Optional.empty());

        ErrorResponse response = client.get()
                .uri("/api/decks/{deckId}/cards/{cardId}", TEST_DECK_ID, TEST_CARD_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        verify(cardService).getCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID);

        logger.debug("Test passed: Card not found rejected");
    }

    // Card Update Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should update card successfully")
    void testUpdateCardSuccess() {
        logger.debug("Test: Successful card update");

        UpdateCardRequest request = new UpdateCardRequest(UPDATED_FRONT, UPDATED_BACK, UPDATED_NOTES);

        CardEntity updatedCard = new CardEntity(testDeck, UPDATED_FRONT, UPDATED_BACK, UPDATED_NOTES);
        updatedCard.setId(TEST_CARD_ID);

        when(cardService.updateCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID,
                UPDATED_FRONT, UPDATED_BACK, UPDATED_NOTES))
                .thenReturn(updatedCard);

        CardResponse response = client.put()
                .uri("/api/decks/{deckId}/cards/{cardId}", TEST_DECK_ID, TEST_CARD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);
        assertThat(response.getFront()).isEqualTo(UPDATED_FRONT);
        assertThat(response.getBack()).isEqualTo(UPDATED_BACK);
        assertThat(response.getNotes()).isEqualTo(UPDATED_NOTES);

        verify(cardService).updateCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID,
                UPDATED_FRONT, UPDATED_BACK, UPDATED_NOTES);

        logger.debug("Test passed: Card updated successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when update creates duplicate")
    void testUpdateCardDuplicate() {
        logger.debug("Test: Update that would create duplicate");

        UpdateCardRequest request = new UpdateCardRequest(UPDATED_FRONT, UPDATED_BACK, UPDATED_NOTES);

        when(cardService.updateCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID,
                UPDATED_FRONT, UPDATED_BACK, UPDATED_NOTES))
                .thenThrow(new IllegalArgumentException("Card with same front and back already exists in this deck"));

        ErrorResponse response = client.put()
                .uri("/api/decks/{deckId}/cards/{cardId}", TEST_DECK_ID, TEST_CARD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("already exists");

        logger.debug("Test passed: Duplicate update rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when updating non-existent card")
    void testUpdateCardNotFound() {
        logger.debug("Test: Update non-existent card");

        UpdateCardRequest request = new UpdateCardRequest(UPDATED_FRONT, UPDATED_BACK, UPDATED_NOTES);

        when(cardService.updateCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID,
                UPDATED_FRONT, UPDATED_BACK, UPDATED_NOTES))
                .thenThrow(new IllegalArgumentException("Card not found or access denied: " + TEST_CARD_ID));

        ErrorResponse response = client.put()
                .uri("/api/decks/{deckId}/cards/{cardId}", TEST_DECK_ID, TEST_CARD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Update non-existent card rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when update front is blank")
    void testUpdateCardBlankFront() {
        logger.debug("Test: Update card with blank front");

        UpdateCardRequest request = new UpdateCardRequest("", UPDATED_BACK, UPDATED_NOTES);

        client.put()
                .uri("/api/decks/{deckId}/cards/{cardId}", TEST_DECK_ID, TEST_CARD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(cardService, never()).updateCard(anyLong(), anyLong(), anyLong(),
                anyString(), anyString(), anyString());

        logger.debug("Test passed: Blank front rejected");
    }

    // Card Deletion Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should delete card successfully")
    void testDeleteCardSuccess() {
        logger.debug("Test: Successful card deletion");

        doNothing().when(cardService).deleteCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID);

        client.delete()
                .uri("/api/decks/{deckId}/cards/{cardId}", TEST_DECK_ID, TEST_CARD_ID)
                .exchange()
                .expectStatus().isNoContent();

        verify(cardService).deleteCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID);

        logger.debug("Test passed: Card deleted successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when deleting non-existent card")
    void testDeleteCardNotFound() {
        logger.debug("Test: Delete non-existent card");

        doThrow(new IllegalArgumentException("Card not found or access denied: " + TEST_CARD_ID))
                .when(cardService).deleteCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID);

        ErrorResponse response = client.delete()
                .uri("/api/decks/{deckId}/cards/{cardId}", TEST_DECK_ID, TEST_CARD_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        verify(cardService).deleteCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID);

        logger.debug("Test passed: Delete non-existent card rejected");
    }

    // Tag Management Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should add tag to card successfully")
    void testAddTagToCardSuccess() {
        logger.debug("Test: Successful add tag to card");

        CardEntity cardWithTag = new CardEntity(testDeck, TEST_FRONT, TEST_BACK, TEST_NOTES);
        cardWithTag.setId(TEST_CARD_ID);
        cardWithTag.addTag(testTag);

        when(cardService.addTagToCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID))
                .thenReturn(cardWithTag);

        CardResponse response = client.post()
                .uri("/api/decks/{deckId}/cards/{cardId}/tags/{tagId}",
                        TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);
        assertThat(response.getTags()).hasSize(1);

        verify(cardService).addTagToCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID);

        logger.debug("Test passed: Tag added to card successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when adding tag to non-existent card")
    void testAddTagToCardNotFound() {
        logger.debug("Test: Add tag to non-existent card");

        when(cardService.addTagToCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID))
                .thenThrow(new IllegalArgumentException("Card not found or access denied: " + TEST_CARD_ID));

        ErrorResponse response = client.post()
                .uri("/api/decks/{deckId}/cards/{cardId}/tags/{tagId}",
                        TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Add tag to non-existent card rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when tag not found")
    void testAddNonExistentTagToCard() {
        logger.debug("Test: Add non-existent tag to card");

        when(cardService.addTagToCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID))
                .thenThrow(new IllegalArgumentException("Tag not found or access denied: " + TEST_TAG_ID));

        ErrorResponse response = client.post()
                .uri("/api/decks/{deckId}/cards/{cardId}/tags/{tagId}",
                        TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Add non-existent tag rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should remove tag from card successfully")
    void testRemoveTagFromCardSuccess() {
        logger.debug("Test: Successful remove tag from card");

        when(cardService.removeTagFromCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID))
                .thenReturn(testCard);

        CardResponse response = client.delete()
                .uri("/api/decks/{deckId}/cards/{cardId}/tags/{tagId}",
                        TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_CARD_ID);

        verify(cardService).removeTagFromCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID);

        logger.debug("Test passed: Tag removed from card successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when removing tag from non-existent card")
    void testRemoveTagFromCardNotFound() {
        logger.debug("Test: Remove tag from non-existent card");

        when(cardService.removeTagFromCard(TEST_USER_ID, TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID))
                .thenThrow(new IllegalArgumentException("Card not found or access denied: " + TEST_CARD_ID));

        ErrorResponse response = client.delete()
                .uri("/api/decks/{deckId}/cards/{cardId}/tags/{tagId}",
                        TEST_DECK_ID, TEST_CARD_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Remove tag from non-existent card rejected");
    }
}