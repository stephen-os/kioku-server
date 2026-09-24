package com.kioku.api.dto.sync;

/**
 * Everything that changed since the client's cursor.
 *
 * <p>{@code seq} is the new high-water mark to send as {@code since} next
 * time. Tombstones are included: a client learns about deletions by receiving
 * them.
 */
public record SyncPullResponse(long seq, SyncPayload changes) {
}
