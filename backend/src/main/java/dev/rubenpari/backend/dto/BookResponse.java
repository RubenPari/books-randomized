package dev.rubenpari.backend.dto;

import java.util.Set;

public record BookResponse(
        String id,
        String externalId,
        String title,
        Set<String> authors,
        Set<String> categories,
        String language,
        Double rating,
        Integer publicationYear,
        String description,
        String coverUrl
) {
}
