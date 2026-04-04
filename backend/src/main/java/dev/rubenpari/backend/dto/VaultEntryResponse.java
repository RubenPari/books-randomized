package dev.rubenpari.backend.dto;

import java.time.Instant;

/** API response DTO for a single vault entry, including the full book details. */
public record VaultEntryResponse(
        String id,
        BookResponse book,
        String note,
        Integer personalRating,
        Instant createdAt
) {
}
