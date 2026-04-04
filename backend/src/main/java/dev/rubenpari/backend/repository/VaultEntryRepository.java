package dev.rubenpari.backend.repository;

import dev.rubenpari.backend.model.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link VaultEntry} entities.
 * Provides queries for listing a user's saved books and detecting duplicates.
 */
public interface VaultEntryRepository extends JpaRepository<VaultEntry, UUID> {
    /** Returns all vault entries for a given user, most recently added first. */
    @Query("select v from VaultEntry v where v.user.id = :userId order by v.createdAt desc")
    List<VaultEntry> findByUserId(@Param("userId") UUID userId);

    /** Finds a vault entry by user and book external ID, used to prevent duplicate saves. */
    @Query("select v from VaultEntry v where v.user.id = :userId and v.book.externalId = :externalId")
    Optional<VaultEntry> findByUserIdAndExternalId(@Param("userId") UUID userId, @Param("externalId") String externalId);
}
