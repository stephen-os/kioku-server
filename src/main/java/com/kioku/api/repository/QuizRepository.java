package com.kioku.api.repository;

import com.kioku.api.model.Quiz;
import org.springframework.stereotype.Repository;

/** Persistence for {@link Quiz}. Sync operations come from {@link SyncRepository}. */
@Repository
public interface QuizRepository extends SyncRepository<Quiz> {
}
