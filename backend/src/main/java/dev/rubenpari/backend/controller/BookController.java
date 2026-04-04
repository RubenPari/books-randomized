package dev.rubenpari.backend.controller;

import dev.rubenpari.backend.dto.BookResponse;
import dev.rubenpari.backend.model.Book;
import dev.rubenpari.backend.repository.BookRepository;
import dev.rubenpari.backend.service.BookService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for book-related endpoints under {@code /api/books}.
 * Provides random book discovery (with optional filters) and lookup by external ID.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    private final BookRepository bookRepository;

    public BookController(BookService bookService, BookRepository bookRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    /** Returns a random book matching the given filters, avoiding previously discovered books. */
    @GetMapping("/random")
    public BookResponse randomBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String targetLanguage,
            @RequestParam(required = false) String excludeIds
    ) {
        Map<String, String> filters = new HashMap<>();
        if (category != null) {
            filters.put("category", category);
        }
        if (language != null) {
            filters.put("language", language);
        }
        if (minRating != null) {
            filters.put("minRating", minRating.toString());
        }
        if (yearFrom != null) {
            filters.put("yearFrom", yearFrom.toString());
        }
        if (yearTo != null) {
            filters.put("yearTo", yearTo.toString());
        }
        if (excludeIds != null) {
            filters.put("excludeIds", excludeIds);
        }

        UUID userId = userDetails == null ? null : UUID.fromString(userDetails.getUsername());
        Book book = bookService.randomBook(userId, sessionId, filters, targetLanguage);
        return mapBook(book);
    }

    @GetMapping("/{externalId}")
    public BookResponse getBook(@PathVariable String externalId) {
        Book book = bookRepository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalStateException("Book not found"));
        return mapBook(book);
    }

    private BookResponse mapBook(Book book) {
        return new BookResponse(
                book.getId().toString(),
                book.getExternalId(),
                book.getTitle(),
                book.getAuthors(),
                book.getCategories(),
                book.getLanguage(),
                book.getRating(),
                book.getPublicationYear(),
                book.getDescription(),
                book.getCoverUrl()
        );
    }
}
