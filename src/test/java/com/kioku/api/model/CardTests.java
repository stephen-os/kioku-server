package com.kioku.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Card entity.
 */
@DisplayName("Card Entity Tests")
class CardTests {

    private static final String FRONT = "Apple";
    private static final String BACK = "Red";
    private static final String NOTES = "A fruit that goes well with peanut butter.";

    private Card card;

    @BeforeEach
    void setUp() {
        card = new Card(FRONT, BACK);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("creates card with valid front and back")
        void createsCardWithValidFrontAndBack() {
            Card newCard = new Card(FRONT, BACK);
            assertNotNull(newCard);
            assertEquals(FRONT, newCard.getFront());
            assertEquals(BACK, newCard.getBack());
        }

        @Test
        @DisplayName("trims whitespace from front and back")
        void trimsWhitespaceFromFrontAndBack() {
            Card newCard = new Card("  Apple  ", "  Red  ");
            assertEquals("Apple", newCard.getFront());
            assertEquals("Red", newCard.getBack());
        }

        @Test
        @DisplayName("throws exception when front is null")
        void throwsExceptionWhenFrontIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Card(null, BACK)
            );
            assertEquals("Front text cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("throws exception when front is empty")
        void throwsExceptionWhenFrontIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> new Card("", BACK));
        }

        @Test
        @DisplayName("throws exception when front is whitespace only")
        void throwsExceptionWhenFrontIsWhitespaceOnly() {
            assertThrows(IllegalArgumentException.class, () -> new Card("   ", BACK));
        }

        @Test
        @DisplayName("throws exception when back is null")
        void throwsExceptionWhenBackIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Card(FRONT, null)
            );
            assertEquals("Back text cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("throws exception when back is empty")
        void throwsExceptionWhenBackIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> new Card(FRONT, ""));
        }

        @Test
        @DisplayName("throws exception when back is whitespace only")
        void throwsExceptionWhenBackIsWhitespaceOnly() {
            assertThrows(IllegalArgumentException.class, () -> new Card(FRONT, "   "));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getCardId returns null before persistence")
        void getCardIdReturnsNullBeforePersistence() {
            assertNull(card.getCardId());
        }

        @Test
        @DisplayName("getFront returns front text")
        void getFrontReturnsFrontText() {
            assertEquals(FRONT, card.getFront());
        }

        @Test
        @DisplayName("getBack returns back text")
        void getBackReturnsBackText() {
            assertEquals(BACK, card.getBack());
        }

        @Test
        @DisplayName("getNotes returns null by default")
        void getNotesReturnsNullByDefault() {
            assertNull(card.getNotes());
        }

        @Test
        @DisplayName("getTags returns empty set by default")
        void getTagsReturnsEmptySetByDefault() {
            assertNotNull(card.getTags());
            assertTrue(card.getTags().isEmpty());
        }

        @Test
        @DisplayName("getCreatedAt returns null before persistence")
        void getCreatedAtReturnsNullBeforePersistence() {
            assertNull(card.getCreatedAt());
        }

        @Test
        @DisplayName("getUpdatedAt returns null before persistence")
        void getUpdatedAtReturnsNullBeforePersistence() {
            assertNull(card.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {

        @Nested
        @DisplayName("setFront")
        class SetFrontTests {

            @Test
            @DisplayName("updates front text")
            void updatesFrontText() {
                card.setFront("Banana");
                assertEquals("Banana", card.getFront());
            }

            @Test
            @DisplayName("trims whitespace")
            void trimsWhitespace() {
                card.setFront("  Banana  ");
                assertEquals("Banana", card.getFront());
            }

            @Test
            @DisplayName("throws exception when null")
            void throwsExceptionWhenNull() {
                assertThrows(IllegalArgumentException.class, () -> card.setFront(null));
            }

            @Test
            @DisplayName("throws exception when empty")
            void throwsExceptionWhenEmpty() {
                assertThrows(IllegalArgumentException.class, () -> card.setFront(""));
            }

            @Test
            @DisplayName("throws exception when whitespace only")
            void throwsExceptionWhenWhitespaceOnly() {
                assertThrows(IllegalArgumentException.class, () -> card.setFront("   "));
            }
        }

        @Nested
        @DisplayName("setBack")
        class SetBackTests {

            @Test
            @DisplayName("updates back text")
            void updatesBackText() {
                card.setBack("Yellow");
                assertEquals("Yellow", card.getBack());
            }

            @Test
            @DisplayName("trims whitespace")
            void trimsWhitespace() {
                card.setBack("  Yellow  ");
                assertEquals("Yellow", card.getBack());
            }

            @Test
            @DisplayName("throws exception when null")
            void throwsExceptionWhenNull() {
                assertThrows(IllegalArgumentException.class, () -> card.setBack(null));
            }

            @Test
            @DisplayName("throws exception when empty")
            void throwsExceptionWhenEmpty() {
                assertThrows(IllegalArgumentException.class, () -> card.setBack(""));
            }

            @Test
            @DisplayName("throws exception when whitespace only")
            void throwsExceptionWhenWhitespaceOnly() {
                assertThrows(IllegalArgumentException.class, () -> card.setBack("   "));
            }
        }

        @Nested
        @DisplayName("setNotes")
        class SetNotesTests {

            @Test
            @DisplayName("sets notes")
            void setsNotes() {
                card.setNotes(NOTES);
                assertEquals(NOTES, card.getNotes());
            }

            @Test
            @DisplayName("allows null to clear notes")
            void allowsNullToClearNotes() {
                card.setNotes(NOTES);
                card.setNotes(null);
                assertNull(card.getNotes());
            }

            @Test
            @DisplayName("allows empty string")
            void allowsEmptyString() {
                card.setNotes("");
                assertEquals("", card.getNotes());
            }
        }
    }

    @Nested
    @DisplayName("Tag Management Tests")
    class TagManagementTests {

        private Tag tag;

        @BeforeEach
        void setUpTag() {
            tag = new Tag("fruit");
        }

        @Nested
        @DisplayName("addTag")
        class AddTagTests {

            @Test
            @DisplayName("adds tag to card")
            void addsTagToCard() {
                card.addTag(tag);
                assertTrue(card.getTags().contains(tag));
            }

            @Test
            @DisplayName("maintains bidirectional relationship")
            void maintainsBidirectionalRelationship() {
                card.addTag(tag);
                assertTrue(tag.getCards().contains(card));
            }

            @Test
            @DisplayName("throws exception when tag is null")
            void throwsExceptionWhenTagIsNull() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> card.addTag(null)
                );
                assertEquals("Tag cannot be null", exception.getMessage());
            }

            @Test
            @DisplayName("can add multiple tags")
            void canAddMultipleTags() {
                Tag tag2 = new Tag("favorite");
                card.addTag(tag);
                card.addTag(tag2);
                assertEquals(2, card.getTags().size());
                assertTrue(card.getTags().contains(tag));
                assertTrue(card.getTags().contains(tag2));
            }
        }

        @Nested
        @DisplayName("removeTag")
        class RemoveTagTests {

            @Test
            @DisplayName("removes tag from card")
            void removesTagFromCard() {
                card.addTag(tag);
                card.removeTag(tag);
                assertFalse(card.getTags().contains(tag));
            }

            @Test
            @DisplayName("maintains bidirectional relationship on removal")
            void maintainsBidirectionalRelationshipOnRemoval() {
                card.addTag(tag);
                card.removeTag(tag);
                assertFalse(tag.getCards().contains(card));
            }

            @Test
            @DisplayName("throws exception when tag is null")
            void throwsExceptionWhenTagIsNull() {
                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> card.removeTag(null)
                );
                assertEquals("Tag cannot be null", exception.getMessage());
            }

            @Test
            @DisplayName("handles removing non-existent tag gracefully")
            void handlesRemovingNonExistentTagGracefully() {
                assertDoesNotThrow(() -> card.removeTag(tag));
            }
        }

        @Nested
        @DisplayName("clearTags")
        class ClearTagsTests {

            @Test
            @DisplayName("removes all tags from card")
            void removesAllTagsFromCard() {
                Tag tag2 = new Tag("favorite");
                card.addTag(tag);
                card.addTag(tag2);

                card.clearTags();

                assertTrue(card.getTags().isEmpty());
            }

            @Test
            @DisplayName("removes card from all tag collections")
            void removesCardFromAllTagCollections() {
                Tag tag2 = new Tag("favorite");
                card.addTag(tag);
                card.addTag(tag2);

                card.clearTags();

                assertFalse(tag.getCards().contains(card));
                assertFalse(tag2.getCards().contains(card));
            }

            @Test
            @DisplayName("handles clearing when no tags present")
            void handlesClearingWhenNoTagsPresent() {
                assertDoesNotThrow(() -> card.clearTags());
                assertTrue(card.getTags().isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("onCreate sets createdAt and updatedAt")
        void onCreateSetsTimestamps() {
            card.onCreate();

            assertNotNull(card.getCreatedAt());
            assertNotNull(card.getUpdatedAt());
            assertEquals(card.getCreatedAt(), card.getUpdatedAt());
        }

        @Test
        @DisplayName("onUpdate updates updatedAt timestamp")
        void onUpdateUpdatesTimestamp() throws InterruptedException {
            card.onCreate();
            var createdAt = card.getCreatedAt();
            var initialUpdatedAt = card.getUpdatedAt();

            Thread.sleep(10);
            card.onUpdate();

            assertEquals(createdAt, card.getCreatedAt());
            assertTrue(card.getUpdatedAt().isAfter(initialUpdatedAt));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("card equals itself")
        void cardEqualsItself() {
            assertEquals(card, card);
        }

        @Test
        @DisplayName("card does not equal null")
        void cardDoesNotEqualNull() {
            assertNotEquals(null, card);
        }

        @Test
        @DisplayName("card does not equal different type")
        void cardDoesNotEqualDifferentType() {
            assertNotEquals("not a card", card);
        }

        @Test
        @DisplayName("cards without IDs are only equal to themselves")
        void cardsWithoutIdsAreOnlyEqualToThemselves() {
            Card card2 = new Card(FRONT, BACK);
            assertNotEquals(card, card2);
        }

        @Test
        @DisplayName("cards with different content but no ID are not equal")
        void cardsWithDifferentContentButNoIdAreNotEqual() {
            Card card2 = new Card("Banana", "Yellow");
            assertNotEquals(card, card2);
        }

        @Test
        @DisplayName("hashCode is consistent")
        void hashCodeIsConsistent() {
            int hash1 = card.hashCode();
            int hash2 = card.hashCode();
            assertEquals(hash1, hash2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString includes front and back")
        void toStringIncludesFrontAndBack() {
            String result = card.toString();
            assertTrue(result.contains("front='" + FRONT + "'"));
            assertTrue(result.contains("back='" + BACK + "'"));
        }

        @Test
        @DisplayName("toString includes notes when set")
        void toStringIncludesNotesWhenSet() {
            card.setNotes(NOTES);
            String result = card.toString();
            assertTrue(result.contains("notes='" + NOTES + "'"));
        }

        @Test
        @DisplayName("toString includes tag count")
        void toStringIncludesTagCount() {
            String result = card.toString();
            assertTrue(result.contains("tagCount=0"));
        }

        @Test
        @DisplayName("toString includes timestamps")
        void toStringIncludesTimestamps() {
            card.onCreate();
            String result = card.toString();
            assertTrue(result.contains("createdAt="));
            assertTrue(result.contains("updatedAt="));
        }
    }
}
