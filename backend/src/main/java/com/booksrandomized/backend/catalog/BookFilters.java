package com.booksrandomized.backend.catalog;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BookFilters(
        @Pattern(regexp = "[A-Za-z]{2,3}") String language,
        @Size(max = 10) List<@Size(min = 1, max = 80) String> subjects,
        @Min(0) @Max(3000) Integer publishedFrom,
        @Min(0) @Max(3000) Integer publishedTo,
        @DecimalMin("0.0") @DecimalMax("5.0") Double minimumRating,
        @Min(0) Integer minimumRatingsCount,
        @Min(1) Integer minimumPages,
        @Min(1) Integer maximumPages) {
    public BookFilters {
        subjects = subjects == null ? List.of() : subjects.stream()
                .map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
    }
}
