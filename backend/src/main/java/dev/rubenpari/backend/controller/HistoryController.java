package dev.rubenpari.backend.controller;

import dev.rubenpari.backend.dto.BookMapper;
import dev.rubenpari.backend.dto.DiscoveryResponse;
import dev.rubenpari.backend.model.Discovery;
import dev.rubenpari.backend.security.AuthUserIds;
import dev.rubenpari.backend.service.DiscoveryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing the authenticated user's book discovery history
 * under {@code /api/history}.
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final DiscoveryService discoveryService;
    private final BookMapper bookMapper;

    public HistoryController(DiscoveryService discoveryService, BookMapper bookMapper) {
        this.discoveryService = discoveryService;
        this.bookMapper = bookMapper;
    }

    @GetMapping
    public List<DiscoveryResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = AuthUserIds.userId(userDetails);
        return discoveryService.listHistory(userId).stream().map(this::mapDiscovery).toList();
    }

    private DiscoveryResponse mapDiscovery(Discovery discovery) {
        return new DiscoveryResponse(
                discovery.getId().toString(),
                bookMapper.toResponse(discovery.getBook()),
                discovery.getSessionId(),
                discovery.getDiscoveredAt()
        );
    }
}
