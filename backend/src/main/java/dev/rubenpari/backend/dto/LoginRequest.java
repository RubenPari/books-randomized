package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request payload for user login with email and password. */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {
}
