package dev.rubenpari.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
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
            @Qualifier("translateRestClient") RestClient translateRestClient,
            @Value("${app.translator.api-key}") String apiKey
    ) {
        this.restClient = translateRestClient;
        this.apiKey = apiKey;
    }

    /** Plain text (titles, names, categories). */
    public String translatePlain(String text, String targetLanguage) {
        return translateSingle(text, targetLanguage, "text");
    }

    /** HTML snippets (e.g. ISBNdb descriptions with {@code <p>} tags). */
    public String translateHtml(String html, String targetLanguage) {
        return translateSingle(html, targetLanguage, "html");
    }

    /**
     * Translates many plain strings in one request (order preserved). Blank entries stay blank.
     */
    public List<String> translatePlainBatch(List<String> texts, String targetLanguage) {
        if (texts == null || texts.isEmpty()) {
            return texts == null ? List.of() : new ArrayList<>(texts);
        }
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(targetLanguage)) {
            return new ArrayList<>(texts);
        }

        List<Integer> nonBlankIndices = new ArrayList<>();
        List<String> payloadStrings = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String t = texts.get(i);
            if (StringUtils.hasText(t)) {
                nonBlankIndices.add(i);
                payloadStrings.add(t);
            }
        }
        if (payloadStrings.isEmpty()) {
            return new ArrayList<>(texts);
        }

        Map<String, Object> payload = Map.of(
                "q", payloadStrings,
                "target", targetLanguage,
                "format", "text");

        GoogleTranslateV2Response response = restClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(GoogleTranslateV2Response.class);

        List<String> translatedChunk = extractTranslations(response);
        if (translatedChunk == null || translatedChunk.size() != payloadStrings.size()) {
            return new ArrayList<>(texts);
        }

        List<String> out = new ArrayList<>(texts);
        for (int j = 0; j < nonBlankIndices.size(); j++) {
            String tr = translatedChunk.get(j);
            out.set(nonBlankIndices.get(j), tr != null ? tr : texts.get(nonBlankIndices.get(j)));
        }
        return out;
    }

    private String translateSingle(String text, String targetLanguage, String format) {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(targetLanguage) || !StringUtils.hasText(text)) {
            return text;
        }

        Map<String, Object> payload = Map.of(
                "q", text,
                "target", targetLanguage,
                "format", format);

        GoogleTranslateV2Response response = restClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(GoogleTranslateV2Response.class);

        List<String> list = extractTranslations(response);
        if (list == null || list.isEmpty()) {
            return text;
        }
        String translated = list.get(0);
        return translated != null ? translated : text;
    }

    private static List<String> extractTranslations(GoogleTranslateV2Response response) {
        if (response == null
                || response.data() == null
                || response.data().translations() == null
                || response.data().translations().isEmpty()) {
            return null;
        }
        return response.data().translations().stream()
                .map(GoogleTranslateItem::translatedText)
                .toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTranslateV2Response(GoogleTranslateData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTranslateData(List<GoogleTranslateItem> translations) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTranslateItem(String translatedText) {}
}
