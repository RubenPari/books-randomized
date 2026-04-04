package dev.rubenpari.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Translates text via an external translation API.
 * Returns the original text unchanged if the API key is not configured
 * or if the translation API returns no result.
 */
@Service
public class TranslationService {
    private final RestClient restClient;
    private final String apiKey;

    public TranslationService(
            @Value("${app.translator.api-base-url}") String apiBaseUrl,
            @Value("${app.translator.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(apiBaseUrl).build();
        this.apiKey = apiKey;
    }

    /** Translates the given text to the target language. Falls back to the original on failure. */
    public String translateDescription(String text, String targetLanguage) {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(targetLanguage)) {
            return text;
        }

        Map<String, Object> payload = Map.of(
                "q", text,
                "target", targetLanguage
        );

        TranslationResponse response = restClient.post()
                .uri("/translate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(TranslationResponse.class);

        if (response == null || response.translatedText() == null) {
            return text;
        }

        return response.translatedText();
    }

    /** DTO mapping the JSON response from the translation API. */
    public record TranslationResponse(String translatedText) {
    }
}
