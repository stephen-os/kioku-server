package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A question within a quiz, carrying its choices inline.
 */
@Entity
@Table(name = "questions")
public class Question extends SyncableEntity {

    @Column(name = "quiz_id", nullable = false)
    private UUID quizId;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType = QuestionType.MULTIPLE_CHOICE;

    @NotBlank(message = "Question content is required")
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 10)
    private ContentType contentType = ContentType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_language", length = 20)
    private CodeLanguage contentLanguage;

    /** Used by fill-in-the-blank questions only. */
    @Column(name = "correct_answer", columnDefinition = "text")
    private String correctAnswer;

    /** Whether a multiple-choice question accepts more than one correct option. */
    @Column(name = "multiple_answers", nullable = false)
    private boolean multipleAnswers = false;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(nullable = false)
    private int position = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<ChoiceValue> choices = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tag_ids", nullable = false, columnDefinition = "uuid[]")
    private UUID[] tagIds = new UUID[0];

    /** JPA, and sync when it materialises an entity it has not seen before. */
    public Question() {
    }

    public UUID getQuizId() { return quizId; }
    public void setQuizId(UUID quizId) { this.quizId = quizId; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }

    public CodeLanguage getContentLanguage() { return contentLanguage; }
    public void setContentLanguage(CodeLanguage contentLanguage) { this.contentLanguage = contentLanguage; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public boolean isMultipleAnswers() { return multipleAnswers; }
    public void setMultipleAnswers(boolean multipleAnswers) { this.multipleAnswers = multipleAnswers; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public List<ChoiceValue> getChoices() { return choices; }
    public void setChoices(List<ChoiceValue> choices) {
        this.choices = choices == null ? new ArrayList<>() : new ArrayList<>(choices);
    }

    public UUID[] getTagIds() { return tagIds; }
    public void setTagIds(UUID[] tagIds) { this.tagIds = tagIds == null ? new UUID[0] : tagIds; }

    @Override
    public String toString() {
        return "Question{id=" + getId() + ", quizId=" + quizId + "}";
    }
}
