package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmRequest(@NotBlank String token) {
}
