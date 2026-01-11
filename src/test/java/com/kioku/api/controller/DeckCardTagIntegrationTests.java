package com.kioku.api.controller;

import com.kioku.api.dto.request.*;
import com.kioku.api.dto.response.*;
import com.kioku.api.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for deck, card, and tag workflows.
 *
 * <p>These tests verify the complete workflow a user would follow:
 * <ul>
 *   <li>Creating decks and adding cards</li>
 *   <li>Tagging cards correctly</li>
 *   <li>Searching by name and tag</li>
 *   <li>Verifying tag associations with decks</li>
 *   <li>Import/export functionality</li>
 *   <li>Edge cases users might encounter</li>
 * </ul>
 *
 * <p>Uses H2 in-memory database for testing.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Deck/Card/Tag Integration Tests")
class DeckCardTagIntegrationTests {

    private static final Logger logger = LoggerFactory.getLogger(DeckCardTagIntegrationTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String authToken;
    private Long testDeckId;
    private Long testCardId;
    private Long testTagId;

    // Test data constants
    private static final String TEST_EMAIL = "integration-test@example.com";
    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final String TEST_DECK_NAME = "Japanese N5 Vocabulary";
    private static final String TEST_DECK_DESCRIPTION = "Essential JLPT N5 vocabulary for beginners";
    private static final String TEST_CARD_FRONT = "食べる";
    private static final String TEST_CARD_BACK = "to eat";
    private static final String TEST_CARD_NOTES = "ru-verb, Group 2";
    private static final String TEST_TAG_NAME = "verbs";

    @BeforeAll
    void beforeAll() throws Exception {
        logger.info("Setting up integration test user");

        // Clean up any existing test user
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);

        // Register test user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        authToken = authResponse.getToken();

        logger.info("Integration test user created successfully");
    }

    @AfterAll
    void afterAll() {
        logger.info("Cleaning up integration test data");
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        userRepository.findByEmail("other-user@example.com").ifPresent(userRepository::delete);
    }

    // ==================== DECK WORKFLOW TESTS ====================

    @Test
    @Order(1)
    @DisplayName("User can create a new deck")
    void testCreateDeck() throws Exception {
        logger.info("Test: Creating a new deck");

        CreateDeckRequest request = new CreateDeckRequest();
        request.setName(TEST_DECK_NAME);
        request.setDescription(TEST_DECK_DESCRIPTION);

        MvcResult result = mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(TEST_DECK_NAME))
                .andExpect(jsonPath("$.description").value(TEST_DECK_DESCRIPTION))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        DeckResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), DeckResponse.class);
        testDeckId = response.getId();

        assertThat(testDeckId).isNotNull();
        assertThat(response.getName()).isEqualTo(TEST_DECK_NAME);
        assertThat(response.getDescription()).isEqualTo(TEST_DECK_DESCRIPTION);

        logger.info("Deck created with ID: {}", testDeckId);
    }

    @Test
    @Order(2)
    @DisplayName("User can get deck by ID")
    void testGetDeckById() throws Exception {
        logger.info("Test: Getting deck by ID: {}", testDeckId);

        mockMvc.perform(get("/api/decks/{deckId}", testDeckId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDeckId))
                .andExpect(jsonPath("$.name").value(TEST_DECK_NAME))
                .andExpect(jsonPath("$.description").value(TEST_DECK_DESCRIPTION));

        logger.info("Successfully retrieved deck by ID");
    }

    @Test
    @Order(3)
    @DisplayName("User can list all their decks")
    void testListDecks() throws Exception {
        logger.info("Test: Listing all user decks");

        mockMvc.perform(get("/api/decks")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testDeckId))
                .andExpect(jsonPath("$[0].name").value(TEST_DECK_NAME));

        logger.info("Successfully listed user decks");
    }

    // ==================== CARD WORKFLOW TESTS ====================

    @Test
    @Order(10)
    @DisplayName("User can add a card to a deck")
    void testCreateCard() throws Exception {
        logger.info("Test: Creating a card in deck: {}", testDeckId);

        CreateCardRequest request = new CreateCardRequest();
        request.setFront(TEST_CARD_FRONT);
        request.setBack(TEST_CARD_BACK);
        request.setNotes(TEST_CARD_NOTES);

        MvcResult result = mockMvc.perform(post("/api/decks/{deckId}/cards", testDeckId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.front").value(TEST_CARD_FRONT))
                .andExpect(jsonPath("$.back").value(TEST_CARD_BACK))
                .andExpect(jsonPath("$.notes").value(TEST_CARD_NOTES))
                .andReturn();

        CardResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), CardResponse.class);
        testCardId = response.getId();

        assertThat(testCardId).isNotNull();
        logger.info("Card created with ID: {}", testCardId);
    }

    @Test
    @Order(11)
    @DisplayName("User can add multiple cards to a deck")
    void testCreateMultipleCards() throws Exception {
        logger.info("Test: Creating multiple cards in deck");

        String[][] cardsData = {
                {"飲む", "to drink", "u-verb"},
                {"見る", "to see", "ru-verb"},
                {"行く", "to go", "u-verb, irregular"},
                {"来る", "to come", "ru-verb, irregular"}
        };

        for (String[] cardData : cardsData) {
            CreateCardRequest request = new CreateCardRequest();
            request.setFront(cardData[0]);
            request.setBack(cardData[1]);
            request.setNotes(cardData[2]);

            mockMvc.perform(post("/api/decks/{deckId}/cards", testDeckId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        logger.info("Created {} additional cards", cardsData.length);
    }

    @Test
    @Order(12)
    @DisplayName("User can get all cards in a deck")
    void testGetDeckCards() throws Exception {
        logger.info("Test: Getting all cards from deck: {}", testDeckId);

        mockMvc.perform(get("/api/decks/{deckId}/cards", testDeckId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5)); // 1 + 4 additional

        logger.info("Successfully retrieved all cards from deck");
    }

    @Test
    @Order(13)
    @DisplayName("User can get a specific card by ID")
    void testGetCardById() throws Exception {
        logger.info("Test: Getting card by ID: {}", testCardId);

        mockMvc.perform(get("/api/decks/{deckId}/cards/{cardId}", testDeckId, testCardId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCardId))
                .andExpect(jsonPath("$.front").value(TEST_CARD_FRONT))
                .andExpect(jsonPath("$.back").value(TEST_CARD_BACK));

        logger.info("Successfully retrieved card by ID");
    }

    @Test
    @Order(14)
    @DisplayName("User can update a card")
    void testUpdateCard() throws Exception {
        logger.info("Test: Updating card: {}", testCardId);

        String updatedNotes = "ru-verb, Group 2, Updated notes";
        UpdateCardRequest request = new UpdateCardRequest();
        request.setFront(TEST_CARD_FRONT);  // Required field
        request.setBack(TEST_CARD_BACK);    // Required field
        request.setNotes(updatedNotes);

        mockMvc.perform(put("/api/decks/{deckId}/cards/{cardId}", testDeckId, testCardId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value(updatedNotes));

        logger.info("Successfully updated card");
    }

    @Test
    @Order(15)
    @DisplayName("User can search cards by front text")
    void testSearchCardsByFront() throws Exception {
        logger.info("Test: Searching cards by front text");

        // The search parameter is on the main cards endpoint, not a separate /search endpoint
        mockMvc.perform(get("/api/decks/{deckId}/cards", testDeckId)
                        .header("Authorization", "Bearer " + authToken)
                        .param("search", "食"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].front").value(TEST_CARD_FRONT));

        logger.info("Successfully searched cards by front text");
    }

    // ==================== TAG WORKFLOW TESTS ====================

    @Test
    @Order(20)
    @DisplayName("User can create a tag in a deck")
    void testCreateTag() throws Exception {
        logger.info("Test: Creating a tag in deck: {}", testDeckId);

        CreateTagRequest request = new CreateTagRequest();
        request.setName(TEST_TAG_NAME);

        MvcResult result = mockMvc.perform(post("/api/decks/{deckId}/tags", testDeckId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(TEST_TAG_NAME))
                .andReturn();

        TagResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), TagResponse.class);
        testTagId = response.getId();

        assertThat(testTagId).isNotNull();
        logger.info("Tag created with ID: {}", testTagId);
    }

    @Test
    @Order(21)
    @DisplayName("User can create multiple tags")
    void testCreateMultipleTags() throws Exception {
        logger.info("Test: Creating multiple tags");

        String[] tagNames = {"nouns", "adjectives", "u-verbs", "ru-verbs"};

        for (String tagName : tagNames) {
            CreateTagRequest request = new CreateTagRequest();
            request.setName(tagName);

            mockMvc.perform(post("/api/decks/{deckId}/tags", testDeckId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        logger.info("Created {} additional tags", tagNames.length);
    }

    @Test
    @Order(22)
    @DisplayName("User can list all tags in a deck")
    void testGetDeckTags() throws Exception {
        logger.info("Test: Getting all tags from deck: {}", testDeckId);

        mockMvc.perform(get("/api/decks/{deckId}/tags", testDeckId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5)); // 1 + 4 additional

        logger.info("Successfully retrieved all tags from deck");
    }

    @Test
    @Order(23)
    @DisplayName("User can add a tag to a card")
    void testAddTagToCard() throws Exception {
        logger.info("Test: Adding tag {} to card {}", testTagId, testCardId);

        mockMvc.perform(post("/api/decks/{deckId}/cards/{cardId}/tags/{tagId}",
                        testDeckId, testCardId, testTagId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        logger.info("Successfully added tag to card");
    }

    @Test
    @Order(24)
    @DisplayName("Card shows associated tags")
    void testCardShowsTags() throws Exception {
        logger.info("Test: Verifying card shows associated tags");

        mockMvc.perform(get("/api/decks/{deckId}/cards/{cardId}", testDeckId, testCardId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags[0].name").value(TEST_TAG_NAME));

        logger.info("Card correctly shows associated tags");
    }

    @Test
    @Order(25)
    @DisplayName("User can search cards by tag")
    void testSearchCardsByTag() throws Exception {
        logger.info("Test: Searching cards by tag: {}", testTagId);

        // The tagId parameter is on the main cards endpoint, not a separate /by-tag endpoint
        mockMvc.perform(get("/api/decks/{deckId}/cards", testDeckId)
                        .header("Authorization", "Bearer " + authToken)
                        .param("tagId", testTagId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testCardId));

        logger.info("Successfully searched cards by tag");
    }

    @Test
    @Order(26)
    @DisplayName("User can remove a tag from a card")
    void testRemoveTagFromCard() throws Exception {
        logger.info("Test: Removing tag from card");

        mockMvc.perform(delete("/api/decks/{deckId}/cards/{cardId}/tags/{tagId}",
                        testDeckId, testCardId, testTagId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify tag was removed
        mockMvc.perform(get("/api/decks/{deckId}/cards/{cardId}", testDeckId, testCardId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isEmpty());

        logger.info("Successfully removed tag from card");
    }

    // ==================== EXPORT/IMPORT TESTS ====================

    @Test
    @Order(30)
    @DisplayName("User can export a deck")
    void testExportDeck() throws Exception {
        logger.info("Test: Exporting deck: {}", testDeckId);

        mockMvc.perform(get("/api/export/deck/{deckId}", testDeckId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDeckId))
                .andExpect(jsonPath("$.name").value(TEST_DECK_NAME))
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.metadata.version").value("1.0"))
                .andExpect(jsonPath("$.metadata.cardCount").value(5))
                .andExpect(jsonPath("$.metadata.tagCount").value(5));

        logger.info("Successfully exported deck");
    }

    @Test
    @Order(31)
    @DisplayName("User can import a new deck")
    @SuppressWarnings("unchecked")
    void testImportDeck() throws Exception {
        logger.info("Test: Importing a new deck");

        String importedDeckName = "Imported Deck";
        CardImportDto card1 = new CardImportDto("Hello", "こんにちは", "Greeting",
                Arrays.asList("greetings", "basic"));
        CardImportDto card2 = new CardImportDto("Goodbye", "さようなら", "Farewell",
                Collections.singletonList("greetings"));
        TagImportDto tag1 = new TagImportDto("greetings");
        TagImportDto tag2 = new TagImportDto("basic");

        DeckImportRequest request = new DeckImportRequest(
                importedDeckName,
                "Imported deck for testing",
                Arrays.asList(card1, card2),
                Arrays.asList(tag1, tag2)
        );

        MvcResult result = mockMvc.perform(post("/api/import/deck")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(importedDeckName))
                .andReturn();

        DeckResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), DeckResponse.class);
        Long importedDeckId = response.getId();

        assertThat(importedDeckId).isNotNull();
        assertThat(response.getName()).isEqualTo(importedDeckName);

        // Verify imported deck contents
        mockMvc.perform(get("/api/decks/{deckId}/cards", importedDeckId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/decks/{deckId}/tags", importedDeckId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        logger.info("Successfully imported deck with ID: {}", importedDeckId);
    }

    // ==================== EDGE CASES TESTS ====================

    @Test
    @Order(40)
    @DisplayName("Cannot create deck with duplicate name")
    void testDuplicateDeckName() throws Exception {
        logger.info("Test: Attempting to create deck with duplicate name");

        CreateDeckRequest request = new CreateDeckRequest();
        request.setName(TEST_DECK_NAME);

        mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        logger.info("Correctly rejected duplicate deck name");
    }

    @Test
    @Order(41)
    @DisplayName("Cannot create tag with duplicate name in same deck")
    void testDuplicateTagName() throws Exception {
        logger.info("Test: Attempting to create tag with duplicate name");

        CreateTagRequest request = new CreateTagRequest();
        request.setName(TEST_TAG_NAME);

        mockMvc.perform(post("/api/decks/{deckId}/tags", testDeckId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        logger.info("Correctly rejected duplicate tag name");
    }

    @Test
    @Order(42)
    @DisplayName("Cannot access another user's deck")
    void testAccessDenied() throws Exception {
        logger.info("Test: Attempting to access deck with wrong user");

        // Create another user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("other-user@example.com");
        registerRequest.setPassword("OtherPassword123!");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        String otherToken = authResponse.getToken();

        // Try to access the first user's deck
        mockMvc.perform(get("/api/decks/{deckId}", testDeckId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound()); // Returns 404 for inaccessible deck

        logger.info("Correctly denied access to other user's deck");
    }

    @Test
    @Order(43)
    @DisplayName("Cannot import deck with duplicate cards")
    void testImportDuplicateCards() throws Exception {
        logger.info("Test: Attempting to import deck with duplicate cards");

        CardImportDto card1 = new CardImportDto("Same", "Same");
        CardImportDto card2 = new CardImportDto("Same", "Same"); // Duplicate

        DeckImportRequest request = new DeckImportRequest(
                "Deck with duplicates",
                "Should fail",
                Arrays.asList(card1, card2)
        );

        mockMvc.perform(post("/api/import/deck")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        logger.info("Correctly rejected import with duplicate cards");
    }

    @Test
    @Order(44)
    @DisplayName("Can create card with special characters")
    void testSpecialCharactersInCard() throws Exception {
        logger.info("Test: Creating card with special characters");

        CreateCardRequest request = new CreateCardRequest();
        request.setFront("日本語・中文・한국어");
        request.setBack("Japanese / Chinese / Korean");
        request.setNotes("Languages with CJK characters");

        mockMvc.perform(post("/api/decks/{deckId}/cards", testDeckId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.front").value("日本語・中文・한국어"));

        logger.info("Successfully created card with special characters");
    }

    @Test
    @Order(45)
    @DisplayName("Can update deck name and description")
    void testUpdateDeck() throws Exception {
        logger.info("Test: Updating deck");

        String updatedDescription = "Updated description for testing";
        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setName(TEST_DECK_NAME);  // Required field
        request.setDescription(updatedDescription);

        mockMvc.perform(put("/api/decks/{deckId}", testDeckId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(updatedDescription));

        logger.info("Successfully updated deck");
    }

    // ==================== CLEANUP TESTS ====================

    @Test
    @Order(99)
    @DisplayName("User can delete a card")
    void testDeleteCard() throws Exception {
        logger.info("Test: Deleting card: {}", testCardId);

        mockMvc.perform(delete("/api/decks/{deckId}/cards/{cardId}", testDeckId, testCardId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify deletion - controller returns 404 for missing cards
        mockMvc.perform(get("/api/decks/{deckId}/cards/{cardId}", testDeckId, testCardId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

        logger.info("Successfully deleted card");
    }
}
