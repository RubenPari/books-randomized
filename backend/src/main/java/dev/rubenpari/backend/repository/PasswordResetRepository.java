package dev.rubenpari.backend.repository;

import dev.rubenpari.backend.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data repository for {@link PasswordReset} entities. Tokens are looked up by their plain-text value. */
public interface PasswordResetRepository extends JpaRepository<PasswordReset, UUID> {
    Optional<PasswordReset> findByToken(String token);
}
