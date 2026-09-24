package com.kioku.api.dto.sync;

import java.time.Instant;
import java.util.UUID;

/** A tag as it travels over sync. See {@link DeckDto} on ownership. */
public record TagDto(
        UUID id,
        UUID deckId,
        String name,
        int position,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long serverSeq
) {
}
