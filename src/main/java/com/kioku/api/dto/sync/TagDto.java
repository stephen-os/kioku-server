package com.kioku.api.dto.sync;

import java.time.Instant;
import java.util.UUID;

/**
 * A tag as it travels over sync. See {@link DeckDto} on ownership.
 *
 * <p>Exactly one of {@code deckId} and {@code quizId} is set: a tag belongs to
 * a deck (used by cards) or to a quiz (used by questions).
 */
public record TagDto(
        UUID id,
        UUID deckId,
        UUID quizId,
        String name,
        int position,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long serverSeq
) {
}
