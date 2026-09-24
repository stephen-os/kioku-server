package com.kioku.api.service;

import com.kioku.api.model.RefreshToken;
import com.kioku.api.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Issues, rotates and revokes refresh tokens.
 *
 * <p>Tokens are opaque random strings rather than JWTs, so they can be revoked
 * server-side. Only a SHA-256 hash is stored: reading the database yields
 * nothing that can be presented to the API.
 *
 * <p>Rotation is the important property. Exchanging a token revokes it and
 * issues a new one, so a stolen token is usable at most once, and the theft
 * surfaces as the real client's next refresh failing rather than as a silent
 * parallel session.
 */
@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration lifetime;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${jwt.refresh-expiration:2592000000}") long refreshExpirationMillis) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.lifetime = Duration.ofMillis(refreshExpirationMillis);
    }

    /** Mints a token for the user and returns the raw value, which is not stored. */
    @Transactional
    public String issue(UUID userId) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        refreshTokenRepository.save(new RefreshToken(userId, hash(token), Instant.now().plus(lifetime)));
        logger.debug("Issued refresh token for user={}", userId);
        return token;
    }

    /**
     * Exchanges a token for a new one, returning the owning user.
     *
     * <p>Empty when the token is unknown, expired, or already used: all three
     * mean the caller cannot prove a live session, and none of them should be
     * distinguished in the response.
     */
    @Transactional
    public Optional<UUID> rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(hash(rawToken));
        if (found.isEmpty() || !found.get().isUsable()) {
            logger.debug("Refresh rejected: token unknown, expired or already used");
            return Optional.empty();
        }

        RefreshToken token = found.get();
        token.revoke();
        refreshTokenRepository.save(token);
        return Optional.of(token.getUserId());
    }

    /**
     * Revokes every live token for a user.
     *
     * <p>Called when a password changes or is reset: a credential change
     * should end sessions established with the old one.
     */
    @Transactional
    public void revokeAllFor(UUID userId) {
        List<RefreshToken> live = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId);
        live.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(live);
        logger.debug("Revoked {} refresh tokens for user={}", live.size(), userId);
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
