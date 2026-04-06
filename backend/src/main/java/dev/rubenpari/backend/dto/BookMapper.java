package dev.rubenpari.backend.dto;

import dev.rubenpari.backend.model.Book;
import org.springframework.stereotype.Component;

/** Maps {@link Book} entities to {@link BookResponse} DTOs for REST layers. */
@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
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
