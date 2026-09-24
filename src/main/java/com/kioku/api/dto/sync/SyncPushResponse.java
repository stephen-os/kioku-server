package com.kioku.api.dto.sync;

/**
 * Outcome of a push.
 *
 * <p>{@code rejected} holds the server's copy of any entity where the stored
 * version won the conflict, so the client can reconcile rather than guess.
 */
public record SyncPushResponse(long seq, SyncPayload rejected) {
}
