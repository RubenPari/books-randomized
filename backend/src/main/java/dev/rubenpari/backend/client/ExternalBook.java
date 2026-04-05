package dev.rubenpari.backend.client;

import dev.rubenpari.backend.client.isbndb.IsbnDbApiBook;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalized book payload consumed by {@link dev.rubenpari.backend.service.BookService},
 * mapped from ISBNdb API v2 responses.
 */
public class ExternalBook {

    private static final Pattern YEAR_PREFIX = Pattern.compile("^(\\d{4})");

    private String id;
    private String title;
    private String description;
    private List<String> authors = List.of();
    private List<String> categories = List.of();
    private String language;
    private Double rating;
    private Integer year;
    private String coverUrl;

    public static String extractIsbnId(IsbnDbApiBook b) {
        if (b == null) {
            return null;
        }
        return firstNonBlank(b.getIsbn13(), b.getIsbn10(), b.getIsbnLegacy());
    }

    public static ExternalBook fromIsbnDb(IsbnDbApiBook b) {
        ExternalBook e = new ExternalBook();
        e.setId(extractIsbnId(b));
        e.setTitle(b.getTitle());
        e.setDescription(firstNonBlank(b.getSynopsis(), b.getExcerpt()));
        e.setAuthors(b.getAuthors() != null ? List.copyOf(b.getAuthors()) : List.of());
        e.setCategories(b.getSubjects() != null ? List.copyOf(b.getSubjects()) : List.of());
        e.setLanguage(normalizeBookLanguage(b.getLanguage()));
        e.setYear(parsePublicationYear(b.getDatePublished()));
        e.setCoverUrl(trimToNull(b.getImage()));
        e.setRating(null);
        return e;
    }

    private static String firstNonBlank(String... parts) {
        if (parts == null) {
            return null;
        }
        for (String p : parts) {
            if (StringUtils.hasText(p)) {
                return p.trim();
            }
        }
        return null;
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * If ISBNdb returns a full language name, keep it; if it looks like a short code, normalize case.
     */
    private static String normalizeBookLanguage(String raw) {
        String s = trimToNull(raw);
        if (s == null) {
            return null;
        }
        if (s.length() <= 3 && s.chars().allMatch(Character::isLetter)) {
            return s.toLowerCase();
        }
        return s;
    }

    private static Integer parsePublicationYear(String datePublished) {
        String s = trimToNull(datePublished);
        if (s == null) {
            return null;
        }
        Matcher m = YEAR_PREFIX.matcher(s);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors != null ? authors : List.of();
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories != null ? categories : List.of();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
