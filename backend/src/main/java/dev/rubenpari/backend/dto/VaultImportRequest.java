package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VaultImportRequest(@NotEmpty List<VaultEntryRequest> entries) {
}
