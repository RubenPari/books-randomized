package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Request payload for exchanging a refresh token for a new JWT token pair. */
public record RefreshRequest(@NotBlank String refreshToken) {
}
