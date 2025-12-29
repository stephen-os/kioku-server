package com.kioku.api.service;

import com.kioku.api.entity.Card;
import com.kioku.api.entity.Deck;
import com.kioku.api.entity.Tag;
import com.kioku.api.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final DeckService deckService;
    private final TagService tagService;

    public CardService(CardRepository cardRepository, DeckService deckService, TagService tagService) {
        this.cardRepository = cardRepository;
        this.deckService = deckService;
        this.tagService = tagService;
    }

    /**
     * Create a new card in a deck (with ownership check)
     */
    public Card createCard(Long userId, Long deckId, String front, String back, String notes) {
        // Verify user owns the deck
        Deck deck = deckService.getDeckOrThrow(deckId, userId);

        // Check for duplicate
        if (cardRepository.existsByDeckIdAndFrontAndBack(deckId, front, back)) {
            throw new IllegalArgumentException("Card with same front and back already exists in this deck");
        }

        Card card = new Card(deck, front, back, notes);
        return cardRepository.save(card);
    }

    /**
     * Get all cards in a deck (with ownership check)
     */
    public List<Card> getDeckCards(Long userId, Long deckId) {
        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        return cardRepository.findByDeckId(deckId);
    }

    /**
     * Get a specific card (with ownership check)
     */
    public Optional<Card> getCard(Long userId, Long deckId, Long cardId) {
        // Verify user owns the deck
        if (!deckService.userOwnsDeck(deckId, userId)) {
            return Optional.empty();
        }

        return cardRepository.findByIdAndDeckId(cardId, deckId);
    }

    /**
     * Get a card or throw exception if not found or not owned
     */
    public Card getCardOrThrow(Long userId, Long deckId, Long cardId) {
        return getCard(userId, deckId, cardId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Card not found or access denied: " + cardId));
    }

    /**
     * Update a card (with ownership check)
     */
    public Card updateCard(Long userId, Long deckId, Long cardId, String front, String back, String notes) {
        Card card = getCardOrThrow(userId, deckId, cardId);

        // Check if update would create duplicate
        if (!card.getFront().equals(front) || !card.getBack().equals(back)) {
            if (cardRepository.existsByDeckIdAndFrontAndBack(deckId, front, back)) {
                throw new IllegalArgumentException("Card with same front and back already exists in this deck");
            }
        }

        card.setFront(front);
        card.setBack(back);
        card.setNotes(notes);
        return cardRepository.save(card);
    }

    /**
     * Delete a card (with ownership check)
     */
    public void deleteCard(Long userId, Long deckId, Long cardId) {
        Card card = getCardOrThrow(userId, deckId, cardId);
        cardRepository.delete(card);
    }

    /**
     * Search cards in a deck (with ownership check)
     */
    public List<Card> searchCards(Long userId, Long deckId, String searchTerm) {
        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        return cardRepository.searchByDeckId(deckId, searchTerm);
    }

    /**
     * Get cards by tag (with ownership check)
     */
    public List<Card> getCardsByTag(Long userId, Long deckId, Long tagId) {
        // Verify user owns the deck
        deckService.getDeckOrThrow(deckId, userId);

        // Verify user owns the tag
        tagService.getTagOrThrow(userId, tagId);

        return cardRepository.findByDeckIdAndTagId(deckId, tagId);
    }

    /**
     * Add tag to card (with ownership check)
     */
    public Card addTagToCard(Long userId, Long deckId, Long cardId, Long tagId) {
        Card card = getCardOrThrow(userId, deckId, cardId);
        Tag tag = tagService.getTagOrThrow(userId, tagId);

        card.addTag(tag);
        return cardRepository.save(card);
    }

    /**
     * Remove tag from card (with ownership check)
     */
    public Card removeTagFromCard(Long userId, Long deckId, Long cardId, Long tagId) {
        Card card = getCardOrThrow(userId, deckId, cardId);
        Tag tag = tagService.getTagOrThrow(userId, tagId);

        card.removeTag(tag);
        return cardRepository.save(card);
    }

    /**
     * Set audio URLs for a card (with ownership check)
     */
    public Card setCardAudio(Long userId, Long deckId, Long cardId, String frontAudioUrl, String backAudioUrl) {
        Card card = getCardOrThrow(userId, deckId, cardId);

        card.setFrontAudioUrl(frontAudioUrl);
        card.setBackAudioUrl(backAudioUrl);
        return cardRepository.save(card);
    }

    /**
     * Check for duplicate card
     */
    public boolean isDuplicate(Long deckId, String front, String back) {
        return cardRepository.existsByDeckIdAndFrontAndBack(deckId, front, back);
    }
}