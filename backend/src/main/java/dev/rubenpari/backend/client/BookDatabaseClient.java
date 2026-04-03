package dev.rubenpari.backend.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class BookDatabaseClient {
    private final RestClient restClient;
    private final String apiKey;

    public BookDatabaseClient(
            @Value("${app.bookdatabase.base-url}") String baseUrl,
            @Value("${app.bookdatabase.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public ExternalBook fetchRandom(Map<String, String> filters) {
        RestClient.RequestHeadersSpec<?> request = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/books/random");
                    filters.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                });

        if (StringUtils.hasText(apiKey)) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }

        return request.retrieve().body(ExternalBook.class);
    }
}
