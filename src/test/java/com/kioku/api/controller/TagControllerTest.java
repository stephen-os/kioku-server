package com.kioku.api.controller;

import com.kioku.api.dto.request.CreateTagRequest;
import com.kioku.api.dto.request.UpdateTagRequest;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.dto.response.TagResponse;
import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.security.JwtAuthenticationFilter;
import com.kioku.api.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
 * Integration tests for TagController using RestTestClient (Spring Boot 4.0).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Tag creation within decks</li>
 *   <li>Tag retrieval (deck-scoped and user-level)</li>
 *   <li>Tag update endpoint</li>
 *   <li>Tag deletion endpoint</li>
 *   <li>Request validation</li>
 *   <li>Duplicate name detection (per deck)</li>
 *   <li>Authorization checks</li>
 *   <li>Error handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(
        controllers = TagController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@DisplayName("TagController Integration Tests")
class TagControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(TagControllerTest.class);

    // Test data constants
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_DECK_ID = 1L;
    private static final Long TEST_TAG_ID = 1L;
    private static final String TEST_NAME = "verbs";
    private static final String UPDATED_NAME = "verbs-updated";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    RestTestClient client;
    private User testUser;
    private Deck testDeck;
    private Tag testTag;

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
        logger.debug("Setting up TagController test");

        testUser = new User("test@example.com", "hashedPassword");
        testUser.setId(TEST_USER_ID);

        testDeck = new Deck(testUser, "Test Deck", "Test Description");
        testDeck.setId(TEST_DECK_ID);

        testTag = new Tag(testDeck, TEST_NAME);
        testTag.setId(TEST_TAG_ID);

        client = RestTestClient.bindTo(mockMvc).build();
    }

    // Tag Creation Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should create tag successfully")
    void testCreateTagSuccess() {
        logger.debug("Test: Successful tag creation");

        CreateTagRequest request = new CreateTagRequest(TEST_NAME);

        when(tagService.createTag(TEST_USER_ID, TEST_DECK_ID, TEST_NAME))
                .thenReturn(testTag);

        TagResponse response = client.post()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TagResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.getName()).isEqualTo(TEST_NAME);

        verify(tagService).createTag(TEST_USER_ID, TEST_DECK_ID, TEST_NAME);

        logger.debug("Test passed: Tag created successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when creating tag with duplicate name in same deck")
    void testCreateTagDuplicateName() {
        logger.debug("Test: Tag creation with duplicate name");

        CreateTagRequest request = new CreateTagRequest(TEST_NAME);

        when(tagService.createTag(TEST_USER_ID, TEST_DECK_ID, TEST_NAME))
                .thenThrow(new IllegalArgumentException("Tag with name '" + TEST_NAME + "' already exists in this deck"));

        ErrorResponse response = client.post()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("already exists");

        verify(tagService).createTag(TEST_USER_ID, TEST_DECK_ID, TEST_NAME);

        logger.debug("Test passed: Duplicate name rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when deck not owned by user")
    void testCreateTagDeckNotOwned() {
        logger.debug("Test: Tag creation in deck not owned by user");

        CreateTagRequest request = new CreateTagRequest(TEST_NAME);

        when(tagService.createTag(TEST_USER_ID, TEST_DECK_ID, TEST_NAME))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        ErrorResponse response = client.post()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
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
    @DisplayName("Should return 400 when name is missing")
    void testCreateTagMissingName() {
        logger.debug("Test: Tag creation with missing name");

        String requestJson = "{}";

        client.post()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(tagService, never()).createTag(anyLong(), anyLong(), anyString());

        logger.debug("Test passed: Missing name rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when name is blank")
    void testCreateTagBlankName() {
        logger.debug("Test: Tag creation with blank name");

        CreateTagRequest request = new CreateTagRequest("   ");

        client.post()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(tagService, never()).createTag(anyLong(), anyLong(), anyString());

        logger.debug("Test passed: Blank name rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when name exceeds max length")
    void testCreateTagNameTooLong() {
        logger.debug("Test: Tag creation with name exceeding max length");

        String longName = "a".repeat(101);
        CreateTagRequest request = new CreateTagRequest(longName);

        client.post()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(tagService, never()).createTag(anyLong(), anyLong(), anyString());

        logger.debug("Test passed: Name too long rejected");
    }

    // Tag Retrieval Tests - Deck Scoped

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve all tags in deck successfully")
    void testGetDeckTagsSuccess() {
        logger.debug("Test: Successful retrieval of deck tags");

        Tag tag2 = new Tag(testDeck, "adjectives");
        tag2.setId(2L);

        List<Tag> tags = Arrays.asList(testTag, tag2);

        when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(tags);

        List<TagResponse> response = client.get()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<TagResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.get(1).getId()).isEqualTo(2L);

        verify(tagService).getDeckTags(TEST_USER_ID, TEST_DECK_ID);

        logger.debug("Test passed: Deck tags retrieved successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return empty list when deck has no tags")
    void testGetDeckTagsEmpty() {
        logger.debug("Test: Retrieve tags when deck has none");

        when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID)).thenReturn(Arrays.asList());

        List<TagResponse> response = client.get()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<TagResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        verify(tagService).getDeckTags(TEST_USER_ID, TEST_DECK_ID);

        logger.debug("Test passed: Empty list returned");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when deck not found for tag retrieval")
    void testGetDeckTagsDeckNotFound() {
        logger.debug("Test: Retrieve tags from non-existent deck");

        when(tagService.getDeckTags(TEST_USER_ID, TEST_DECK_ID))
                .thenThrow(new IllegalArgumentException("Deck not found or access denied"));

        ErrorResponse response = client.get()
                .uri("/api/decks/{deckId}/tags", TEST_DECK_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("Deck not found or access denied");

        logger.debug("Test passed: Deck not found rejected");
    }

    // Tag Retrieval Tests - User Level

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve all user tags across all decks")
    void testGetAllUserTagsSuccess() {
        logger.debug("Test: Successful retrieval of all user tags");

        Deck deck2 = new Deck(testUser, "Deck 2", "Description 2");
        deck2.setId(2L);

        Tag tag2 = new Tag(deck2, "adjectives");
        tag2.setId(2L);

        List<Tag> tags = Arrays.asList(testTag, tag2);

        when(tagService.getAllUserTags(TEST_USER_ID)).thenReturn(tags);

        List<TagResponse> response = client.get()
                .uri("/api/tags")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<TagResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getName()).isEqualTo(TEST_NAME);
        assertThat(response.get(1).getName()).isEqualTo("adjectives");

        verify(tagService).getAllUserTags(TEST_USER_ID);

        logger.debug("Test passed: All user tags retrieved successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return empty list when user has no tags")
    void testGetAllUserTagsEmpty() {
        logger.debug("Test: Retrieve all tags when user has none");

        when(tagService.getAllUserTags(TEST_USER_ID)).thenReturn(Arrays.asList());

        List<TagResponse> response = client.get()
                .uri("/api/tags")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<TagResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        verify(tagService).getAllUserTags(TEST_USER_ID);

        logger.debug("Test passed: Empty list returned");
    }

    // Tag Retrieval Tests - Single Tag

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve tag by ID successfully")
    void testGetTagSuccess() {
        logger.debug("Test: Successful retrieval of tag by ID");

        when(tagService.getTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID))
                .thenReturn(Optional.of(testTag));

        TagResponse response = client.get()
                .uri("/api/decks/{deckId}/tags/{tagId}", TEST_DECK_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TagResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.getName()).isEqualTo(TEST_NAME);

        verify(tagService).getTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID);

        logger.debug("Test passed: Tag retrieved successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 404 when tag not found")
    void testGetTagNotFound() {
        logger.debug("Test: Tag retrieval with non-existent tag");

        when(tagService.getTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID))
                .thenReturn(Optional.empty());

        ErrorResponse response = client.get()
                .uri("/api/decks/{deckId}/tags/{tagId}", TEST_DECK_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        verify(tagService).getTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID);

        logger.debug("Test passed: Tag not found rejected");
    }

    // Tag Update Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should update tag successfully")
    void testUpdateTagSuccess() {
        logger.debug("Test: Successful tag update");

        UpdateTagRequest request = new UpdateTagRequest(UPDATED_NAME);

        Tag updatedTag = new Tag(testDeck, UPDATED_NAME);
        updatedTag.setId(TEST_TAG_ID);

        when(tagService.updateTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID, UPDATED_NAME))
                .thenReturn(updatedTag);

        TagResponse response = client.put()
                .uri("/api/decks/{deckId}/tags/{tagId}", TEST_DECK_ID, TEST_TAG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TagResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_TAG_ID);
        assertThat(response.getName()).isEqualTo(UPDATED_NAME);

        verify(tagService).updateTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID, UPDATED_NAME);

        logger.debug("Test passed: Tag updated successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when update creates duplicate name")
    void testUpdateTagDuplicateName() {
        logger.debug("Test: Update that would create duplicate name");

        UpdateTagRequest request = new UpdateTagRequest(UPDATED_NAME);

        when(tagService.updateTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID, UPDATED_NAME))
                .thenThrow(new IllegalArgumentException("Tag with name '" + UPDATED_NAME + "' already exists in this deck"));

        ErrorResponse response = client.put()
                .uri("/api/decks/{deckId}/tags/{tagId}", TEST_DECK_ID, TEST_TAG_ID)
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
    @DisplayName("Should return 400 when updating non-existent tag")
    void testUpdateTagNotFound() {
        logger.debug("Test: Update non-existent tag");

        UpdateTagRequest request = new UpdateTagRequest(UPDATED_NAME);

        when(tagService.updateTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID, UPDATED_NAME))
                .thenThrow(new IllegalArgumentException("Tag not found or access denied: " + TEST_TAG_ID));

        ErrorResponse response = client.put()
                .uri("/api/decks/{deckId}/tags/{tagId}", TEST_DECK_ID, TEST_TAG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        logger.debug("Test passed: Update non-existent tag rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when update name is blank")
    void testUpdateTagBlankName() {
        logger.debug("Test: Update tag with blank name");

        UpdateTagRequest request = new UpdateTagRequest("");

        client.put()
                .uri("/api/decks/{deckId}/tags/{tagId}", TEST_DECK_ID, TEST_TAG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(tagService, never()).updateTag(anyLong(), anyLong(), anyLong(), anyString());

        logger.debug("Test passed: Blank name rejected");
    }

    // Tag Deletion Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should delete tag successfully")
    void testDeleteTagSuccess() {
        logger.debug("Test: Successful tag deletion");

        doNothing().when(tagService).deleteTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID);

        client.delete()
                .uri("/api/decks/{deckId}/tags/{tagId}", TEST_DECK_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isNoContent();

        verify(tagService).deleteTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID);

        logger.debug("Test passed: Tag deleted successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when deleting non-existent tag")
    void testDeleteTagNotFound() {
        logger.debug("Test: Delete non-existent tag");

        doThrow(new IllegalArgumentException("Tag not found or access denied: " + TEST_TAG_ID))
                .when(tagService).deleteTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID);

        ErrorResponse response = client.delete()
                .uri("/api/decks/{deckId}/tags/{tagId}", TEST_DECK_ID, TEST_TAG_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("not found or access denied");

        verify(tagService).deleteTag(TEST_USER_ID, TEST_DECK_ID, TEST_TAG_ID);

        logger.debug("Test passed: Delete non-existent tag rejected");
    }
}