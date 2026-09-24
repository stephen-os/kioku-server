package com.kioku.api.service;

import com.kioku.api.model.User;
import com.kioku.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How an account presents itself.
 *
 * <p>An account is the identity now, so it needs a name and an avatar;
 * otherwise every place a person should appear shows an email address.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserProfileTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = new User("profile@example.com",
                "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMN");
        userId = userRepository.save(user).getId();
    }

    @Test
    @DisplayName("a new account has a default avatar and no display name")
    void defaultsAreSensible() {
        User user = userService.getAccountProfile(userId);

        assertThat(user.getAvatar()).isEqualTo("avatar-smile");
        assertThat(user.getDisplayName()).isNull();
    }

    @Test
    @DisplayName("sets a display name and avatar together")
    void setsBothFields() {
        User updated = userService.updateProfile(userId, "Stephen", "avatar-fox");

        assertThat(updated.getDisplayName()).isEqualTo("Stephen");
        assertThat(updated.getAvatar()).isEqualTo("avatar-fox");
    }

    @Test
    @DisplayName("omitting a field leaves it alone rather than clearing it")
    void omittedFieldsSurvive() {
        userService.updateProfile(userId, "Stephen", "avatar-fox");

        User afterNameOnly = userService.updateProfile(userId, "Steve", null);

        assertThat(afterNameOnly.getDisplayName()).isEqualTo("Steve");
        assertThat(afterNameOnly.getAvatar())
                .as("changing a name must not reset the avatar")
                .isEqualTo("avatar-fox");
    }

    @Test
    @DisplayName("a blank display name clears it, so the client falls back to the email")
    void blankNameClears() {
        userService.updateProfile(userId, "Stephen", null);

        User cleared = userService.updateProfile(userId, "   ", null);

        assertThat(cleared.getDisplayName()).isNull();
    }

    @Test
    @DisplayName("a blank avatar is ignored, because there is no sensible empty avatar")
    void blankAvatarIsIgnored() {
        userService.updateProfile(userId, null, "avatar-fox");

        User unchanged = userService.updateProfile(userId, null, "  ");

        assertThat(unchanged.getAvatar()).isEqualTo("avatar-fox");
    }

    @Test
    @DisplayName("display names are trimmed")
    void namesAreTrimmed() {
        User updated = userService.updateProfile(userId, "  Stephen  ", null);

        assertThat(updated.getDisplayName()).isEqualTo("Stephen");
    }
}
