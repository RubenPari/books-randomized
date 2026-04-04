package dev.rubenpari.backend.dto;

/** Response payload returned after successful authentication, containing JWT token pair. */
public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
