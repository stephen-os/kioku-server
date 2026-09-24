package com.kioku.api.repository;

import com.kioku.api.model.Tag;
import org.springframework.stereotype.Repository;

/** Persistence for {@link Tag}. Sync operations come from {@link SyncRepository}. */
@Repository
public interface TagRepository extends SyncRepository<Tag> {
}
