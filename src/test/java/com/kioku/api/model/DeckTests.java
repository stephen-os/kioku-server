package com.kioku.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Deck entity.
 *
 * Uses a fruit theme: decks represent fruit collections like "Citrus Fruits", "Tropical Fruits".
 * Cards within decks are individual fruits with their descriptions.
 *
 * @author Stephen Watson
 */
@DisplayName("Deck Entity Tests")
class DeckTests {

    private static final String DECK_NAME = "Citrus Fruits";
    private static final String DECK_DESCRIPTION = "A collection of citrus fruits and their properties";

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck(DECK_NAME, DECK_DESCRIPTION);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("creates deck with name only")
        void createsDeckWithNameOnly() {
            Deck newDeck = new Deck("Tropical Fruits");
            assertNotNull(newDeck);
            assertEquals("Tropical Fruits", newDeck.getName());
            assertEquals("", newDeck.getDescription());
        }

        @Test
        @DisplayName("creates deck with name and description")
        void createsDeckWithNameAndDescription() {
            Deck newDeck = new Deck(DECK_NAME, DECK_DESCRIPTION);
            assertNotNull(newDeck);
            assertEquals(DECK_NAME, newDeck.getName());
            assertEquals(DECK_DESCRIPTION, newDeck.getDescription());
        }

        @Test
        @DisplayName("trims whitespace from name")
        void trimsWhitespaceFromName() {
            Deck newDeck = new Deck("  Berry Fruits  ");
            assertEquals("Berry Fruits", newDeck.getName());
        }

        @Test
        @DisplayName("trims whitespace from description")
        void trimsWhitespaceFromDescription() {
            Deck newDeck = new Deck(DECK_NAME, "  A collection of berries  ");
            assertEquals("A collection of berries", newDeck.getDescription());
        }

        @Test
        @DisplayName("converts empty description to empty string")
        void convertsEmptyDescriptionToEmptyString() {
            Deck newDeck = new Deck(DECK_NAME, "");
            assertEquals("", newDeck.getDescription());
        }

        @Test
        @DisplayName("converts whitespace-only description to empty string")
        void convertsWhitespaceOnlyDescriptionToEmptyString() {
            Deck newDeck = new Deck(DECK_NAME, "   ");
            assertEquals("", newDeck.getDescription());
        }

        @Test
        @DisplayName("throws exception when name is null")
        void throwsExceptionWhenNameIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Deck(null)
            );
            assertEquals("Deck name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("throws exception when name is empty")
        void throwsExceptionWhenNameIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> new Deck(""));
        }

        @Test
        @DisplayName("throws exception when name is whitespace only")
        void throwsExceptionWhenNameIsWhitespaceOnly() {
            assertThrows(IllegalArgumentException.class, () -> new Deck("   "));
        }

        @Test
        @DisplayName("initializes empty card collection")
        void initializesEmptyCardCollection() {
            Deck newDeck = new Deck(DECK_NAME);
            assertNotNull(newDeck.getCards());
            assertTrue(newDeck.getCards().isEmpty());
        }

        @Test
        @DisplayName("initializes empty tag collection")
        void initializesEmptyTagCollection() {
            Deck newDeck = new Deck(DECK_NAME);
            assertNotNull(newDeck.getTags());
            assertTrue(newDeck.getTags().isEmpty());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getId returns null before persistence")
        void getIdReturnsNullBeforePersistence() {
            assertNull(deck.getId());
        }

        @Test
        @DisplayName("getName returns deck name")
        void getNameReturnsDeckName() {
            assertEquals(DECK_NAME, deck.getName());
        }

        @Test
        @DisplayName("getDescription returns deck description")
        void getDescriptionReturnsDeckDescription() {
            assertEquals(DECK_DESCRIPTION, deck.getDescription());
        }

        @Test
        @DisplayName("getCards returns empty set by default")
        void getCardsReturnsEmptySetByDefault() {
            assertNotNull(deck.getCards());
            assertTrue(deck.getCards().isEmpty());
        }

        @Test
        @DisplayName("getTags returns empty set by default")
        void getTagsReturnsEmptySetByDefault() {
            assertNotNull(deck.getTags());
            assertTrue(deck.getTags().isEmpty());
        }

        @Test
        @DisplayName("getCardCount returns zero by default")
        void getCardCountReturnsZeroByDefault() {
            assertEquals(0, deck.getCardCount());
        }

        @Test
        @DisplayName("getCreatedAt returns null before persistence")
        void getCreatedAtReturnsNullBeforePersistence() {
            assertNull(deck.getCreatedAt());
        }

        @Test
        @DisplayName("getUpdatedAt returns null before persistence")
        void getUpdatedAtReturnsNullBeforePersistence() {
            assertNull(deck.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {

        @Nested
        @DisplayName("setName")
        class SetNameTests {

            @Test
            @DisplayName("updates deck name")
            void updatesDeckName() {
                deck.setName("Tropical Fruits");
                assertEquals("Tropical Fruits", deck.getName());
            }

            @Test
            @DisplayName("trims whitespace")
            void trimsWhitespace() {
                deck.setName("  Berry Fruits  ");
                assertEquals("Berry Fruits", deck.getName());
            }

            @Test
            @DisplayName("throws exception when null")
            void throwsExceptionWhenNull() {
                assertThrows(IllegalArgumentException.class, () -> deck.setName(null));
            }

            @Test
            @DisplayName("throws exception when empty")
            void throwsExceptionWhenEmpty() {
                assertThrows(IllegalArgumentException.class, () -> deck.setName(""));
            }

            @Test
            @DisplayName("throws exception when whitespace only")
            void throwsExceptionWhenWhitespaceOnly() {
                assertThrows(IllegalArgumentException.class, () -> deck.setName("   "));
            }
        }

        @Nested
        @DisplayName("setDescription")
        class SetDescriptionTests {

            @Test
            @DisplayName("updates description")
            void updatesDescription() {
                deck.setDescription("Updated description");
                assertEquals("Updated description", deck.getDescription());
            }

            @Test
            @DisplayName("trims whitespace")
            void trimsWhitespace() {
                deck.setDescription("  Trimmed description  ");
                assertEquals("Trimmed description", deck.getDescription());
            }

            @Test
            @DisplayName("allows null to clear description to empty string")
            void allowsNullToClearDescription() {
                deck.setDescription(null);
                assertEquals("", deck.getDescription());
            }

            @Test
            @DisplayName("converts empty to empty string")
            void convertsEmptyToEmptyString() {
                deck.setDescription("");
                assertEquals("", deck.getDescription());
            }

            @Test
            @DisplayName("converts whitespace-only to empty string")
            void convertsWhitespaceOnlyToEmptyString() {
                deck.setDescription("   ");
                assertEquals("", deck.getDescription());
            }
        }
    }

    @Nested
    @DisplayName("Card Management Tests")
    class CardManagementTests {

        private Card card;

        @BeforeEach
        void setUpCard() {
            card = new Card("Orange", "A round citrus fruit");
        }

        @Test
        @DisplayName("addCard adds card to deck")
        void addCardAddsCardToDeck() {
            deck.addCard(card);
            assertTrue(deck.getCards().contains(card));
            assertEquals(1, deck.getCardCount());
        }

        @Test
        @DisplayName("addCard throws exception when card is null")
        void addCardThrowsExceptionWhenCardIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> deck.addCard(null)
            );
            assertEquals("Card cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("removeCard removes card from deck")
        void removeCardRemovesCardFromDeck() {
            deck.addCard(card);
            deck.removeCard(card);
            assertFalse(deck.getCards().contains(card));
            assertEquals(0, deck.getCardCount());
        }

        @Test
        @DisplayName("removeCard throws exception when card is null")
        void removeCardThrowsExceptionWhenCardIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> deck.removeCard(null)
            );
            assertEquals("Card cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("getCardCount reflects added cards")
        void getCardCountReflectsAddedCards() {
            Card lemon = new Card("Lemon", "A sour citrus fruit");
            Card lime = new Card("Lime", "A green citrus fruit");

            deck.addCard(card);
            deck.addCard(lemon);
            deck.addCard(lime);

            assertEquals(3, deck.getCardCount());
        }

        @Test
        @DisplayName("can add multiple cards")
        void canAddMultipleCards() {
            Card lemon = new Card("Lemon", "A sour citrus fruit");
            Card lime = new Card("Lime", "A green citrus fruit");
            Card grapefruit = new Card("Grapefruit", "A bitter citrus fruit");

            deck.addCard(card);
            deck.addCard(lemon);
            deck.addCard(lime);
            deck.addCard(grapefruit);

            assertEquals(4, deck.getCards().size());
            assertTrue(deck.getCards().contains(card));
            assertTrue(deck.getCards().contains(lemon));
            assertTrue(deck.getCards().contains(lime));
            assertTrue(deck.getCards().contains(grapefruit));
        }

        @Test
        @DisplayName("removing non-existent card is handled gracefully")
        void removingNonExistentCardHandledGracefully() {
            Card nonExistent = new Card("Mango", "A tropical fruit");
            assertDoesNotThrow(() -> deck.removeCard(nonExistent));
        }
    }

    @Nested
    @DisplayName("Tag Management Tests")
    class TagManagementTests {

        private Tag tag;

        @BeforeEach
        void setUpTag() {
            tag = new Tag("sour");
        }

        @Test
        @DisplayName("addTag adds tag to deck")
        void addTagAddsTagToDeck() {
            deck.addTag(tag);
            assertTrue(deck.getTags().contains(tag));
        }

        @Test
        @DisplayName("addTag throws exception when tag is null")
        void addTagThrowsExceptionWhenTagIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> deck.addTag(null)
            );
            assertEquals("Tag cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("removeTag removes tag from deck")
        void removeTagRemovesTagFromDeck() {
            deck.addTag(tag);
            deck.removeTag(tag);
            assertFalse(deck.getTags().contains(tag));
        }

        @Test
        @DisplayName("removeTag throws exception when tag is null")
        void removeTagThrowsExceptionWhenTagIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> deck.removeTag(null)
            );
            assertEquals("Tag cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("removeTag also removes tag from all cards")
        void removeTagAlsoRemovesTagFromAllCards() {
            Card orange = new Card("Orange", "A round citrus fruit");
            Card lemon = new Card("Lemon", "A sour citrus fruit");

            deck.addCard(orange);
            deck.addCard(lemon);
            deck.addTag(tag);

            orange.addTag(tag);
            lemon.addTag(tag);

            assertTrue(orange.getTags().contains(tag));
            assertTrue(lemon.getTags().contains(tag));

            deck.removeTag(tag);

            assertFalse(orange.getTags().contains(tag));
            assertFalse(lemon.getTags().contains(tag));
            assertFalse(deck.getTags().contains(tag));
        }

        @Test
        @DisplayName("can add multiple tags")
        void canAddMultipleTags() {
            Tag sweet = new Tag("sweet");
            Tag bitter = new Tag("bitter");

            deck.addTag(tag);
            deck.addTag(sweet);
            deck.addTag(bitter);

            assertEquals(3, deck.getTags().size());
            assertTrue(deck.getTags().contains(tag));
            assertTrue(deck.getTags().contains(sweet));
            assertTrue(deck.getTags().contains(bitter));
        }
    }

    @Nested
    @DisplayName("createTag Tests")
    class CreateTagTests {

        @Test
        @DisplayName("creates and adds tag to deck")
        void createsAndAddsTagToDeck() {
            Tag created = deck.createTag("sour");
            assertNotNull(created);
            assertEquals("sour", created.getName());
            assertTrue(deck.getTags().contains(created));
        }

        @Test
        @DisplayName("trims tag name")
        void trimsTagName() {
            Tag created = deck.createTag("  sweet  ");
            assertEquals("sweet", created.getName());
        }

        @Test
        @DisplayName("throws exception when name is null")
        void throwsExceptionWhenNameIsNull() {
            assertThrows(IllegalArgumentException.class, () -> deck.createTag(null));
        }

        @Test
        @DisplayName("throws exception when name is empty")
        void throwsExceptionWhenNameIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> deck.createTag(""));
        }

        @Test
        @DisplayName("throws exception when tag already exists")
        void throwsExceptionWhenTagAlreadyExists() {
            deck.createTag("sour");
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> deck.createTag("sour")
            );
            assertTrue(exception.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("throws exception when tag exists with different case")
        void throwsExceptionWhenTagExistsWithDifferentCase() {
            deck.createTag("Sour");
            assertThrows(IllegalArgumentException.class, () -> deck.createTag("sour"));
            assertThrows(IllegalArgumentException.class, () -> deck.createTag("SOUR"));
        }
    }

    @Nested
    @DisplayName("findTagByName Tests")
    class FindTagByNameTests {

        @Test
        @DisplayName("finds existing tag by name")
        void findsExistingTagByName() {
            Tag created = deck.createTag("sour");
            Tag found = deck.findTagByName("sour");
            assertEquals(created, found);
        }

        @Test
        @DisplayName("finds tag case-insensitively")
        void findsTagCaseInsensitively() {
            Tag created = deck.createTag("Sour");
            assertEquals(created, deck.findTagByName("sour"));
            assertEquals(created, deck.findTagByName("SOUR"));
            assertEquals(created, deck.findTagByName("Sour"));
        }

        @Test
        @DisplayName("returns null when tag not found")
        void returnsNullWhenTagNotFound() {
            assertNull(deck.findTagByName("nonexistent"));
        }

        @Test
        @DisplayName("returns null when name is null")
        void returnsNullWhenNameIsNull() {
            assertNull(deck.findTagByName(null));
        }

        @Test
        @DisplayName("trims search name")
        void trimsSearchName() {
            Tag created = deck.createTag("sour");
            assertEquals(created, deck.findTagByName("  sour  "));
        }
    }

    @Nested
    @DisplayName("Tag Usage Tests")
    class TagUsageTests {

        private Card orange;
        private Card lemon;
        private Card lime;
        private Tag sour;
        private Tag sweet;
        private Tag unused;

        @BeforeEach
        void setUpCardsAndTags() {
            orange = new Card("Orange", "A round citrus fruit");
            lemon = new Card("Lemon", "A sour citrus fruit");
            lime = new Card("Lime", "A green citrus fruit");

            sour = new Tag("sour");
            sweet = new Tag("sweet");
            unused = new Tag("unused");

            deck.addCard(orange);
            deck.addCard(lemon);
            deck.addCard(lime);

            deck.addTag(sour);
            deck.addTag(sweet);
            deck.addTag(unused);

            lemon.addTag(sour);
            lime.addTag(sour);
            orange.addTag(sweet);
        }

        @Test
        @DisplayName("getTagsInUse returns only tags applied to cards")
        void getTagsInUseReturnsOnlyTagsAppliedToCards() {
            Set<Tag> inUse = deck.getTagsInUse();
            assertEquals(2, inUse.size());
            assertTrue(inUse.contains(sour));
            assertTrue(inUse.contains(sweet));
            assertFalse(inUse.contains(unused));
        }

        @Test
        @DisplayName("getUnusedTags returns tags not applied to any card")
        void getUnusedTagsReturnsTagsNotAppliedToAnyCard() {
            Set<Tag> unusedTags = deck.getUnusedTags();
            assertEquals(1, unusedTags.size());
            assertTrue(unusedTags.contains(unused));
        }

        @Test
        @DisplayName("isTagInUse returns true for used tag")
        void isTagInUseReturnsTrueForUsedTag() {
            assertTrue(deck.isTagInUse(sour));
            assertTrue(deck.isTagInUse(sweet));
        }

        @Test
        @DisplayName("isTagInUse returns false for unused tag")
        void isTagInUseReturnsFalseForUnusedTag() {
            assertFalse(deck.isTagInUse(unused));
        }

        @Test
        @DisplayName("isTagInUse returns false for null")
        void isTagInUseReturnsFalseForNull() {
            assertFalse(deck.isTagInUse(null));
        }

        @Test
        @DisplayName("isTagInUse returns false for tag not in deck")
        void isTagInUseReturnsFalseForTagNotInDeck() {
            Tag external = new Tag("external");
            assertFalse(deck.isTagInUse(external));
        }

        @Test
        @DisplayName("removeUnusedTags removes only unused tags")
        void removeUnusedTagsRemovesOnlyUnusedTags() {
            Set<Tag> removed = deck.removeUnusedTags();

            assertEquals(1, removed.size());
            assertTrue(removed.contains(unused));
            assertFalse(deck.getTags().contains(unused));
            assertTrue(deck.getTags().contains(sour));
            assertTrue(deck.getTags().contains(sweet));
        }

        @Test
        @DisplayName("removeUnusedTags returns empty set when all tags used")
        void removeUnusedTagsReturnsEmptySetWhenAllTagsUsed() {
            orange.addTag(unused);

            Set<Tag> removed = deck.removeUnusedTags();
            assertTrue(removed.isEmpty());
            assertEquals(3, deck.getTags().size());
        }

        @Test
        @DisplayName("tag becomes unused when removed from last card")
        void tagBecomesUnusedWhenRemovedFromLastCard() {
            assertTrue(deck.isTagInUse(sweet));

            orange.removeTag(sweet);

            assertFalse(deck.isTagInUse(sweet));
            assertTrue(deck.getUnusedTags().contains(sweet));
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("onCreate sets createdAt and updatedAt")
        void onCreateSetsTimestamps() {
            deck.onCreate();

            assertNotNull(deck.getCreatedAt());
            assertNotNull(deck.getUpdatedAt());
            assertEquals(deck.getCreatedAt(), deck.getUpdatedAt());
        }

        @Test
        @DisplayName("onUpdate updates updatedAt timestamp")
        void onUpdateUpdatesTimestamp() throws InterruptedException {
            deck.onCreate();
            var createdAt = deck.getCreatedAt();
            var initialUpdatedAt = deck.getUpdatedAt();

            Thread.sleep(10);
            deck.onUpdate();

            assertEquals(createdAt, deck.getCreatedAt());
            assertTrue(deck.getUpdatedAt().isAfter(initialUpdatedAt));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("deck equals itself")
        void deckEqualsItself() {
            assertEquals(deck, deck);
        }

        @Test
        @DisplayName("deck does not equal null")
        void deckDoesNotEqualNull() {
            assertNotEquals(null, deck);
        }

        @Test
        @DisplayName("deck does not equal different type")
        void deckDoesNotEqualDifferentType() {
            assertNotEquals("not a deck", deck);
        }

        @Test
        @DisplayName("decks without IDs are only equal to themselves")
        void decksWithoutIdsAreOnlyEqualToThemselves() {
            Deck deck2 = new Deck(DECK_NAME, DECK_DESCRIPTION);
            assertNotEquals(deck, deck2);
        }

        @Test
        @DisplayName("hashCode is consistent")
        void hashCodeIsConsistent() {
            int hash1 = deck.hashCode();
            int hash2 = deck.hashCode();
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("decks without ID use identity hashCode")
        void decksWithoutIdUseIdentityHashCode() {
            Deck deck2 = new Deck(DECK_NAME, DECK_DESCRIPTION);
            assertNotEquals(deck.hashCode(), deck2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString includes name")
        void toStringIncludesName() {
            String result = deck.toString();
            assertTrue(result.contains("name='" + DECK_NAME + "'"));
        }

        @Test
        @DisplayName("toString includes description")
        void toStringIncludesDescription() {
            String result = deck.toString();
            assertTrue(result.contains("description='" + DECK_DESCRIPTION + "'"));
        }

        @Test
        @DisplayName("toString includes card count")
        void toStringIncludesCardCount() {
            String result = deck.toString();
            assertTrue(result.contains("cardCount=0"));
        }

        @Test
        @DisplayName("toString includes tag count")
        void toStringIncludesTagCount() {
            String result = deck.toString();
            assertTrue(result.contains("tagCount=0"));
        }

        @Test
        @DisplayName("toString reflects card count changes")
        void toStringReflectsCardCountChanges() {
            deck.addCard(new Card("Orange", "A citrus fruit"));
            deck.addCard(new Card("Lemon", "A sour fruit"));

            String result = deck.toString();
            assertTrue(result.contains("cardCount=2"));
        }

        @Test
        @DisplayName("toString reflects tag count changes")
        void toStringReflectsTagCountChanges() {
            deck.addTag(new Tag("sour"));
            deck.addTag(new Tag("sweet"));
            deck.addTag(new Tag("bitter"));

            String result = deck.toString();
            assertTrue(result.contains("tagCount=3"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("allows single character name")
        void allowsSingleCharacterName() {
            Deck singleCharDeck = new Deck("A");
            assertEquals("A", singleCharDeck.getName());
        }

        @Test
        @DisplayName("allows name with special characters")
        void allowsNameWithSpecialCharacters() {
            Deck specialDeck = new Deck("Stone-Fruit Collection");
            assertEquals("Stone-Fruit Collection", specialDeck.getName());
        }

        @Test
        @DisplayName("allows unicode characters in name")
        void allowsUnicodeCharactersInName() {
            Deck unicodeDeck = new Deck("フルーツコレクション");
            assertEquals("フルーツコレクション", unicodeDeck.getName());
        }

        @Test
        @DisplayName("allows very long name within limit")
        void allowsVeryLongNameWithinLimit() {
            String longName = "a".repeat(255);
            Deck longDeck = new Deck(longName);
            assertEquals(longName, longDeck.getName());
        }

        @Test
        @DisplayName("getTagsInUse returns empty set when no cards have tags")
        void getTagsInUseReturnsEmptySetWhenNoCardsHaveTags() {
            deck.addCard(new Card("Orange", "A fruit"));
            deck.addTag(new Tag("unused"));

            assertTrue(deck.getTagsInUse().isEmpty());
        }

        @Test
        @DisplayName("getUnusedTags returns all tags when no cards have tags")
        void getUnusedTagsReturnsAllTagsWhenNoCardsHaveTags() {
            deck.addTag(new Tag("tag1"));
            deck.addTag(new Tag("tag2"));

            assertEquals(2, deck.getUnusedTags().size());
        }

        @Test
        @DisplayName("getTagsInUse returns empty set when deck has no cards")
        void getTagsInUseReturnsEmptySetWhenDeckHasNoCards() {
            assertTrue(deck.getTagsInUse().isEmpty());
        }

        @Test
        @DisplayName("removeUnusedTags returns empty set when no tags exist")
        void removeUnusedTagsReturnsEmptySetWhenNoTagsExist() {
            assertTrue(deck.removeUnusedTags().isEmpty());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("complete deck lifecycle with cards and tags")
        void completeDeckLifecycleWithCardsAndTags() {
            // Create deck
            Deck fruitDeck = new Deck("My Fruits", "A personal fruit collection");

            // Add cards
            Card apple = new Card("Apple", "A red or green fruit");
            Card banana = new Card("Banana", "A yellow curved fruit");
            Card cherry = new Card("Cherry", "A small red stone fruit");

            fruitDeck.addCard(apple);
            fruitDeck.addCard(banana);
            fruitDeck.addCard(cherry);

            assertEquals(3, fruitDeck.getCardCount());

            // Create and apply tags
            Tag red = fruitDeck.createTag("red");
            Tag sweet = fruitDeck.createTag("sweet");
            Tag favorite = fruitDeck.createTag("favorite");

            apple.addTag(red);
            apple.addTag(sweet);
            cherry.addTag(red);
            banana.addTag(sweet);
            apple.addTag(favorite);

            // Verify tag usage
            assertEquals(3, fruitDeck.getTagsInUse().size());
            assertTrue(fruitDeck.getUnusedTags().isEmpty());

            // Remove a card
            fruitDeck.removeCard(apple);
            assertEquals(2, fruitDeck.getCardCount());

            // favorite is now unused
            assertFalse(fruitDeck.isTagInUse(favorite));
            assertEquals(1, fruitDeck.getUnusedTags().size());

            // Clean up unused tags
            Set<Tag> removed = fruitDeck.removeUnusedTags();
            assertEquals(1, removed.size());
            assertTrue(removed.contains(favorite));
            assertEquals(2, fruitDeck.getTags().size());
        }

        @Test
        @DisplayName("tag shared across many cards")
        void tagSharedAcrossManyCards() {
            Tag commonTag = deck.createTag("common");

            for (int i = 0; i < 10; i++) {
                Card card = new Card("Fruit" + i, "Description" + i);
                deck.addCard(card);
                card.addTag(commonTag);
            }

            assertEquals(10, deck.getCardCount());
            assertTrue(deck.isTagInUse(commonTag));
            assertEquals(10, commonTag.getCardCount());
        }

        @Test
        @DisplayName("multiple decks are independent")
        void multipleDecksAreIndependent() {
            Deck deck1 = new Deck("Citrus");
            Deck deck2 = new Deck("Tropical");

            Card orange = new Card("Orange", "Citrus fruit");
            Card mango = new Card("Mango", "Tropical fruit");

            Tag sour = new Tag("sour");
            Tag sweet = new Tag("sweet");

            deck1.addCard(orange);
            deck1.addTag(sour);
            orange.addTag(sour);

            deck2.addCard(mango);
            deck2.addTag(sweet);
            mango.addTag(sweet);

            // Verify independence
            assertEquals(1, deck1.getCardCount());
            assertEquals(1, deck2.getCardCount());
            assertEquals(1, deck1.getTags().size());
            assertEquals(1, deck2.getTags().size());

            assertTrue(deck1.isTagInUse(sour));
            assertFalse(deck1.isTagInUse(sweet));
            assertTrue(deck2.isTagInUse(sweet));
            assertFalse(deck2.isTagInUse(sour));
        }
    }
}
