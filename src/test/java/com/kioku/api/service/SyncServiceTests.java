package com.kioku.api.service;

import com.kioku.api.dto.sync.CardDto;
import com.kioku.api.dto.sync.QuestionDto;
import com.kioku.api.dto.sync.QuizAttemptDto;
import com.kioku.api.dto.sync.QuizDto;
import com.kioku.api.dto.sync.StudySessionDto;
import com.kioku.api.dto.sync.DeckDto;
import com.kioku.api.dto.sync.SyncPayload;
import com.kioku.api.dto.sync.SyncPullResponse;
import com.kioku.api.dto.sync.SyncPushResponse;
import com.kioku.api.dto.sync.TagDto;
import com.kioku.api.model.ChoiceValue;
import com.kioku.api.model.ContentType;
import com.kioku.api.model.QuestionResultValue;
import com.kioku.api.model.QuestionType;
import com.kioku.api.model.User;
import com.kioku.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behaviour of the sync protocol against real Postgres.
 *
 * <p>These cover the rules that are easy to get wrong and expensive to get
 * wrong quietly: conflict resolution, edit-beats-delete, tombstone delivery,
 * cursor advancement, and cross-user isolation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SyncServiceTests {

    @Autowired
    private SyncService syncService;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;
    private UUID otherUserId;

    @BeforeEach
    void setUp() {
        userId = newUser("sync-user@example.com");
        otherUserId = newUser("other-user@example.com");
    }

    private UUID newUser(String email) {
        User user = new User(email, "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMN");
        return userRepository.save(user).getId();
    }

    private static DeckDto deck(UUID id, String name, Instant updatedAt, Instant deletedAt) {
        return new DeckDto(id, name, "", false, false, updatedAt, updatedAt, deletedAt, 0L);
    }

    private static CardDto card(UUID id, UUID deckId, String front, Instant updatedAt, UUID[] tagIds) {
        return new CardDto(id, deckId, front, ContentType.TEXT, null, "back", ContentType.TEXT,
                null, null, 0, tagIds, updatedAt, updatedAt, null, 0L);
    }

    private static SyncPayload decks(DeckDto... d) {
        return of(b -> b.decks = List.of(d));
    }

    /** Builds a payload with only the types a test cares about. */
    private static SyncPayload of(java.util.function.Consumer<Parts> fill) {
        Parts p = new Parts();
        fill.accept(p);
        return new SyncPayload(p.decks, p.tags, p.cards, p.quizzes, p.questions,
                p.studySessions, p.quizAttempts);
    }

    private static final class Parts {
        List<DeckDto> decks = List.of();
        List<TagDto> tags = List.of();
        List<CardDto> cards = List.of();
        List<QuizDto> quizzes = List.of();
        List<QuestionDto> questions = List.of();
        List<StudySessionDto> studySessions = List.of();
        List<QuizAttemptDto> quizAttempts = List.of();
    }

    @Nested
    @DisplayName("Pull")
    class Pull {

        @Test
        @DisplayName("returns nothing and a zero mark for a user who has never synced")
        void emptyForNewUser() {
            SyncPullResponse response = syncService.pull(userId, 0);

            assertThat(response.changes().isEmpty()).isTrue();
            assertThat(response.seq()).isZero();
        }

        @Test
        @DisplayName("returns only what changed after the cursor")
        void returnsOnlyChangesAfterCursor() {
            Instant now = Instant.now();
            long first = syncService.push(userId, decks(deck(UUID.randomUUID(), "One", now, null))).seq();
            syncService.push(userId, decks(deck(UUID.randomUUID(), "Two", now, null)));

            SyncPullResponse afterFirst = syncService.pull(userId, first);

            assertThat(afterFirst.changes().decks()).hasSize(1);
            assertThat(afterFirst.changes().decks().getFirst().name()).isEqualTo("Two");
        }

        @Test
        @DisplayName("delivers tombstones, because that is how a client learns of deletions")
        void deliversTombstones() {
            UUID deckId = UUID.randomUUID();
            Instant created = Instant.now().minus(1, ChronoUnit.HOURS);
            long afterCreate = syncService.push(userId, decks(deck(deckId, "Doomed", created, null))).seq();

            Instant deleted = Instant.now();
            syncService.push(userId, decks(deck(deckId, "Doomed", deleted, deleted)));

            List<DeckDto> changes = syncService.pull(userId, afterCreate).changes().decks();

            assertThat(changes).hasSize(1);
            assertThat(changes.getFirst().deletedAt()).isNotNull();
        }

        @Test
        @DisplayName("never returns another user's content")
        void isolatesUsers() {
            Instant now = Instant.now();
            syncService.push(otherUserId, decks(deck(UUID.randomUUID(), "Theirs", now, null)));

            assertThat(syncService.pull(userId, 0).changes().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Push")
    class Push {

        @Test
        @DisplayName("accepts an entity the server has never seen")
        void acceptsNewEntity() {
            Instant now = Instant.now();
            SyncPushResponse response =
                    syncService.push(userId, decks(deck(UUID.randomUUID(), "Fresh", now, null)));

            assertThat(response.rejected().isEmpty()).isTrue();
            assertThat(syncService.pull(userId, 0).changes().decks()).hasSize(1);
        }

        @Test
        @DisplayName("advances the sequence once per batch, not per entity")
        void oneSequencePerBatch() {
            Instant now = Instant.now();
            SyncPushResponse response = syncService.push(userId, of(p -> p.decks = List.of(
                    deck(UUID.randomUUID(), "A", now, null),
                    deck(UUID.randomUUID(), "B", now, null))));

            List<DeckDto> stored = syncService.pull(userId, 0).changes().decks();

            assertThat(stored).hasSize(2);
            assertThat(stored).allMatch(d -> d.serverSeq() == response.seq());
        }

        @Test
        @DisplayName("is idempotent: resending the same version changes nothing")
        void resendIsNoOp() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            DeckDto dto = deck(id, "Once", now, null);

            syncService.push(userId, decks(dto));
            syncService.push(userId, decks(dto));

            assertThat(syncService.pull(userId, 0).changes().decks()).hasSize(1);
        }

        @Test
        @DisplayName("a newer edit wins")
        void newerEditWins() {
            UUID id = UUID.randomUUID();
            Instant older = Instant.now().minus(1, ChronoUnit.HOURS);
            syncService.push(userId, decks(deck(id, "Old", older, null)));

            Instant newer = Instant.now();
            SyncPushResponse response = syncService.push(userId, decks(deck(id, "New", newer, null)));

            assertThat(response.rejected().isEmpty()).isTrue();
            assertThat(syncService.pull(userId, 0).changes().decks().getFirst().name()).isEqualTo("New");
        }

        @Test
        @DisplayName("a stale edit loses and the server copy comes back")
        void staleEditLoses() {
            UUID id = UUID.randomUUID();
            Instant newer = Instant.now();
            syncService.push(userId, decks(deck(id, "Current", newer, null)));

            Instant older = newer.minus(1, ChronoUnit.HOURS);
            SyncPushResponse response = syncService.push(userId, decks(deck(id, "Stale", older, null)));

            assertThat(response.rejected().decks()).hasSize(1);
            assertThat(response.rejected().decks().getFirst().name()).isEqualTo("Current");
        }

        @Test
        @DisplayName("an edit newer than a delete resurrects the entity")
        void editBeatsDelete() {
            UUID id = UUID.randomUUID();
            Instant deleted = Instant.now().minus(1, ChronoUnit.HOURS);
            syncService.push(userId, decks(deck(id, "Gone", deleted, deleted)));

            Instant edited = Instant.now();
            syncService.push(userId, decks(deck(id, "Back", edited, null)));

            DeckDto stored = syncService.pull(userId, 0).changes().decks().getFirst();

            assertThat(stored.deletedAt()).isNull();
            assertThat(stored.name()).isEqualTo("Back");
        }

        @Test
        @DisplayName("a delete newer than an edit still wins")
        void deleteBeatsOlderEdit() {
            UUID id = UUID.randomUUID();
            Instant edited = Instant.now().minus(1, ChronoUnit.HOURS);
            syncService.push(userId, decks(deck(id, "Alive", edited, null)));

            Instant deleted = Instant.now();
            syncService.push(userId, decks(deck(id, "Alive", deleted, deleted)));

            assertThat(syncService.pull(userId, 0).changes().decks().getFirst().deletedAt()).isNotNull();
        }

        @Test
        @DisplayName("rejects a timestamp too far in the future rather than letting it win forever")
        void rejectsClockSkew() {
            UUID id = UUID.randomUUID();
            Instant sane = Instant.now();
            syncService.push(userId, decks(deck(id, "Sane", sane, null)));

            Instant wayAhead = Instant.now().plus(SyncService.MAX_CLOCK_SKEW).plus(1, ChronoUnit.HOURS);
            SyncPushResponse response = syncService.push(userId, decks(deck(id, "Skewed", wayAhead, null)));

            assertThat(response.rejected().decks()).hasSize(1);
            assertThat(syncService.pull(userId, 0).changes().decks().getFirst().name()).isEqualTo("Sane");
        }

        @Test
        @DisplayName("cannot overwrite another user's entity with the same id")
        void cannotCrossUserBoundary() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            syncService.push(otherUserId, decks(deck(id, "Theirs", now, null)));

            Instant later = now.plus(1, ChronoUnit.HOURS);
            syncService.push(userId, decks(deck(id, "Mine", later, null)));

            assertThat(syncService.pull(otherUserId, 0).changes().decks().getFirst().name())
                    .isEqualTo("Theirs");
        }

        @Test
        @DisplayName("round-trips a card's tag ids through the uuid[] column")
        void roundTripsTagIds() {
            UUID deckId = UUID.randomUUID();
            UUID tagA = UUID.randomUUID();
            UUID tagB = UUID.randomUUID();
            Instant now = Instant.now();

            syncService.push(userId, of(p -> {
                p.decks = List.of(deck(deckId, "Tagged", now, null));
                p.tags = List.of(new TagDto(tagA, deckId, null, "alpha", 0, now, now, null, 0L),
                                 new TagDto(tagB, deckId, null, "beta", 1, now, now, null, 0L));
                p.cards = List.of(card(UUID.randomUUID(), deckId, "Q", now, new UUID[]{tagA, tagB}));
            }));

            CardDto stored = syncService.pull(userId, 0).changes().cards().getFirst();

            assertThat(stored.tagIds()).containsExactly(tagA, tagB);
        }
    }

    @Nested
    @DisplayName("Quizzes")
    class Quizzes {

        private QuizDto quiz(UUID id, String name, Instant at) {
            return new QuizDto(id, name, "", false, false, at, at, null, 0L);
        }

        private QuestionDto question(UUID id, UUID quizId, Instant at, List<ChoiceValue> choices) {
            return new QuestionDto(id, quizId, QuestionType.MULTIPLE_CHOICE, "Which?",
                    ContentType.TEXT, null, null, false, "because", 0, choices,
                    new UUID[0], at, at, null, 0L);
        }

        @Test
        @DisplayName("round-trips a question's inline choices through jsonb")
        void roundTripsChoices() {
            UUID quizId = UUID.randomUUID();
            Instant now = Instant.now();
            ChoiceValue right = new ChoiceValue(UUID.randomUUID(), "Right", true, 0);
            ChoiceValue wrong = new ChoiceValue(UUID.randomUUID(), "Wrong", false, 1);

            syncService.push(userId, of(p -> {
                p.quizzes = List.of(quiz(quizId, "Fruit", now));
                p.questions = List.of(question(UUID.randomUUID(), quizId, now, List.of(right, wrong)));
            }));

            QuestionDto stored = syncService.pull(userId, 0).changes().questions().getFirst();

            assertThat(stored.choices()).containsExactly(right, wrong);
            assertThat(stored.questionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        }

        @Test
        @DisplayName("resolves a question edit by the same last-write-wins rule")
        void questionsUseTheSameConflictRule() {
            UUID quizId = UUID.randomUUID();
            UUID questionId = UUID.randomUUID();
            Instant older = Instant.now().minus(1, ChronoUnit.HOURS);
            syncService.push(userId, of(p -> {
                p.quizzes = List.of(quiz(quizId, "Fruit", older));
                p.questions = List.of(question(questionId, quizId, older, List.of()));
            }));

            Instant stale = older.minus(1, ChronoUnit.HOURS);
            SyncPushResponse response = syncService.push(userId,
                    of(p -> p.questions = List.of(question(questionId, quizId, stale, List.of()))));

            assertThat(response.rejected().questions()).hasSize(1);
        }

        @Test
        @DisplayName("a question carries tags scoped to its quiz, not to a deck")
        void questionTagsAreQuizScoped() {
            UUID quizId = UUID.randomUUID();
            UUID tagId = UUID.randomUUID();
            Instant now = Instant.now();

            syncService.push(userId, of(p -> {
                p.quizzes = List.of(quiz(quizId, "Scoped", now));
                p.tags = List.of(new TagDto(tagId, null, quizId, "tricky", 0, now, now, null, 0L));
                p.questions = List.of(new QuestionDto(UUID.randomUUID(), quizId,
                        QuestionType.FILL_IN_BLANK, "Blank?", ContentType.TEXT, null,
                        "answer", false, null, 0, List.of(), new UUID[]{tagId},
                        now, now, null, 0L));
            }));

            SyncPayload changes = syncService.pull(userId, 0).changes();

            assertThat(changes.tags()).hasSize(1);
            assertThat(changes.tags().getFirst().quizId()).isEqualTo(quizId);
            assertThat(changes.tags().getFirst().deckId()).isNull();
            assertThat(changes.questions().getFirst().tagIds()).containsExactly(tagId);
        }

        @Test
        @DisplayName("keeps quizzes out of another user's pull")
        void isolatesQuizzes() {
            Instant now = Instant.now();
            syncService.push(otherUserId, of(p -> p.quizzes = List.of(quiz(UUID.randomUUID(), "Theirs", now))));

            assertThat(syncService.pull(userId, 0).changes().quizzes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Progress")
    class Progress {

        @Test
        @DisplayName("records a study session against its deck")
        void recordsStudySession() {
            UUID deckId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant started = now.minus(10, ChronoUnit.MINUTES);

            syncService.push(userId, of(p -> {
                p.decks = List.of(deck(deckId, "Studied", now, null));
                p.studySessions = List.of(new StudySessionDto(UUID.randomUUID(), deckId,
                        started, now, 600, 25, now, now, null, 0L));
            }));

            StudySessionDto stored = syncService.pull(userId, 0).changes().studySessions().getFirst();

            assertThat(stored.cardsStudied()).isEqualTo(25);
            assertThat(stored.durationSeconds()).isEqualTo(600);
        }

        @Test
        @DisplayName("round-trips an attempt's inline question results through jsonb")
        void roundTripsQuestionResults() {
            UUID quizId = UUID.randomUUID();
            Instant now = Instant.now();
            QuestionResultValue hit = new QuestionResultValue(UUID.randomUUID(), UUID.randomUUID(), "A", true);
            QuestionResultValue miss = new QuestionResultValue(UUID.randomUUID(), UUID.randomUUID(), "B", false);

            syncService.push(userId, of(p -> {
                p.quizzes = List.of(new QuizDto(quizId, "Scored", "", false, false, now, now, null, 0L));
                p.quizAttempts = List.of(new QuizAttemptDto(UUID.randomUUID(), quizId,
                        now.minus(5, ChronoUnit.MINUTES), now, 300, 2, 1, 50.0f,
                        List.of(hit, miss), now, now, null, 0L));
            }));

            QuizAttemptDto stored = syncService.pull(userId, 0).changes().quizAttempts().getFirst();

            assertThat(stored.questionResults()).containsExactly(hit, miss);
            assertThat(stored.scorePercentage()).isEqualTo(50.0f);
        }

        @Test
        @DisplayName("completing a session later updates the same row rather than adding one")
        void completingASessionUpdatesInPlace() {
            UUID deckId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            Instant started = Instant.now().minus(10, ChronoUnit.MINUTES);

            syncService.push(userId, of(p -> {
                p.decks = List.of(deck(deckId, "Ongoing", started, null));
                p.studySessions = List.of(new StudySessionDto(sessionId, deckId,
                        started, null, null, 0, started, started, null, 0L));
            }));

            Instant ended = Instant.now();
            syncService.push(userId, of(p -> p.studySessions = List.of(
                    new StudySessionDto(sessionId, deckId, started, ended, 600, 25, started, ended, null, 0L))));

            List<StudySessionDto> stored = syncService.pull(userId, 0).changes().studySessions();

            assertThat(stored).hasSize(1);
            assertThat(stored.getFirst().endedAt()).isNotNull();
            assertThat(stored.getFirst().cardsStudied()).isEqualTo(25);
        }
    }
}
