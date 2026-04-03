package dev.rubenpari.backend.repository;

import dev.rubenpari.backend.model.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, UUID> {
    @Query("select v from VaultEntry v where v.user.id = :userId order by v.createdAt desc")
    List<VaultEntry> findByUserId(@Param("userId") UUID userId);

    @Query("select v from VaultEntry v where v.user.id = :userId and v.book.externalId = :externalId")
    Optional<VaultEntry> findByUserIdAndExternalId(@Param("userId") UUID userId, @Param("externalId") String externalId);
}
