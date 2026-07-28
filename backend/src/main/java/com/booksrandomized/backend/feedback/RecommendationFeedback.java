package com.booksrandomized.backend.feedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recommendation_feedback")
class RecommendationFeedback {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "catalog_book_id", nullable = false) private String catalogBookId;
    @Column(nullable = false) private String sentiment;
    @Column private String reason;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected RecommendationFeedback() {}
    UUID id() { return id; }
    String catalogBookId() { return catalogBookId; }
    String sentiment() { return sentiment; }
    String reason() { return reason; }
    Instant createdAt() { return createdAt; }
}
