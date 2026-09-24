package com.kioku.api.dto.sync;

import java.util.List;

/**
 * The set of entities moving in one direction.
 *
 * <p>Null lists become empty, so a client may omit any type it has no changes
 * for rather than sending empty arrays for all seven.
 */
public record SyncPayload(
        List<DeckDto> decks,
        List<TagDto> tags,
        List<CardDto> cards,
        List<QuizDto> quizzes,
        List<QuestionDto> questions,
        List<StudySessionDto> studySessions,
        List<QuizAttemptDto> quizAttempts
) {
    public SyncPayload {
        decks = decks == null ? List.of() : decks;
        tags = tags == null ? List.of() : tags;
        cards = cards == null ? List.of() : cards;
        quizzes = quizzes == null ? List.of() : quizzes;
        questions = questions == null ? List.of() : questions;
        studySessions = studySessions == null ? List.of() : studySessions;
        quizAttempts = quizAttempts == null ? List.of() : quizAttempts;
    }

    public static SyncPayload empty() {
        return new SyncPayload(null, null, null, null, null, null, null);
    }

    public boolean isEmpty() {
        return decks.isEmpty() && tags.isEmpty() && cards.isEmpty()
                && quizzes.isEmpty() && questions.isEmpty()
                && studySessions.isEmpty() && quizAttempts.isEmpty();
    }
}
