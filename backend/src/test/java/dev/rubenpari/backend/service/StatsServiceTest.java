package dev.rubenpari.backend.service;

import dev.rubenpari.backend.model.Book;
import dev.rubenpari.backend.model.Discovery;
import dev.rubenpari.backend.model.VaultEntry;
import dev.rubenpari.backend.repository.DiscoveryRepository;
import dev.rubenpari.backend.repository.VaultEntryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatsServiceTest {
    @Test
    void computesBasicStats() {
        DiscoveryRepository discoveryRepository = mock(DiscoveryRepository.class);
        VaultEntryRepository vaultEntryRepository = mock(VaultEntryRepository.class);
        StatsService statsService = new StatsService(discoveryRepository, vaultEntryRepository);

        Book book = new Book();
        book.setExternalId("ext-1");
        book.setTitle("Test");
        book.setAuthors(Set.of("Author A"));
        book.setCategories(Set.of("Fantasy"));
        book.setLanguage("it");
        book.setRating(4.5);

        Discovery discovery = new Discovery();
        discovery.setBook(book);

        VaultEntry vaultEntry = new VaultEntry();
        vaultEntry.setBook(book);

        UUID userId = UUID.randomUUID();
        when(discoveryRepository.findByUserId(userId)).thenReturn(List.of(discovery));
        when(vaultEntryRepository.findByUserId(userId)).thenReturn(List.of(vaultEntry));

        var response = statsService.computeStats(userId);
        assertEquals(1, response.totalDiscovered());
        assertEquals(1, response.totalVaulted());
        assertEquals(4.5, response.averageRating());
        assertEquals(1, response.byCategory().get("Fantasy"));
        assertEquals(1, response.byAuthor().get("Author A"));
        assertEquals(1, response.byLanguage().get("it"));
    }
}
