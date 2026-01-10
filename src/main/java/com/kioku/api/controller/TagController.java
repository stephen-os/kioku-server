package com.kioku.api.controller;

import com.kioku.api.dto.request.CreateTagRequest;
import com.kioku.api.dto.request.UpdateTagRequest;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.dto.response.TagResponse;
import com.kioku.api.model.Tag;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.TagService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for tag operations within decks.
 *
 * <p>This controller provides endpoints for managing tags within specific decks.
 * Tags are deck-scoped, meaning the same tag name can exist in different decks.
 *
 * <p><strong>Deck-Scoped Endpoints:</strong>
 * <ul>
 *   <li>POST /api/decks/{deckId}/tags - Create a tag in a deck</li>
 *   <li>GET /api/decks/{deckId}/tags - Get all tags in a deck</li>
 *   <li>GET /api/decks/{deckId}/tags/{tagId} - Get a specific tag</li>
 *   <li>PUT /api/decks/{deckId}/tags/{tagId} - Update a tag</li>
 *   <li>DELETE /api/decks/{deckId}/tags/{tagId} - Delete a tag</li>
 * </ul>
 *
 * <p><strong>User-Level Endpoints:</strong>
 * <ul>
 *   <li>GET /api/tags - Get all tags across all user's decks</li>
 * </ul>
 *
 * <p><strong>Authentication:</strong>
 * All endpoints require authentication. The authenticated user ID is automatically
 * resolved via the {@code @CurrentUser} annotation.
 *
 * <p><strong>Authorization:</strong>
 * Users can only access and modify tags in their own decks. The service layer
 * enforces ownership verification.
 *
 * <p><strong>Features:</strong>
 * <ul>
 *   <li>Duplicate name detection (per deck)</li>
 *   <li>Automatic card dissociation on tag deletion</li>
 *   <li>Cross-deck tag retrieval</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong>
 * All exceptions are handled by {@link GlobalExceptionHandler}:
 * <ul>
 *   <li>400 Bad Request - Validation errors, duplicate names</li>
 *   <li>404 Not Found - Tag or deck not found</li>
 *   <li>403 Forbidden - User doesn't own the deck</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@RestController
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    private final TagService tagService;

    /**
     * Constructs a TagController with required dependencies.
     *
     * @param tagService the tag service for business logic
     */
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Creates a new tag within a deck.
     *
     * <p><strong>Endpoint:</strong> POST /api/decks/{deckId}/tags
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "name": "verbs"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Checks for duplicate tag names within the deck</li>
     *   <li>Validates field constraints</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 201 Created with TagResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck to add the tag to
     * @param request the tag creation request containing the tag name
     * @return ResponseEntity containing the created tag
     * @throws IllegalArgumentException if duplicate or access denied (handled by GlobalExceptionHandler)
     */
    @PostMapping("/api/decks/{deckId}/tags")
    public ResponseEntity<TagResponse> createTag(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @Valid @RequestBody CreateTagRequest request) {

        logger.debug("Creating tag '{}' in deck {} for user {}", request.getName(), deckId, userId);

        Tag tag = tagService.createTag(userId, deckId, request.getName());

        logger.info("Tag {} created successfully in deck {}", tag.getId(), deckId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TagResponse(tag));
    }

    /**
     * Retrieves all tags in a specific deck.
     *
     * <p><strong>Endpoint:</strong> GET /api/decks/{deckId}/tags
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with List of TagResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @return ResponseEntity containing the list of tags
     * @throws IllegalArgumentException if deck not found or access denied (handled by GlobalExceptionHandler)
     */
    @GetMapping("/api/decks/{deckId}/tags")
    public ResponseEntity<List<TagResponse>> getDeckTags(
            @CurrentUser Long userId,
            @PathVariable Long deckId) {

        logger.debug("Retrieving tags for deck {} by user {}", deckId, userId);

        List<Tag> tags = tagService.getDeckTags(userId, deckId);
        List<TagResponse> response = tags.stream()
                .map(TagResponse::new)
                .collect(Collectors.toList());

        logger.debug("Retrieved {} tags from deck {}", response.size(), deckId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all tags across all of the user's decks.
     *
     * <p><strong>Endpoint:</strong> GET /api/tags
     *
     * <p>This endpoint returns all tags the user has created in any of their decks.
     * Useful for displaying a complete list of tags, but remember that tags are
     * deck-specific and cannot be moved between decks.
     *
     * <p><strong>Response:</strong> 200 OK with List of TagResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @return ResponseEntity containing all user's tags across all decks
     */
    @GetMapping("/api/tags")
    public ResponseEntity<List<TagResponse>> getAllUserTags(@CurrentUser Long userId) {

        logger.debug("Retrieving all tags for user {}", userId);

        List<Tag> tags = tagService.getAllUserTags(userId);
        List<TagResponse> response = tags.stream()
                .map(TagResponse::new)
                .collect(Collectors.toList());

        logger.debug("Retrieved {} total tags for user {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific tag by ID.
     *
     * <p><strong>Endpoint:</strong> GET /api/decks/{deckId}/tags/{tagId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies tag exists in the specified deck</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with TagResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param tagId the ID of the tag to retrieve
     * @return ResponseEntity containing the tag
     * @throws IllegalArgumentException if tag not found or access denied (handled by GlobalExceptionHandler)
     */
    @GetMapping("/api/decks/{deckId}/tags/{tagId}")
    public ResponseEntity<?> getTag(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long tagId) {

        logger.debug("Retrieving tag {} from deck {} for user {}", tagId, deckId, userId);

        Optional<Tag> tagOptional = tagService.getTag(userId, deckId, tagId);

        if (tagOptional.isEmpty()) {
            logger.warn("Tag {} not found or access denied in deck {}", tagId, deckId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Tag not found or access denied"));
        }

        Tag tag = tagOptional.get();
        logger.debug("Tag {} retrieved successfully", tagId);
        return ResponseEntity.ok(new TagResponse(tag));
    }

    /**
     * Updates an existing tag.
     *
     * <p><strong>Endpoint:</strong> PUT /api/decks/{deckId}/tags/{tagId}
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "name": "verbs-updated"
     * }
     * </pre>
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Checks that updated name doesn't conflict with another tag in the deck</li>
     *   <li>Validates field constraints</li>
     * </ul>
     *
     * <p><strong>Response:</strong> 200 OK with updated TagResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param tagId the ID of the tag to update
     * @param request the update request containing the new tag name
     * @return ResponseEntity containing the updated tag
     * @throws IllegalArgumentException if duplicate or access denied (handled by GlobalExceptionHandler)
     */
    @PutMapping("/api/decks/{deckId}/tags/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long tagId,
            @Valid @RequestBody UpdateTagRequest request) {

        logger.debug("Updating tag {} in deck {} for user {}", tagId, deckId, userId);

        Tag tag = tagService.updateTag(userId, deckId, tagId, request.getName());

        logger.info("Tag {} updated successfully", tagId);
        return ResponseEntity.ok(new TagResponse(tag));
    }

    /**
     * Deletes a tag.
     *
     * <p><strong>Endpoint:</strong> DELETE /api/decks/{deckId}/tags/{tagId}
     *
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>Verifies user owns the deck</li>
     *   <li>Verifies tag exists in the specified deck</li>
     * </ul>
     *
     * <p><strong>Side Effects:</strong>
     * Removes the tag from all cards in the deck that have it.
     *
     * <p><strong>Response:</strong> 204 No Content
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param deckId the ID of the deck
     * @param tagId the ID of the tag to delete
     * @return ResponseEntity with no content
     * @throws IllegalArgumentException if tag not found or access denied (handled by GlobalExceptionHandler)
     */
    @DeleteMapping("/api/decks/{deckId}/tags/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @CurrentUser Long userId,
            @PathVariable Long deckId,
            @PathVariable Long tagId) {

        logger.debug("Deleting tag {} from deck {} for user {}", tagId, deckId, userId);

        tagService.deleteTag(userId, deckId, tagId);

        logger.info("Tag {} deleted successfully", tagId);
        return ResponseEntity.noContent().build();
    }
}