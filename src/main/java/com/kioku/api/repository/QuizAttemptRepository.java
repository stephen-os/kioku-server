package com.kioku.api.repository;

import com.kioku.api.model.QuizAttempt;
import org.springframework.stereotype.Repository;

/** Persistence for {@link QuizAttempt}. Sync operations come from {@link SyncRepository}. */
@Repository
public interface QuizAttemptRepository extends SyncRepository<QuizAttempt> {
}
