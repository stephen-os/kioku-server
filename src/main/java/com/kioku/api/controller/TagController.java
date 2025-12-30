package com.kioku.api.controller;

import com.kioku.api.dto.CreateTagRequest;
import com.kioku.api.dto.ErrorResponse;
import com.kioku.api.dto.TagResponse;
import com.kioku.api.entity.Tag;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Create a new tag
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<?> createTag(
            @CurrentUser Long userId,
            @Valid @RequestBody CreateTagRequest request) {
        try {
            Tag tag = tagService.createTag(userId, request.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(new TagResponse(tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all tags for current user
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<List<TagResponse>> getUserTags(@CurrentUser Long userId) {
        List<Tag> tags = tagService.getUserTags(userId);
        List<TagResponse> response = tags.stream()
                .map(TagResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific tag
     * GET /api/tags/{tagId}
     */
    @GetMapping("/{tagId}")
    public ResponseEntity<?> getTag(
            @CurrentUser Long userId,
            @PathVariable Long tagId) {
        return tagService.getTag(userId, tagId)
                .map(tag -> ResponseEntity.ok((Object) new TagResponse(tag)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Tag not found or access denied")));
    }

    /**
     * Update a tag
     * PUT /api/tags/{tagId}
     */
    @PutMapping("/{tagId}")
    public ResponseEntity<?> updateTag(
            @CurrentUser Long userId,
            @PathVariable Long tagId,
            @Valid @RequestBody CreateTagRequest request) {
        try {
            Tag tag = tagService.updateTag(userId, tagId, request.getName());
            return ResponseEntity.ok(new TagResponse(tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete a tag
     * DELETE /api/tags/{tagId}
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<?> deleteTag(
            @CurrentUser Long userId,
            @PathVariable Long tagId) {
        try {
            tagService.deleteTag(userId, tagId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}