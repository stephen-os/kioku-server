package com.kioku.api.repository;

import com.kioku.api.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tag entity database operations.
 *
 * <p>This repository provides methods for:
 * <ul>
 *   <li>Finding tags by deck (tags are deck-specific)</li>
 *   <li>Finding tags by ID within a specific deck</li>
 *   <li>Checking for duplicate tag names within a deck</li>
 *   <li>Finding tags by user (across all their decks)</li>
 * </ul>
 *
 * <p><strong>Deck Isolation:</strong> Tags belong to specific decks. A "verbs" tag
 * in one deck is completely separate from a "verbs" tag in another deck.
 *
 * <p><strong>Security Note:</strong> This repository does not enforce user ownership.
 * Service layer methods must verify that users own the deck before calling these methods.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

    /**
     * Finds all tags in a specific deck.
     *
     * <p><strong>Security:</strong> Does not check deck ownership.
     * Service layer must verify user owns the deck.
     *
     * @param deckId the deck ID
     * @return list of tags in the deck
     */
    List<TagEntity> findByDeckId(Long deckId);

    /**
     * Finds a specific tag within a specific deck.
     *
     * <p>This method ensures the tag belongs to the specified deck,
     * preventing access to tags from other decks even if the tag ID is known.
     *
     * <p><strong>Security:</strong> Does not check deck ownership.
     * Service layer must verify user owns the deck.
     *
     * @param id the tag ID
     * @param deckId the deck ID
     * @return an Optional containing the tag if found in the deck, empty otherwise
     */
    Optional<TagEntity> findByIdAndDeckId(Long id, Long deckId);

    /**
     * Checks if a tag with the given name exists in a specific deck.
     *
     * <p>Used to prevent duplicate tag names within the same deck.
     * Tags in different decks can have the same name.
     *
     * @param deckId the deck ID
     * @param name the tag name
     * @return {@code true} if a tag with this name exists in the deck, {@code false} otherwise
     */
    boolean existsByDeckIdAndName(Long deckId, String name);

    /**
     * Finds all tags belonging to a specific user (across all their decks).
     *
     * <p>This is useful for showing all tags a user has created,
     * but remember that tags are deck-specific and cannot be shared.
     *
     * @param userId the user ID
     * @return list of all tags created by the user
     */
    List<TagEntity> findByUserId(Long userId);

    /**
     * Counts the number of tags in a deck.
     *
     * @param deckId the deck ID
     * @return the number of tags in the deck
     */
    long countByDeckId(Long deckId);

    /**
     * Deletes all tags in a specific deck.
     *
     * <p><strong>Warning:</strong> This will remove all tag associations
     * from cards in the deck.
     *
     * @param deckId the deck ID
     */
    void deleteByDeckId(Long deckId);
}