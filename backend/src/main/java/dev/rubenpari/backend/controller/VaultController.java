package dev.rubenpari.backend.controller;

import dev.rubenpari.backend.dto.BookResponse;
import dev.rubenpari.backend.dto.VaultImportRequest;
import dev.rubenpari.backend.dto.VaultEntryRequest;
import dev.rubenpari.backend.dto.VaultEntryResponse;
import dev.rubenpari.backend.model.VaultEntry;
import dev.rubenpari.backend.service.VaultService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vault")
public class VaultController {
    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping
    public List<VaultEntryResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return vaultService.listVault(userId).stream().map(this::mapEntry).toList();
    }

    @PostMapping
    public VaultEntryResponse add(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody VaultEntryRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        VaultEntry entry = vaultService.addToVault(userId, request);
        return mapEntry(entry);
    }

    @DeleteMapping("/{entryId}")
    public void remove(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID entryId) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        vaultService.removeFromVault(userId, entryId);
    }

    @GetMapping("/export")
    public List<VaultEntryResponse> exportVault(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return vaultService.listVault(userId).stream().map(this::mapEntry).toList();
    }

    @PostMapping("/import")
    public void importVault(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody VaultImportRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        vaultService.importEntries(userId, request.entries());
    }

    private VaultEntryResponse mapEntry(VaultEntry entry) {
        BookResponse book = new BookResponse(
                entry.getBook().getId().toString(),
                entry.getBook().getExternalId(),
                entry.getBook().getTitle(),
                entry.getBook().getAuthors(),
                entry.getBook().getCategories(),
                entry.getBook().getLanguage(),
                entry.getBook().getRating(),
                entry.getBook().getPublicationYear(),
                entry.getBook().getDescription(),
                entry.getBook().getCoverUrl()
        );
        return new VaultEntryResponse(
                entry.getId().toString(),
                book,
                entry.getNote(),
                entry.getPersonalRating(),
                entry.getCreatedAt()
        );
    }
}
