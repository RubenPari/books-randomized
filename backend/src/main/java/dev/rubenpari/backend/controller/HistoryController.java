package dev.rubenpari.backend.controller;

import dev.rubenpari.backend.dto.BookResponse;
import dev.rubenpari.backend.dto.DiscoveryResponse;
import dev.rubenpari.backend.model.Discovery;
import dev.rubenpari.backend.service.DiscoveryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final DiscoveryService discoveryService;

    public HistoryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping
    public List<DiscoveryResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return discoveryService.listHistory(userId).stream().map(this::mapDiscovery).toList();
    }

    private DiscoveryResponse mapDiscovery(Discovery discovery) {
        BookResponse book = new BookResponse(
                discovery.getBook().getId().toString(),
                discovery.getBook().getExternalId(),
                discovery.getBook().getTitle(),
                discovery.getBook().getAuthors(),
                discovery.getBook().getCategories(),
                discovery.getBook().getLanguage(),
                discovery.getBook().getRating(),
                discovery.getBook().getPublicationYear(),
                discovery.getBook().getDescription(),
                discovery.getBook().getCoverUrl()
        );
        return new DiscoveryResponse(
                discovery.getId().toString(),
                book,
                discovery.getSessionId(),
                discovery.getDiscoveredAt()
        );
    }
}
