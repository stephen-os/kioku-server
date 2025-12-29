package com.kioku.api.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        User user = new User("test@example.com", "hashedPassword123");

        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword123", user.getPasswordHash());
        assertNull(user.getId()); // Not persisted yet
        assertNull(user.getCreatedAt()); // Not set until @PrePersist
    }

    @Test
    void testUserEquality() {
        User user1 = new User("test@example.com", "hash1");
        user1.setId(1L);

        User user2 = new User("test@example.com", "hash2");
        user2.setId(1L);

        User user3 = new User("other@example.com", "hash3");
        user3.setId(2L);

        // Same ID and email = equal
        assertEquals(user1, user2);

        // Different ID = not equal
        assertNotEquals(user1, user3);
    }

    @Test
    void testUserHashCode() {
        User user1 = new User("test@example.com", "hash1");
        user1.setId(1L);

        User user2 = new User("test@example.com", "hash2");
        user2.setId(1L);

        // Same ID and email = same hash code
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToStringDoesNotExposePassword() {
        User user = new User("test@example.com", "secretPassword");
        String userString = user.toString();

        // Verify password is not in toString
        assertFalse(userString.contains("secretPassword"));
        assertTrue(userString.contains("test@example.com"));
    }

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getEmail());
        assertNull(user.getPasswordHash());
    }
}