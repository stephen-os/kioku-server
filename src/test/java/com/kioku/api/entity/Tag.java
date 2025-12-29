package com.kioku.api.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    @Test
    void testTagCreation() {
        User user = new User("user@example.com", "hashedPassword");
        Tag tag = new Tag(user, "verbs");

        assertEquals("verbs", tag.getName());
        assertEquals(user, tag.getUser());
        assertNull(tag.getId());
        assertTrue(tag.getCards().isEmpty());
    }

    @Test
    void testTagEquality() {
        User user = new User("user@example.com", "hash");

        Tag tag1 = new Tag(user, "verbs");
        tag1.setId(1L);

        Tag tag2 = new Tag(user, "verbs");
        tag2.setId(1L);

        Tag tag3 = new Tag(user, "nouns");
        tag3.setId(2L);

        assertEquals(tag1, tag2);
        assertNotEquals(tag1, tag3);
    }

    @Test
    void testTagHashCode() {
        User user = new User("user@example.com", "hash");

        Tag tag1 = new Tag(user, "verbs");
        tag1.setId(1L);

        Tag tag2 = new Tag(user, "verbs");
        tag2.setId(1L);

        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test
    void testToString() {
        User user = new User("user@example.com", "hash");
        Tag tag = new Tag(user, "verbs");

        String tagString = tag.toString();

        assertTrue(tagString.contains("verbs"));
    }

    @Test
    void testNoArgsConstructor() {
        Tag tag = new Tag();

        assertNotNull(tag);
        assertNull(tag.getName());
        assertNull(tag.getUser());
    }
}