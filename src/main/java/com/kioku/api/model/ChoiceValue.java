package com.kioku.api.model;

import java.util.UUID;

/**
 * One option on a multiple-choice question.
 *
 * <p>Stored inline on the question as jsonb rather than as its own row: a
 * choice is never meaningful on its own and is always edited alongside its
 * question, so keeping them together makes a question edit atomic.
 */
public record ChoiceValue(UUID id, String text, boolean correct, int position) {
}
