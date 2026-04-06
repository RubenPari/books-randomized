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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Core service for book discovery. Fetches random books from the external API,
 * avoids duplicates by checking user history and client-supplied exclude IDs,
 * optionally translates metadata via Google (target locale), and records each discovery.
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
        safeFilters.remove("language");

        Set<String> excluded = new HashSet<>();
        if (filters.containsKey("excludeIds")) {
            for (String id : filters.get("excludeIds").split(",")) {
                if (id != null && !id.isBlank()) {
                    excluded.add(id.trim());
                }
            }
        }

        int attempts = 0;
        boolean receivedAnyCandidate = false;
        boolean rejectedAsDuplicate = false;
        while (attempts < 5) {
            attempts += 1;
            ExternalBook external = isbnDbClient.fetchRandom(safeFilters);
            if (external == null || external.getId() == null) {
                continue;
            }

            receivedAnyCandidate = true;

            if (excluded.contains(external.getId())) {
                rejectedAsDuplicate = true;
                continue;
            }

            Optional<Discovery> existingDiscovery = userId == null
                    ? Optional.empty()
                    : discoveryRepository.findByUserIdAndExternalId(userId, external.getId());
            if (existingDiscovery.isPresent()) {
                rejectedAsDuplicate = true;
                continue;
            }

            Book book = bookRepository.findByExternalId(external.getId()).orElseGet(() -> mapBook(external));
            String freshCover = external.getCoverUrl();
            if (freshCover != null && !freshCover.isBlank()) {
                book.setCoverUrl(freshCover);
            }
            translateBookMetadata(book, targetLanguage);
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

        if (receivedAnyCandidate && rejectedAsDuplicate) {
            throw new IllegalStateException(
                    "No new book found after several tries (all were already discovered or excluded). "
                            + "Clear history or widen filters.");
        }
        throw new IllegalStateException(
                "Book API did not return a usable book after several attempts (check ISBNDB_API_KEY and filters)");
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

    /**
     * ISBNdb queries stay unfiltered by language (English-oriented seeds and fields).
     * Localized strings are produced here via Google Translate when {@code targetLanguage} is set.
     */
    private void translateBookMetadata(Book book, String targetLanguage) {
        if (!StringUtils.hasText(targetLanguage)) {
            return;
        }

        List<String> authorList =
                book.getAuthors() == null ? List.of() : new ArrayList<>(book.getAuthors());
        List<String> categoryList =
                book.getCategories() == null ? List.of() : new ArrayList<>(book.getCategories());

        List<String> batch = new ArrayList<>();
        batch.add(book.getTitle() == null ? "" : book.getTitle());
        batch.addAll(authorList);
        batch.addAll(categoryList);

        List<String> translatedBatch = translationService.translatePlainBatch(batch, targetLanguage);
        if (translatedBatch.size() == batch.size()) {
            int i = 0;
            book.setTitle(translatedBatch.get(i++));
            Set<String> authorsOut = new LinkedHashSet<>();
            for (int a = 0; a < authorList.size(); a++) {
                authorsOut.add(translatedBatch.get(i++));
            }
            book.setAuthors(authorsOut);
            Set<String> categoriesOut = new LinkedHashSet<>();
            for (int c = 0; c < categoryList.size(); c++) {
                categoriesOut.add(translatedBatch.get(i++));
            }
            book.setCategories(categoriesOut);
        } else {
            book.setTitle(translationService.translatePlain(book.getTitle(), targetLanguage));
            Set<String> authorsOut = new LinkedHashSet<>();
            for (String a : authorList) {
                authorsOut.add(translationService.translatePlain(a, targetLanguage));
            }
            book.setAuthors(authorsOut);
            Set<String> categoriesOut = new LinkedHashSet<>();
            for (String c : categoryList) {
                categoriesOut.add(translationService.translatePlain(c, targetLanguage));
            }
            book.setCategories(categoriesOut);
        }

        String description = book.getDescription();
        if (!StringUtils.hasText(description)) {
            return;
        }
        boolean looksLikeHtml = description.indexOf('<') >= 0 && description.lastIndexOf('>') > description.indexOf('<');
        book.setDescription(
                looksLikeHtml
                        ? translationService.translateHtml(description, targetLanguage)
                        : translationService.translatePlain(description, targetLanguage));
    }
}
