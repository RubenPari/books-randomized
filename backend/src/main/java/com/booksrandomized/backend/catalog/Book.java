package com.booksrandomized.backend.catalog;

import java.util.List;

public record Book(
        String id,
        String title,
        List<String> authors,
        Integer firstPublishedYear,
        String coverUrl,
        List<String> subjects,
        List<String> languages,
        Double rating,
        Integer ratingsCount,
        Integer pageCount) {
    public Book {
        authors = List.copyOf(authors);
        subjects = List.copyOf(subjects);
        languages = List.copyOf(languages);
    }

    public Book(String id, String title, List<String> authors, Integer firstPublishedYear,
            String coverUrl, List<String> subjects) {
        this(id, title, authors, firstPublishedYear, coverUrl, subjects, List.of(), null, null, null);
    }
}
