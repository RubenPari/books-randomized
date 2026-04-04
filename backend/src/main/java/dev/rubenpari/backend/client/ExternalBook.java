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

    /** Extracts author names from the embedded HAL response. */
    public List<String> getAuthors() {
        if (embedded == null || embedded.authors == null) {
            return List.of();
        }
        return embedded.authors.stream()
                .filter(a -> a.name != null)
                .map(a -> a.name)
                .toList();
    }

    /** Extracts subject names as categories from the embedded HAL response. */
    public List<String> getCategories() {
        if (embedded == null || embedded.subjects == null) {
            return List.of();
        }
        return embedded.subjects.stream()
                .filter(s -> s.name != null)
                .map(s -> s.name)
                .toList();
    }

    /** Extracts the first image content URL from the embedded HAL response. */
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

    /** Not provided by the API at the book level; always returns null. */
    public String getLanguage() {
        return null;
    }

    /** Not provided by the API at the book level; always returns null. */
    public Double getRating() {
        return null;
    }

    /** Not provided by the API at the book level; always returns null. */
    public Integer getYear() {
        return null;
    }

    // ── Nested mapping classes ───────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Embedded {
        List<EmbeddedAuthor> authors = new ArrayList<>();
        List<EmbeddedImage> images = new ArrayList<>();
        List<EmbeddedSubject> subjects = new ArrayList<>();
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
