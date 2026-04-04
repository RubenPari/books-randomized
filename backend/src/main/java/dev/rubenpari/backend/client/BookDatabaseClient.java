package dev.rubenpari.backend.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST client for the external book database API.
 * Fetches random books with optional query-parameter filters and X-API-KEY authentication.
 * Enriches book data with edition information (cover, language, genres) via a secondary call.
 */
@Component
public class BookDatabaseClient {
    private static final Logger log = LoggerFactory.getLogger(BookDatabaseClient.class);
    private final RestClient restClient;
    private final String apiKey;

    public BookDatabaseClient(
            @Value("${app.bookdatabase.base-url}") String baseUrl,
            @Value("${app.bookdatabase.api-key}") String apiKey
    ) {
        // The external API sends JSON with a text/plain Content-Type header.
        // Register a Jackson converter that also accepts text/plain so RestClient
        // can deserialize the response body regardless of the declared media type.
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter();
        converter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN
        ));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof JacksonJsonHttpMessageConverter);
                    converters.add(converter);
                })
                .build();
        this.apiKey = apiKey;
    }

    /** Calls {@code GET /books/random}, forwarding all filter params, and enriches the result. */
    public ExternalBook fetchRandom(Map<String, String> filters) {
        try {
            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/books/random");
                        filters.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    });

            if (StringUtils.hasText(apiKey)) {
                request = request.header("X-API-KEY", apiKey);
            }

            ExternalBook book = request.retrieve().body(ExternalBook.class);
            enrich(book);
            return book;
        } catch (RestClientResponseException ex) {
            log.error("BookDatabase API returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }

    /**
     * Fetches the first edition for the book and backfills cover, language, and categories
     * (genres) when they are missing from the book-level response.
     */
    private void enrich(ExternalBook book) {
        if (book == null) return;
        String editionId = book.getFirstEditionId();
        if (editionId == null) return;

        try {
            ExternalEdition edition = get("/editions/" + editionId, ExternalEdition.class);
            if (edition == null) return;

            // Cover: use edition cover if the book has none
            if (book.getCoverUrl() == null) {
                book.setEnrichedCoverUrl(edition.getCoverUrl());
            }

            // Categories: append genres from the edition when the book has no subjects
            if (book.getCategories().isEmpty()) {
                book.setEnrichedCategories(edition.getGenres());
            }

            // Language: fetch the language resource for its ISO code
            String languageId = edition.getLanguageId();
            if (languageId != null) {
                try {
                    ExternalLanguage lang = get("/languages/" + languageId, ExternalLanguage.class);
                    if (lang != null && lang.getIso639Code() != null) {
                        book.setLanguage(lang.getIso639Code());
                    }
                } catch (RestClientException langEx) {
                    log.warn("Could not fetch language {}: {}", languageId, langEx.getMessage());
                }
            }
        } catch (RestClientException ex) {
            log.warn("Could not enrich book {} from edition {}: {}", book.getId(), editionId, ex.getMessage());
        }
    }

    private <T> T get(String path, Class<T> type) {
        RestClient.RequestHeadersSpec<?> request = restClient.get().uri(path);
        if (StringUtils.hasText(apiKey)) {
            request = request.header("X-API-KEY", apiKey);
        }
        return request.retrieve().body(type);
    }
}
