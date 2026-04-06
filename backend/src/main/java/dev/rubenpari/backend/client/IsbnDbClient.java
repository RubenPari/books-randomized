package dev.rubenpari.backend.client;

import dev.rubenpari.backend.client.isbndb.IsbnDbApiBook;
import dev.rubenpari.backend.client.isbndb.IsbnDbBooksResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * REST client for ISBNdb API v2. Implements "random" discovery via paginated search
 * ({@code GET /books/{query}}) and a random page + row pick.
 *
 * @see <a href="https://api2.isbndb.com/doc.json">ISBNdb OpenAPI</a>
 */
@Component
public class IsbnDbClient {

    private static final Logger log = LoggerFactory.getLogger(IsbnDbClient.class);

    private static final int PAGE_SIZE = 30;
    private static final int MAX_PAGE_CAP = 50;

    private static final List<String> RANDOM_SEED_QUERIES = List.of(
            "novel", "history", "science", "poetry", "fiction", "biography", "art", "music",
            "philosophy", "nature", "travel", "cooking", "children", "fantasy", "mystery",
            "romance", "drama", "essay", "classic", "modern", "roman", "storia", "poesia",
            "romanzi", "saggi", "arte", "musica", "viaggio", "cucina", "bambini", "thriller",
            "avventura", "letteratura", "lingua", "matematica", "fisica", "medicina", "law",
            "economy", "politics", "religion", "psychology", "education", "sports", "garden"
    );

    /** Map common 2-letter UI / ISO 639-1 codes to ISBNdb language filters (often 3-letter). */
    private static final Map<String, String> LANGUAGE_TO_ISBNDB = Map.ofEntries(
            Map.entry("it", "ita"),
            Map.entry("en", "eng"),
            Map.entry("fr", "fra"),
            Map.entry("de", "deu"),
            Map.entry("es", "spa"),
            Map.entry("pt", "por"),
            Map.entry("nl", "nld"),
            Map.entry("pl", "pol"),
            Map.entry("ru", "rus"),
            Map.entry("ja", "jpn"),
            Map.entry("zh", "chi"),
            Map.entry("ko", "kor"),
            Map.entry("sv", "swe"),
            Map.entry("no", "nor"),
            Map.entry("da", "dan"),
            Map.entry("fi", "fin"),
            Map.entry("el", "gre"),
            Map.entry("tr", "tur"),
            Map.entry("cs", "cze"),
            Map.entry("hu", "hun"),
            Map.entry("ro", "rum")
    );

    private final RestClient restClient;
    private final String apiKey;

    public IsbnDbClient(
            @Value("${app.isbndb.base-url}") String baseUrl,
            @Value("${app.isbndb.api-key}") String apiKey
    ) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restClient = RestClient.builder().baseUrl(normalized).build();
        this.apiKey = apiKey;
    }

    /**
     * Runs one search + random pick. Caller ({@link dev.rubenpari.backend.service.BookService})
     * retries on duplicates / empty outcomes.
     */
    public ExternalBook fetchRandom(Map<String, String> filters) {
        if (StringUtils.hasText(filters.get("minRating"))) {
            log.debug("ISBNdb has no ratings; ignoring minRating={}", filters.get("minRating"));
        }

        String chosenSubject = pickOneCategorySearchTerm(filters.get("category"));
        String searchQuery = chosenSubject != null ? chosenSubject : randomSeedQuery();
        String column = chosenSubject != null ? "subjects" : null;
        String languageParam = toIsbnDbLanguage(trimToNull(filters.get("language")));
        Integer yearParam = pickYearFilter(filters);

        ExternalBook picked = pickRandomFromSearch(searchQuery, column, languageParam, yearParam);
        if (picked == null && languageParam != null) {
            log.debug(
                    "ISBNdb search yielded no book with language={}; retrying without language filter",
                    languageParam
            );
            picked = pickRandomFromSearch(searchQuery, column, null, yearParam);
        }
        return picked;
    }

    /**
     * One search flow: first page (and optionally a random later page), then random row with ISBN.
     */
    private ExternalBook pickRandomFromSearch(
            String searchQuery,
            String column,
            String language,
            Integer year
    ) {
        IsbnDbBooksResponse first = searchBooks(searchQuery, column, language, year, 1);
        if (first == null || first.getData() == null || first.getData().isEmpty()) {
            return null;
        }

        int total = Math.max(first.getTotal(), first.getData().size());
        int pageCount = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int maxPage = Math.min(MAX_PAGE_CAP, pageCount);
        int page = maxPage == 1 ? 1 : ThreadLocalRandom.current().nextInt(1, maxPage + 1);

        IsbnDbBooksResponse pageResponse = first;
        if (page > 1) {
            pageResponse = searchBooks(searchQuery, column, language, year, page);
            if (pageResponse == null || pageResponse.getData() == null || pageResponse.getData().isEmpty()) {
                pageResponse = first;
            }
        }

        List<IsbnDbApiBook> pool = booksWithResolvableId(pageResponse.getData());
        if (pool.isEmpty()) {
            pool = booksWithResolvableId(first.getData());
        }
        if (pool.isEmpty()) {
            return null;
        }

        IsbnDbApiBook choice = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        return ExternalBook.fromIsbnDb(choice);
    }

    private static String randomSeedQuery() {
        return RANDOM_SEED_QUERIES.get(ThreadLocalRandom.current().nextInt(RANDOM_SEED_QUERIES.size()));
    }

    private IsbnDbBooksResponse searchBooks(
            String query,
            String column,
            String language,
            Integer year,
            int page
    ) {
        try {
            RestClient.RequestHeadersSpec<?> spec = restClient.get()
                    .uri(uriBuilder -> buildBooksUri(uriBuilder, query, column, language, year, page));
            if (StringUtils.hasText(apiKey)) {
                spec = spec.header("Authorization", apiKey);
            }
            return spec.retrieve().body(IsbnDbBooksResponse.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                log.debug("ISBNdb search returned 404 for query page {}: {}", page, ex.getResponseBodyAsString());
                return null;
            }
            log.error("ISBNdb API returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        } catch (RestClientException ex) {
            log.error("ISBNdb request failed: {}", ex.getMessage());
            throw ex;
        }
    }

    private static java.net.URI buildBooksUri(
            UriBuilder uriBuilder,
            String query,
            String column,
            String language,
            Integer year,
            int page
    ) {
        UriBuilder ub = uriBuilder.path("/books").pathSegment(query);
        if (StringUtils.hasText(column)) {
            ub.queryParam("column", column);
        }
        if (StringUtils.hasText(language)) {
            ub.queryParam("language", language);
        }
        if (year != null) {
            ub.queryParam("year", year);
        }
        ub.queryParam("page", page);
        ub.queryParam("pageSize", PAGE_SIZE);
        return ub.build();
    }

    private static List<IsbnDbApiBook> booksWithResolvableId(List<IsbnDbApiBook> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<IsbnDbApiBook> out = new ArrayList<>();
        for (IsbnDbApiBook b : raw) {
            if (ExternalBook.extractIsbnId(b) != null) {
                out.add(b);
            }
        }
        return out;
    }

    /**
     * Client may send several subject filters as a comma-separated list (multiselect).
     * ISBNdb search uses one query per request; pick one term at random per fetch.
     */
    private static String pickOneCategorySearchTerm(String commaSeparated) {
        if (!StringUtils.hasText(commaSeparated)) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        for (String part : commaSeparated.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                parts.add(t);
            }
        }
        if (parts.isEmpty()) {
            return null;
        }
        return parts.get(ThreadLocalRandom.current().nextInt(parts.size()));
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String toIsbnDbLanguage(String code) {
        if (code == null) {
            return null;
        }
        String c = code.trim().toLowerCase(Locale.ROOT);
        if (c.length() == 3 && c.chars().allMatch(Character::isLetter)) {
            return c;
        }
        if (c.length() == 2) {
            return LANGUAGE_TO_ISBNDB.getOrDefault(c, c);
        }
        return c;
    }

    private static Integer pickYearFilter(Map<String, String> filters) {
        String fromRaw = trimToNull(filters.get("yearFrom"));
        String toRaw = trimToNull(filters.get("yearTo"));
        if (fromRaw == null && toRaw == null) {
            return null;
        }
        int now = Year.now().getValue();
        int yFrom;
        int yTo;
        if (fromRaw != null && toRaw != null) {
            yFrom = Integer.parseInt(fromRaw);
            yTo = Integer.parseInt(toRaw);
        } else if (fromRaw != null) {
            yFrom = Integer.parseInt(fromRaw);
            yTo = now;
        } else {
            yFrom = 1900;
            yTo = Integer.parseInt(toRaw);
        }
        if (yFrom > yTo) {
            int t = yFrom;
            yFrom = yTo;
            yTo = t;
        }
        yFrom = Math.min(yFrom, now);
        yTo = Math.min(yTo, now);
        if (yFrom > yTo) {
            return yFrom;
        }
        return ThreadLocalRandom.current().nextInt(yFrom, yTo + 1);
    }
}
