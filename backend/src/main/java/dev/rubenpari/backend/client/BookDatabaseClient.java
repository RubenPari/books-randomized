package dev.rubenpari.backend.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * REST client for the external book database API.
 * Fetches random books with optional query-parameter filters and Bearer token authentication.
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

    /** Calls {@code GET /books/random} on the external API, forwarding all filter params. */
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

            return request.retrieve().body(ExternalBook.class);
        } catch (RestClientResponseException ex) {
            log.error("BookDatabase API returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
