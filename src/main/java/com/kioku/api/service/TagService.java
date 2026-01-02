package com.kioku.api.service;

import com.kioku.api.entity.DeckEntity;
import com.kioku.api.entity.TagEntity;
import com.kioku.api.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Tag entity business logic.
 *
 * <p>This service provides:
 * <ul>
 *   <li>Tag creation within specific decks (deck-specific tags)</li>
 *   <li>Tag retrieval with ownership verification</li>
 *   <li>Tag updates with duplicate prevention</li>
 *   <li>Tag deletion</li>
 *   <li>Tag lookup by name within decks</li>
 * </ul>
 *
 * <p><strong>Deck-Specific Tags:</strong> Tags belong to specific decks, not users.
 * A "verbs" tag in a Japanese deck is separate from a "verbs" tag in a Spanish deck.
 * This prevents tag pollution across unrelated decks.
 *
 * <p><strong>Security:</strong> All methods verify that the user owns the deck
 * before performing operations. Methods that accept {@code userId} and {@code deckId}
 * will check ownership and throw exceptions if access is denied.
 *
 * <p><strong>Transaction Management:</strong>
 * <ul>
 *   <li>Class is annotated with {@code @Transactional} for write operations</li>
 *   <li>Read-only methods use {@code @Transactional(readOnly = true)}</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;
    private final DeckService deckService;

    /**
     * Constructs a new TagService.
     *
     * @param tagRepository the tag repository
     * @param deckService the deck service for ownership verification
     */
    public TagService(TagRepository tagRepository, DeckService deckService) {
        this.tagRepository = tagRepository;
        this.deckService = deckService;
    }

    /**
     * Creates a new tag within a deck.
     *
     * <p>Verifies that:
     * <ul>
     *   <li>The user owns the deck</li>
     *   <li>No tag with the same name exists in the deck</li>
     * </ul>
     *
     * <p><strong>Note:</strong> Tag names must be unique within a deck, but
     * different decks can have tags with the same name.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param name the tag name
     * @return the created tag
     * @throws IllegalArgumentException if user doesn't own deck or duplicate exists
     */
    public TagEntity createTag(Long userId, Long deckId, String name) {
        logger.debug("Creating tag '{}' in deck id={} for user id={}", name, deckId, userId);

        // Verify user owns the deck
        DeckEntity deckEntity = deckService.getDeckOrThrow(deckId, userId);

        // Check for duplicate name within deck
        if (tagRepository.existsByDeckIdAndName(deckId, name)) {
            logger.warn("Tag creation failed: tag '{}' already exists in deck id={}", name, deckId);
            throw new IllegalArgumentException("Tag with name '" + name + "' already exists in this deck");
        }

        TagEntity tagEntity = new TagEntity(deckEntity, name);
        TagEntity savedTagEntity = tagRepository.save(tagEntity);

        logger.debug("Tag created successfully with id={}", savedTagEntity.getId());
        return savedTagEntity;
    }

    /**
     * Gets all tags in a specific deck.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @return list of tags in the deck
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public List<TagEntity> getDeckTags(Long userId, Long deckId) {
        logger.debug("Getting tags for deck id={}, user id={}", deckId, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        List<TagEntity> tagEntities = tagRepository.findByDeckId(deckId);
        logger.debug("Found {} tags in deck id={}", tagEntities.size(), deckId);

        return tagEntities;
    }

    /**
     * Gets all tags across all of a user's decks.
     *
     * <p>This returns all tags the user has created in any of their decks.
     * Useful for showing a complete list of tags, but remember that tags
     * are deck-specific and cannot be moved between decks.
     *
     * @param userId the user ID
     * @return list of all user's tags across all decks
     */
    @Transactional(readOnly = true)
    public List<TagEntity> getAllUserTags(Long userId) {
        logger.debug("Getting all tags for user id={}", userId);

        List<TagEntity> tagEntities = tagRepository.findByUserId(userId);
        logger.debug("Found {} total tags for user id={}", tagEntities.size(), userId);

        return tagEntities;
    }

    /**
     * Gets a specific tag.
     *
     * <p>Verifies that the user owns the deck that contains the tag.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param tagId the tag ID
     * @return an Optional containing the tag if found and owned, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<TagEntity> getTag(Long userId, Long deckId, Long tagId) {
        logger.debug("Getting tag id={} from deck id={} for user id={}", tagId, deckId, userId);

        // Verify user owns the deck
        if (!deckService.userOwnsDeck(deckId, userId)) {
            logger.warn("Access denied: user id={} does not own deck id={}", userId, deckId);
            return Optional.empty();
        }

        Optional<TagEntity> tag = tagRepository.findByIdAndDeckId(tagId, deckId);
        logger.debug("Tag found: {}", tag.isPresent());

        return tag;
    }

    /**
     * Gets a tag or throws exception if not found or not owned.
     *
     * @param userId the user ID
     * @param deckId the deck ID (can be null to skip deck check)
     * @param tagId the tag ID
     * @return the tag
     * @throws IllegalArgumentException if tag not found or access denied
     */
    @Transactional(readOnly = true)
    public TagEntity getTagOrThrow(Long userId, Long deckId, Long tagId) {
        if (deckId != null) {
            return getTag(userId, deckId, tagId)
                    .orElseThrow(() -> {
                        logger.warn("Tag not found or access denied: tag id={}, deck id={}, user id={}",
                                tagId, deckId, userId);
                        return new IllegalArgumentException("Tag not found or access denied: " + tagId);
                    });
        } else {
            // Verify user owns the tag (check via user_id)
            Optional<TagEntity> tag = tagRepository.findById(tagId);
            if (tag.isEmpty() || !tag.get().getUser().getId().equals(userId)) {
                logger.warn("Tag not found or access denied: tag id={}, user id={}", tagId, userId);
                throw new IllegalArgumentException("Tag not found or access denied: " + tagId);
            }
            return tag.get();
        }
    }

    /**
     * Simplified getTagOrThrow without deck check.
     *
     * <p>Verifies user owns the tag by checking the tag's user_id.
     * Use this when you have the tag ID but not the deck ID.
     *
     * @param userId the user ID
     * @param tagId the tag ID
     * @return the tag
     * @throws IllegalArgumentException if tag not found or access denied
     */
    @Transactional(readOnly = true)
    public TagEntity getTagOrThrow(Long userId, Long tagId) {
        return getTagOrThrow(userId, null, tagId);
    }

    /**
     * Gets or creates a tag by name within a deck.
     *
     * <p>If a tag with the given name exists in the deck, returns it.
     * Otherwise, creates a new tag with that name.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param name the tag name
     * @return the existing or newly created tag
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    public TagEntity getOrCreateTag(Long userId, Long deckId, String name) {
        logger.debug("Getting or creating tag '{}' in deck id={} for user id={}", name, deckId, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        Optional<TagEntity> existingTag = findTagByName(userId, deckId, name);

        if (existingTag.isPresent()) {
            logger.debug("Tag '{}' already exists with id={}", name, existingTag.get().getId());
            return existingTag.get();
        }

        logger.debug("Tag '{}' does not exist, creating new tag", name);
        return createTag(userId, deckId, name);
    }

    /**
     * Updates a tag's name.
     *
     * <p>Verifies that:
     * <ul>
     *   <li>The user owns the deck</li>
     *   <li>The new name doesn't conflict with another tag in the same deck</li>
     * </ul>
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param tagId the tag ID
     * @param name the new tag name
     * @return the updated tag
     * @throws IllegalArgumentException if access denied or update would create duplicate
     */
    public TagEntity updateTag(Long userId, Long deckId, Long tagId, String name) {
        logger.debug("Updating tag id={} in deck id={} for user id={}", tagId, deckId, userId);

        TagEntity tagEntity = getTagOrThrow(userId, deckId, tagId);

        // Check if new name conflicts with another tag in the same deck
        if (!tagEntity.getName().equals(name)) {
            if (tagRepository.existsByDeckIdAndName(deckId, name)) {
                logger.warn("Tag update failed: tag '{}' already exists in deck id={}", name, deckId);
                throw new IllegalArgumentException("Tag with name '" + name + "' already exists in this deck");
            }
        }

        tagEntity.setName(name);
        TagEntity updatedTagEntity = tagRepository.save(tagEntity);

        logger.debug("Tag id={} updated successfully", tagId);
        return updatedTagEntity;
    }

    /**
     * Deletes a tag.
     *
     * <p>This will remove the tag from all cards in the deck.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param tagId the tag ID
     * @throws IllegalArgumentException if tag not found or access denied
     */
    public void deleteTag(Long userId, Long deckId, Long tagId) {
        logger.debug("Deleting tag id={} from deck id={} for user id={}", tagId, deckId, userId);

        TagEntity tagEntity = getTagOrThrow(userId, deckId, tagId);
        tagRepository.delete(tagEntity);

        logger.debug("Tag id={} deleted successfully", tagId);
    }

    /**
     * Finds a tag by name within a deck.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @param name the tag name
     * @return an Optional containing the tag if found, empty otherwise
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public Optional<TagEntity> findTagByName(Long userId, Long deckId, String name) {
        logger.debug("Finding tag '{}' in deck id={} for user id={}", name, deckId, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        // Find all tags in deck and filter by name
        List<TagEntity> deckTagEntities = tagRepository.findByDeckId(deckId);
        Optional<TagEntity> tag = deckTagEntities.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst();

        logger.debug("Tag '{}' found: {}", name, tag.isPresent());
        return tag;
    }

    /**
     * Counts the number of tags in a deck.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @return the number of tags
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    @Transactional(readOnly = true)
    public long countTags(Long userId, Long deckId) {
        logger.debug("Counting tags in deck id={} for user id={}", deckId, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        long count = tagRepository.countByDeckId(deckId);
        logger.debug("Deck id={} has {} tags", deckId, count);

        return count;
    }

    /**
     * Deletes all tags in a deck.
     *
     * <p>This will remove all tags from all cards in the deck.
     *
     * @param userId the user ID
     * @param deckId the deck ID
     * @throws IllegalArgumentException if user doesn't own the deck
     */
    public void deleteAllTagsInDeck(Long userId, Long deckId) {
        logger.debug("Deleting all tags in deck id={} for user id={}", deckId, userId);

        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        tagRepository.deleteByDeckId(deckId);

        logger.debug("All tags deleted from deck id={}", deckId);
    }
}