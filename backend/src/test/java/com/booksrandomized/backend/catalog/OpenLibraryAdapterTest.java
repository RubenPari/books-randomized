package com.booksrandomized.backend.catalog;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.booksrandomized.backend.support.PostgresIntegrationTest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "catalog.open-library.user-agent=BooksRandomizedIntegrationTest/1.0",
            "spring.http.clients.connect-timeout=150ms",
            "spring.http.clients.read-timeout=150ms",
            "spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=1s"
        })
@AutoConfigureMockMvc
class OpenLibraryAdapterTest extends PostgresIntegrationTest {
    private static final TestUpstream UPSTREAM = TestUpstream.start();

    @Autowired CatalogClient catalog;
    @Autowired MockMvc mvc;

    @BeforeEach
    void resetUpstream() {
        UPSTREAM.reset();
    }

    @AfterAll
    static void stopUpstream() {
        UPSTREAM.stop();
    }

    @Test
    void normalizesUntrustedUpstreamFieldsAndEmitsConfiguredUserAgent() {
        List<Book> books = catalog.search("Dune", 5);

        assertThat(books).containsExactly(new Book(
                "OL1W", "Dune", List.of("Frank Herbert"), 1965,
                "https://covers.openlibrary.org/b/id/123-M.jpg",
                List.of("Science fiction", "Epic")));
        assertThat(UPSTREAM.userAgent()).isEqualTo("BooksRandomizedIntegrationTest/1.0");
    }

    @Test
    void hungUpstreamReturnsGatewayTimeoutWithinBound() throws Exception {
        long started = System.nanoTime();
        try {
            mvc.perform(get("/api/catalog/search").param("query", "hang").param("limit", "5"))
                    .andExpect(status().isGatewayTimeout())
                    .andExpect(jsonPath("$.title").value("Catalog timeout"));
        } finally {
            UPSTREAM.releaseHang();
        }

        assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(2));
    }

    @Test
    void upstream5xxReturnsBadGateway() throws Exception {
        mvc.perform(get("/api/catalog/search").param("query", "failure").param("limit", "5"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Catalog unavailable"));
    }

    @Test
    void nullBodyReturnsEmptyBooks() {
        assertThat(catalog.search("null-body", 5)).isEmpty();
    }

    @Test
    void nullDocsReturnsEmptyBooks() {
        assertThat(catalog.search("null-docs", 5)).isEmpty();
    }

    @Test
    void emptyDocsReturnsEmptyBooks() {
        assertThat(catalog.search("empty-docs", 5)).isEmpty();
    }

    @Test
    void malformedBodyReturnsBoundedUpstreamError() throws Exception {
        mvc.perform(get("/api/catalog/search").param("query", "malformed").param("limit", "5"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Catalog unavailable"));
    }

    @Test
    void sameNormalizedKeyUsesSpringCacheProxy() {
        catalog.search(" Cache-Hit ", 3);
        catalog.search("cache-hit", 3);

        assertThat(UPSTREAM.requests("cache-hit")).isOne();
    }

    @Test
    void expiredEntryIsRefetchedInsteadOfServingStaleData() {
        catalog.search("cache-expiry", 3);

        await().atMost(Duration.ofSeconds(3))
                .pollDelay(Duration.ofMillis(1100))
                .pollInterval(Duration.ofMillis(20))
                .untilAsserted(() -> {
                    catalog.search("cache-expiry", 3);
                    assertThat(UPSTREAM.requests("cache-expiry")).isEqualTo(2);
                });
    }

    @TestConfiguration
    static class LocalUpstreamConfiguration {
        @Bean
        RestClientCustomizer localUpstreamRouter() {
            return builder -> builder.requestInterceptor((request, body, execution) ->
                    execution.execute(new HttpRequestWrapper(request) {
                        @Override
                        public URI getURI() {
                            return URI.create(UPSTREAM.baseUrl()
                                    + request.getURI().getRawPath()
                                    + "?"
                                    + request.getURI().getRawQuery());
                        }
                    }, body));
        }
    }

    private static final class TestUpstream {
        private final HttpServer server;
        private final Map<String, Integer> requests = new ConcurrentHashMap<>();
        private final AtomicReference<String> userAgent = new AtomicReference<>();
        private volatile CountDownLatch hang = new CountDownLatch(1);

        private TestUpstream(HttpServer server) {
            this.server = server;
        }

        static TestUpstream start() {
            try {
                System.out.println(
                        "RESOURCE_REGISTER test_upstream=JDK-HttpServer host=127.0.0.1 port=ephemeral");
                HttpServer server = HttpServer.create(
                        new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
                TestUpstream upstream = new TestUpstream(server);
                server.createContext("/search.json", upstream::handle);
                server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
                server.start();
                System.out.println("RESOURCE_UPDATE test_upstream_port=" + server.getAddress().getPort());
                return upstream;
            } catch (IOException exception) {
                throw new IllegalStateException("Could not start local catalog test server", exception);
            }
        }

        String baseUrl() {
            return "http://127.0.0.1:" + server.getAddress().getPort();
        }

        String userAgent() {
            return userAgent.get();
        }

        int requests(String query) {
            return requests.getOrDefault(query, 0);
        }

        void reset() {
            requests.clear();
            userAgent.set(null);
            hang = new CountDownLatch(1);
        }

        void releaseHang() {
            hang.countDown();
        }

        void stop() {
            releaseHang();
            server.stop(0);
            System.out.println("CLEANUP_TEST_UPSTREAM port=" + server.getAddress().getPort());
        }

        private void handle(HttpExchange exchange) throws IOException {
            String query = queryParameters(exchange.getRequestURI()).get("q");
            requests.merge(query.trim().toLowerCase(), 1, Integer::sum);
            userAgent.set(exchange.getRequestHeaders().getFirst("User-Agent"));
            switch (query.trim().toLowerCase()) {
                case "hang" -> hang(exchange);
                case "failure" -> respond(exchange, 503, "{}");
                case "null-body" -> respond(exchange, 204, "");
                case "null-docs" -> respond(exchange, 200, "{\"docs\":null}");
                case "empty-docs" -> respond(exchange, 200, "{\"docs\":[]}");
                case "malformed" -> respond(exchange, 200, "{\"docs\":");
                default -> respond(exchange, 200, """
                        {"docs":[
                          {"key":"/works/OL1W","title":"  Dune\\n<script>  ",
                           "author_name":[" Frank   Herbert "],"first_publish_year":1965,
                           "cover_i":123,"subject":[" Science   fiction ","\\u0000Epic"]},
                          {"key":"","title":"   ","author_name":[]}
                        ]}
                        """);
            }
        }

        private void hang(HttpExchange exchange) throws IOException {
            try {
                hang.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            respond(exchange, 200, "{\"docs\":[]}");
        }

        private static Map<String, String> queryParameters(URI uri) {
            return Stream.of(uri.getRawQuery().split("&"))
                    .map(parameter -> parameter.split("=", 2))
                    .collect(Collectors.toMap(
                            pair -> URLDecoder.decode(pair[0], UTF_8),
                            pair -> URLDecoder.decode(pair[1], UTF_8)));
        }

        private static void respond(HttpExchange exchange, int status, String body) throws IOException {
            byte[] bytes = body.getBytes(UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }
}
