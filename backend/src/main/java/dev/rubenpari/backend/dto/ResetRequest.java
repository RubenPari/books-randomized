package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request payload for initiating a password reset. An email with a reset token will be sent. */
public record ResetRequest(
        @Email @NotBlank String email
) {
}
