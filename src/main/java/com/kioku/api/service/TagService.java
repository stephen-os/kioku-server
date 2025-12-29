package com.kioku.api.service;

import com.kioku.api.entity.Tag;
import com.kioku.api.entity.User;
import com.kioku.api.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final UserService userService;

    public TagService(TagRepository tagRepository, UserService userService) {
        this.tagRepository = tagRepository;
        this.userService = userService;
    }

    /**
     * Create a new tag for a user
     */
    public Tag createTag(Long userId, String name) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check for duplicate name
        if (tagRepository.existsByUserIdAndName(userId, name)) {
            throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
        }

        Tag tag = new Tag(user, name);
        return tagRepository.save(tag);
    }

    /**
     * Get all tags for a user
     */
    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByUserId(userId);
    }

    /**
     * Get a specific tag by ID (with ownership check)
     */
    public Optional<Tag> getTag(Long userId, Long tagId) {
        Optional<Tag> tag = tagRepository.findById(tagId);

        // Verify ownership
        if (tag.isPresent() && !tag.get().getUser().getId().equals(userId)) {
            return Optional.empty();
        }

        return tag;
    }

    /**
     * Get a tag or throw exception if not found or not owned
     */
    public Tag getTagOrThrow(Long userId, Long tagId) {
        return getTag(userId, tagId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tag not found or access denied: " + tagId));
    }

    /**
     * Get or create a tag by name
     */
    public Tag getOrCreateTag(Long userId, String name) {
        Optional<Tag> existingTag = tagRepository.findByUserIdAndName(userId, name);

        if (existingTag.isPresent()) {
            return existingTag.get();
        }

        return createTag(userId, name);
    }

    /**
     * Update a tag (with ownership check)
     */
    public Tag updateTag(Long userId, Long tagId, String name) {
        Tag tag = getTagOrThrow(userId, tagId);

        // Check if new name conflicts with another tag
        if (!tag.getName().equals(name)) {
            if (tagRepository.existsByUserIdAndName(userId, name)) {
                throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
            }
        }

        tag.setName(name);
        return tagRepository.save(tag);
    }

    /**
     * Delete a tag (with ownership check)
     */
    public void deleteTag(Long userId, Long tagId) {
        Tag tag = getTagOrThrow(userId, tagId);
        tagRepository.delete(tag);
    }

    /**
     * Find tag by name (with ownership check)
     */
    public Optional<Tag> findTagByName(Long userId, String name) {
        return tagRepository.findByUserIdAndName(userId, name);
    }
}