package dev.rubenpari.backend.service;

import dev.rubenpari.backend.dto.VaultEntryRequest;
import dev.rubenpari.backend.model.Book;
import dev.rubenpari.backend.model.User;
import dev.rubenpari.backend.model.VaultEntry;
import dev.rubenpari.backend.repository.BookRepository;
import dev.rubenpari.backend.repository.UserRepository;
import dev.rubenpari.backend.repository.VaultEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Manages the user's personal book vault.
 * Supports adding, listing, removing, and bulk-importing vault entries.
 */
@Service
public class VaultService {
    private final VaultEntryRepository vaultEntryRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public VaultService(VaultEntryRepository vaultEntryRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.vaultEntryRepository = vaultEntryRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /** Saves a book to the user's vault. Throws if the book is already saved or does not exist. */
    @Transactional
    public VaultEntry addToVault(UUID userId, VaultEntryRequest request) {
        Book book = bookRepository.findByExternalId(request.externalBookId())
                .orElseThrow(() -> new IllegalStateException("Book not found"));
        vaultEntryRepository.findByUserIdAndExternalId(userId, request.externalBookId())
                .ifPresent(entry -> {
                    throw new IllegalStateException("Book already in vault");
                });
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        VaultEntry entry = new VaultEntry();
        entry.setUser(user);
        entry.setBook(book);
        entry.setNote(request.note());
        entry.setPersonalRating(request.personalRating());
        return vaultEntryRepository.save(entry);
    }

    public List<VaultEntry> listVault(UUID userId) {
        return vaultEntryRepository.findByUserId(userId);
    }

    /** Bulk-imports multiple vault entries, silently skipping duplicates and missing books. */
    @Transactional
    public void importEntries(UUID userId, List<VaultEntryRequest> entries) {
        for (VaultEntryRequest entry : entries) {
            try {
                addToVault(userId, entry);
            } catch (IllegalStateException ignored) {
                // Skip duplicates or missing books during import
            }
        }
    }

    /** Removes a vault entry after verifying that the authenticated user owns it. */
    public void removeFromVault(UUID userId, UUID entryId) {
        VaultEntry entry = vaultEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalStateException("Vault entry not found"));
        if (!entry.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Not allowed");
        }
        vaultEntryRepository.delete(entry);
    }
}
