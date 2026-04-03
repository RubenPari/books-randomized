package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record VaultEntryRequest(
        @NotBlank String externalBookId,
        String note,
        Integer personalRating
) {
}
