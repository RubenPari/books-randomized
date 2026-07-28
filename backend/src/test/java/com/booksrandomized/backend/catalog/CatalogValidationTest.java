package com.booksrandomized.backend.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.booksrandomized.backend.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogValidationTest extends PostgresIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void malformedLimitIsAProblemDetail() throws Exception {
        mvc.perform(get("/api/catalog/search").param("query", "dune").param("limit", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void missingQueryIsAProblemDetail() throws Exception {
        mvc.perform(get("/api/catalog/search"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/problem+json"));
    }

    @Test
    void nonnumericLimitIsAProblemDetail() throws Exception {
        mvc.perform(get("/api/catalog/search").param("query", "dune").param("limit", "many"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/problem+json"));
    }
}
