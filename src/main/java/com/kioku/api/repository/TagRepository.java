package com.kioku.api.repository;

import com.kioku.api.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
 * <p><strong>Unidirectional Relationship:</strong> Tag doesn't have deck or user references,
 * so queries use native SQL to access the deck_id foreign key column.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Finds all tags in a specific deck.
     *
     * @param deckId the deck ID
     * @return list of tags in the deck
     */
    @Query(value = "SELECT * FROM tags WHERE deck_id = :deckId", nativeQuery = true)
    List<Tag> findByDeckIdNative(@Param("deckId") Long deckId);

    default List<Tag> findByDeckId(Long deckId) {
        return findByDeckIdNative(deckId);
    }

    /**
     * Finds a specific tag within a specific deck.
     *
     * @param id the tag ID
     * @param deckId the deck ID
     * @return an Optional containing the tag if found in the deck, empty otherwise
     */
    @Query(value = "SELECT * FROM tags WHERE id = :id AND deck_id = :deckId", nativeQuery = true)
    Optional<Tag> findByIdAndDeckIdNative(@Param("id") Long id, @Param("deckId") Long deckId);

    default Optional<Tag> findByIdAndDeckId(Long id, Long deckId) {
        return findByIdAndDeckIdNative(id, deckId);
    }

    /**
     * Checks if a tag with the given name exists in a specific deck.
     *
     * @param deckId the deck ID
     * @param name the tag name
     * @return {@code true} if a tag with this name exists in the deck, {@code false} otherwise
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM tags WHERE deck_id = :deckId AND name = :name", nativeQuery = true)
    boolean existsByDeckIdAndNameNative(@Param("deckId") Long deckId, @Param("name") String name);

    default boolean existsByDeckIdAndName(Long deckId, String name) {
        return existsByDeckIdAndNameNative(deckId, name);
    }

    /**
     * Finds all tags belonging to a specific user (across all their decks).
     *
     * @param userId the user ID
     * @return list of all tags created by the user
     */
    @Query(value = """
        SELECT t.*
        FROM tags t
        INNER JOIN decks d ON t.deck_id = d.id
        WHERE d.user_id = :userId
        """, nativeQuery = true)
    List<Tag> findByUserIdNative(@Param("userId") Long userId);

    default List<Tag> findByUserId(Long userId) {
        return findByUserIdNative(userId);
    }

    /**
     * Counts the number of tags in a deck.
     *
     * @param deckId the deck ID
     * @return the number of tags in the deck
     */
    @Query(value = "SELECT COUNT(*) FROM tags WHERE deck_id = :deckId", nativeQuery = true)
    long countByDeckIdNative(@Param("deckId") Long deckId);

    default long countByDeckId(Long deckId) {
        return countByDeckIdNative(deckId);
    }

    /**
     * Deletes all tags in a specific deck.
     *
     * @param deckId the deck ID
     */
    @Modifying
    @Query(value = "DELETE FROM tags WHERE deck_id = :deckId", nativeQuery = true)
    void deleteByDeckIdNative(@Param("deckId") Long deckId);

    default void deleteByDeckId(Long deckId) {
        deleteByDeckIdNative(deckId);
    }
}
