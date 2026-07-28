package com.booksrandomized.backend.collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reading_list_items")
class ReadingListItem {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "catalog_book_id", nullable = false) private String catalogBookId;
    @Column(nullable = false) private String status;
    @Column(name = "added_at", nullable = false) private Instant addedAt;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String authors;

    protected ReadingListItem() {}
    UUID id() { return id; }
    UUID userId() { return userId; }
    String catalogBookId() { return catalogBookId; }
    String status() { return status; }
    Instant addedAt() { return addedAt; }
    String title() { return title; }
    String authors() { return authors; }
}
