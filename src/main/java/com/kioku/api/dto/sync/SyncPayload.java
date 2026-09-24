package com.kioku.api.dto.sync;

import java.util.List;

/**
 * The set of entities moving in one direction.
 *
 * <p>Null lists are treated as empty so a client can omit types it has no
 * changes for.
 */
public record SyncPayload(
        List<DeckDto> decks,
        List<TagDto> tags,
        List<CardDto> cards
) {
    public SyncPayload {
        decks = decks == null ? List.of() : decks;
        tags = tags == null ? List.of() : tags;
        cards = cards == null ? List.of() : cards;
    }

    public static SyncPayload empty() {
        return new SyncPayload(List.of(), List.of(), List.of());
    }

    public boolean isEmpty() {
        return decks.isEmpty() && tags.isEmpty() && cards.isEmpty();
    }
}
