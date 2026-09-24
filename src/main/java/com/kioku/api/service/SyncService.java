package com.kioku.api.service;

import com.kioku.api.dto.sync.CardDto;
import com.kioku.api.dto.sync.DeckDto;
import com.kioku.api.dto.sync.SyncPayload;
import com.kioku.api.dto.sync.SyncPullResponse;
import com.kioku.api.dto.sync.SyncPushResponse;
import com.kioku.api.dto.sync.TagDto;
import com.kioku.api.model.Card;
import com.kioku.api.model.Deck;
import com.kioku.api.model.SyncableEntity;
import com.kioku.api.model.Tag;
import com.kioku.api.model.UserSyncState;
import com.kioku.api.repository.CardRepository;
import com.kioku.api.repository.DeckRepository;
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
    private final UserSyncStateRepository syncStateRepository;

    public SyncService(DeckRepository deckRepository,
                       TagRepository tagRepository,
                       CardRepository cardRepository,
                       UserSyncStateRepository syncStateRepository) {
        this.deckRepository = deckRepository;
        this.tagRepository = tagRepository;
        this.cardRepository = cardRepository;
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
        List<DeckDto> decks = deckRepository
                .findByUserIdAndServerSeqGreaterThanOrderByServerSeqAsc(userId, since)
                .stream().map(SyncService::toDto).toList();
        List<TagDto> tags = tagRepository
                .findByUserIdAndServerSeqGreaterThanOrderByServerSeqAsc(userId, since)
                .stream().map(SyncService::toDto).toList();
        List<CardDto> cards = cardRepository
                .findByUserIdAndServerSeqGreaterThanOrderByServerSeqAsc(userId, since)
                .stream().map(SyncService::toDto).toList();

        long mark = syncStateRepository.findById(userId)
                .map(UserSyncState::getCurrentSeq)
                .orElse(0L);

        logger.debug("Pull user={} since={} -> {} decks, {} tags, {} cards, mark={}",
                userId, since, decks.size(), tags.size(), cards.size(), mark);

        return new SyncPullResponse(mark, new SyncPayload(decks, tags, cards));
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

        List<DeckDto> rejectedDecks = new ArrayList<>();
        for (DeckDto dto : incoming.decks()) {
            Optional<Deck> stored = deckRepository.findByIdAndUserId(dto.id(), userId);
            if (accepts(stored.orElse(null), dto.id(), dto.updatedAt(), ceiling)) {
                deckRepository.save(apply(stored.orElseGet(Deck::new), dto, userId, seq));
            } else {
                stored.map(SyncService::toDto).ifPresent(rejectedDecks::add);
            }
        }

        List<TagDto> rejectedTags = new ArrayList<>();
        for (TagDto dto : incoming.tags()) {
            Optional<Tag> stored = tagRepository.findByIdAndUserId(dto.id(), userId);
            if (accepts(stored.orElse(null), dto.id(), dto.updatedAt(), ceiling)) {
                tagRepository.save(apply(stored.orElseGet(Tag::new), dto, userId, seq));
            } else {
                stored.map(SyncService::toDto).ifPresent(rejectedTags::add);
            }
        }

        List<CardDto> rejectedCards = new ArrayList<>();
        for (CardDto dto : incoming.cards()) {
            Optional<Card> stored = cardRepository.findByIdAndUserId(dto.id(), userId);
            if (accepts(stored.orElse(null), dto.id(), dto.updatedAt(), ceiling)) {
                cardRepository.save(apply(stored.orElseGet(Card::new), dto, userId, seq));
            } else {
                stored.map(SyncService::toDto).ifPresent(rejectedCards::add);
            }
        }

        syncStateRepository.save(state);

        logger.debug("Push user={} seq={} rejected {} decks, {} tags, {} cards",
                userId, seq, rejectedDecks.size(), rejectedTags.size(), rejectedCards.size());

        return new SyncPushResponse(seq, new SyncPayload(rejectedDecks, rejectedTags, rejectedCards));
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

    private static <E extends SyncableEntity> E stamp(E entity, UUID id, UUID userId,
                                                      Instant createdAt, Instant updatedAt,
                                                      Instant deletedAt, long seq) {
        entity.setId(id);
        entity.setUserId(userId);
        entity.setCreatedAt(createdAt == null ? updatedAt : createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setDeletedAt(deletedAt);
        entity.setServerSeq(seq);
        return entity;
    }

    private static Deck apply(Deck deck, DeckDto dto, UUID userId, long seq) {
        deck.setName(dto.name());
        deck.setDescription(dto.description());
        deck.setShuffleCards(dto.shuffleCards());
        deck.setFavorite(dto.favorite());
        return stamp(deck, dto.id(), userId, dto.createdAt(), dto.updatedAt(), dto.deletedAt(), seq);
    }

    private static Tag apply(Tag tag, TagDto dto, UUID userId, long seq) {
        tag.setDeckId(dto.deckId());
        tag.setName(dto.name());
        tag.setPosition(dto.position());
        return stamp(tag, dto.id(), userId, dto.createdAt(), dto.updatedAt(), dto.deletedAt(), seq);
    }

    private static Card apply(Card card, CardDto dto, UUID userId, long seq) {
        card.setDeckId(dto.deckId());
        card.setFront(dto.front());
        card.setFrontType(dto.frontType());
        card.setFrontLanguage(dto.frontLanguage());
        card.setBack(dto.back());
        card.setBackType(dto.backType());
        card.setBackLanguage(dto.backLanguage());
        card.setNotes(dto.notes());
        card.setPosition(dto.position());
        card.setTagIds(dto.tagIds());
        return stamp(card, dto.id(), userId, dto.createdAt(), dto.updatedAt(), dto.deletedAt(), seq);
    }

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
}
