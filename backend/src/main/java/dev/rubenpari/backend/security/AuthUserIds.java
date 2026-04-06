package dev.rubenpari.backend.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

/** Resolves the authenticated user's id from {@link UserDetails#getUsername()} (JWT subject). */
public final class AuthUserIds {

    private AuthUserIds() {}

    public static UUID userId(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }

    /** For endpoints that allow anonymous access; {@code null} when not authenticated. */
    public static UUID nullableUserId(UserDetails userDetails) {
        return userDetails == null ? null : userId(userDetails);
    }
}
