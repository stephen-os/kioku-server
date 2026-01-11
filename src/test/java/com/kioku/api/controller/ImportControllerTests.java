package com.kioku.api.controller;

import com.kioku.api.dto.request.CardImportDto;
import com.kioku.api.dto.request.DeckImportRequest;
import com.kioku.api.dto.request.TagImportDto;
import com.kioku.api.dto.response.DeckResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.model.Deck;
import com.kioku.api.repository.DeckRepository;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.security.CurrentUserArgumentResolver;
import com.kioku.api.security.JwtAuthenticationFilter;
import com.kioku.api.service.DeckService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for ImportController using RestTestClient (Spring Boot 4.0).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Deck import endpoint functionality</li>
 *   <li>Complete deck import with cards and tags</li>
 *   <li>Import with tags inferred from cards</li>
 *   <li>Import validation (name, cards, tags)</li>
 *   <li>Duplicate deck name detection</li>
 *   <li>Duplicate card detection within import</li>
 *   <li>Request validation (missing/blank fields)</li>
 *   <li>Maximum length validation for all fields</li>
 *   <li>Error handling and rollback behavior</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(
        controllers = ImportController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(ImportControllerTests.ControllerTestConfig.class)
@DisplayName("ImportController Integration Tests")
class ImportControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(ImportControllerTests.class);

    // Test data constants
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_DECK_ID = 1L;
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
    private DeckRepository deckRepository;

    @MockitoBean
    private CurrentUserArgumentResolver currentUserArgumentResolver;

    RestTestClient client;
    private Deck testDeck;

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
        logger.debug("Setting up ImportController test");

        testDeck = mock(Deck.class);
        when(testDeck.getId()).thenReturn(TEST_DECK_ID);
        when(testDeck.getName()).thenReturn(TEST_DECK_NAME);
        when(testDeck.getDescription()).thenReturn(TEST_DECK_DESCRIPTION);
        when(testDeck.getCardCount()).thenReturn(2);

        client = RestTestClient.bindTo(mockMvc).build();
    }

    @Nested
    @DisplayName("Import Deck Success Scenarios")
    class ImportDeckSuccessTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should import deck with cards and tags successfully")
        void testImportDeckWithCardsAndTagsSuccess() {
            logger.debug("Test: Successful deck import with cards and tags");

            CardImportDto card1 = new CardImportDto(
                    TEST_CARD_FRONT_1, TEST_CARD_BACK_1, TEST_CARD_NOTES_1,
                    Arrays.asList(TEST_TAG_NAME_1, TEST_TAG_NAME_2)
            );
            CardImportDto card2 = new CardImportDto(
                    TEST_CARD_FRONT_2, TEST_CARD_BACK_2, null,
                    Collections.singletonList(TEST_TAG_NAME_1)
            );
            TagImportDto tag1 = new TagImportDto(TEST_TAG_NAME_1);
            TagImportDto tag2 = new TagImportDto(TEST_TAG_NAME_2);

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Arrays.asList(card1, card2),
                    Arrays.asList(tag1, tag2)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION))
                    .thenReturn(testDeck);
            when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(TEST_DECK_ID);
            assertThat(response.getName()).isEqualTo(TEST_DECK_NAME);
            assertThat(response.getDescription()).isEqualTo(TEST_DECK_DESCRIPTION);

            verify(deckService).isDuplicateName(TEST_USER_ID, TEST_DECK_NAME);
            verify(deckService).createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION);
            verify(deckRepository).save(any(Deck.class));

            logger.debug("Test passed: Deck imported with cards and tags successfully");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should import deck with cards only (no explicit tags)")
        void testImportDeckWithCardsOnlySuccess() {
            logger.debug("Test: Successful import with cards only (tags inferred)");

            CardImportDto card1 = new CardImportDto(
                    TEST_CARD_FRONT_1, TEST_CARD_BACK_1, TEST_CARD_NOTES_1,
                    Arrays.asList(TEST_TAG_NAME_1, TEST_TAG_NAME_2)
            );
            CardImportDto card2 = new CardImportDto(
                    TEST_CARD_FRONT_2, TEST_CARD_BACK_2, null,
                    Collections.singletonList(TEST_TAG_NAME_1)
            );

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Arrays.asList(card1, card2)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION))
                    .thenReturn(testDeck);
            when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(TEST_DECK_ID);

            logger.debug("Test passed: Deck imported with cards only");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should import deck with cards without any tags")
        void testImportDeckWithCardsNoTagsSuccess() {
            logger.debug("Test: Successful import with cards but no tags");

            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);
            CardImportDto card2 = new CardImportDto(TEST_CARD_FRONT_2, TEST_CARD_BACK_2);

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Arrays.asList(card1, card2)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION))
                    .thenReturn(testDeck);
            when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(TEST_DECK_ID);

            logger.debug("Test passed: Deck imported with cards but no tags");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should import deck without description")
        void testImportDeckWithoutDescriptionSuccess() {
            logger.debug("Test: Successful import without description");

            Deck deckNoDescription = mock(Deck.class);
            when(deckNoDescription.getId()).thenReturn(TEST_DECK_ID);
            when(deckNoDescription.getName()).thenReturn(TEST_DECK_NAME);
            when(deckNoDescription.getDescription()).thenReturn(null);
            when(deckNoDescription.getCardCount()).thenReturn(1);

            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    null,
                    Collections.singletonList(card1)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, null))
                    .thenReturn(deckNoDescription);
            when(deckRepository.save(any(Deck.class))).thenReturn(deckNoDescription);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo(TEST_DECK_NAME);
            assertThat(response.getDescription()).isNull();

            logger.debug("Test passed: Deck imported without description");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should import deck with single card")
        void testImportDeckWithSingleCardSuccess() {
            logger.debug("Test: Successful import with single card");

            Deck singleCardDeck = mock(Deck.class);
            when(singleCardDeck.getId()).thenReturn(TEST_DECK_ID);
            when(singleCardDeck.getName()).thenReturn(TEST_DECK_NAME);
            when(singleCardDeck.getDescription()).thenReturn(TEST_DECK_DESCRIPTION);
            when(singleCardDeck.getCardCount()).thenReturn(1);

            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION))
                    .thenReturn(singleCardDeck);
            when(deckRepository.save(any(Deck.class))).thenReturn(singleCardDeck);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(TEST_DECK_ID);

            logger.debug("Test passed: Deck imported with single card");
        }
    }

    @Nested
    @DisplayName("Import Deck Validation - Deck Name")
    class ImportDeckNameValidationTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when deck name already exists")
        void testImportDeckDuplicateName() {
            logger.debug("Test: Import with duplicate deck name");

            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);
            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(true);

            ErrorResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).contains("already exists");

            verify(deckService).isDuplicateName(TEST_USER_ID, TEST_DECK_NAME);
            verify(deckService, never()).createDeck(anyLong(), anyString(), anyString());

            logger.debug("Test passed: Duplicate deck name rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when deck name is missing")
        void testImportDeckMissingName() {
            logger.debug("Test: Import with missing deck name");

            String requestJson = """
                    {
                        "description": "Test description",
                        "cards": [{"front": "test", "back": "test"}]
                    }
                    """;

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());
            verify(deckService, never()).createDeck(anyLong(), anyString(), anyString());

            logger.debug("Test passed: Missing deck name rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when deck name is blank")
        void testImportDeckBlankName() {
            logger.debug("Test: Import with blank deck name");

            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);
            DeckImportRequest request = new DeckImportRequest(
                    "   ",
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1)
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Blank deck name rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when deck name exceeds max length")
        void testImportDeckNameTooLong() {
            logger.debug("Test: Import with deck name exceeding max length");

            String longName = "a".repeat(256);
            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);
            DeckImportRequest request = new DeckImportRequest(
                    longName,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1)
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Long deck name rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when description exceeds max length")
        void testImportDeckDescriptionTooLong() {
            logger.debug("Test: Import with description exceeding max length");

            String longDescription = "a".repeat(1001);
            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);
            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    longDescription,
                    Collections.singletonList(card1)
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Long description rejected");
        }
    }

    @Nested
    @DisplayName("Import Deck Validation - Cards")
    class ImportDeckCardValidationTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when cards list is null")
        void testImportDeckNullCards() {
            logger.debug("Test: Import with null cards list");

            String requestJson = """
                    {
                        "name": "Test Deck",
                        "description": "Test description"
                    }
                    """;

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Null cards rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when cards list is empty")
        void testImportDeckEmptyCards() {
            logger.debug("Test: Import with empty cards list");

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.emptyList()
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Empty cards rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when card front is missing")
        void testImportDeckMissingCardFront() {
            logger.debug("Test: Import with missing card front");

            String requestJson = """
                    {
                        "name": "Test Deck",
                        "description": "Test description",
                        "cards": [{"back": "test back"}]
                    }
                    """;

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Missing card front rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when card back is missing")
        void testImportDeckMissingCardBack() {
            logger.debug("Test: Import with missing card back");

            String requestJson = """
                    {
                        "name": "Test Deck",
                        "description": "Test description",
                        "cards": [{"front": "test front"}]
                    }
                    """;

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Missing card back rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when card front exceeds max length")
        void testImportDeckCardFrontTooLong() {
            logger.debug("Test: Import with card front exceeding max length");

            String longFront = "a".repeat(1001);
            CardImportDto card1 = new CardImportDto(longFront, TEST_CARD_BACK_1);
            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1)
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Long card front rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when card back exceeds max length")
        void testImportDeckCardBackTooLong() {
            logger.debug("Test: Import with card back exceeding max length");

            String longBack = "a".repeat(1001);
            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, longBack);
            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1)
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Long card back rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when card notes exceeds max length")
        void testImportDeckCardNotesTooLong() {
            logger.debug("Test: Import with card notes exceeding max length");

            String longNotes = "a".repeat(2001);
            CardImportDto card1 = new CardImportDto(
                    TEST_CARD_FRONT_1, TEST_CARD_BACK_1, longNotes, null
            );
            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1)
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Long card notes rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when duplicate cards exist in import")
        void testImportDeckDuplicateCards() {
            logger.debug("Test: Import with duplicate cards");

            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);
            CardImportDto card2 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1); // Duplicate

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Arrays.asList(card1, card2)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION))
                    .thenReturn(testDeck);

            ErrorResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ErrorResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).contains("Duplicate card");

            logger.debug("Test passed: Duplicate cards rejected");
        }
    }

    @Nested
    @DisplayName("Import Deck Validation - Tags")
    class ImportDeckTagValidationTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when tag name is blank")
        void testImportDeckBlankTagName() {
            logger.debug("Test: Import with blank tag name");

            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);
            TagImportDto blankTag = new TagImportDto("   ");

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1),
                    Collections.singletonList(blankTag)
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Blank tag name rejected");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should return 400 when tag name exceeds max length")
        void testImportDeckTagNameTooLong() {
            logger.debug("Test: Import with tag name exceeding max length");

            CardImportDto card1 = new CardImportDto(TEST_CARD_FRONT_1, TEST_CARD_BACK_1);
            String longTagName = "a".repeat(51);
            TagImportDto longTag = new TagImportDto(longTagName);

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1),
                    Collections.singletonList(longTag)
            );

            client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(deckService, never()).isDuplicateName(anyLong(), anyString());

            logger.debug("Test passed: Long tag name rejected");
        }
    }

    @Nested
    @DisplayName("Import Deck Edge Cases")
    class ImportDeckEdgeCasesTests {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should handle special characters in card content")
        void testImportDeckSpecialCharacters() {
            logger.debug("Test: Import with special characters");

            CardImportDto card1 = new CardImportDto(
                    "食べる・飲む",  // Japanese with interpunct
                    "to eat / to drink",
                    "Includes 'る' and 'む' verb endings",
                    Arrays.asList("verbs", "jlpt-n5")
            );

            DeckImportRequest request = new DeckImportRequest(
                    "日本語 Vocabulary",
                    "Description with émojis 🎌 and spëcial çharacters",
                    Collections.singletonList(card1)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, "日本語 Vocabulary")).thenReturn(false);
            when(deckService.createDeck(eq(TEST_USER_ID), eq("日本語 Vocabulary"), anyString()))
                    .thenReturn(testDeck);
            when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();

            logger.debug("Test passed: Special characters handled correctly");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should handle tags referenced in cards but not in tags list")
        void testImportDeckInferredTags() {
            logger.debug("Test: Import with tags inferred from cards");

            // Card references tags that aren't in the tags list
            CardImportDto card1 = new CardImportDto(
                    TEST_CARD_FRONT_1, TEST_CARD_BACK_1, null,
                    Arrays.asList("inferred-tag-1", "inferred-tag-2")
            );

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1),
                    Collections.emptyList()  // No explicit tags
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION))
                    .thenReturn(testDeck);
            when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();

            logger.debug("Test passed: Tags inferred from cards correctly");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should handle many cards in single import")
        void testImportDeckManyCards() {
            logger.debug("Test: Import with many cards");

            // Create 50 cards
            List<CardImportDto> cards = new java.util.ArrayList<>();
            for (int i = 0; i < 50; i++) {
                cards.add(new CardImportDto("Front " + i, "Back " + i));
            }

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    cards
            );

            Deck largeDeck = mock(Deck.class);
            when(largeDeck.getId()).thenReturn(TEST_DECK_ID);
            when(largeDeck.getName()).thenReturn(TEST_DECK_NAME);
            when(largeDeck.getDescription()).thenReturn(TEST_DECK_DESCRIPTION);
            when(largeDeck.getCardCount()).thenReturn(50);

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION))
                    .thenReturn(largeDeck);
            when(deckRepository.save(any(Deck.class))).thenReturn(largeDeck);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();

            logger.debug("Test passed: Many cards imported successfully");
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("Should handle card with empty notes (not null)")
        void testImportDeckCardWithEmptyNotes() {
            logger.debug("Test: Import with card having empty notes");

            CardImportDto card1 = new CardImportDto(
                    TEST_CARD_FRONT_1, TEST_CARD_BACK_1, "", null
            );

            DeckImportRequest request = new DeckImportRequest(
                    TEST_DECK_NAME,
                    TEST_DECK_DESCRIPTION,
                    Collections.singletonList(card1)
            );

            when(deckService.isDuplicateName(TEST_USER_ID, TEST_DECK_NAME)).thenReturn(false);
            when(deckService.createDeck(TEST_USER_ID, TEST_DECK_NAME, TEST_DECK_DESCRIPTION))
                    .thenReturn(testDeck);
            when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

            DeckResponse response = client.post()
                    .uri("/api/import/deck")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(DeckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();

            logger.debug("Test passed: Card with empty notes handled correctly");
        }
    }
}
