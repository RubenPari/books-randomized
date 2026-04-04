package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Request payload for saving a book to the user's vault, with optional note and rating. */
public record VaultEntryRequest(
        @NotBlank String externalBookId,
        String note,
        Integer personalRating
) {
}
