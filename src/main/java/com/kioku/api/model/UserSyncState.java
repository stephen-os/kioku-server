package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;


/**
 * Per-user sequence counter backing {@code server_seq}.
 *
 * <p>Kept as its own row so assignment can take a row lock. Handing out a
 * high-water mark past an in-flight transaction silently loses changes: a
 * client that pulls between two commits advances past a sequence it never
 * received and never asks for it again. See the sequence assignment section of
 * {@code docs/sync-contract.md}.
 */
@Entity
@Table(name = "user_sync_state")
public class UserSyncState {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "current_seq", nullable = false)
    private long currentSeq;

    protected UserSyncState() {
    }

    public UserSyncState(UUID userId) {
        this.userId = userId;
        this.currentSeq = 0L;
    }

    public UUID getUserId() { return userId; }

    public long getCurrentSeq() { return currentSeq; }
    public void setCurrentSeq(long currentSeq) { this.currentSeq = currentSeq; }

    /** Advances the counter and returns the newly allocated sequence. */
    public long nextSeq() {
        return ++currentSeq;
    }
}
