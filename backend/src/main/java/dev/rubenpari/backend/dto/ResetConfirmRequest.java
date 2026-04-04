package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request payload for completing a password reset with a valid token and new password. */
public record ResetConfirmRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8) String newPassword
) {
}
