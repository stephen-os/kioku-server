package com.kioku.api.repository;

import com.kioku.api.model.StudySession;
import org.springframework.stereotype.Repository;

/** Persistence for {@link StudySession}. Sync operations come from {@link SyncRepository}. */
@Repository
public interface StudySessionRepository extends SyncRepository<StudySession> {
}
