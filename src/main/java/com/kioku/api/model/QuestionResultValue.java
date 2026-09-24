package com.kioku.api.model;

import java.util.UUID;

/**
 * How one question went in one attempt.
 *
 * <p>Inline on the attempt for the same reason choices are inline on a
 * question: a result only means anything as part of the attempt that
 * produced it.
 */
public record QuestionResultValue(UUID id, UUID questionId, String userAnswer, boolean correct) {
}
