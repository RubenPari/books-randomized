package dev.rubenpari.backend.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
