package dev.rubenpari.backend.dto;

import java.time.Instant;

public record DiscoveryResponse(
        String id,
        BookResponse book,
        String sessionId,
        Instant discoveredAt
) {
}
