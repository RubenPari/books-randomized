package com.booksrandomized.backend.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.booksrandomized.backend.support.PostgresIntegrationTest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DatabaseMigrationTest extends PostgresIntegrationTest {
    @Autowired DataSource dataSource;

    @Test
    void flywayCreatesEveryFoundationTable() throws Exception {
        Set<String> tables = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             ResultSet rows = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            while (rows.next()) {
                tables.add(rows.getString("TABLE_NAME").toLowerCase());
            }
        }
        assertThat(tables).contains(
                "users", "refresh_tokens", "reading_list_items", "discovered_books",
                "recommendation_feedback", "catalog_cache");
    }
}
