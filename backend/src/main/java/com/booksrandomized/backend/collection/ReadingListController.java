package com.booksrandomized.backend.collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/reading-list")
class ReadingListController {
    private final CollectionService collections;
    ReadingListController(CollectionService collections) { this.collections = collections; }

    @GetMapping
    List<CollectionService.ReadingView> list(@AuthenticationPrincipal Jwt jwt) {
        return collections.reading(user(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CollectionService.ReadingView create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Save request) {
        return collections.save(user(jwt), request.catalogBookId(), request.status(),
                request.title() == null ? "" : request.title(),
                request.authors() == null ? List.of() : request.authors());
    }

    @PutMapping("/{bookId}")
    CollectionService.ReadingView update(@AuthenticationPrincipal Jwt jwt,
            @PathVariable @NotBlank @Size(max = 100) String bookId, @Valid @RequestBody Status request) {
        return collections.save(user(jwt), bookId, request.status(), "", List.of());
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable @NotBlank @Size(max = 100) String bookId) {
        collections.removeReading(user(jwt), bookId);
    }

    private static UUID user(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    record Save(@NotBlank @Size(max = 100) String catalogBookId,
                @NotBlank @Pattern(regexp = "WANT_TO_READ|READING|READ") String status,
                @Size(max = 500) String title,
                @Size(max = 20) List<@Size(max = 200) String> authors) {}
    record Status(@NotBlank @Pattern(regexp = "WANT_TO_READ|READING|READ") String status) {}
}
