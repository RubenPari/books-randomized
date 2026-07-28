package com.booksrandomized.backend.collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("/api/discovered")
class DiscoveredController {
    private final CollectionService collections;
    DiscoveredController(CollectionService collections) { this.collections = collections; }

    @GetMapping
    List<CollectionService.DiscoveryView> list(@AuthenticationPrincipal Jwt jwt) {
        return collections.discovered(user(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CollectionService.DiscoveryView create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Save request) {
        return collections.discover(user(jwt), request.catalogBookId());
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable @NotBlank @Size(max = 100) String bookId) {
        collections.removeDiscovered(user(jwt), bookId);
    }

    private static UUID user(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    record Save(@NotBlank @Size(max = 100) String catalogBookId) {}
}
