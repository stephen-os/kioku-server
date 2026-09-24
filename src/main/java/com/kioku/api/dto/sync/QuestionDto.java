package com.kioku.api.dto.sync;

import com.kioku.api.model.ChoiceValue;
import com.kioku.api.model.CodeLanguage;
import com.kioku.api.model.ContentType;
import com.kioku.api.model.QuestionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A question as it travels over sync, choices included.
 *
 * <p>Choices ride along rather than syncing separately, so editing a question
 * and its options is one atomic change.
 */
public record QuestionDto(
        UUID id,
        UUID quizId,
        QuestionType questionType,
        String content,
        ContentType contentType,
        CodeLanguage contentLanguage,
        String correctAnswer,
        boolean multipleAnswers,
        String explanation,
        int position,
        List<ChoiceValue> choices,
        UUID[] tagIds,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long serverSeq
) {
}
