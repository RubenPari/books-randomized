package dev.rubenpari.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Translates text via Google Cloud Translation API v2.
 * Returns the original text unchanged if the API key is not configured
 * or if the translation API returns no result.
 *
 * <p>Configure {@code TRANSLATE_API_BASE_URL} to the v2 endpoint root, e.g.
 * {@code https://translation.googleapis.com/language/translate/v2}, and
 * {@code TRANSLATE_API_KEY} to your Google API key (sent as the {@code key} query parameter).
 */
@Service
public class TranslationService {
    private final RestClient restClient;
    private final String apiKey;

    public TranslationService(
            @Value("${app.translator.api-base-url}") String apiBaseUrl,
            @Value("${app.translator.api-key}") String apiKey
    ) {
        String normalized = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        if (!StringUtils.hasText(normalized)) {
            normalized = "https://translation.googleapis.com/language/translate/v2";
        } else if (!normalized.contains("/language/translate/v2")) {
            normalized = normalized + "/language/translate/v2";
        }
        this.restClient = RestClient.builder().baseUrl(normalized).build();
        this.apiKey = apiKey;
    }

    /** Translates the given text to the target language. Falls back to the original on failure. */
    public String translateDescription(String text, String targetLanguage) {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(targetLanguage) || !StringUtils.hasText(text)) {
            return text;
        }

        Map<String, Object> payload = Map.of(
                "q", text,
                "target", targetLanguage,
                "format", "text");

        GoogleTranslateV2Response response = restClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(GoogleTranslateV2Response.class);

        if (response == null
                || response.data() == null
                || response.data().translations() == null
                || response.data().translations().isEmpty()) {
            return text;
        }
        String translated = response.data().translations().get(0).translatedText();
        return translated != null ? translated : text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTranslateV2Response(GoogleTranslateData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTranslateData(List<GoogleTranslateItem> translations) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTranslateItem(String translatedText) {}
}
