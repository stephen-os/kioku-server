package com.kioku.api.controller;

import com.kioku.api.dto.request.CreateDeckRequest;
import com.kioku.api.dto.request.UpdateDeckRequest;
import com.kioku.api.dto.response.DeckResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.UserEntity;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.security.JwtAuthenticationFilter;
import com.kioku.api.service.DeckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for DeckController using RestTestClient (Spring Boot 4.0).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Deck creation endpoint</li>
 *   <li>Deck retrieval endpoints (all decks, single deck)</li>
 *   <li>Deck update endpoint</li>
 *   <li>Deck deletion endpoint</li>
 *   <li>Request validation</li>
 *   <li>Duplicate name detection</li>
 *   <li>Authorization checks</li>
 *   <li>Error handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(
        controllers = DeckController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@DisplayName("DeckController Integration Tests")
class DeckControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(DeckControllerTest.class);

    // Test data constants
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_DECK_ID = 1L;
    private static final String TEST_NAME = "Japanese Verbs";
    private static final String TEST_DESCRIPTION = "Common Japanese verbs for JLPT N5 level";
    private static final String UPDATED_NAME = "Japanese Verbs - Updated";
    private static final String UPDATED_DESCRIPTION = "Updated description for JLPT N4 level";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private DeckService deckService;

    RestTestClient client;
    private UserEntity testUser;
    private DeckEntity testDeck;

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
        logger.debug("Setting up DeckController test");

        testUser = new UserEntity("test@example.com", "hashedPassword");
        testUser.setId(TEST_USER_ID);

        testDeck = new DeckEntity(testUser, TEST_NAME, TEST_DESCRIPTION);
        testDeck.setId(TEST_DECK_ID);

        client = RestTestClient.bindTo(mockMvc).build();
    }

    // Deck Creation Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should create deck successfully")
    void testCreateDeckSuccess() {
        logger.debug("Test: Successful deck creation");

        CreateDeckRequest request = new CreateDeckRequest(TEST_NAME, TEST_DESCRIPTION);

        when(deckService.createDeck(TEST_USER_ID, TEST_NAME, TEST_DESCRIPTION))
                .thenReturn(testDeck);

        DeckResponse response = client.post()
                .uri("/api/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DeckResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_DECK_ID);
        assertThat(response.getName()).isEqualTo(TEST_NAME);
        assertThat(response.getDescription()).isEqualTo(TEST_DESCRIPTION);

        verify(deckService).createDeck(TEST_USER_ID, TEST_NAME, TEST_DESCRIPTION);

        logger.debug("Test passed: Deck created successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when creating deck with duplicate name")
    void testCreateDeckDuplicateName() {
        logger.debug("Test: Deck creation with duplicate name");

        CreateDeckRequest request = new CreateDeckRequest(TEST_NAME, TEST_DESCRIPTION);

        when(deckService.createDeck(TEST_USER_ID, TEST_NAME, TEST_DESCRIPTION))
                .thenThrow(new IllegalArgumentException("Deck with name '" + TEST_NAME + "' already exists"));

        ErrorResponse response = client.post()
                .uri("/api/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("already exists");

        verify(deckService).createDeck(TEST_USER_ID, TEST_NAME, TEST_DESCRIPTION);

        logger.debug("Test passed: Duplicate name rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when name is missing")
    void testCreateDeckMissingName() {
        logger.debug("Test: Deck creation with missing name");

        String requestJson = "{\"description\":\"" + TEST_DESCRIPTION + "\"}";

        client.post()
                .uri("/api/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(deckService, never()).createDeck(anyLong(), anyString(), anyString());

        logger.debug("Test passed: Missing name rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when name is blank")
    void testCreateDeckBlankName() {
        logger.debug("Test: Deck creation with blank name");

        CreateDeckRequest request = new CreateDeckRequest("   ", TEST_DESCRIPTION);

        client.post()
                .uri("/api/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(deckService, never()).createDeck(anyLong(), anyString(), anyString());

        logger.debug("Test passed: Blank name rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when name exceeds max length")
    void testCreateDeckNameTooLong() {
        logger.debug("Test: Deck creation with name exceeding max length");

        String longName = "a".repeat(256);
        CreateDeckRequest request = new CreateDeckRequest(longName, TEST_DESCRIPTION);

        client.post()
                .uri("/api/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(deckService, never()).createDeck(anyLong(), anyString(), anyString());

        logger.debug("Test passed: Name too long rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when description exceeds max length")
    void testCreateDeckDescriptionTooLong() {
        logger.debug("Test: Deck creation with description exceeding max length");

        String longDescription = "a".repeat(1001);
        CreateDeckRequest request = new CreateDeckRequest(TEST_NAME, longDescription);

        client.post()
                .uri("/api/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(deckService, never()).createDeck(anyLong(), anyString(), anyString());

        logger.debug("Test passed: Description too long rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should create deck successfully without description")
    void testCreateDeckWithoutDescription() {
        logger.debug("Test: Deck creation without description");

        CreateDeckRequest request = new CreateDeckRequest(TEST_NAME, null);
        DeckEntity deckWithoutDescription = new DeckEntity(testUser, TEST_NAME, null);
        deckWithoutDescription.setId(TEST_DECK_ID);

        when(deckService.createDeck(TEST_USER_ID, TEST_NAME, null))
                .thenReturn(deckWithoutDescription);

        DeckResponse response = client.post()
                .uri("/api/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DeckResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(TEST_NAME);
        assertThat(response.getDescription()).isNull();

        verify(deckService).createDeck(TEST_USER_ID, TEST_NAME, null);

        logger.debug("Test passed: Deck created without description");
    }

    // Deck Retrieval Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve all user decks successfully")
    void testGetUserDecksSuccess() {
        logger.debug("Test: Successful retrieval of all user decks");

        DeckEntity deck2 = new DeckEntity(testUser, "Spanish Vocab", "Spanish vocabulary");
        deck2.setId(2L);

        List<DeckEntity> decks = Arrays.asList(testDeck, deck2);

        when(deckService.getUserDecks(TEST_USER_ID)).thenReturn(decks);

        List<DeckResponse> response = client.get()
                .uri("/api/decks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<DeckResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getId()).isEqualTo(TEST_DECK_ID);
        assertThat(response.get(1).getId()).isEqualTo(2L);

        verify(deckService).getUserDecks(TEST_USER_ID);

        logger.debug("Test passed: All decks retrieved successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return empty list when user has no decks")
    void testGetUserDecksEmpty() {
        logger.debug("Test: Retrieve decks when user has none");

        when(deckService.getUserDecks(TEST_USER_ID)).thenReturn(Arrays.asList());

        List<DeckResponse> response = client.get()
                .uri("/api/decks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<DeckResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        verify(deckService).getUserDecks(TEST_USER_ID);

        logger.debug("Test passed: Empty list returned");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve deck by ID successfully")
    void testGetDeckSuccess() {
        logger.debug("Test: Successful retrieval of deck by ID");

        when(deckService.getDeck(TEST_DECK_ID, TEST_USER_ID))
                .thenReturn(Optional.of(testDeck));

        DeckResponse response = client.get()
                .uri("/api/decks/{deckId}", TEST_DECK_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeckResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_DECK_ID);
        assertThat(response.getName()).isEqualTo(TEST_NAME);

        verify(deckService).getDeck(TEST_DECK_ID, TEST_USER_ID);

        logger.debug("Test passed: Deck retrieved successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 404 when deck not found")
    void testGetDeckNotFound() {
        logger.debug("Test: Deck retrieval with non-existent deck");

        when(deckService.getDeck(TEST_DECK_ID, TEST_USER_ID))
                .thenReturn(Optional.empty());

        ErrorResponse response = client.get()
                .uri("/api/decks/{deckId}", TEST_DECK_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        verify(deckService).getDeck(TEST_DECK_ID, TEST_USER_ID);

        logger.debug("Test passed: Deck not found rejected");
    }

    // Deck Update Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should update deck successfully")
    void testUpdateDeckSuccess() {
        logger.debug("Test: Successful deck update");

        UpdateDeckRequest request = new UpdateDeckRequest(UPDATED_NAME, UPDATED_DESCRIPTION);

        DeckEntity updatedDeck = new DeckEntity(testUser, UPDATED_NAME, UPDATED_DESCRIPTION);
        updatedDeck.setId(TEST_DECK_ID);

        when(deckService.updateDeck(TEST_DECK_ID, TEST_USER_ID, UPDATED_NAME, UPDATED_DESCRIPTION))
                .thenReturn(updatedDeck);

        DeckResponse response = client.put()
                .uri("/api/decks/{deckId}", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeckResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_DECK_ID);
        assertThat(response.getName()).isEqualTo(UPDATED_NAME);
        assertThat(response.getDescription()).isEqualTo(UPDATED_DESCRIPTION);

        verify(deckService).updateDeck(TEST_DECK_ID, TEST_USER_ID, UPDATED_NAME, UPDATED_DESCRIPTION);

        logger.debug("Test passed: Deck updated successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when update creates duplicate name")
    void testUpdateDeckDuplicateName() {
        logger.debug("Test: Update that would create duplicate name");

        UpdateDeckRequest request = new UpdateDeckRequest(UPDATED_NAME, UPDATED_DESCRIPTION);

        when(deckService.updateDeck(TEST_DECK_ID, TEST_USER_ID, UPDATED_NAME, UPDATED_DESCRIPTION))
                .thenThrow(new IllegalArgumentException("Deck with name '" + UPDATED_NAME + "' already exists"));

        ErrorResponse response = client.put()
                .uri("/api/decks/{deckId}", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("already exists");

        logger.debug("Test passed: Duplicate name update rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when updating non-existent deck")
    void testUpdateDeckNotFound() {
        logger.debug("Test: Update non-existent deck");

        UpdateDeckRequest request = new UpdateDeckRequest(UPDATED_NAME, UPDATED_DESCRIPTION);

        when(deckService.updateDeck(TEST_DECK_ID, TEST_USER_ID, UPDATED_NAME, UPDATED_DESCRIPTION))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied: " + TEST_DECK_ID));

        ErrorResponse response = client.put()
                .uri("/api/decks/{deckId}", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Update non-existent deck rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when update name is blank")
    void testUpdateDeckBlankName() {
        logger.debug("Test: Update deck with blank name");

        UpdateDeckRequest request = new UpdateDeckRequest("", UPDATED_DESCRIPTION);

        client.put()
                .uri("/api/decks/{deckId}", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(deckService, never()).updateDeck(anyLong(), anyLong(), anyString(), anyString());

        logger.debug("Test passed: Blank name rejected");
    }

    // Deck Deletion Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should delete deck successfully")
    void testDeleteDeckSuccess() {
        logger.debug("Test: Successful deck deletion");

        doNothing().when(deckService).deleteDeck(TEST_DECK_ID, TEST_USER_ID);

        client.delete()
                .uri("/api/decks/{deckId}", TEST_DECK_ID)
                .exchange()
                .expectStatus().isNoContent();

        verify(deckService).deleteDeck(TEST_DECK_ID, TEST_USER_ID);

        logger.debug("Test passed: Deck deleted successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when deleting non-existent deck")
    void testDeleteDeckNotFound() {
        logger.debug("Test: Delete non-existent deck");

        doThrow(new IllegalArgumentException("Deck not found or access denied: " + TEST_DECK_ID))
                .when(deckService).deleteDeck(TEST_DECK_ID, TEST_USER_ID);

        ErrorResponse response = client.delete()
                .uri("/api/decks/{deckId}", TEST_DECK_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        verify(deckService).deleteDeck(TEST_DECK_ID, TEST_USER_ID);

        logger.debug("Test passed: Delete non-existent deck rejected");
    }
}