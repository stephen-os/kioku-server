package com.kioku.api.service;

import com.kioku.api.dto.sync.CardDto;
import com.kioku.api.dto.sync.DeckDto;
import com.kioku.api.dto.sync.SyncPayload;
import com.kioku.api.dto.sync.SyncPullResponse;
import com.kioku.api.dto.sync.SyncPushResponse;
import com.kioku.api.dto.sync.TagDto;
import com.kioku.api.model.ContentType;
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
        return new SyncPayload(List.of(d), List.of(), List.of());
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
            SyncPushResponse response = syncService.push(userId, new SyncPayload(
                    List.of(deck(UUID.randomUUID(), "A", now, null),
                            deck(UUID.randomUUID(), "B", now, null)),
                    List.of(), List.of()));

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

            syncService.push(userId, new SyncPayload(
                    List.of(deck(deckId, "Tagged", now, null)),
                    List.of(new TagDto(tagA, deckId, "alpha", 0, now, now, null, 0L),
                            new TagDto(tagB, deckId, "beta", 1, now, now, null, 0L)),
                    List.of(card(UUID.randomUUID(), deckId, "Q", now, new UUID[]{tagA, tagB}))));

            CardDto stored = syncService.pull(userId, 0).changes().cards().getFirst();

            assertThat(stored.tagIds()).containsExactly(tagA, tagB);
        }
    }
}
