package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Request payload for bulk-importing multiple books into the user's vault. */
public record VaultImportRequest(@NotEmpty List<VaultEntryRequest> entries) {
}
