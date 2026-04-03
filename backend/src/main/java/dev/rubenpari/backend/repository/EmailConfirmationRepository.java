package dev.rubenpari.backend.repository;

import dev.rubenpari.backend.model.EmailConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, UUID> {
    Optional<EmailConfirmation> findByToken(String token);
}
