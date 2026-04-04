package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Request payload for confirming a user's email address using a one-time token. */
public record ConfirmRequest(@NotBlank String token) {
}
