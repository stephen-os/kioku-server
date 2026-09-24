package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A quiz. Questions reference it by id; there is no collection here. */
@Entity
@Table(name = "quizzes")
public class Quiz extends SyncableEntity {

    @NotBlank(message = "Quiz name is required")
    @Size(max = 255, message = "Quiz name must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    @Size(max = 1000, message = "Quiz description must not exceed 1000 characters")
    @Column(nullable = false, length = 1000)
    private String description = "";

    @Column(name = "shuffle_questions", nullable = false)
    private boolean shuffleQuestions = false;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;

    /** JPA, and sync when it materialises an entity it has not seen before. */
    public Quiz() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public boolean isShuffleQuestions() { return shuffleQuestions; }
    public void setShuffleQuestions(boolean shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    @Override
    public String toString() {
        return "Quiz{id=" + getId() + ", name='" + name + "'}";
    }
}
