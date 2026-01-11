package com.kioku.api.controller;

import com.kioku.api.dto.response.CardExportDto;
import com.kioku.api.dto.response.DeckExportResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.dto.response.TagExportDto;
import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Tag;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.security.CurrentUserArgumentResolver;
import com.kioku.api.security.JwtAuthenticationFilter;
import com.kioku.api.service.CardService;
import com.kioku.api.service.DeckService;
import com.kioku.api.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for ExportController using RestTestClient (Spring Boot 4.0).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Deck export endpoint functionality</li>
 *   <li>Complete deck export with cards and tags</li>
 *   <li>Export of decks with no cards/tags</li>
 *   <li>Export metadata generation</li>
 *   <li>Card-tag associations in exports</li>
 *   <li>Authorization checks (deck ownership)</li>
 *   <li>Error handling for non-existent decks</li>
 *   <li>Error handling for unauthorized access</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(
        controllers = ExportController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(ExportControllerTests.ControllerTestConfig.class)
@DisplayName("ExportController Integration Tests")
class ExportControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(ExportControllerTests.class);

    // Test data constants
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_DECK_ID = 1L;
    private static final Long TEST_CARD_ID_1 = 1L;
    private static final Long TEST_CARD_ID_2 = 2L;
    private static final Long TEST_TAG_ID_1 = 1L;
    private static final Long TEST_TAG_ID_2 = 2L;
    private static final String TEST_DECK_NAME = "Japanese N5 Vocabulary";
    private static final String TEST_DECK_DESCRIPTION = "Essential JLPT N5 vocabulary";
    private static final String TEST_CARD_FRONT_1 = "食べる";
    private static final String TEST_CARD_BACK_1 = "to eat";
    private static final String TEST_CARD_NOTES_1 = "ru-verb";
    private static final String TEST_CARD_FRONT_2 = "飲む";
    private static final String TEST_CARD_BACK_2 = "to drink";
    private static final String TEST_TAG_NAME_1 = "verbs";
    private static final String TEST_TAG_NAME_2 = "food";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private CurrentUserArgumentResolver currentUserArgumentResolver;

    RestTestClient client;
    private Deck testDeck;
    private Card testCard1;
    private Card testCard2;
    private Tag testTag1;
    private Tag testTag2;

    @TestConfiguration
    static class ControllerTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
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
                    return TEST_USER_ID;
                }
            });
        }
    }

    @BeforeEach
    void setUp() {
        logger.debug("Setting up ExportController test");

        // Create mock tags
        testTag1 = mock(Tag.class);
        when(testTag1.getId()).thenReturn(TEST_TAG_ID_1);
        when(testTag1.getName()).thenReturn(TEST_TAG_NAME_1);

        testTag2 = mock(Tag.class);
        when(testTag2.getId()).thenReturn(TEST_TAG_ID_2);
        when(testTag2.getName()).thenReturn(TEST_TAG_NAME_2);

        // Create mock cards with tags
        Set<Tag> card1Tags = new HashSet<>(Arrays.asList(testTag1, testTag2));
        testCard1 = mock(Card.class);
        when(testCard1.getCardId()).thenReturn(TEST_CARD_ID_1);
        when(testCard1.getFront()).thenReturn(TEST_CARD_FRONT_1);
        when(testCard1.getBack()).thenReturn(TEST_CARD_BACK_1);
        when(testCard1.getNotes()).thenReturn(TEST_CARD_NOTES_1);
        when(testCard1.getTags()).thenReturn(card1Tags);
        when(testCard1.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(5));
        when(testCard1.getUpdatedAt()).thenReturn(LocalDateTime.now().minusDays(2));

        Set<Tag> card2Tags = new HashSet<>(Collections.singletonList(testTag1));
        testCard2 = mock(Card.class);
        when(testCard2.getCardId()).thenReturn(TEST_CARD_ID_2);
        when(testCard2.getFront()).thenReturn(TEST_CARD_FRONT_2);
        when(testCard2.getBack()).thenReturn(TEST_CARD_BACK_2);
        when(testCard2.getNotes()).thenReturn(null);
        when(testCard2.getTags()).thenReturn(card2Tags);
        when(testCard2.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(3));
        when(testCard2.getUpdatedAt()).thenReturn(LocalDateTime.now().minusDays(1));

        // Create mock deck
        testDeck = mock(Deck.class);
        when(testDeck.getId()).thenReturn(TEST_DECK_ID);
        when(testDeck.getName()).thenReturn(TEST_DECK_NAME);
        when(testDeck.getDescription()).thenReturn(TEST_DECK_DESCRIPTION);
        when(testDeck.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(10));
        when(testDeck.getUpdatedAt()).thenReturn(LocalDateTime.now().minusDays(1));

        client = RestTestClient.bindTo(mockMvc).build();
    }

    @Nested
    @DisplayName("Export Deck Success Scenarios")
    class ExportDeckSuccessTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should export deck with cards and tags successfully")
        @SuppressWarnings("unchecked")
        void testExportDeckWithCardsAndTagsSuccess() {
            logger.debug("Test: Successful deck export with cards and tags");

            List<Card> cards = Arrays.asList(testCard1, testCard2);
            List<Tag> tags = Arrays.asList(testTag1, testTag2);

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(testDeck);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(cards);
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(tags);

            // Use Map to avoid Jackson deserialization issues with DTOs
            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();

            // Verify deck properties
            assertThat(((Number) response.get("id")).longValue()).isEqualTo(TEST_DECK_ID);
            assertThat(response.get("name")).isEqualTo(TEST_DECK_NAME);
            assertThat(response.get("description")).isEqualTo(TEST_DECK_DESCRIPTION);
            assertThat(response.get("createdAt")).isNotNull();
            assertThat(response.get("updatedAt")).isNotNull();

            // Verify cards
            List<Map<String, Object>> responseCards = (List<Map<String, Object>>) response.get("cards");
            assertThat(responseCards).hasSize(2);

            Map<String, Object> card1Response = responseCards.stream()
                    .filter(c -> ((Number) c.get("id")).longValue() == TEST_CARD_ID_1)
                    .findFirst()
                    .orElseThrow();
            assertThat(card1Response.get("front")).isEqualTo(TEST_CARD_FRONT_1);
            assertThat(card1Response.get("back")).isEqualTo(TEST_CARD_BACK_1);
            assertThat(card1Response.get("notes")).isEqualTo(TEST_CARD_NOTES_1);
            List<String> card1Tags = (List<String>) card1Response.get("tags");
            assertThat(card1Tags).containsExactlyInAnyOrder(TEST_TAG_NAME_1, TEST_TAG_NAME_2);

            Map<String, Object> card2Response = responseCards.stream()
                    .filter(c -> ((Number) c.get("id")).longValue() == TEST_CARD_ID_2)
                    .findFirst()
                    .orElseThrow();
            assertThat(card2Response.get("front")).isEqualTo(TEST_CARD_FRONT_2);
            assertThat(card2Response.get("back")).isEqualTo(TEST_CARD_BACK_2);
            assertThat(card2Response.get("notes")).isNull();
            List<String> card2Tags = (List<String>) card2Response.get("tags");
            assertThat(card2Tags).containsExactly(TEST_TAG_NAME_1);

            // Verify tags
            List<Map<String, Object>> responseTags = (List<Map<String, Object>>) response.get("tags");
            assertThat(responseTags).hasSize(2);
            assertThat(responseTags).extracting(t -> (String) t.get("name"))
                    .containsExactlyInAnyOrder(TEST_TAG_NAME_1, TEST_TAG_NAME_2);

            // Verify metadata
            Map<String, Object> metadata = (Map<String, Object>) response.get("metadata");
            assertThat(metadata).isNotNull();
            assertThat(metadata.get("version")).isEqualTo("1.0");
            assertThat(((Number) metadata.get("cardCount")).intValue()).isEqualTo(2);
            assertThat(((Number) metadata.get("tagCount")).intValue()).isEqualTo(2);
            assertThat(metadata.get("exportDate")).isNotNull();

            // Verify service interactions
            verify(deckService).getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID);
            verify(cardService).getDeckCards(TEST_USER_ID, TEST_DECK_ID);
            verify(tagService).getDeckTags(TEST_USER_ID, TEST_DECK_ID);

            logger.debug("Test passed: Deck exported with cards and tags successfully");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should export deck with no cards successfully")
        @SuppressWarnings("unchecked")
        void testExportDeckWithNoCardsSuccess() {
            logger.debug("Test: Successful export of deck with no cards");

            List<Card> emptyCards = Collections.emptyList();
            List<Tag> tags = Arrays.asList(testTag1, testTag2);

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(testDeck);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(emptyCards);
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(tags);

            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(((Number) response.get("id")).longValue()).isEqualTo(TEST_DECK_ID);
            assertThat(response.get("name")).isEqualTo(TEST_DECK_NAME);
            List<Map<String, Object>> responseCards = (List<Map<String, Object>>) response.get("cards");
            assertThat(responseCards).isEmpty();
            List<Map<String, Object>> responseTags = (List<Map<String, Object>>) response.get("tags");
            assertThat(responseTags).hasSize(2);
            Map<String, Object> metadata = (Map<String, Object>) response.get("metadata");
            assertThat(((Number) metadata.get("cardCount")).intValue()).isEqualTo(0);
            assertThat(((Number) metadata.get("tagCount")).intValue()).isEqualTo(2);

            logger.debug("Test passed: Deck with no cards exported successfully");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should export deck with no tags successfully")
        @SuppressWarnings("unchecked")
        void testExportDeckWithNoTagsSuccess() {
            logger.debug("Test: Successful export of deck with no tags");

            // Create cards without tags for this test
            Card cardNoTags = mock(Card.class);
            when(cardNoTags.getCardId()).thenReturn(TEST_CARD_ID_1);
            when(cardNoTags.getFront()).thenReturn(TEST_CARD_FRONT_1);
            when(cardNoTags.getBack()).thenReturn(TEST_CARD_BACK_1);
            when(cardNoTags.getNotes()).thenReturn(TEST_CARD_NOTES_1);
            when(cardNoTags.getTags()).thenReturn(Collections.emptySet());
            when(cardNoTags.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(cardNoTags.getUpdatedAt()).thenReturn(LocalDateTime.now());

            List<Card> cards = Collections.singletonList(cardNoTags);
            List<Tag> emptyTags = Collections.emptyList();

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(testDeck);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(cards);
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(emptyTags);

            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(((Number) response.get("id")).longValue()).isEqualTo(TEST_DECK_ID);
            List<Map<String, Object>> responseCards = (List<Map<String, Object>>) response.get("cards");
            assertThat(responseCards).hasSize(1);
            List<String> cardTags = (List<String>) responseCards.get(0).get("tags");
            assertThat(cardTags).isEmpty();
            List<Map<String, Object>> responseTags = (List<Map<String, Object>>) response.get("tags");
            assertThat(responseTags).isEmpty();
            Map<String, Object> metadata = (Map<String, Object>) response.get("metadata");
            assertThat(((Number) metadata.get("cardCount")).intValue()).isEqualTo(1);
            assertThat(((Number) metadata.get("tagCount")).intValue()).isEqualTo(0);

            logger.debug("Test passed: Deck with no tags exported successfully");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should export completely empty deck successfully")
        @SuppressWarnings("unchecked")
        void testExportEmptyDeckSuccess() {
            logger.debug("Test: Successful export of empty deck");

            List<Card> emptyCards = Collections.emptyList();
            List<Tag> emptyTags = Collections.emptyList();

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(testDeck);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(emptyCards);
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(emptyTags);

            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(((Number) response.get("id")).longValue()).isEqualTo(TEST_DECK_ID);
            assertThat(response.get("name")).isEqualTo(TEST_DECK_NAME);
            assertThat(response.get("description")).isEqualTo(TEST_DECK_DESCRIPTION);
            List<Map<String, Object>> responseCards = (List<Map<String, Object>>) response.get("cards");
            assertThat(responseCards).isEmpty();
            List<Map<String, Object>> responseTags = (List<Map<String, Object>>) response.get("tags");
            assertThat(responseTags).isEmpty();
            Map<String, Object> metadata = (Map<String, Object>) response.get("metadata");
            assertThat(((Number) metadata.get("cardCount")).intValue()).isEqualTo(0);
            assertThat(((Number) metadata.get("tagCount")).intValue()).isEqualTo(0);

            logger.debug("Test passed: Empty deck exported successfully");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should export deck with null description successfully")
        @SuppressWarnings("unchecked")
        void testExportDeckWithNullDescriptionSuccess() {
            logger.debug("Test: Export deck with null description");

            Deck deckNoDescription = mock(Deck.class);
            when(deckNoDescription.getId()).thenReturn(TEST_DECK_ID);
            when(deckNoDescription.getName()).thenReturn(TEST_DECK_NAME);
            when(deckNoDescription.getDescription()).thenReturn(null);
            when(deckNoDescription.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(deckNoDescription.getUpdatedAt()).thenReturn(LocalDateTime.now());

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(deckNoDescription);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(Collections.emptyList());
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(Collections.emptyList());

            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.get("name")).isEqualTo(TEST_DECK_NAME);
            assertThat(response.get("description")).isNull();

            logger.debug("Test passed: Deck with null description exported successfully");
        }
    }

    @Nested
    @DisplayName("Export Deck Error Scenarios")
    class ExportDeckErrorTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when deck not found")
        void testExportDeckNotFound() {
            logger.debug("Test: Export non-existent deck");

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID))
                    .thenThrow(new IllegalArgumentException("Deck not found or access denied: " + TEST_DECK_ID));

            ErrorResponse response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).contains("not found or access denied");

            verify(deckService).getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID);
            verify(cardService, never()).getDeckCards(anyLong(), anyLong());
            verify(tagService, never()).getDeckTags(anyLong(), anyLong());

            logger.debug("Test passed: Non-existent deck rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when deck not owned by user")
        void testExportDeckNotOwnedByUser() {
            logger.debug("Test: Export deck not owned by user");

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID))
                    .thenThrow(new IllegalArgumentException("Deck not found or access denied: " + TEST_DECK_ID));

            ErrorResponse response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).contains("not found or access denied");

            logger.debug("Test passed: Unauthorized access denied");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when deck ID is invalid")
        void testExportDeckWithInvalidId() {
            logger.debug("Test: Export with invalid deck ID (negative)");

            Long invalidDeckId = -1L;

            when(deckService.getDeckOrThrow(invalidDeckId, TEST_USER_ID))
                    .thenThrow(new IllegalArgumentException("Invalid deck ID"));

            ErrorResponse response = client.get()
                    .uri("/api/export/deck/{deckId}", invalidDeckId)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).contains("Invalid");

            logger.debug("Test passed: Invalid deck ID rejected");
        }
    }

    @Nested
    @DisplayName("Export Deck Card Details")
    class ExportDeckCardDetailsTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should export card timestamps correctly")
        @SuppressWarnings("unchecked")
        void testExportCardTimestamps() {
            logger.debug("Test: Verify card timestamps in export");

            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 20, 14, 45, 0);

            Card cardWithTimestamps = mock(Card.class);
            when(cardWithTimestamps.getCardId()).thenReturn(TEST_CARD_ID_1);
            when(cardWithTimestamps.getFront()).thenReturn(TEST_CARD_FRONT_1);
            when(cardWithTimestamps.getBack()).thenReturn(TEST_CARD_BACK_1);
            when(cardWithTimestamps.getNotes()).thenReturn(null);
            when(cardWithTimestamps.getTags()).thenReturn(Collections.emptySet());
            when(cardWithTimestamps.getCreatedAt()).thenReturn(createdAt);
            when(cardWithTimestamps.getUpdatedAt()).thenReturn(updatedAt);

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(testDeck);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID))
                    .thenReturn(Collections.singletonList(cardWithTimestamps));
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(Collections.emptyList());

            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            List<Map<String, Object>> responseCards = (List<Map<String, Object>>) response.get("cards");
            assertThat(responseCards).hasSize(1);

            Map<String, Object> exportedCard = responseCards.get(0);
            // Verify timestamps are present (format may vary)
            assertThat(exportedCard.get("createdAt")).isNotNull();
            assertThat(exportedCard.get("updatedAt")).isNotNull();

            logger.debug("Test passed: Card timestamps exported correctly");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should export cards with multiple tags correctly")
        @SuppressWarnings("unchecked")
        void testExportCardWithMultipleTags() {
            logger.debug("Test: Verify card with multiple tags in export");

            Tag tag3 = mock(Tag.class);
            when(tag3.getId()).thenReturn(3L);
            when(tag3.getName()).thenReturn("jlpt-n5");

            Set<Tag> multipleTags = new HashSet<>(Arrays.asList(testTag1, testTag2, tag3));

            Card cardWithManyTags = mock(Card.class);
            when(cardWithManyTags.getCardId()).thenReturn(TEST_CARD_ID_1);
            when(cardWithManyTags.getFront()).thenReturn(TEST_CARD_FRONT_1);
            when(cardWithManyTags.getBack()).thenReturn(TEST_CARD_BACK_1);
            when(cardWithManyTags.getNotes()).thenReturn(TEST_CARD_NOTES_1);
            when(cardWithManyTags.getTags()).thenReturn(multipleTags);
            when(cardWithManyTags.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(cardWithManyTags.getUpdatedAt()).thenReturn(LocalDateTime.now());

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(testDeck);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID))
                    .thenReturn(Collections.singletonList(cardWithManyTags));
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID))
                    .thenReturn(Arrays.asList(testTag1, testTag2, tag3));

            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            List<Map<String, Object>> responseCards = (List<Map<String, Object>>) response.get("cards");
            assertThat(responseCards).hasSize(1);
            List<String> cardTags = (List<String>) responseCards.get(0).get("tags");
            assertThat(cardTags).containsExactlyInAnyOrder(TEST_TAG_NAME_1, TEST_TAG_NAME_2, "jlpt-n5");
            List<Map<String, Object>> responseTags = (List<Map<String, Object>>) response.get("tags");
            assertThat(responseTags).hasSize(3);

            logger.debug("Test passed: Card with multiple tags exported correctly");
        }
    }

    @Nested
    @DisplayName("Export Metadata Tests")
    class ExportMetadataTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should include correct export metadata")
        @SuppressWarnings("unchecked")
        void testExportMetadataCorrectness() {
            logger.debug("Test: Verify export metadata");

            List<Card> cards = Arrays.asList(testCard1, testCard2);
            List<Tag> tags = Arrays.asList(testTag1, testTag2);

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(testDeck);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(cards);
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(tags);

            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            Map<String, Object> metadata = (Map<String, Object>) response.get("metadata");
            assertThat(metadata).isNotNull();
            assertThat(metadata.get("version")).isEqualTo("1.0");
            assertThat(((Number) metadata.get("cardCount")).intValue()).isEqualTo(2);
            assertThat(((Number) metadata.get("tagCount")).intValue()).isEqualTo(2);
            assertThat(metadata.get("exportDate")).isNotNull();

            logger.debug("Test passed: Export metadata verified");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should have matching card and tag counts in metadata")
        @SuppressWarnings("unchecked")
        void testExportMetadataCountsMatch() {
            logger.debug("Test: Verify metadata counts match actual data");

            List<Card> cards = Collections.singletonList(testCard1);
            List<Tag> tags = Arrays.asList(testTag1, testTag2);

            when(deckService.getDeckOrThrow(TEST_DECK_ID, TEST_USER_ID)).thenReturn(testDeck);
            when(cardService.getDeckCards(TEST_USER_ID, TEST_DECK_ID)).thenReturn(cards);
            when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(tags);

            Map<String, Object> response = client.get()
                    .uri("/api/export/deck/{deckId}", TEST_DECK_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            Map<String, Object> metadata = (Map<String, Object>) response.get("metadata");
            List<Map<String, Object>> responseCards = (List<Map<String, Object>>) response.get("cards");
            List<Map<String, Object>> responseTags = (List<Map<String, Object>>) response.get("tags");
            assertThat(((Number) metadata.get("cardCount")).intValue()).isEqualTo(responseCards.size());
            assertThat(((Number) metadata.get("tagCount")).intValue()).isEqualTo(responseTags.size());

            logger.debug("Test passed: Metadata counts match actual data");
        }
    }
}
