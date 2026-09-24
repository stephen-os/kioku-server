package com.kioku.api.dto.sync;

import com.kioku.api.model.QuestionResultValue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A quiz attempt as it travels over sync, per-question results included. */
public record QuizAttemptDto(
        UUID id,
        UUID quizId,
        Instant startedAt,
        Instant completedAt,
        Integer durationSeconds,
        int totalQuestions,
        int correctAnswers,
        float scorePercentage,
        List<QuestionResultValue> questionResults,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long serverSeq
) {
}
