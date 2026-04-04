package dev.rubenpari.backend.repository;

import dev.rubenpari.backend.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data repository for {@link RefreshToken} entities. Tokens are looked up by their SHA-256 hash. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
