package dev.rubenpari.backend.controller;

import dev.rubenpari.backend.dto.BookMapper;
import dev.rubenpari.backend.dto.BookResponse;
import dev.rubenpari.backend.exception.NotFoundException;
import dev.rubenpari.backend.model.Book;
import dev.rubenpari.backend.repository.BookRepository;
import dev.rubenpari.backend.security.AuthUserIds;
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
    private final BookMapper bookMapper;

    public BookController(BookService bookService, BookRepository bookRepository, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    /** Returns a random book matching the given filters, avoiding previously discovered books. */
    @GetMapping("/random")
    public BookResponse randomBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String category,
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

        UUID userId = AuthUserIds.nullableUserId(userDetails);
        Book book = bookService.randomBook(userId, sessionId, filters, targetLanguage);
        return bookMapper.toResponse(book);
    }

    @GetMapping("/{externalId}")
    public BookResponse getBook(@PathVariable String externalId) {
        Book book = bookRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Book not found"));
        return bookMapper.toResponse(book);
    }
}
