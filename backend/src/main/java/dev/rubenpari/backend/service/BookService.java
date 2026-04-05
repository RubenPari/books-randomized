package dev.rubenpari.backend.service;

import dev.rubenpari.backend.client.ExternalBook;
import dev.rubenpari.backend.client.IsbnDbClient;
import dev.rubenpari.backend.model.Book;
import dev.rubenpari.backend.model.Discovery;
import dev.rubenpari.backend.model.User;
import dev.rubenpari.backend.repository.BookRepository;
import dev.rubenpari.backend.repository.DiscoveryRepository;
import dev.rubenpari.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Core service for book discovery. Fetches random books from the external API,
 * avoids duplicates by checking user history and client-supplied exclude IDs,
 * optionally translates descriptions, and records each discovery.
 */
@Service
public class BookService {
    private final IsbnDbClient isbnDbClient;
    private final BookRepository bookRepository;
    private final DiscoveryRepository discoveryRepository;
    private final UserRepository userRepository;
    private final TranslationService translationService;

    public BookService(
            IsbnDbClient isbnDbClient,
            BookRepository bookRepository,
            DiscoveryRepository discoveryRepository,
            UserRepository userRepository,
            TranslationService translationService
    ) {
        this.isbnDbClient = isbnDbClient;
        this.bookRepository = bookRepository;
        this.discoveryRepository = discoveryRepository;
        this.userRepository = userRepository;
        this.translationService = translationService;
    }

    /**
     * Fetches a random book that the user has not yet discovered.
     * Retries up to 5 times to find a non-duplicate result.
     * Persists the book locally (if new) and records a discovery event for authenticated users.
     */
    @Transactional
    public Book randomBook(UUID userId, String sessionId, Map<String, String> filters, String targetLanguage) {
        Map<String, String> safeFilters = new HashMap<>(filters);
        Set<String> excluded = new HashSet<>();
        if (filters.containsKey("excludeIds")) {
            for (String id : filters.get("excludeIds").split(",")) {
                if (id != null && !id.isBlank()) {
                    excluded.add(id.trim());
                }
            }
        }

        int attempts = 0;
        while (attempts < 5) {
            attempts += 1;
            ExternalBook external = isbnDbClient.fetchRandom(safeFilters);
            if (external == null || external.getId() == null) {
                throw new IllegalStateException("Book API did not return a book");
            }

            if (excluded.contains(external.getId())) {
                continue;
            }

            Optional<Discovery> existingDiscovery = userId == null
                    ? Optional.empty()
                    : discoveryRepository.findByUserIdAndExternalId(userId, external.getId());
            if (existingDiscovery.isPresent()) {
                continue;
            }

            Book book = bookRepository.findByExternalId(external.getId()).orElseGet(() -> mapBook(external));
            String freshCover = external.getCoverUrl();
            if (freshCover != null && !freshCover.isBlank()) {
                book.setCoverUrl(freshCover);
            }
            if (book.getDescription() != null && targetLanguage != null) {
                String translated = translationService.translateDescription(book.getDescription(), targetLanguage);
                book.setDescription(translated);
            }
            bookRepository.save(book);

            if (userId != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found"));
                Discovery discovery = new Discovery();
                discovery.setBook(book);
                discovery.setUser(user);
                discovery.setSessionId(sessionId == null ? "default" : sessionId);
                discoveryRepository.save(discovery);
            }

            return book;
        }

        throw new IllegalStateException("Unable to find a new book without duplicates");
    }

    /** Maps an external API response to a local {@link Book} entity. */
    private Book mapBook(ExternalBook external) {
        Book book = new Book();
        book.setExternalId(external.getId());
        book.setTitle(external.getTitle() == null ? "Unknown" : external.getTitle());
        Set<String> authors = new HashSet<>();
        if (external.getAuthors() != null) {
            authors.addAll(external.getAuthors());
        }
        Set<String> categories = new HashSet<>();
        if (external.getCategories() != null) {
            categories.addAll(external.getCategories());
        }
        book.setAuthors(authors);
        book.setCategories(categories);
        book.setLanguage(external.getLanguage());
        book.setRating(external.getRating());
        book.setPublicationYear(external.getYear());
        book.setDescription(external.getDescription());
        book.setCoverUrl(external.getCoverUrl());
        return book;
    }
}
