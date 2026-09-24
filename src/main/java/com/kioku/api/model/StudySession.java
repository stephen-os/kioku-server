package com.kioku.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A record of one study run over a deck.
 *
 * <p>Carries the same sync columns as content. In practice two devices never
 * produce conflicting versions of the same session, so the last-write-wins
 * rule almost never fires; it is here so the update from starting a session to
 * completing it resolves without a second code path.
 */
@Entity
@Table(name = "study_sessions")
public class StudySession extends SyncableEntity {

    @Column(name = "deck_id", nullable = false)
    private UUID deckId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "cards_studied", nullable = false)
    private int cardsStudied = 0;

    /** JPA, and sync when it materialises an entity it has not seen before. */
    public StudySession() {
    }

    public UUID getDeckId() { return deckId; }
    public void setDeckId(UUID deckId) { this.deckId = deckId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getCardsStudied() { return cardsStudied; }
    public void setCardsStudied(int cardsStudied) { this.cardsStudied = cardsStudied; }

    @Override
    public String toString() {
        return "StudySession{id=" + getId() + ", deckId=" + deckId + "}";
    }
}
