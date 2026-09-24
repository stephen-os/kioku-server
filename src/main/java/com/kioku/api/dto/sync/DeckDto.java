package com.kioku.api.dto.sync;

import java.time.Instant;
import java.util.UUID;

/**
 * A deck as it travels over sync, in both directions.
 *
 * <p>There is no {@code userId}: ownership comes from the authenticated
 * caller, never from the payload, so a client cannot claim another user's
 * rows. {@code serverSeq} is ignored on push and assigned by the server.
 */
public record DeckDto(
        UUID id,
        String name,
        String description,
        boolean shuffleCards,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long serverSeq
) {
}
