package com.kioku.api.repository;

import com.kioku.api.model.Question;
import org.springframework.stereotype.Repository;

/** Persistence for {@link Question}. Sync operations come from {@link SyncRepository}. */
@Repository
public interface QuestionRepository extends SyncRepository<Question> {
}
