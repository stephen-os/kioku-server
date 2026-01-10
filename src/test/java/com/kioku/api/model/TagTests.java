package com.kioku.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Tag entity.
 *
 * Uses a fruit theme: tags represent fruit categories like "citrus", "tropical", "berry".
 *
 * @author Stephen Watson
 */
@DisplayName("Tag Entity Tests")
class TagTests {

    private static final String TAG_NAME = "citrus";

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = new Tag(TAG_NAME);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("creates tag with valid name")
        void createsTagWithValidName() {
            Tag newTag = new Tag("tropical");
            assertNotNull(newTag);
            assertEquals("tropical", newTag.getName());
        }

        @Test
        @DisplayName("trims whitespace from name")
        void trimsWhitespaceFromName() {
            Tag newTag = new Tag("  berry  ");
            assertEquals("berry", newTag.getName());
        }

        @Test
        @DisplayName("throws exception when name is null")
        void throwsExceptionWhenNameIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Tag(null)
            );
            assertEquals("Tag name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("throws exception when name is empty")
        void throwsExceptionWhenNameIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> new Tag(""));
        }

        @Test
        @DisplayName("throws exception when name is whitespace only")
        void throwsExceptionWhenNameIsWhitespaceOnly() {
            assertThrows(IllegalArgumentException.class, () -> new Tag("   "));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getId returns null before persistence")
        void getIdReturnsNullBeforePersistence() {
            assertNull(tag.getId());
        }

        @Test
        @DisplayName("getName returns tag name")
        void getNameReturnsTagName() {
            assertEquals(TAG_NAME, tag.getName());
        }

        @Test
        @DisplayName("getCards returns empty set by default")
        void getCardsReturnsEmptySetByDefault() {
            assertNotNull(tag.getCards());
            assertTrue(tag.getCards().isEmpty());
        }

        @Test
        @DisplayName("getCardCount returns zero by default")
        void getCardCountReturnsZeroByDefault() {
            assertEquals(0, tag.getCardCount());
        }
    }

    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {

        @Nested
        @DisplayName("setName")
        class SetNameTests {

            @Test
            @DisplayName("updates tag name")
            void updatesTagName() {
                tag.setName("tropical");
                assertEquals("tropical", tag.getName());
            }

            @Test
            @DisplayName("trims whitespace")
            void trimsWhitespace() {
                tag.setName("  berry  ");
                assertEquals("berry", tag.getName());
            }

            @Test
            @DisplayName("throws exception when null")
            void throwsExceptionWhenNull() {
                assertThrows(IllegalArgumentException.class, () -> tag.setName(null));
            }

            @Test
            @DisplayName("throws exception when empty")
            void throwsExceptionWhenEmpty() {
                assertThrows(IllegalArgumentException.class, () -> tag.setName(""));
            }

            @Test
            @DisplayName("throws exception when whitespace only")
            void throwsExceptionWhenWhitespaceOnly() {
                assertThrows(IllegalArgumentException.class, () -> tag.setName("   "));
            }
        }
    }

    @Nested
    @DisplayName("Card Association Tests")
    class CardAssociationTests {

        private Card card;

        @BeforeEach
        void setUpCard() {
            card = new Card("Orange", "A citrus fruit");
        }

        @Test
        @DisplayName("getCardCount reflects added cards")
        void getCardCountReflectsAddedCards() {
            card.addTag(tag);
            assertEquals(1, tag.getCardCount());
        }

        @Test
        @DisplayName("getCardCount reflects multiple cards")
        void getCardCountReflectsMultipleCards() {
            Card card2 = new Card("Lemon", "A sour citrus fruit");
            Card card3 = new Card("Lime", "A green citrus fruit");

            card.addTag(tag);
            card2.addTag(tag);
            card3.addTag(tag);

            assertEquals(3, tag.getCardCount());
        }

        @Test
        @DisplayName("getCardCount decreases when card removes tag")
        void getCardCountDecreasesWhenCardRemovesTag() {
            card.addTag(tag);
            assertEquals(1, tag.getCardCount());

            card.removeTag(tag);
            assertEquals(0, tag.getCardCount());
        }

        @Test
        @DisplayName("getCards contains card after addTag")
        void getCardsContainsCardAfterAddTag() {
            card.addTag(tag);
            assertTrue(tag.getCards().contains(card));
        }

        @Test
        @DisplayName("getCards does not contain card after removeTag")
        void getCardsDoesNotContainCardAfterRemoveTag() {
            card.addTag(tag);
            card.removeTag(tag);
            assertFalse(tag.getCards().contains(card));
        }

        @Test
        @DisplayName("bidirectional relationship maintained on add")
        void bidirectionalRelationshipMaintainedOnAdd() {
            card.addTag(tag);
            assertTrue(card.getTags().contains(tag));
            assertTrue(tag.getCards().contains(card));
        }

        @Test
        @DisplayName("bidirectional relationship maintained on remove")
        void bidirectionalRelationshipMaintainedOnRemove() {
            card.addTag(tag);
            card.removeTag(tag);
            assertFalse(card.getTags().contains(tag));
            assertFalse(tag.getCards().contains(card));
        }
    }

    @Nested
    @DisplayName("isInUse Tests")
    class IsInUseTests {

        private Card card;

        @BeforeEach
        void setUpCard() {
            card = new Card("Mango", "A tropical fruit");
        }

        @Test
        @DisplayName("returns false when no cards have tag")
        void returnsFalseWhenNoCardsHaveTag() {
            assertFalse(tag.isInUse());
        }

        @Test
        @DisplayName("returns true when at least one card has tag")
        void returnsTrueWhenAtLeastOneCardHasTag() {
            card.addTag(tag);
            assertTrue(tag.isInUse());
        }

        @Test
        @DisplayName("returns false after all cards remove tag")
        void returnsFalseAfterAllCardsRemoveTag() {
            Card card2 = new Card("Pineapple", "A spiky tropical fruit");

            card.addTag(tag);
            card2.addTag(tag);
            assertTrue(tag.isInUse());

            card.removeTag(tag);
            assertTrue(tag.isInUse());

            card2.removeTag(tag);
            assertFalse(tag.isInUse());
        }

        @Test
        @DisplayName("returns true with many cards")
        void returnsTrueWithManyCards() {
            for (int i = 0; i < 10; i++) {
                Card c = new Card("Fruit" + i, "Description" + i);
                c.addTag(tag);
            }
            assertTrue(tag.isInUse());
            assertEquals(10, tag.getCardCount());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("tag equals itself")
        void tagEqualsItself() {
            assertEquals(tag, tag);
        }

        @Test
        @DisplayName("tag does not equal null")
        void tagDoesNotEqualNull() {
            assertNotEquals(null, tag);
        }

        @Test
        @DisplayName("tag does not equal different type")
        void tagDoesNotEqualDifferentType() {
            assertNotEquals("not a tag", tag);
        }

        @Test
        @DisplayName("tags without IDs are only equal to themselves")
        void tagsWithoutIdsAreOnlyEqualToThemselves() {
            Tag tag2 = new Tag(TAG_NAME);
            assertNotEquals(tag, tag2);
        }

        @Test
        @DisplayName("tags with same name but no ID are not equal")
        void tagsWithSameNameButNoIdAreNotEqual() {
            Tag tag2 = new Tag("citrus");
            assertNotEquals(tag, tag2);
        }

        @Test
        @DisplayName("hashCode is consistent")
        void hashCodeIsConsistent() {
            int hash1 = tag.hashCode();
            int hash2 = tag.hashCode();
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("tags without ID use identity hashCode")
        void tagsWithoutIdUseIdentityHashCode() {
            Tag tag2 = new Tag("citrus");
            assertNotEquals(tag.hashCode(), tag2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString includes name")
        void toStringIncludesName() {
            String result = tag.toString();
            assertTrue(result.contains("name='" + TAG_NAME + "'"));
        }

        @Test
        @DisplayName("toString includes card count")
        void toStringIncludesCardCount() {
            String result = tag.toString();
            assertTrue(result.contains("cardCount=0"));
        }

        @Test
        @DisplayName("toString reflects card count changes")
        void toStringReflectsCardCountChanges() {
            Card card = new Card("Grapefruit", "A bitter citrus fruit");
            card.addTag(tag);

            String result = tag.toString();
            assertTrue(result.contains("cardCount=1"));
        }

        @Test
        @DisplayName("toString includes id as null before persistence")
        void toStringIncludesIdAsNullBeforePersistence() {
            String result = tag.toString();
            assertTrue(result.contains("id=null"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("allows single character name")
        void allowsSingleCharacterName() {
            Tag singleCharTag = new Tag("A");
            assertEquals("A", singleCharTag.getName());
        }

        @Test
        @DisplayName("allows name with special characters")
        void allowsNameWithSpecialCharacters() {
            Tag specialTag = new Tag("stone-fruit");
            assertEquals("stone-fruit", specialTag.getName());
        }

        @Test
        @DisplayName("allows name with numbers")
        void allowsNameWithNumbers() {
            Tag numberedTag = new Tag("top10");
            assertEquals("top10", numberedTag.getName());
        }

        @Test
        @DisplayName("allows name with unicode characters")
        void allowsNameWithUnicodeCharacters() {
            Tag unicodeTag = new Tag("フルーツ");
            assertEquals("フルーツ", unicodeTag.getName());
        }

        @Test
        @DisplayName("allows very long name within limit")
        void allowsVeryLongNameWithinLimit() {
            String longName = "a".repeat(100);
            Tag longTag = new Tag(longName);
            assertEquals(longName, longTag.getName());
        }

        @Test
        @DisplayName("allows updating name multiple times")
        void allowsUpdatingNameMultipleTimes() {
            tag.setName("tropical");
            assertEquals("tropical", tag.getName());
            tag.setName("berry");
            assertEquals("berry", tag.getName());
            tag.setName("stone-fruit");
            assertEquals("stone-fruit", tag.getName());
        }

        @Test
        @DisplayName("card can have same tag added twice without duplicates")
        void cardCanHaveSameTagAddedTwiceWithoutDuplicates() {
            Card card = new Card("Apple", "A common fruit");
            card.addTag(tag);
            card.addTag(tag);
            assertEquals(1, card.getTags().size());
            assertEquals(1, tag.getCardCount());
        }
    }

    @Nested
    @DisplayName("Multiple Tags Tests")
    class MultipleTagsTests {

        @Test
        @DisplayName("card can have multiple different tags")
        void cardCanHaveMultipleDifferentTags() {
            Tag tropical = new Tag("tropical");
            Tag favorite = new Tag("favorite");
            Card card = new Card("Mango", "A sweet tropical fruit");

            card.addTag(tag);
            card.addTag(tropical);
            card.addTag(favorite);

            assertEquals(3, card.getTags().size());
            assertTrue(card.getTags().contains(tag));
            assertTrue(card.getTags().contains(tropical));
            assertTrue(card.getTags().contains(favorite));
        }

        @Test
        @DisplayName("multiple cards can share same tag")
        void multipleCardsCanShareSameTag() {
            Card orange = new Card("Orange", "A citrus fruit");
            Card lemon = new Card("Lemon", "A sour citrus fruit");
            Card lime = new Card("Lime", "A green citrus fruit");

            orange.addTag(tag);
            lemon.addTag(tag);
            lime.addTag(tag);

            assertEquals(3, tag.getCardCount());
            assertTrue(tag.getCards().contains(orange));
            assertTrue(tag.getCards().contains(lemon));
            assertTrue(tag.getCards().contains(lime));
        }

        @Test
        @DisplayName("removing tag from one card does not affect others")
        void removingTagFromOneCardDoesNotAffectOthers() {
            Card orange = new Card("Orange", "A citrus fruit");
            Card lemon = new Card("Lemon", "A sour citrus fruit");

            orange.addTag(tag);
            lemon.addTag(tag);

            orange.removeTag(tag);

            assertFalse(orange.getTags().contains(tag));
            assertTrue(lemon.getTags().contains(tag));
            assertEquals(1, tag.getCardCount());
        }
    }
}
