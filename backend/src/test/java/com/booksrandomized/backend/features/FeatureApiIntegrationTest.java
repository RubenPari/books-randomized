package com.booksrandomized.backend.features;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.booksrandomized.backend.auth.PasswordResetNotifier;
import com.booksrandomized.backend.catalog.Book;
import com.booksrandomized.backend.catalog.CatalogClient;
import com.booksrandomized.backend.catalog.UpstreamCatalogException;
import com.booksrandomized.backend.support.PostgresIntegrationTest;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class FeatureApiIntegrationTest extends PostgresIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired StubCatalog catalog;
    @Autowired CapturingNotifier notifier;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void resetDoubles() {
        catalog.reset();
        notifier.token.set(null);
        catalog.failure = null;
    }

    @Test
    void registrationCreatesAnAuthenticatedSessionWithoutReturningARefreshToken() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"reader@example.test","password":"correct horse battery staple"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.user.email").value("reader@example.test"));
    }

    @Test
    void refreshRequiresCsrfRotatesAndRejectsReuse() throws Exception {
        Session registered = register("rotation@example.test");

        mvc.perform(post("/api/auth/refresh").cookie(registered.refresh()))
                .andExpect(status().isForbidden());

        MvcResult rotated = mvc.perform(post("/api/auth/refresh")
                        .cookie(registered.refresh()).with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(rotated.getResponse().getCookie("refresh_token").getValue())
                .isNotEqualTo(registered.refresh().getValue());

        mvc.perform(post("/api/auth/refresh").cookie(registered.refresh()).with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("refresh-reuse"));
    }

    @Test
    void resetAndChangePasswordInvalidateOldCredentialsAndRefreshSessions() throws Exception {
        Session session = register("passwords@example.test");
        mvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"passwords@example.test\"}"))
                .andExpect(status().isAccepted());
        String token = notifier.token.get();
        assertThat(token).isNotBlank();
        assertThat(jdbc.queryForObject("""
                select p.token_hash from password_reset_tokens p
                join users u on u.id=p.user_id where u.email='passwords@example.test'
                """, String.class))
                .doesNotContain(token);
        mvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Reset(token, "replacement password 1"))))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("passwords@example.test", "correct horse battery staple")))
                .andExpect(status().isUnauthorized());
        Session loggedIn = login("passwords@example.test", "replacement password 1");
        mvc.perform(post("/api/auth/change-password")
                        .header("Authorization", bearer(loggedIn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"replacement password 1\","
                                + "\"newPassword\":\"replacement password 2\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").cookie(loggedIn.refresh()).with(csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", bearer(session)))
                .andExpect(status().isOk());
    }

    @Test
    void expiredResetTokensAreRejectedAndLogoutRevokesAndClearsRefreshCookie() throws Exception {
        Session session = register("expiry@example.test");
        mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"expiry@example.test\"}"))
                .andExpect(status().isAccepted());
        String reset = notifier.token.get();
        jdbc.update("update password_reset_tokens set expires_at=now()-interval '1 minute'");
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Reset(reset, "replacement password 1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("invalid-reset-token"));

        MvcResult logout = mvc.perform(post("/api/auth/logout")
                        .cookie(session.refresh()).with(csrf()))
                .andExpect(status().isNoContent()).andReturn();
        assertThat(logout.getResponse().getCookie("refresh_token").getMaxAge()).isZero();
        mvc.perform(post("/api/auth/refresh").cookie(session.refresh()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void collectionsAreIdempotentAndIsolatedByJwtOwner() throws Exception {
        Session alice = register("alice@example.test");
        Session bob = register("bob@example.test");
        String save = "{\"catalogBookId\":\"OL-PRIVATE\",\"status\":\"WANT_TO_READ\"}";
        mvc.perform(post("/api/reading-list").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(save))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/reading-list").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(save))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/reading-list").header("Authorization", bearer(alice)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(delete("/api/reading-list/OL-PRIVATE").header("Authorization", bearer(bob)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/reading-list").header("Authorization", bearer(alice)))
                .andExpect(jsonPath("$[0].catalogBookId").value("OL-PRIVATE"));
        mvc.perform(post("/api/reading-list").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catalogBookId\":\"X\",\"status\":\"READ\","
                                + "\"userId\":\"00000000-0000-0000-0000-000000000000\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void concurrentDuplicateCreatesStillProduceOneReadingListRow() throws Exception {
        Session session = register("concurrent@example.test");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, 8).mapToObj(index -> executor.submit(() ->
                    mvc.perform(post("/api/reading-list").header("Authorization", bearer(session))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"catalogBookId\":\"RACE\",\"status\":\"READING\"}"))
                            .andExpect(status().isCreated()))).toList();
            for (var future : futures) future.get();
        }
        mvc.perform(get("/api/reading-list").header("Authorization", bearer(session)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void discoveredCrudAndFeedbackUpsertRemainSingleRow() throws Exception {
        Session session = register("crud@example.test");
        mvc.perform(post("/api/discovered").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catalogBookId\":\"CRUD\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/feedback").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catalogBookId\":\"CRUD\",\"sentiment\":\"LIKE\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/feedback").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catalogBookId\":\"CRUD\",\"sentiment\":\"DISLIKE\",\"reason\":\"not for me\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentiment").value("DISLIKE"));
        mvc.perform(get("/api/feedback").header("Authorization", bearer(session)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(delete("/api/discovered/CRUD").header("Authorization", bearer(session)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/discovered").header("Authorization", bearer(session)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void malformedAuthAndFilterBodiesReturnProblems() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:invalid-request"));
        Session session = register("validation@example.test");
        mvc.perform(post("/api/books/random").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"minimumRating\":99,\"subjects\":[\"History\"],\"unexpected\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:invalid-request"));
    }

    @Test
    void randomDiscoveryFiltersPersistsAndNeverReturnsSavedOrDiscoveredBooks() throws Exception {
        Session session = register("discover@example.test");
        mvc.perform(post("/api/reading-list").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catalogBookId\":\"SAVED\",\"status\":\"READ\"}"))
                .andExpect(status().isCreated());
        catalog.responses.add(List.of(
                book("SAVED", "History", 4.8, 200, 320),
                book("MATCH", "History", 4.7, 120, 280),
                book("WRONG", "Science", 4.9, 500, 400)));
        mvc.perform(post("/api/books/random").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"eng","subjects":["History","Biography"],
                                 "publishedFrom":1900,"publishedTo":2026,
                                 "minimumRating":4.5,"minimumRatingsCount":100,
                                 "minimumPages":200,"maximumPages":350}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.book.id").value("MATCH"))
                .andExpect(jsonPath("$.explanationKeys[0]").value("discovery.explanation.filters"));
        mvc.perform(get("/api/discovered").header("Authorization", bearer(session)))
                .andExpect(jsonPath("$[0].catalogBookId").value("MATCH"))
                .andExpect(jsonPath("$[0].title").value("MATCH title"))
                .andExpect(jsonPath("$[0].authors[0]").value("Author"));
        mvc.perform(post("/api/reading-list").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"catalogBookId":"MATCH","status":"WANT_TO_READ",
                                 "title":"MATCH title","authors":["Author"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("MATCH title"))
                .andExpect(jsonPath("$.authors[0]").value("Author"));
        mvc.perform(get("/api/reading-list").header("Authorization", bearer(session)))
                .andExpect(jsonPath("$[?(@.catalogBookId=='MATCH')].title").value("MATCH title"));
    }

    @Test
    void collectionBookIdsAreCanonicalizedAcrossWorksPrefix() throws Exception {
        Session session = register("canonical@example.test");
        mvc.perform(post("/api/reading-list").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"catalogBookId":"/works/OL123W","status":"WANT_TO_READ",
                                 "title":"Canonical","authors":["Ursula K. Le Guin"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.catalogBookId").value("OL123W"))
                .andExpect(jsonPath("$.title").value("Canonical"));
        mvc.perform(get("/api/reading-list").header("Authorization", bearer(session)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].catalogBookId").value("OL123W"))
                .andExpect(jsonPath("$[0].authors[0]").value("Ursula K. Le Guin"));
        mvc.perform(delete("/api/reading-list/OL123W").header("Authorization", bearer(session)))
                .andExpect(status().isNoContent());
    }

    @Test
    void randomDiscoveryIsBoundedValidatesRangesAndPersonalizesFromFeedback() throws Exception {
        Session session = register("personalized@example.test");
        mvc.perform(post("/api/books/random").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publishedFrom\":2020,\"publishedTo\":1900}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("invalid-filter-range"));

        catalog.responses.addAll(List.of(List.of(), List.of(), List.of(), List.of(), List.of()));
        mvc.perform(post("/api/books/random").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("no-book-match"));
        assertThat(catalog.calls).isEqualTo(5);
        assertThat(catalog.batches).hasSize(5).doesNotContainSequence(1, 2, 3, 4, 5);

        catalog.reset();
        catalog.responses.add(List.of(book("LIKED", "History", 4.0, 20, 200)));
        mvc.perform(post("/api/books/random").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/feedback").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catalogBookId\":\"LIKED\",\"sentiment\":\"LIKE\",\"reason\":\"great\"}"))
                .andExpect(status().isOk());
        catalog.reset();
        catalog.responses.add(List.of(
                book("SCIENCE", "Science", 4.0, 20, 200),
                book("HISTORY", "History", 4.0, 20, 200)));
        mvc.perform(post("/api/books/random").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.book.id").value("HISTORY"))
                .andExpect(jsonPath("$.explanationKeys[1]").value("discovery.explanation.personalized"));
    }

    @Test
    void randomDiscoveryPropagatesTimeoutWithoutRetryingOrLeakingDetails() throws Exception {
        Session session = register("timeout@example.test");
        catalog.failure = UpstreamCatalogException.timeout(new IllegalStateException("upstream secret"));
        mvc.perform(post("/api/books/random").header("Authorization", bearer(session))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.title").value("Catalog timeout"))
                .andExpect(jsonPath("$.detail").value("The catalog service timed out"));
        assertThat(catalog.calls).isOne();
    }

    private Session register(String email) throws Exception {
        return session(mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, "correct horse battery staple")))
                .andExpect(status().isCreated()).andReturn());
    }

    private Session login(String email, String password) throws Exception {
        return session(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, password)))
                .andExpect(status().isOk()).andReturn());
    }

    private Session session(MvcResult result) throws Exception {
        JsonNode body = json.readTree(result.getResponse().getContentAsString());
        return new Session(body.path("accessToken").asText(), result.getResponse().getCookie("refresh_token"));
    }

    private String credentials(String email, String password) throws Exception {
        return json.writeValueAsString(new Credentials(email, password));
    }

    private static String bearer(Session session) { return "Bearer " + session.access(); }

    private static Book book(String id, String subject, double rating, int ratings, int pages) {
        return new Book(id, id + " title", List.of("Author"), 2000, null,
                List.of(subject), List.of("eng"), rating, ratings, pages);
    }

    record Credentials(String email, String password) {}
    record Reset(String token, String newPassword) {}
    record Session(String access, Cookie refresh) {}

    @TestConfiguration
    static class Doubles {
        @Bean @Primary StubCatalog catalog() { return new StubCatalog(); }
        @Bean @Primary CapturingNotifier notifier() { return new CapturingNotifier(); }
    }

    static final class StubCatalog implements CatalogClient {
        final List<List<Book>> responses = new ArrayList<>();
        int calls;
        final List<Integer> batches = new ArrayList<>();
        RuntimeException failure;
        void reset() { responses.clear(); batches.clear(); calls = 0; failure = null; }
        @Override public List<Book> search(String query, int limit) { return searchBatch(query, limit, 1); }
        @Override public List<Book> searchBatch(String query, int limit, int batch) {
            int index = calls++;
            batches.add(batch);
            if (failure != null) throw failure;
            return index < responses.size() ? responses.get(index) : List.of();
        }
    }

    static final class CapturingNotifier implements PasswordResetNotifier {
        final AtomicReference<String> token = new AtomicReference<>();
        @Override public void send(String email, String token) { this.token.set(token); }
    }
}
