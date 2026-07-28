package com.booksrandomized.backend.catalog;

import com.booksrandomized.backend.collection.CollectionService;
import com.booksrandomized.backend.feedback.FeedbackService;
import com.booksrandomized.backend.shared.ApiException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
class DiscoveryService {
    private static final int MAX_BATCHES = 5;
    private static final int BATCH_SIZE = 40;
    private final CatalogClient catalog;
    private final CollectionService collections;
    private final FeedbackService feedback;
    private final RandomGenerator random;

    DiscoveryService(CatalogClient catalog, CollectionService collections,
            FeedbackService feedback, RandomGenerator random) {
        this.catalog = catalog;
        this.collections = collections;
        this.feedback = feedback;
        this.random = random;
    }

    Result random(UUID userId, BookFilters filters) {
        validateRanges(filters);
        Set<String> excluded = collections.excluded(userId);
        Map<String, Integer> weights = feedback.subjectWeights(userId);
        String query = query(filters, weights);
        Set<Integer> batches = new HashSet<>();
        while (batches.size() < MAX_BATCHES) {
            int batch = random.nextInt(1, 101);
            if (!batches.add(batch)) continue;
            List<Book> eligible = catalog.searchBatch(query, BATCH_SIZE, batch).stream()
                    .filter(book -> !excluded.contains(book.id()))
                    .filter(book -> matches(book, filters))
                    .toList();
            if (!eligible.isEmpty()) {
                Book selected = select(eligible, weights);
                collections.discover(userId, selected.id(), selected.subjects(),
                        selected.title(), selected.authors());
                List<String> explanations = new ArrayList<>();
                explanations.add(filtersPresent(filters)
                        ? "discovery.explanation.filters" : "discovery.explanation.random");
                if (score(selected, weights) > 0) {
                    explanations.add("discovery.explanation.personalized");
                }
                return new Result(selected, List.copyOf(explanations));
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "no-book-match",
                "No undiscovered book matched these filters");
    }

    private Book select(List<Book> books, Map<String, Integer> weights) {
        int best = books.stream().mapToInt(book -> score(book, weights)).max().orElse(0);
        List<Book> preferred = books.stream().filter(book -> score(book, weights) == best)
                .sorted(Comparator.comparing(Book::id)).toList();
        return preferred.get(random.nextInt(preferred.size()));
    }

    private static int score(Book book, Map<String, Integer> weights) {
        return book.subjects().stream().map(subject -> subject.toLowerCase(Locale.ROOT))
                .mapToInt(subject -> weights.getOrDefault(subject, 0)).sum();
    }

    private static boolean matches(Book book, BookFilters filters) {
        if (filters.language() != null && book.languages().stream()
                .noneMatch(language -> language.equalsIgnoreCase(filters.language()))) return false;
        if (!filters.subjects().isEmpty() && book.subjects().stream().noneMatch(subject ->
                filters.subjects().stream().anyMatch(wanted -> subject.equalsIgnoreCase(wanted)))) return false;
        if (filters.publishedFrom() != null
                && (book.firstPublishedYear() == null || book.firstPublishedYear() < filters.publishedFrom())) return false;
        if (filters.publishedTo() != null
                && (book.firstPublishedYear() == null || book.firstPublishedYear() > filters.publishedTo())) return false;
        if (filters.minimumRating() != null
                && (book.rating() == null || book.rating() < filters.minimumRating())) return false;
        if (filters.minimumRatingsCount() != null
                && (book.ratingsCount() == null || book.ratingsCount() < filters.minimumRatingsCount())) return false;
        if (filters.minimumPages() != null
                && (book.pageCount() == null || book.pageCount() < filters.minimumPages())) return false;
        return filters.maximumPages() == null
                || (book.pageCount() != null && book.pageCount() <= filters.maximumPages());
    }

    private static String query(BookFilters filters, Map<String, Integer> weights) {
        List<String> terms = new ArrayList<>();
        if (!filters.subjects().isEmpty()) {
            terms.add("(" + String.join(" OR ", filters.subjects().stream()
                    .map(subject -> "subject:\"" + subject.replace("\"", "") + "\"").toList()) + ")");
        } else {
            weights.entrySet().stream().filter(entry -> entry.getValue() > 0)
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(entry -> terms.add("subject:\"" + entry.getKey().replace("\"", "") + "\""));
        }
        if (filters.language() != null) terms.add("language:" + filters.language().toLowerCase(Locale.ROOT));
        return terms.isEmpty() ? "*" : String.join(" ", terms);
    }

    private static boolean filtersPresent(BookFilters filters) {
        return filters.language() != null || !filters.subjects().isEmpty()
                || filters.publishedFrom() != null || filters.publishedTo() != null
                || filters.minimumRating() != null || filters.minimumRatingsCount() != null
                || filters.minimumPages() != null || filters.maximumPages() != null;
    }

    private static void validateRanges(BookFilters filters) {
        if (filters.publishedFrom() != null && filters.publishedTo() != null
                && filters.publishedFrom() > filters.publishedTo()) invalidRange();
        if (filters.minimumPages() != null && filters.maximumPages() != null
                && filters.minimumPages() > filters.maximumPages()) invalidRange();
    }

    private static void invalidRange() {
        throw new ApiException(HttpStatus.BAD_REQUEST, "invalid-filter-range",
                "Filter range minimum must not exceed maximum");
    }

    record Result(Book book, List<String> explanationKeys) {}
}
