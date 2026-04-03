package dev.rubenpari.backend.dto;

import java.time.Instant;

public record VaultEntryResponse(
        String id,
        BookResponse book,
        String note,
        Integer personalRating,
        Instant createdAt
) {
}
