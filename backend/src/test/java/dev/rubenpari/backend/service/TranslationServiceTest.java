package dev.rubenpari.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TranslationServiceTest {

    @Test
    void returnsOriginalWhenApiKeyMissing() {
        RestClient client = RestClient.builder().baseUrl("https://translate.test").build();
        TranslationService svc = new TranslationService(client, "");
        assertEquals("hello", svc.translatePlain("hello", "it"));
    }

    @Test
    void translatePlain_postsAndParsesResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://translate.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        TranslationService svc = new TranslationService(client, "secret");

        server.expect(requestTo(containsString("key=secret")))
                .andExpect(content().string(containsString("\"q\":\"hello\"")))
                .andRespond(
                        withSuccess(
                                "{\"data\":{\"translations\":[{\"translatedText\":\"ciao\"}]}}",
                                MediaType.APPLICATION_JSON));

        assertEquals("ciao", svc.translatePlain("hello", "it"));
        server.verify();
    }

    @Test
    void translatePlainBatch_keepsOrder() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://translate.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        TranslationService svc = new TranslationService(client, "secret");

        server.expect(requestTo(containsString("key=secret")))
                .andRespond(
                        withSuccess(
                                "{\"data\":{\"translations\":[{\"translatedText\":\"A\"},{\"translatedText\":\"B\"}]}}",
                                MediaType.APPLICATION_JSON));

        List<String> out = svc.translatePlainBatch(List.of("a", "b"), "it");
        assertEquals(List.of("A", "B"), out);
        server.verify();
    }
}
