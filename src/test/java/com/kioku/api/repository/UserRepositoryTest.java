package com.kioku.api.repository;

import com.kioku.api.TestContainersConfiguration;
import com.kioku.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSaveUser() {
        User user = new User("test@example.com", "hashedPassword");
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    void testFindByEmail() {
        User user = new User("findme@example.com", "hashedPassword");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("findme@example.com");

        assertTrue(found.isPresent());
        assertEquals("findme@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail() {
        User user = new User("exists@example.com", "hashedPassword");
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("exists@example.com"));
        assertFalse(userRepository.existsByEmail("notexists@example.com"));
    }

    @Test
    void testUniqueEmailConstraint() {
        User user1 = new User("duplicate@example.com", "hash1");
        userRepository.save(user1);

        User user2 = new User("duplicate@example.com", "hash2");
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }

    @Test
    void testCreatedAtIsAutoSet() {
        User user = new User("timestamp@example.com", "hashedPassword");
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getCreatedAt());
        assertTrue(savedUser.getCreatedAt().isBefore(
                java.time.LocalDateTime.now().plusSeconds(1)
        ));
    }
}