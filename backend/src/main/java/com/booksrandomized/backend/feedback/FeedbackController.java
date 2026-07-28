package com.booksrandomized.backend.feedback;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
class FeedbackController {
    private final FeedbackService feedback;
    FeedbackController(FeedbackService feedback) { this.feedback = feedback; }

    @GetMapping
    List<FeedbackService.View> list(@AuthenticationPrincipal Jwt jwt) {
        return feedback.list(user(jwt));
    }

    @PostMapping
    FeedbackService.View create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request request) {
        return feedback.upsert(user(jwt), request.catalogBookId(), request.sentiment(), request.reason());
    }

    @PutMapping
    FeedbackService.View update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Request request) {
        return create(jwt, request);
    }

    private static UUID user(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    record Request(@NotBlank @Size(max = 100) String catalogBookId,
                   @NotBlank @Pattern(regexp = "LIKE|DISLIKE") String sentiment,
                   @Size(max = 500) String reason) {}
}
