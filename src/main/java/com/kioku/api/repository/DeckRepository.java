package com.kioku.api.repository;

import com.kioku.api.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
 * <p><strong>Unidirectional Relationship:</strong> Deck doesn't have a user reference,
 * so queries use native SQL to access the user_id foreign key column.
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
     * @param userId the user ID
     * @return list of user's decks
     */
    @Query(value = "SELECT * FROM decks WHERE user_id = :userId ORDER BY created_at DESC", nativeQuery = true)
    List<Deck> findByUserIdNative(@Param("userId") Long userId);

    default List<Deck> findByUserId(Long userId) {
        return findByUserIdNative(userId);
    }

    /**
     * Finds a specific deck belonging to a specific user.
     *
     * @param id the deck ID
     * @param userId the user ID
     * @return an Optional containing the deck if found and owned, empty otherwise
     */
    @Query(value = "SELECT * FROM decks WHERE id = :id AND user_id = :userId", nativeQuery = true)
    Optional<Deck> findByIdAndUserIdNative(@Param("id") Long id, @Param("userId") Long userId);

    default Optional<Deck> findByIdAndUserId(Long id, Long userId) {
        return findByIdAndUserIdNative(id, userId);
    }

    /**
     * Checks if a user owns a specific deck.
     *
     * @param id the deck ID
     * @param userId the user ID
     * @return {@code true} if the user owns the deck, {@code false} otherwise
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM decks WHERE id = :id AND user_id = :userId", nativeQuery = true)
    boolean existsByIdAndUserIdNative(@Param("id") Long id, @Param("userId") Long userId);

    default boolean existsByIdAndUserId(Long id, Long userId) {
        return existsByIdAndUserIdNative(id, userId);
    }

    /**
     * Checks if a deck with the given name exists for a user.
     *
     * @param userId the user ID
     * @param name the deck name
     * @return {@code true} if a deck with this name exists for the user, {@code false} otherwise
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM decks WHERE user_id = :userId AND name = :name", nativeQuery = true)
    boolean existsByUserIdAndNameNative(@Param("userId") Long userId, @Param("name") String name);

    default boolean existsByUserIdAndName(Long userId, String name) {
        return existsByUserIdAndNameNative(userId, name);
    }

    /**
     * Finds a deck by user ID and name.
     *
     * @param userId the user ID
     * @param name the deck name
     * @return an Optional containing the deck if found, empty otherwise
     */
    @Query(value = "SELECT * FROM decks WHERE user_id = :userId AND name = :name", nativeQuery = true)
    Optional<Deck> findByUserIdAndNameNative(@Param("userId") Long userId, @Param("name") String name);

    default Optional<Deck> findByUserIdAndName(Long userId, String name) {
        return findByUserIdAndNameNative(userId, name);
    }

    /**
     * Counts the number of decks a user has.
     *
     * @param userId the user ID
     * @return the number of decks
     */
    @Query(value = "SELECT COUNT(*) FROM decks WHERE user_id = :userId", nativeQuery = true)
    long countByUserIdNative(@Param("userId") Long userId);

    default long countByUserId(Long userId) {
        return countByUserIdNative(userId);
    }

    /**
     * Finds decks created after a specific date.
     *
     * @param userId the user ID
     * @param date the date to search from
     * @return list of decks created after the date
     */
    @Query(value = "SELECT * FROM decks WHERE user_id = :userId AND created_at > :date ORDER BY created_at DESC", nativeQuery = true)
    List<Deck> findByUserIdAndCreatedAtAfterNative(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    default List<Deck> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime date) {
        return findByUserIdAndCreatedAtAfterNative(userId, date);
    }

    /**
     * Finds recently updated decks for a user.
     *
     * @param userId the user ID
     * @param limit the maximum number of decks to return
     * @return list of recently updated decks
     */
    @Query(value = "SELECT * FROM decks WHERE user_id = :userId ORDER BY updated_at DESC LIMIT :limit", nativeQuery = true)
    List<Deck> findRecentlyUpdatedDecksNative(@Param("userId") Long userId, @Param("limit") int limit);

    default List<Deck> findRecentlyUpdatedDecks(Long userId, int limit) {
        return findRecentlyUpdatedDecksNative(userId, limit);
    }

    /**
     * Deletes all decks belonging to a user.
     *
     * @param userId the user ID
     */
    @Modifying
    @Query(value = "DELETE FROM decks WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserIdNative(@Param("userId") Long userId);

    default void deleteByUserId(Long userId) {
        deleteByUserIdNative(userId);
    }
}
