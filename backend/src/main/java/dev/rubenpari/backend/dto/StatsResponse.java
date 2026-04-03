package dev.rubenpari.backend.dto;

import java.util.Map;

public record StatsResponse(
        long totalDiscovered,
        long totalVaulted,
        double averageRating,
        Map<String, Long> byCategory,
        Map<String, Long> byAuthor,
        Map<String, Long> byLanguage
) {
}
