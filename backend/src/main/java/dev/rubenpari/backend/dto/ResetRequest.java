package dev.rubenpari.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetRequest(
        @Email @NotBlank String email
) {
}
