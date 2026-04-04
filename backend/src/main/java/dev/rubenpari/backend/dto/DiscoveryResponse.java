package dev.rubenpari.backend.dto;

import java.time.Instant;

/** API response DTO for a single discovery event, including the book and session context. */
public record DiscoveryResponse(
        String id,
        BookResponse book,
        String sessionId,
        Instant discoveredAt
) {
}
