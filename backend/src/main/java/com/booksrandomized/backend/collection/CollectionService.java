package com.booksrandomized.backend.collection;

import com.booksrandomized.backend.catalog.BookIds;
import com.booksrandomized.backend.shared.ApiException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class CollectionService {
    private final ReadingListRepository reading;
    private final DiscoveredRepository discovered;
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    CollectionService(ReadingListRepository reading, DiscoveredRepository discovered,
            JdbcTemplate jdbc, ObjectMapper json) {
        this.reading = reading;
        this.discovered = discovered;
        this.jdbc = jdbc;
        this.json = json;
    }

    @Transactional
    public ReadingView save(UUID userId, String bookId, String status, String title, List<String> authors) {
        String id = BookIds.canonicalize(bookId);
        String snapshotTitle = title == null ? "" : title.trim();
        List<String> snapshotAuthors = authors == null ? List.of() : List.copyOf(authors);
        if (snapshotTitle.isBlank()) {
            DiscoveredBook existing = discovered.findByUserIdAndCatalogBookId(userId, id).orElse(null);
            if (existing != null && existing.title() != null && !existing.title().isBlank()) {
                snapshotTitle = existing.title();
                snapshotAuthors = parseAuthors(existing.authors());
            }
        }
        reading.upsert(UUID.randomUUID(), userId, id, status, Instant.now(),
                snapshotTitle, writeAuthors(snapshotAuthors));
        return reading.findByUserIdAndCatalogBookId(userId, id).map(this::view).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<ReadingView> reading(UUID userId) {
        return reading.findAllByUserIdOrderByAddedAtDesc(userId).stream().map(this::view).toList();
    }

    @Transactional
    public void removeReading(UUID userId, String bookId) {
        if (reading.deleteByUserIdAndCatalogBookId(userId, BookIds.canonicalize(bookId)) == 0) {
            notFound();
        }
    }

    @Transactional
    public DiscoveryView discover(UUID userId, String bookId) {
        return discover(userId, bookId, List.of(), "", List.of());
    }

    @Transactional
    public DiscoveryView discover(UUID userId, String bookId, List<String> subjects,
            String title, List<String> authors) {
        String id = BookIds.canonicalize(bookId);
        String snapshotTitle = title == null ? "" : title.trim();
        List<String> snapshotAuthors = authors == null ? List.of() : List.copyOf(authors);
        discovered.insertIfAbsent(UUID.randomUUID(), userId, id, Instant.now(),
                snapshotTitle, writeAuthors(snapshotAuthors));
        subjects.stream().map(String::trim).filter(subject -> !subject.isBlank()).distinct().limit(12)
                .forEach(subject -> jdbc.update("""
                        insert into discovered_book_subjects(user_id,catalog_book_id,subject)
                        values (?,?,?) on conflict do nothing
                        """, userId, id, subject.toLowerCase(java.util.Locale.ROOT)));
        return discovered.findByUserIdAndCatalogBookId(userId, id).map(this::view).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<DiscoveryView> discovered(UUID userId) {
        return discovered.findAllByUserIdOrderByDiscoveredAtDesc(userId).stream()
                .map(this::view).toList();
    }

    @Transactional
    public void removeDiscovered(UUID userId, String bookId) {
        if (discovered.deleteByUserIdAndCatalogBookId(userId, BookIds.canonicalize(bookId)) == 0) {
            notFound();
        }
    }

    @Transactional(readOnly = true)
    public Set<String> excluded(UUID userId) {
        Set<String> ids = new java.util.HashSet<>(discovered.findBookIds(userId));
        reading.findAllByUserIdOrderByAddedAtDesc(userId).forEach(item -> ids.add(item.catalogBookId()));
        return Set.copyOf(ids);
    }

    private static void notFound() {
        throw new ApiException(HttpStatus.NOT_FOUND, "not-found", "The requested collection item was not found");
    }

    private ReadingView view(ReadingListItem item) {
        return new ReadingView(item.id(), item.catalogBookId(), item.status(), item.addedAt(),
                item.title() == null ? "" : item.title(), parseAuthors(item.authors()));
    }

    private DiscoveryView view(DiscoveredBook item) {
        return new DiscoveryView(item.id(), item.catalogBookId(), item.discoveredAt(),
                item.title() == null ? "" : item.title(), parseAuthors(item.authors()));
    }

    private String writeAuthors(List<String> authors) {
        try {
            return json.writeValueAsString(authors == null ? List.of() : authors);
        } catch (RuntimeException exception) {
            return "[]";
        }
    }

    private List<String> parseAuthors(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return List.of(json.readValue(raw, String[].class));
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    public record ReadingView(UUID id, String catalogBookId, String status, Instant addedAt,
            String title, List<String> authors) {}
    public record DiscoveryView(UUID id, String catalogBookId, Instant discoveredAt,
            String title, List<String> authors) {}
}
