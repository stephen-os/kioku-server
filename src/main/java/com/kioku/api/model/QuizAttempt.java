package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One run through a quiz, with its per-question results inline.
 *
 * <p>See {@link StudySession} on why these carry the ordinary sync columns
 * despite being records of events.
 */
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt extends SyncableEntity {

    @Column(name = "quiz_id", nullable = false)
    private UUID quizId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions = 0;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers = 0;

    @Column(name = "score_percentage", nullable = false)
    private float scorePercentage = 0f;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_results", nullable = false, columnDefinition = "jsonb")
    private List<QuestionResultValue> questionResults = new ArrayList<>();

    /** JPA, and sync when it materialises an entity it has not seen before. */
    public QuizAttempt() {
    }

    public UUID getQuizId() { return quizId; }
    public void setQuizId(UUID quizId) { this.quizId = quizId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public float getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(float scorePercentage) { this.scorePercentage = scorePercentage; }

    public List<QuestionResultValue> getQuestionResults() { return questionResults; }
    public void setQuestionResults(List<QuestionResultValue> questionResults) {
        this.questionResults = questionResults == null ? new ArrayList<>() : new ArrayList<>(questionResults);
    }

    @Override
    public String toString() {
        return "QuizAttempt{id=" + getId() + ", quizId=" + quizId + "}";
    }
}
