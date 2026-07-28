package com.booksrandomized.backend.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class OpenLibraryAdapter implements CatalogClient {
    private static final Pattern TAGS = Pattern.compile("<[^>]*>");
    private static final Pattern UNSAFE = Pattern.compile("[\\p{Cc}\\p{Cf}]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private final RestClient client;

    public OpenLibraryAdapter(
            RestClient.Builder builder,
            @Value("${catalog.open-library.base-url:https://openlibrary.org}") String baseUrl,
            @Value("${catalog.open-library.user-agent}") String userAgent) {
        this.client = builder
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    @Override
    @Cacheable(cacheNames = "catalogSearch", key = "#query.trim().toLowerCase() + ':' + #limit", sync = true)
    public List<Book> search(String query, int limit) {
        return searchBatch(query, limit, 1);
    }

    @Override
    @Cacheable(cacheNames = "catalogSearch", key = "#query.trim().toLowerCase() + ':' + #limit + ':' + #batch", sync = true)
    public List<Book> searchBatch(String query, int limit, int batch) {
        try {
            SearchResponse response = client.get()
                    .uri(uri -> uri.path("/search.json")
                            .queryParam("q", query)
                            .queryParam("limit", limit)
                            .queryParam("page", batch)
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);
            if (response == null || response.docs() == null) {
                return List.of();
            }
            return response.docs().stream().map(OpenLibraryAdapter::normalize)
                    .filter(Objects::nonNull).toList();
        } catch (ResourceAccessException exception) {
            throw UpstreamCatalogException.timeout(exception);
        } catch (RestClientResponseException exception) {
            throw UpstreamCatalogException.unavailable(exception);
        } catch (RestClientException exception) {
            throw UpstreamCatalogException.unavailable(exception);
        }
    }

    private static Book normalize(Document document) {
        String id = BookIds.canonicalize(clean(document.key()));
        String title = clean(document.title());
        if (id == null) {
            id = "";
        }
        if (id.isBlank() || title.isBlank()) {
            return null;
        }
        List<String> authors = cleanList(document.authorNames(), 8);
        List<String> subjects = cleanList(document.subjects(), 12);
        String cover = document.coverId() == null
                ? null : "https://covers.openlibrary.org/b/id/" + document.coverId() + "-M.jpg";
        return new Book(id, title, authors, document.firstPublishYear(), cover, subjects,
                cleanList(document.languages(), 8), document.rating(), document.ratingsCount(),
                document.pageCount());
    }

    private static List<String> cleanList(List<String> values, int maximum) {
        if (values == null) {
            return List.of();
        }
        return values.stream().map(OpenLibraryAdapter::clean).filter(value -> !value.isBlank())
                .distinct().limit(maximum).toList();
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        String withoutTags = TAGS.matcher(value).replaceAll(" ");
        String withoutControls = UNSAFE.matcher(withoutTags).replaceAll(" ");
        return WHITESPACE.matcher(withoutControls).replaceAll(" ").trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SearchResponse(List<Document> docs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Document(
            String key,
            String title,
            @JsonProperty("author_name") List<String> authorNames,
            @JsonProperty("first_publish_year") Integer firstPublishYear,
            @JsonProperty("cover_i") Long coverId,
            @JsonProperty("subject") List<String> subjects,
            @JsonProperty("language") List<String> languages,
            @JsonProperty("ratings_average") Double rating,
            @JsonProperty("ratings_count") Integer ratingsCount,
            @JsonProperty("number_of_pages_median") Integer pageCount) {}
}
