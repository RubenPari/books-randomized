package dev.rubenpari.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class MailtrapService {
    private final RestClient restClient;
    private final String apiToken;
    private final String fromEmail;
    private final String fromName;

    public MailtrapService(
            @Value("${app.mailtrap.api-base-url}") String apiBaseUrl,
            @Value("${app.mailtrap.api-token}") String apiToken,
            @Value("${app.mailtrap.from-email}") String fromEmail,
            @Value("${app.mailtrap.from-name}") String fromName
    ) {
        this.restClient = RestClient.builder().baseUrl(apiBaseUrl).build();
        this.apiToken = apiToken;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public void sendEmail(String toEmail, String subject, String html) {
        if (!StringUtils.hasText(apiToken)) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "from", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "html", html
        );

        restClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
