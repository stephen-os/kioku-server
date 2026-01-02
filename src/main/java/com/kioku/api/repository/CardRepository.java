package com.kioku.api.repository;

import com.kioku.api.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CardEntity entity database operations.
 *
 * <p>This repository provides methods for:
 * <ul>
 *   <li>Finding cards by deck</li>
 *   <li>Finding cards by ID within a specific deck</li>
 *   <li>Checking for duplicate cards (same front/back in deck)</li>
 *   <li>Searching cards by content (front or back text)</li>
 *   <li>Finding cards by tags</li>
 * </ul>
 *
 * <p><strong>Security Note:</strong> This repository does not enforce user ownership.
 * Service layer methods must verify that users own the deck before calling these methods.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

    /**
     * Finds all cards in a specific deck.
     *
     * <p><strong>Security:</strong> Does not check deck ownership.
     * Service layer must verify user owns the deck.
     *
     * @param deckId the deck ID
     * @return list of cards in the deck, ordered by creation date
     */
    List<CardEntity> findByDeckId(Long deckId);

    /**
     * Finds a specific card within a specific deck.
     *
     * <p>This method ensures the card belongs to the specified deck,
     * preventing access to cards from other decks even if the card ID is known.
     *
     * <p><strong>Security:</strong> Does not check deck ownership.
     * Service layer must verify user owns the deck.
     *
     * @param id the card ID
     * @param deckId the deck ID
     * @return an Optional containing the card if found in the deck, empty otherwise
     */
    Optional<CardEntity> findByIdAndDeckId(Long id, Long deckId);

    /**
     * Checks if a card with the same front and back text exists in a deck.
     *
     * <p>Used to prevent duplicate cards within the same deck.
     * Comparison is case-sensitive and exact match.
     *
     * @param deckId the deck ID
     * @param front the front text
     * @param back the back text
     * @return {@code true} if a duplicate exists, {@code false} otherwise
     */
    boolean existsByDeckIdAndFrontAndBack(Long deckId, String front, String back);

    /**
     * Searches for cards within a deck by text content.
     *
     * <p>Searches both front and back text using case-insensitive partial matching.
     * For example, searching "eat" would match "eating", "beaten", "eat", etc.
     *
     * <p><strong>Performance Note:</strong> Uses LIKE queries which may be slow
     * on large datasets. Consider full-text search for production use.
     *
     * @param deckId the deck ID
     * @param searchTerm the text to search for (partial match, case-insensitive)
     * @return list of matching cards
     */
    @Query("SELECT c FROM CardEntity c WHERE c.deck.id = :deckId AND " +
            "(LOWER(c.front) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.back) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<CardEntity> searchByDeckId(@Param("deckId") Long deckId, @Param("searchTerm") String searchTerm);

    /**
     * Finds all cards in a deck that have a specific tag.
     *
     * <p>Uses a JOIN on the many-to-many card_tags relationship.
     *
     * <p><strong>Security:</strong> Does not check deck or tag ownership.
     * Service layer must verify user owns both the deck and the tag.
     *
     * @param deckId the deck ID
     * @param tagId the tag ID
     * @return list of cards that have the specified tag
     */
    @Query("SELECT c FROM CardEntity c JOIN c.tags t WHERE t.id = :tagId AND c.deck.id = :deckId")
    List<CardEntity> findByDeckIdAndTagId(@Param("deckId") Long deckId, @Param("tagId") Long tagId);

    /**
     * Counts the number of cards in a deck.
     *
     * @param deckId the deck ID
     * @return the number of cards in the deck
     */
    long countByDeckId(Long deckId);
}