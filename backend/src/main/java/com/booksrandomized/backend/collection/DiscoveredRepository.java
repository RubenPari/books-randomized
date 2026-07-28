package com.booksrandomized.backend.collection;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DiscoveredRepository extends JpaRepository<DiscoveredBook, UUID> {
    List<DiscoveredBook> findAllByUserIdOrderByDiscoveredAtDesc(UUID userId);
    Optional<DiscoveredBook> findByUserIdAndCatalogBookId(UUID userId, String catalogBookId);

    @Query("select d.catalogBookId from DiscoveredBook d where d.userId = :userId")
    Set<String> findBookIds(UUID userId);

    @Modifying
    @Query(value = """
            insert into discovered_books(id,user_id,catalog_book_id,discovered_at,title,authors)
            values (:id,:userId,:bookId,:now,:title,:authors)
            on conflict (user_id,catalog_book_id) do nothing
            """, nativeQuery = true)
    void insertIfAbsent(UUID id, UUID userId, String bookId, Instant now, String title, String authors);

    long deleteByUserIdAndCatalogBookId(UUID userId, String catalogBookId);
}
