package com.kioku.api.repository;

import com.kioku.api.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence for {@link Card}.
 *
 * <p>Only what sync needs: pull everything changed since a cursor, and load a
 * single row for conflict resolution on push. Tombstoned rows are included
 * deliberately, because a client learns about deletions by receiving them.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    /** The pull query. Includes tombstones. */
    List<Card> findByUserIdAndServerSeqGreaterThanOrderByServerSeqAsc(UUID userId, long since);

    /** Loads the stored copy so push can apply the conflict rule. */
    Optional<Card> findByIdAndUserId(UUID id, UUID userId);

    /** Highest sequence this user has for this type; used to report the mark. */
    Optional<Card> findFirstByUserIdOrderByServerSeqDesc(UUID userId);
}
