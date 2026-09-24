package com.kioku.api.repository;

import com.kioku.api.model.Deck;
import org.springframework.stereotype.Repository;

/** Persistence for {@link Deck}. Sync operations come from {@link SyncRepository}. */
@Repository
public interface DeckRepository extends SyncRepository<Deck> {
}
