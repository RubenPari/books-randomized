package com.booksrandomized.backend.collection;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface ReadingListRepository extends JpaRepository<ReadingListItem, UUID> {
    List<ReadingListItem> findAllByUserIdOrderByAddedAtDesc(UUID userId);
    Optional<ReadingListItem> findByUserIdAndCatalogBookId(UUID userId, String catalogBookId);
    boolean existsByUserIdAndCatalogBookId(UUID userId, String catalogBookId);

    @Modifying
    @Query(value = """
            insert into reading_list_items(id,user_id,catalog_book_id,status,added_at,title,authors)
            values (:id,:userId,:bookId,:status,:now,:title,:authors)
            on conflict (user_id,catalog_book_id) do update set
                status=excluded.status,
                title=case when excluded.title <> '' then excluded.title else reading_list_items.title end,
                authors=case when excluded.title <> '' then excluded.authors else reading_list_items.authors end
            """, nativeQuery = true)
    void upsert(UUID id, UUID userId, String bookId, String status, Instant now, String title, String authors);

    long deleteByUserIdAndCatalogBookId(UUID userId, String catalogBookId);
}
