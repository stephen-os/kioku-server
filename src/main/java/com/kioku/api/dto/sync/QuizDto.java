package com.kioku.api.dto.sync;

import java.time.Instant;
import java.util.UUID;

/** A quiz as it travels over sync. See {@link DeckDto} on ownership. */
public record QuizDto(
        UUID id,
        String name,
        String description,
        boolean shuffleQuestions,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long serverSeq
) {
}
