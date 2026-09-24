package com.kioku.api.repository;

import com.kioku.api.model.UserSyncState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence for the per-user sequence counter.
 */
@Repository
public interface UserSyncStateRepository extends JpaRepository<UserSyncState, UUID> {

    /**
     * Loads the counter under a write lock.
     *
     * <p>The lock is the point. Two concurrent pushes that both read the
     * counter before either commits would hand out the same sequence, and a
     * client pulling in between would advance past a change it never saw.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserSyncState> findByUserId(UUID userId);
}
