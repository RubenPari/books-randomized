package com.booksrandomized.backend.collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "discovered_books")
class DiscoveredBook {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "catalog_book_id", nullable = false) private String catalogBookId;
    @Column(name = "discovered_at", nullable = false) private Instant discoveredAt;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String authors;

    protected DiscoveredBook() {}
    UUID id() { return id; }
    UUID userId() { return userId; }
    String catalogBookId() { return catalogBookId; }
    Instant discoveredAt() { return discoveredAt; }
    String title() { return title; }
    String authors() { return authors; }
}
