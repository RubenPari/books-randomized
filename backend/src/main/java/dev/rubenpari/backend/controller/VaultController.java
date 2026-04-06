package dev.rubenpari.backend.controller;

import dev.rubenpari.backend.dto.BookMapper;
import dev.rubenpari.backend.dto.VaultImportRequest;
import dev.rubenpari.backend.dto.VaultEntryRequest;
import dev.rubenpari.backend.dto.VaultEntryResponse;
import dev.rubenpari.backend.model.VaultEntry;
import dev.rubenpari.backend.security.AuthUserIds;
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

/**
 * REST controller for the user's book vault under {@code /api/vault}.
 * Supports listing, adding, removing, exporting, and bulk-importing entries.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/vault")
public class VaultController {
    private final VaultService vaultService;
    private final BookMapper bookMapper;

    public VaultController(VaultService vaultService, BookMapper bookMapper) {
        this.vaultService = vaultService;
        this.bookMapper = bookMapper;
    }

    @GetMapping
    public List<VaultEntryResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = AuthUserIds.userId(userDetails);
        return vaultService.listVault(userId).stream().map(this::mapEntry).toList();
    }

    @PostMapping
    public VaultEntryResponse add(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody VaultEntryRequest request) {
        UUID userId = AuthUserIds.userId(userDetails);
        VaultEntry entry = vaultService.addToVault(userId, request);
        return mapEntry(entry);
    }

    @DeleteMapping("/{entryId}")
    public void remove(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID entryId) {
        UUID userId = AuthUserIds.userId(userDetails);
        vaultService.removeFromVault(userId, entryId);
    }

    @GetMapping("/export")
    public List<VaultEntryResponse> exportVault(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = AuthUserIds.userId(userDetails);
        return vaultService.listVault(userId).stream().map(this::mapEntry).toList();
    }

    @PostMapping("/import")
    public void importVault(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody VaultImportRequest request) {
        UUID userId = AuthUserIds.userId(userDetails);
        vaultService.importEntries(userId, request.entries());
    }

    private VaultEntryResponse mapEntry(VaultEntry entry) {
        return new VaultEntryResponse(
                entry.getId().toString(),
                bookMapper.toResponse(entry.getBook()),
                entry.getNote(),
                entry.getPersonalRating(),
                entry.getCreatedAt()
        );
    }
}
