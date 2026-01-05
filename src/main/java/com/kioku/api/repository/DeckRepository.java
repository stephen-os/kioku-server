package com.kioku.api.repository;

import com.kioku.api.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Deck entity database operations.
 *
 * <p>This repository provides methods for:
 * <ul>
 *   <li>Finding decks by user (user ownership)</li>
 *   <li>Finding decks by ID within a user's collection</li>
 *   <li>Checking for duplicate deck names per user</li>
 *   <li>Deck analytics and statistics</li>
 * </ul>
 *
 * <p><strong>Ownership Model:</strong> Decks belong to users. Each user has their
 * own collection of decks with unique names. Different users can have decks with
 * the same name.
 *
 * <p><strong>Security Note:</strong> Methods that accept both deck ID and user ID
 * ensure the user owns the deck, preventing unauthorized access.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {

    /**
     * Finds all decks belonging to a specific user.
     *
     * <p>Returns decks ordered by creation date (newest first).
     *
     * @param userId the user ID
     * @return list of user's decks
     */
    List<Deck> findByUserId(Long userId);

    /**
     * Finds a specific deck belonging to a specific user.
     *
     * <p>This method ensures the deck belongs to the specified user,
     * preventing access to other users' decks even if the deck ID is known.
     *
     * <p><strong>Security:</strong> Use this instead of {@code findById()} when
     * you need to verify ownership.
     *
     * @param id the deck ID
     * @param userId the user ID
     * @return an Optional containing the deck if found and owned, empty otherwise
     */
    Optional<Deck> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks if a user owns a specific deck.
     *
     * <p>Useful for quick ownership verification without fetching the entire deck.
     *
     * @param id the deck ID
     * @param userId the user ID
     * @return {@code true} if the user owns the deck, {@code false} otherwise
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Checks if a deck with the given name exists for a user.
     *
     * <p>Used to prevent duplicate deck names within a user's collection.
     * Deck names must be unique per user but different users can have
     * decks with the same name.
     *
     * @param userId the user ID
     * @param name the deck name
     * @return {@code true} if a deck with this name exists for the user, {@code false} otherwise
     */
    boolean existsByUserIdAndName(Long userId, String name);

    /**
     * Finds a deck by user ID and name.
     *
     * <p>Useful for looking up a deck by its name within a user's collection.
     *
     * @param userId the user ID
     * @param name the deck name
     * @return an Optional containing the deck if found, empty otherwise
     */
    Optional<Deck> findByUserIdAndName(Long userId, String name);

    /**
     * Counts the number of decks a user has.
     *
     * @param userId the user ID
     * @return the number of decks
     */
    long countByUserId(Long userId);

    /**
     * Finds decks created after a specific date.
     *
     * <p>Useful for analytics and reporting on deck creation trends.
     *
     * @param userId the user ID
     * @param date the date to search from
     * @return list of decks created after the date
     */
    @Query("SELECT d FROM DeckEntity d WHERE d.user.id = :userId AND d.createdAt > :date ORDER BY d.createdAt DESC")
    List<Deck> findByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    /**
     * Finds recently updated decks for a user.
     *
     * <p>Returns decks ordered by update time (most recently updated first).
     * Useful for "continue studying" features.
     *
     * @param userId the user ID
     * @param limit the maximum number of decks to return
     * @return list of recently updated decks
     */
    @Query(value = "SELECT d FROM DeckEntity d WHERE d.user.id = :userId ORDER BY d.updatedAt DESC LIMIT :limit")
    List<Deck> findRecentlyUpdatedDecks(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * Deletes all decks belonging to a user.
     *
     * <p><strong>Warning:</strong> This will cascade delete all cards and tags
     * in those decks if cascade is configured.
     *
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);
}