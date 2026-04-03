package dev.rubenpari.backend.service;

import dev.rubenpari.backend.dto.StatsResponse;
import dev.rubenpari.backend.model.Book;
import dev.rubenpari.backend.model.Discovery;
import dev.rubenpari.backend.model.VaultEntry;
import dev.rubenpari.backend.repository.DiscoveryRepository;
import dev.rubenpari.backend.repository.VaultEntryRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StatsService {
    private final DiscoveryRepository discoveryRepository;
    private final VaultEntryRepository vaultEntryRepository;

    public StatsService(DiscoveryRepository discoveryRepository, VaultEntryRepository vaultEntryRepository) {
        this.discoveryRepository = discoveryRepository;
        this.vaultEntryRepository = vaultEntryRepository;
    }

    public StatsResponse computeStats(UUID userId) {
        List<Discovery> discoveries = discoveryRepository.findByUserId(userId);
        List<VaultEntry> vaultEntries = vaultEntryRepository.findByUserId(userId);

        Map<String, Long> byCategory = new HashMap<>();
        Map<String, Long> byAuthor = new HashMap<>();
        Map<String, Long> byLanguage = new HashMap<>();

        double ratingSum = 0;
        long ratingCount = 0;

        for (Discovery discovery : discoveries) {
            Book book = discovery.getBook();
            if (book.getCategories() != null) {
                for (String category : book.getCategories()) {
                    byCategory.merge(category, 1L, Long::sum);
                }
            }
            if (book.getAuthors() != null) {
                for (String author : book.getAuthors()) {
                    byAuthor.merge(author, 1L, Long::sum);
                }
            }
            if (book.getLanguage() != null) {
                byLanguage.merge(book.getLanguage(), 1L, Long::sum);
            }
            if (book.getRating() != null) {
                ratingSum += book.getRating();
                ratingCount += 1;
            }
        }

        double averageRating = ratingCount == 0 ? 0 : ratingSum / ratingCount;

        return new StatsResponse(
                discoveries.size(),
                vaultEntries.size(),
                averageRating,
                byCategory,
                byAuthor,
                byLanguage
        );
    }
}
