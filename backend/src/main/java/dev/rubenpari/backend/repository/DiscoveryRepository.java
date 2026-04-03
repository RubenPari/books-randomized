package dev.rubenpari.backend.repository;

import dev.rubenpari.backend.model.Discovery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscoveryRepository extends JpaRepository<Discovery, UUID> {
    @Query("select d from Discovery d where d.user.id = :userId order by d.discoveredAt desc")
    List<Discovery> findByUserId(@Param("userId") UUID userId);

    @Query("select d from Discovery d where d.user.id = :userId and d.book.externalId = :externalId")
    Optional<Discovery> findByUserIdAndExternalId(@Param("userId") UUID userId, @Param("externalId") String externalId);
}
