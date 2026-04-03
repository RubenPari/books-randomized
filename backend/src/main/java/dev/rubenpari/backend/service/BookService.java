package dev.rubenpari.backend.service;

import dev.rubenpari.backend.client.BookDatabaseClient;
import dev.rubenpari.backend.client.ExternalBook;
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

@Service
public class BookService {
    private final BookDatabaseClient bookDatabaseClient;
    private final BookRepository bookRepository;
    private final DiscoveryRepository discoveryRepository;
    private final UserRepository userRepository;
    private final TranslationService translationService;

    public BookService(
            BookDatabaseClient bookDatabaseClient,
            BookRepository bookRepository,
            DiscoveryRepository discoveryRepository,
            UserRepository userRepository,
            TranslationService translationService
    ) {
        this.bookDatabaseClient = bookDatabaseClient;
        this.bookRepository = bookRepository;
        this.discoveryRepository = discoveryRepository;
        this.userRepository = userRepository;
        this.translationService = translationService;
    }

    @Transactional
    public Book randomBook(UUID userId, String sessionId, Map<String, String> filters, String targetLanguage) {
        Map<String, String> safeFilters = new HashMap<>(filters);
        int attempts = 0;
        while (attempts < 5) {
            attempts += 1;
            ExternalBook external = bookDatabaseClient.fetchRandom(safeFilters);
            if (external == null || external.getId() == null) {
                throw new IllegalStateException("Book API did not return a book");
            }

            Optional<Discovery> existingDiscovery = userId == null
                    ? Optional.empty()
                    : discoveryRepository.findByUserIdAndExternalId(userId, external.getId());
            if (existingDiscovery.isPresent()) {
                continue;
            }

            Book book = bookRepository.findByExternalId(external.getId()).orElseGet(() -> mapBook(external));
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
