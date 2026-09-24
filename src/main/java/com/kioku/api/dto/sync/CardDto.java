package com.kioku.api.dto.sync;

import com.kioku.api.model.CodeLanguage;
import com.kioku.api.model.ContentType;

import java.time.Instant;
import java.util.UUID;

/**
 * A card as it travels over sync.
 *
 * <p>{@code tagIds} carries tag membership directly, so retagging is an
 * ordinary field edit resolved by the same last-write-wins rule.
 */
public record CardDto(
        UUID id,
        UUID deckId,
        String front,
        ContentType frontType,
        CodeLanguage frontLanguage,
        String back,
        ContentType backType,
        CodeLanguage backLanguage,
        String notes,
        int position,
        UUID[] tagIds,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long serverSeq
) {
}
