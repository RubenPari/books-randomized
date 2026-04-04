package dev.rubenpari.backend.repository;

import dev.rubenpari.backend.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data repository for {@link Book} entities. Supports lookup by external API identifier. */
public interface BookRepository extends JpaRepository<Book, UUID> {
    Optional<Book> findByExternalId(String externalId);
}
