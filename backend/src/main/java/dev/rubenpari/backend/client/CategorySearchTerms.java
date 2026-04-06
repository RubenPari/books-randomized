package dev.rubenpari.backend.client;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Parses comma-separated subject filters and picks one term per ISBNdb request
 * (multiselect on the client maps to a single search query per fetch).
 */
public final class CategorySearchTerms {

    private CategorySearchTerms() {}

    public static String pickOneRandomTerm(String commaSeparated) {
        return pickOneRandomTerm(commaSeparated, RandomGenerator.getDefault());
    }

    static String pickOneRandomTerm(String commaSeparated, RandomGenerator random) {
        List<String> parts = splitNonBlankParts(commaSeparated);
        if (parts.isEmpty()) {
            return null;
        }
        return parts.get(random.nextInt(parts.size()));
    }

    static List<String> splitNonBlankParts(String commaSeparated) {
        List<String> parts = new ArrayList<>();
        if (!StringUtils.hasText(commaSeparated)) {
            return parts;
        }
        for (String part : commaSeparated.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                parts.add(t);
            }
        }
        return parts;
    }
}
