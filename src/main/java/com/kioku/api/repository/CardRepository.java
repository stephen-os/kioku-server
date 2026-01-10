package com.kioku.api.repository;

import com.kioku.api.model.Card;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Card entity database operations.
 *
 * <p>This repository provides methods for:
 * <ul>
 *   <li>Finding cards by deck with eager tag loading</li>
 *   <li>Finding cards by ID within a specific deck</li>
 *   <li>Checking for duplicate cards (same front/back in deck)</li>
 *   <li>Searching cards by content (front or back text)</li>
 *   <li>Finding cards by tags</li>
 * </ul>
 *
 * <p><strong>Eager Loading:</strong> All query methods use {@code LEFT JOIN FETCH}
 * to eagerly load tags, preventing LazyInitializationException when accessing
 * card tags outside of a transaction context.
 *
 * <p><strong>Unidirectional Relationship:</strong> Card doesn't have a deck reference,
 * so queries use native SQL to access the deck_id foreign key column.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Finds all cards by their IDs with tags eagerly loaded.
     * Uses EntityGraph to load tags without duplication issues.
     *
     * @param ids the card IDs
     * @return list of cards with tags loaded
     */
    @EntityGraph(attributePaths = {"tags"})
    @Query("SELECT c FROM Card c WHERE c.id IN :ids ORDER BY c.createdAt ASC")
    List<Card> findByIdsWithTags(@Param("ids") List<Long> ids);

    /**
     * Finds all card IDs in a specific deck.
     *
     * @param deckId the deck ID
     * @return list of card IDs in the deck
     */
    @Query(value = "SELECT c.id FROM card c WHERE c.deck_id = :deckId ORDER BY c.created_at ASC", nativeQuery = true)
    List<Long> findIdsByDeckId(@Param("deckId") Long deckId);

    /**
     * Finds all cards in a specific deck (native query, tags loaded lazily).
     *
     * @param deckId the deck ID
     * @return list of cards in the deck
     */
    @Query(value = """
        SELECT DISTINCT c.*
        FROM card c
        WHERE c.deck_id = :deckId
        ORDER BY c.created_at ASC
        """, nativeQuery = true)
    List<Card> findByDeckIdNative(@Param("deckId") Long deckId);

    /**
     * Finds all cards in a specific deck with tags eagerly loaded.
     * Uses a two-query strategy: first gets IDs via native query, then loads cards with tags.
     *
     * @param deckId the deck ID
     * @return list of cards in the deck with tags loaded
     */
    default List<Card> findByDeckId(Long deckId) {
        List<Long> ids = findIdsByDeckId(deckId);
        if (ids.isEmpty()) {
            return List.of();
        }
        return findByIdsWithTags(ids);
    }

    /**
     * Finds a specific card within a specific deck.
     *
     * @param id the card ID
     * @param deckId the deck ID
     * @return an Optional containing the card if found in the deck, empty otherwise
     */
    @Query(value = """
        SELECT c.*
        FROM card c
        WHERE c.id = :id AND c.deck_id = :deckId
        """, nativeQuery = true)
    Optional<Card> findByIdAndDeckIdNative(@Param("id") Long id, @Param("deckId") Long deckId);

    /**
     * Finds a specific card within a specific deck.
     *
     * @param id the card ID
     * @param deckId the deck ID
     * @return an Optional containing the card if found in the deck, empty otherwise
     */
    default Optional<Card> findByIdAndDeckId(Long id, Long deckId) {
        return findByIdAndDeckIdNative(id, deckId);
    }

    /**
     * Finds a specific card by ID with tags eagerly loaded.
     * Uses EntityGraph to load tags without duplication issues.
     *
     * @param id the card ID
     * @return an Optional containing the card with tags loaded if found, empty otherwise
     */
    @EntityGraph(attributePaths = {"tags"})
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdWithTags(@Param("id") Long id);

    /**
     * Checks if a card with the same front and back text exists in a deck.
     *
     * @param deckId the deck ID
     * @param front the front text
     * @param back the back text
     * @return {@code true} if a duplicate exists, {@code false} otherwise
     */
    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM card c
        WHERE c.deck_id = :deckId AND c.front = :front AND c.back = :back
        """, nativeQuery = true)
    boolean existsByDeckIdAndFrontAndBackNative(@Param("deckId") Long deckId,
                                                 @Param("front") String front,
                                                 @Param("back") String back);

    default boolean existsByDeckIdAndFrontAndBack(Long deckId, String front, String back) {
        return existsByDeckIdAndFrontAndBackNative(deckId, front, back);
    }

    /**
     * Checks if a card with the same front and back text exists in a deck,
     * excluding a specific card ID.
     *
     * @param deckId the deck ID
     * @param front the front text
     * @param back the back text
     * @param id the card ID to exclude from the check
     * @return {@code true} if a duplicate exists, {@code false} otherwise
     */
    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM card c
        WHERE c.deck_id = :deckId AND c.front = :front AND c.back = :back AND c.id != :id
        """, nativeQuery = true)
    boolean existsByDeckIdAndFrontAndBackAndIdNotNative(@Param("deckId") Long deckId,
                                                         @Param("front") String front,
                                                         @Param("back") String back,
                                                         @Param("id") Long id);

    default boolean existsByDeckIdAndFrontAndBackAndIdNot(Long deckId, String front, String back, Long id) {
        return existsByDeckIdAndFrontAndBackAndIdNotNative(deckId, front, back, id);
    }

    /**
     * Searches for cards within a deck by text content.
     *
     * @param deckId the deck ID
     * @param searchTerm the text to search for (partial match, case-insensitive)
     * @return list of matching cards
     */
    @Query(value = """
        SELECT DISTINCT c.*
        FROM card c
        WHERE c.deck_id = :deckId
        AND (LOWER(c.front) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(c.back) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY c.created_at ASC
        """, nativeQuery = true)
    List<Card> searchByDeckIdNative(@Param("deckId") Long deckId, @Param("searchTerm") String searchTerm);

    default List<Card> searchByDeckId(Long deckId, String searchTerm) {
        return searchByDeckIdNative(deckId, searchTerm);
    }

    /**
     * Finds all cards in a deck that have a specific tag.
     *
     * @param deckId the deck ID
     * @param tagId the tag ID
     * @return list of cards that have the specified tag
     */
    @Query(value = """
        SELECT DISTINCT c.*
        FROM card c
        INNER JOIN card_tags ct ON c.id = ct.card_id
        WHERE c.deck_id = :deckId AND ct.tag_id = :tagId
        ORDER BY c.created_at ASC
        """, nativeQuery = true)
    List<Card> findByDeckIdAndTagIdNative(@Param("deckId") Long deckId, @Param("tagId") Long tagId);

    default List<Card> findByDeckIdAndTagId(Long deckId, Long tagId) {
        return findByDeckIdAndTagIdNative(deckId, tagId);
    }

    /**
     * Counts the number of cards in a deck.
     *
     * @param deckId the deck ID
     * @return the number of cards in the deck
     */
    @Query(value = "SELECT COUNT(*) FROM card WHERE deck_id = :deckId", nativeQuery = true)
    long countByDeckIdNative(@Param("deckId") Long deckId);

    default long countByDeckId(Long deckId) {
        return countByDeckIdNative(deckId);
    }
}
