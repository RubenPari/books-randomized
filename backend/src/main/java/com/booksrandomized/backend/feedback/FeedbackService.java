package com.booksrandomized.backend.feedback;

import com.booksrandomized.backend.catalog.BookIds;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

@Service
public class FeedbackService {
    private final FeedbackRepository repository;
    private final JdbcTemplate jdbc;
    FeedbackService(FeedbackRepository repository, JdbcTemplate jdbc) {
        this.repository = repository;
        this.jdbc = jdbc;
    }

    @Transactional
    public View upsert(UUID userId, String bookId, String sentiment, String reason) {
        String id = BookIds.canonicalize(bookId);
        repository.upsert(UUID.randomUUID(), userId, id, sentiment, reason, Instant.now());
        return repository.findByUserIdAndCatalogBookId(userId, id).map(FeedbackService::view).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<View> list(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(FeedbackService::view).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> subjectWeights(UUID userId) {
        Map<String, Integer> weights = new HashMap<>();
        jdbc.query("""
                select s.subject, sum(case when f.sentiment='LIKE' then 2 else -2 end)
                from recommendation_feedback f
                join discovered_book_subjects s
                  on s.user_id=f.user_id and s.catalog_book_id=f.catalog_book_id
                where f.user_id=?
                group by s.subject
                """, (RowCallbackHandler) result ->
                        weights.put(result.getString(1), result.getInt(2)), userId);
        repository.findAllByUserIdOrderByCreatedAtDesc(userId).forEach(item -> {
            if (item.reason() == null) return;
            int delta = "LIKE".equals(item.sentiment()) ? 2 : -2;
            for (String word : item.reason().split("[,;\\s]+")) {
                if (word.toLowerCase(Locale.ROOT).startsWith("subject:") && word.length() > 8) {
                    weights.merge(word.substring(8).toLowerCase(Locale.ROOT), delta, Integer::sum);
                }
            }
        });
        return Map.copyOf(weights);
    }

    private static View view(RecommendationFeedback item) {
        return new View(item.id(), item.catalogBookId(), item.sentiment(), item.reason(), item.createdAt());
    }

    public record View(UUID id, String catalogBookId, String sentiment, String reason, Instant createdAt) {}
}
