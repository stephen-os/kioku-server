package com.kioku.api.dto.sync;

import java.time.Instant;
import java.util.UUID;

/** A study session as it travels over sync. */
public record StudySessionDto(
        UUID id,
        UUID deckId,
        Instant startedAt,
        Instant endedAt,
        Integer durationSeconds,
        int cardsStudied,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long serverSeq
) {
}
