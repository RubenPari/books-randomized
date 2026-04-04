package dev.rubenpari.backend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO mapping the HAL+JSON response from the external book database API.
 * Unknown JSON properties are silently ignored to allow forward compatibility.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalBook {
    private String id;
    private String title;
    private String description;

    @JsonProperty("_embedded")
    private Embedded embedded;

    // Enriched from edition endpoint — not present in the book response directly
    private String language;
    private Double rating;
    private Integer year;
    private String enrichedCoverUrl;
    private List<String> enrichedCategories;

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

    @JsonProperty("_embedded")
    public void setEmbedded(Embedded embedded) {
        this.embedded = embedded;
    }

    /** Extracts author names, falling back to persons when authors is empty. */
    public List<String> getAuthors() {
        List<EmbeddedAuthor> source = (embedded != null && embedded.authors != null && !embedded.authors.isEmpty())
                ? embedded.authors
                : (embedded != null && embedded.persons != null ? embedded.persons : List.of());
        return source.stream()
                .filter(a -> a.name != null)
                .map(a -> a.name)
                .toList();
    }

    /** Extracts the first image content URL, falling back to an enriched cover from the edition. */
    public String getCoverUrl() {
        if (embedded != null && embedded.images != null && !embedded.images.isEmpty()) {
            EmbeddedImage img = embedded.images.get(0);
            if (img.links != null && img.links.contentUrl != null) {
                return img.links.contentUrl.href;
            }
        }
        return enrichedCoverUrl;
    }

    public void setEnrichedCoverUrl(String enrichedCoverUrl) {
        this.enrichedCoverUrl = enrichedCoverUrl;
    }

    /** Extracts subject names as categories, falling back to enriched genres from the edition. */
    public List<String> getCategories() {
        if (embedded != null && embedded.subjects != null && !embedded.subjects.isEmpty()) {
            return embedded.subjects.stream()
                    .filter(s -> s.name != null)
                    .map(s -> s.name)
                    .toList();
        }
        return enrichedCategories != null ? enrichedCategories : List.of();
    }

    public void setEnrichedCategories(List<String> enrichedCategories) {
        this.enrichedCategories = enrichedCategories;
    }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    /** Returns the ID of the first edition, used to enrich missing fields. */
    public String getFirstEditionId() {
        if (embedded == null || embedded.editions == null || embedded.editions.isEmpty()) {
            return null;
        }
        return embedded.editions.get(0).id;
    }

    // ── Nested mapping classes ───────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Embedded {
        List<EmbeddedAuthor> authors = new ArrayList<>();
        List<EmbeddedAuthor> persons = new ArrayList<>();
        List<EmbeddedImage> images = new ArrayList<>();
        List<EmbeddedSubject> subjects = new ArrayList<>();
        List<EmbeddedEditionRef> editions = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddedAuthor {
        String name;

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddedSubject {
        String name;

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddedEditionRef {
        String id;

        public void setId(String id) {
            this.id = id;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddedImage {
        @JsonProperty("_links")
        ImageLinks links;

        @JsonProperty("_links")
        public void setLinks(ImageLinks links) {
            this.links = links;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ImageLinks {
        ContentUrl contentUrl;

        public void setContentUrl(ContentUrl contentUrl) {
            this.contentUrl = contentUrl;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ContentUrl {
        String href;

        public void setHref(String href) {
            this.href = href;
        }
    }
}
