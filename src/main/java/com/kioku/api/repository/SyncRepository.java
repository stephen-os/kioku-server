package com.kioku.api.repository;

import com.kioku.api.model.SyncableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * What every syncable type needs, and nothing else.
 *
 * <p>Pull everything changed since a cursor, and load one row so push can
 * apply the conflict rule. Having this in one place lets the sync service
 * treat all seven types through a single code path instead of repeating the
 * same block per entity.
 *
 * @param <E> the entity type
 */
@NoRepositoryBean
public interface SyncRepository<E extends SyncableEntity> extends JpaRepository<E, UUID> {

    /** The pull query. Includes tombstones: deletions travel as rows. */
    List<E> findByUserIdAndServerSeqGreaterThanOrderByServerSeqAsc(UUID userId, long since);

    /** Loads the stored copy so push can compare timestamps. */
    Optional<E> findByIdAndUserId(UUID id, UUID userId);
}
