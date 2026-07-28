package com.booksrandomized.backend.catalog;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
class BookController {
    private final DiscoveryService discovery;
    BookController(DiscoveryService discovery) { this.discovery = discovery; }

    @PostMapping("/random")
    DiscoveryService.Result random(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) BookFilters filters) {
        BookFilters effective = filters == null
                ? new BookFilters(null, null, null, null, null, null, null, null) : filters;
        return discovery.random(UUID.fromString(jwt.getSubject()), effective);
    }
}
