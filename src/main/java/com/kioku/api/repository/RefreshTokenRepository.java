package com.kioku.api.repository;

import com.kioku.api.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Persistence for {@link RefreshToken}. */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /** Lookup is by hash; the raw token is never stored. */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Used to revoke every session when a password changes. */
    List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId);
}
