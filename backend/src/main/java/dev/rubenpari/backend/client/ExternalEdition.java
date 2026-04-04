package dev.rubenpari.backend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO mapping the HAL+JSON response from the external book database API edition endpoint.
 * Used to enrich a book with cover image, language, genres, and publication year.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalEdition {
    @JsonProperty("_embedded")
    private Embedded embedded;

    @JsonProperty("_embedded")
    public void setEmbedded(Embedded embedded) {
        this.embedded = embedded;
    }

    /** Returns the ISO 639 language code from the first embedded language, or null. */
    public String getLanguageId() {
        if (embedded == null || embedded.languages == null || embedded.languages.isEmpty()) {
            return null;
        }
        return embedded.languages.get(0).id;
    }

    /** Returns the cover URL from the edition's images if available. */
    public String getCoverUrl() {
        if (embedded == null || embedded.images == null || embedded.images.isEmpty()) {
            return null;
        }
        EmbeddedImage img = embedded.images.get(0);
        if (img.links == null || img.links.contentUrl == null) {
            return null;
        }
        return img.links.contentUrl.href;
    }

    /** Returns genre names from the edition. */
    public List<String> getGenres() {
        if (embedded == null || embedded.genres == null) {
            return List.of();
        }
        return embedded.genres.stream()
                .filter(g -> g.name != null)
                .map(g -> g.name)
                .toList();
    }

    // ── Nested mapping classes ───────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Embedded {
        List<EmbeddedLanguageRef> languages = new ArrayList<>();
        List<EmbeddedImage> images = new ArrayList<>();
        List<EmbeddedGenre> genres = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddedLanguageRef {
        String id;

        public void setId(String id) {
            this.id = id;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddedGenre {
        String name;

        public void setName(String name) {
            this.name = name;
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
