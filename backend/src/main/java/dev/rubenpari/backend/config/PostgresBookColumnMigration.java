package dev.rubenpari.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Hibernate {@code ddl-auto=update} does not always widen existing PostgreSQL {@code varchar(255)}
 * columns when JPA mappings change. Aligns {@code books} and element-collection tables so long
 * descriptions (e.g. translations) and URLs persist.
 */
@Component
@Order(0)
public class PostgresBookColumnMigration implements ApplicationRunner {

    private final DataSource dataSource;

    public PostgresBookColumnMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            if (!"PostgreSQL".equalsIgnoreCase(c.getMetaData().getDatabaseProductName())) {
                return;
            }
        }
        String sql =
                """
                DO $migration$
                BEGIN
                  IF to_regclass('public.books') IS NOT NULL THEN
                    ALTER TABLE books ALTER COLUMN description TYPE TEXT;
                    ALTER TABLE books ALTER COLUMN cover_url TYPE VARCHAR(2048);
                    ALTER TABLE books ALTER COLUMN title TYPE VARCHAR(1024);
                  END IF;
                  IF to_regclass('public.book_authors') IS NOT NULL THEN
                    ALTER TABLE book_authors ALTER COLUMN author TYPE VARCHAR(512);
                  END IF;
                  IF to_regclass('public.book_categories') IS NOT NULL THEN
                    ALTER TABLE book_categories ALTER COLUMN category TYPE VARCHAR(512);
                  END IF;
                END
                $migration$""";
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to widen book-related columns for PostgreSQL", e);
        }
    }
}
