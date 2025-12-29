package com.kioku.api.repository;

import com.kioku.api.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByDeckId(Long deckId);

    Optional<Card> findByIdAndDeckId(Long id, Long deckId);

    // Check for duplicates (same front and back in same deck)
    boolean existsByDeckIdAndFrontAndBack(Long deckId, String front, String back);

    // Search cards within a deck
    @Query("SELECT c FROM Card c WHERE c.deck.id = :deckId AND " +
            "(LOWER(c.front) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.back) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Card> searchByDeckId(@Param("deckId") Long deckId, @Param("searchTerm") String searchTerm);

    // Find cards by tag
    @Query("SELECT c FROM Card c JOIN c.tags t WHERE t.id = :tagId AND c.deck.id = :deckId")
    List<Card> findByDeckIdAndTagId(@Param("deckId") Long deckId, @Param("tagId") Long tagId);
}