package dev.rubenpari.backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/** Builds the Google Cloud Translation v2 {@link RestClient} used by {@link dev.rubenpari.backend.service.TranslationService}. */
@Configuration
public class TranslatorConfig {

    @Bean
    @Qualifier("translateRestClient")
    RestClient translateRestClient(@Value("${app.translator.api-base-url}") String apiBaseUrl) {
        String normalized = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        if (!StringUtils.hasText(normalized)) {
            normalized = "https://translation.googleapis.com/language/translate/v2";
        } else if (!normalized.contains("/language/translate/v2")) {
            normalized = normalized + "/language/translate/v2";
        }
        return RestClient.builder().baseUrl(normalized).build();
    }
}
