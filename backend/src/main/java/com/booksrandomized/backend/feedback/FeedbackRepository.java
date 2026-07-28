package com.booksrandomized.backend.feedback;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.jspecify.annotations.Nullable;

interface FeedbackRepository extends JpaRepository<RecommendationFeedback, UUID> {
    List<RecommendationFeedback> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<RecommendationFeedback> findByUserIdAndCatalogBookId(UUID userId, String catalogBookId);

    @Modifying
    @Query(value = """
            insert into recommendation_feedback(id,user_id,catalog_book_id,sentiment,reason,created_at)
            values (:id,:userId,:bookId,:sentiment,:reason,:now)
            on conflict (user_id,catalog_book_id)
            do update set sentiment=excluded.sentiment, reason=excluded.reason, created_at=excluded.created_at
            """, nativeQuery = true)
    void upsert(UUID id, UUID userId, String bookId, String sentiment, @Nullable String reason, Instant now);
}
