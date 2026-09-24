package com.kioku.api.service;

import com.kioku.api.dto.sync.CardDto;
import com.kioku.api.dto.sync.DeckDto;
import com.kioku.api.dto.sync.QuestionDto;
import com.kioku.api.dto.sync.QuizAttemptDto;
import com.kioku.api.dto.sync.QuizDto;
import com.kioku.api.dto.sync.StudySessionDto;
import com.kioku.api.dto.sync.SyncPayload;
import com.kioku.api.dto.sync.SyncPullResponse;
import com.kioku.api.dto.sync.SyncPushResponse;
import com.kioku.api.dto.sync.TagDto;
import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.Question;
import com.kioku.api.model.Quiz;
import com.kioku.api.model.QuizAttempt;
import com.kioku.api.model.StudySession;
import com.kioku.api.model.SyncableEntity;
import com.kioku.api.model.Tag;
import com.kioku.api.model.UserSyncState;
import com.kioku.api.repository.CardRepository;
import com.kioku.api.repository.DeckRepository;
import com.kioku.api.repository.QuestionRepository;
import com.kioku.api.repository.QuizAttemptRepository;
import com.kioku.api.repository.QuizRepository;
import com.kioku.api.repository.StudySessionRepository;
import com.kioku.api.repository.SyncRepository;
import com.kioku.api.repository.TagRepository;
import com.kioku.api.repository.UserSyncStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reconciles a client's local replica with the server.
 *
 * <p>Rules live in {@code docs/sync-contract.md}. The two that matter most
 * here: conflicts resolve at entity level on {@code updatedAt}, and an edit
 * beats a delete, because a user can always delete again but cannot un-lose
 * content.
 */
@Service
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    /**
     * How far ahead of the server a client's clock may be before its writes
     * are refused. Without a ceiling, one device with a wrong clock silently
     * wins every conflict forever.
     */
    static final Duration MAX_CLOCK_SKEW = Duration.ofMinutes(5);

    private final DeckRepository deckRepository;
    private final TagRepository tagRepository;
    private final CardRepository cardRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final StudySessionRepository studySessionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserSyncStateRepository syncStateRepository;

    public SyncService(DeckRepository deckRepository,
                       TagRepository tagRepository,
                       CardRepository cardRepository,
                       QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       StudySessionRepository studySessionRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       UserSyncStateRepository syncStateRepository) {
        this.deckRepository = deckRepository;
        this.tagRepository = tagRepository;
        this.cardRepository = cardRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.studySessionRepository = studySessionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.syncStateRepository = syncStateRepository;
    }

    /**
     * Everything owned by this user that changed after {@code since},
     * tombstones included.
     *
     * <p>The returned mark is the user's counter rather than the highest
     * sequence in the payload. A push increments that counter and writes its
     * entities in one transaction, so the two become visible together and a
     * pull can never report a mark covering rows it did not return.
     */
    @Transactional(readOnly = true)
    public SyncPullResponse pull(UUID userId, long since) {
        SyncPayload changes = new SyncPayload(
                changed(deckRepository, userId, since, SyncService::toDto),
                changed(tagRepository, userId, since, SyncService::toDto),
                changed(cardRepository, userId, since, SyncService::toDto),
                changed(quizRepository, userId, since, SyncService::toDto),
                changed(questionRepository, userId, since, SyncService::toDto),
                changed(studySessionRepository, userId, since, SyncService::toDto),
                changed(quizAttemptRepository, userId, since, SyncService::toDto));

        long mark = syncStateRepository.findById(userId)
                .map(UserSyncState::getCurrentSeq)
                .orElse(0L);

        logger.debug("Pull user={} since={} mark={}", userId, since, mark);
        return new SyncPullResponse(mark, changes);
    }

    /**
     * Applies a client's changes and reports anything the server's copy won.
     *
     * <p>The whole batch takes a single sequence. It was written atomically by
     * the client, so splitting it across sequences would let another device
     * pull half of it.
     */
    @Transactional
    public SyncPushResponse push(UUID userId, SyncPayload incoming) {
        Instant ceiling = Instant.now().plus(MAX_CLOCK_SKEW);

        // Take the lock before reading the counter. Two pushes that both read
        // it before either commits would allocate the same sequence, and a
        // client pulling in between would advance past a change it never
        // received, with no error to tell it so.
        UserSyncState state = syncStateRepository.findByUserId(userId)
                .orElseGet(() -> syncStateRepository.save(new UserSyncState(userId)));
        long seq = state.nextSeq();

        SyncPayload rejected = new SyncPayload(
                apply(deckRepository, incoming.decks(), userId, seq, ceiling,
                        DeckDto::id, DeckDto::updatedAt, Deck::new, SyncService::write, SyncService::toDto),
                apply(tagRepository, incoming.tags(), userId, seq, ceiling,
                        TagDto::id, TagDto::updatedAt, Tag::new, SyncService::write, SyncService::toDto),
                apply(cardRepository, incoming.cards(), userId, seq, ceiling,
                        CardDto::id, CardDto::updatedAt, Card::new, SyncService::write, SyncService::toDto),
                apply(quizRepository, incoming.quizzes(), userId, seq, ceiling,
                        QuizDto::id, QuizDto::updatedAt, Quiz::new, SyncService::write, SyncService::toDto),
                apply(questionRepository, incoming.questions(), userId, seq, ceiling,
                        QuestionDto::id, QuestionDto::updatedAt, Question::new, SyncService::write, SyncService::toDto),
                apply(studySessionRepository, incoming.studySessions(), userId, seq, ceiling,
                        StudySessionDto::id, StudySessionDto::updatedAt, StudySession::new, SyncService::write, SyncService::toDto),
                apply(quizAttemptRepository, incoming.quizAttempts(), userId, seq, ceiling,
                        QuizAttemptDto::id, QuizAttemptDto::updatedAt, QuizAttempt::new, SyncService::write, SyncService::toDto));

        syncStateRepository.save(state);

        logger.debug("Push user={} seq={} rejected={}", userId, seq, !rejected.isEmpty());
        return new SyncPushResponse(seq, rejected);
    }

    private static <E extends SyncableEntity, D> List<D> changed(
            SyncRepository<E> repo, UUID userId, long since, Function<E, D> toDto) {
        return repo.findByUserIdAndServerSeqGreaterThanOrderByServerSeqAsc(userId, since)
                .stream().map(toDto).toList();
    }

    /**
     * Applies one type's incoming entities, returning the server's copy of any
     * that lost.
     *
     * <p>All seven types resolve identically, so they share this path rather
     * than repeating the comparison per entity.
     */
    private static <E extends SyncableEntity, D> List<D> apply(
            SyncRepository<E> repo,
            List<D> incoming,
            UUID userId,
            long seq,
            Instant ceiling,
            Function<D, UUID> idOf,
            Function<D, Instant> updatedAtOf,
            Supplier<E> factory,
            BiConsumer<E, D> write,
            Function<E, D> toDto) {

        List<D> rejected = new ArrayList<>();
        for (D dto : incoming) {
            UUID id = idOf.apply(dto);
            Instant updatedAt = updatedAtOf.apply(dto);
            Optional<E> stored = id == null ? Optional.empty() : repo.findByIdAndUserId(id, userId);

            if (!accepts(stored.orElse(null), id, updatedAt, ceiling)) {
                stored.map(toDto).ifPresent(rejected::add);
                continue;
            }

            E entity = stored.orElseGet(factory);
            write.accept(entity, dto);
            entity.setId(id);
            entity.setUserId(userId);
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(updatedAt);
            }
            entity.setUpdatedAt(updatedAt);
            entity.setServerSeq(seq);
            repo.save(entity);
        }
        return rejected;
    }

    /**
     * The conflict rule.
     *
     * <p>Deletes are ordinary updates that set {@code deletedAt}, so
     * edit-beats-delete needs no special case: a later edit wins on timestamp
     * and clears the tombstone on its way through.
     */
    private static boolean accepts(SyncableEntity stored, UUID incomingId,
                                   Instant incomingUpdatedAt, Instant ceiling) {
        if (incomingUpdatedAt == null || incomingId == null) {
            return false;
        }
        if (incomingUpdatedAt.isAfter(ceiling)) {
            return false;
        }
        if (stored == null) {
            return true;
        }
        int cmp = incomingUpdatedAt.compareTo(stored.getUpdatedAt());
        if (cmp != 0) {
            return cmp > 0;
        }
        // Identical timestamps: break the tie on id so every node reaches the
        // same answer without coordinating.
        return incomingId.compareTo(stored.getId()) > 0;
    }

    // --- dto -> entity -------------------------------------------------

    private static void write(Deck e, DeckDto d) {
        e.setName(d.name());
        e.setDescription(d.description());
        e.setShuffleCards(d.shuffleCards());
        e.setFavorite(d.favorite());
        e.setDeletedAt(d.deletedAt());
    }

    private static void write(Tag e, TagDto d) {
        e.setDeckId(d.deckId());
        e.setName(d.name());
        e.setPosition(d.position());
        e.setDeletedAt(d.deletedAt());
    }

    private static void write(Card e, CardDto d) {
        e.setDeckId(d.deckId());
        e.setFront(d.front());
        e.setFrontType(d.frontType());
        e.setFrontLanguage(d.frontLanguage());
        e.setBack(d.back());
        e.setBackType(d.backType());
        e.setBackLanguage(d.backLanguage());
        e.setNotes(d.notes());
        e.setPosition(d.position());
        e.setTagIds(d.tagIds());
        e.setDeletedAt(d.deletedAt());
    }

    private static void write(Quiz e, QuizDto d) {
        e.setName(d.name());
        e.setDescription(d.description());
        e.setShuffleQuestions(d.shuffleQuestions());
        e.setFavorite(d.favorite());
        e.setDeletedAt(d.deletedAt());
    }

    private static void write(Question e, QuestionDto d) {
        e.setQuizId(d.quizId());
        e.setQuestionType(d.questionType());
        e.setContent(d.content());
        e.setContentType(d.contentType());
        e.setContentLanguage(d.contentLanguage());
        e.setCorrectAnswer(d.correctAnswer());
        e.setMultipleAnswers(d.multipleAnswers());
        e.setExplanation(d.explanation());
        e.setPosition(d.position());
        e.setChoices(d.choices());
        e.setTagIds(d.tagIds());
        e.setDeletedAt(d.deletedAt());
    }

    private static void write(StudySession e, StudySessionDto d) {
        e.setDeckId(d.deckId());
        e.setStartedAt(d.startedAt());
        e.setEndedAt(d.endedAt());
        e.setDurationSeconds(d.durationSeconds());
        e.setCardsStudied(d.cardsStudied());
        e.setDeletedAt(d.deletedAt());
    }

    private static void write(QuizAttempt e, QuizAttemptDto d) {
        e.setQuizId(d.quizId());
        e.setStartedAt(d.startedAt());
        e.setCompletedAt(d.completedAt());
        e.setDurationSeconds(d.durationSeconds());
        e.setTotalQuestions(d.totalQuestions());
        e.setCorrectAnswers(d.correctAnswers());
        e.setScorePercentage(d.scorePercentage());
        e.setQuestionResults(d.questionResults());
        e.setDeletedAt(d.deletedAt());
    }

    // --- entity -> dto -------------------------------------------------

    static DeckDto toDto(Deck d) {
        return new DeckDto(d.getId(), d.getName(), d.getDescription(), d.isShuffleCards(),
                d.isFavorite(), d.getCreatedAt(), d.getUpdatedAt(), d.getDeletedAt(), d.getServerSeq());
    }

    static TagDto toDto(Tag t) {
        return new TagDto(t.getId(), t.getDeckId(), t.getName(), t.getPosition(),
                t.getCreatedAt(), t.getUpdatedAt(), t.getDeletedAt(), t.getServerSeq());
    }

    static CardDto toDto(Card c) {
        return new CardDto(c.getId(), c.getDeckId(), c.getFront(), c.getFrontType(),
                c.getFrontLanguage(), c.getBack(), c.getBackType(), c.getBackLanguage(),
                c.getNotes(), c.getPosition(), c.getTagIds(),
                c.getCreatedAt(), c.getUpdatedAt(), c.getDeletedAt(), c.getServerSeq());
    }

    static QuizDto toDto(Quiz q) {
        return new QuizDto(q.getId(), q.getName(), q.getDescription(), q.isShuffleQuestions(),
                q.isFavorite(), q.getCreatedAt(), q.getUpdatedAt(), q.getDeletedAt(), q.getServerSeq());
    }

    static QuestionDto toDto(Question q) {
        return new QuestionDto(q.getId(), q.getQuizId(), q.getQuestionType(), q.getContent(),
                q.getContentType(), q.getContentLanguage(), q.getCorrectAnswer(),
                q.isMultipleAnswers(), q.getExplanation(), q.getPosition(), q.getChoices(),
                q.getTagIds(), q.getCreatedAt(), q.getUpdatedAt(), q.getDeletedAt(), q.getServerSeq());
    }

    static StudySessionDto toDto(StudySession s) {
        return new StudySessionDto(s.getId(), s.getDeckId(), s.getStartedAt(), s.getEndedAt(),
                s.getDurationSeconds(), s.getCardsStudied(),
                s.getCreatedAt(), s.getUpdatedAt(), s.getDeletedAt(), s.getServerSeq());
    }

    static QuizAttemptDto toDto(QuizAttempt a) {
        return new QuizAttemptDto(a.getId(), a.getQuizId(), a.getStartedAt(), a.getCompletedAt(),
                a.getDurationSeconds(), a.getTotalQuestions(), a.getCorrectAnswers(),
                a.getScorePercentage(), a.getQuestionResults(),
                a.getCreatedAt(), a.getUpdatedAt(), a.getDeletedAt(), a.getServerSeq());
    }
}
