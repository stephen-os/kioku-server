package com.kioku.api.repository;

import com.kioku.api.model.Card;
import org.springframework.stereotype.Repository;

/** Persistence for {@link Card}. Sync operations come from {@link SyncRepository}. */
@Repository
public interface CardRepository extends SyncRepository<Card> {
}
