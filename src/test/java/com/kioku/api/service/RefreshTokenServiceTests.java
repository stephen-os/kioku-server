package com.kioku.api.service;

import com.kioku.api.model.User;
import com.kioku.api.repository.RefreshTokenRepository;
import com.kioku.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Refresh token issuance, rotation and revocation.
 *
 * <p>The properties worth guarding: a token works exactly once, the raw value
 * never reaches the database, and a credential change ends every session.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefreshTokenServiceTests {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = new User("refresh@example.com",
                "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMN");
        userId = userRepository.save(user).getId();
    }

    @Test
    @DisplayName("a freshly issued token resolves to its owner")
    void issuedTokenResolvesToOwner() {
        String token = refreshTokenService.issue(userId);

        assertThat(refreshTokenService.rotate(token)).contains(userId);
    }

    @Test
    @DisplayName("stores only a hash, never the token itself")
    void storesOnlyAHash() {
        String token = refreshTokenService.issue(userId);

        assertThat(refreshTokenRepository.findAll())
                .isNotEmpty()
                .allSatisfy(stored -> {
                    assertThat(stored.getTokenHash()).isNotEqualTo(token);
                    assertThat(stored.getTokenHash()).hasSize(64);
                });
    }

    @Test
    @DisplayName("rotation makes the old token unusable, so a stolen one works at most once")
    void rotationBurnsTheOldToken() {
        String token = refreshTokenService.issue(userId);

        assertThat(refreshTokenService.rotate(token)).contains(userId);
        assertThat(refreshTokenService.rotate(token)).isEmpty();
    }

    @Test
    @DisplayName("rejects a token it never issued")
    void rejectsUnknownToken() {
        assertThat(refreshTokenService.rotate("not-a-real-token")).isEmpty();
    }

    @Test
    @DisplayName("rejects null and blank without touching the database")
    void rejectsEmptyInput() {
        assertThat(refreshTokenService.rotate(null)).isEmpty();
        assertThat(refreshTokenService.rotate("  ")).isEmpty();
    }

    @Test
    @DisplayName("revoking all ends every live session for that user")
    void revokeAllEndsEverySession() {
        String first = refreshTokenService.issue(userId);
        String second = refreshTokenService.issue(userId);

        refreshTokenService.revokeAllFor(userId);

        assertThat(refreshTokenService.rotate(first)).isEmpty();
        assertThat(refreshTokenService.rotate(second)).isEmpty();
    }

    @Test
    @DisplayName("revoking one user's sessions leaves another user's alone")
    void revokeAllIsScopedToOneUser() {
        User other = userRepository.save(new User("other-refresh@example.com",
                "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMN"));
        String mine = refreshTokenService.issue(userId);
        String theirs = refreshTokenService.issue(other.getId());

        refreshTokenService.revokeAllFor(userId);

        assertThat(refreshTokenService.rotate(mine)).isEmpty();
        assertThat(refreshTokenService.rotate(theirs)).contains(other.getId());
    }

    @Test
    @DisplayName("issues a distinct token every time")
    void issuesDistinctTokens() {
        Optional<String> a = Optional.of(refreshTokenService.issue(userId));
        Optional<String> b = Optional.of(refreshTokenService.issue(userId));

        assertThat(a).isNotEqualTo(b);
    }
}
