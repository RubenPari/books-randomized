package com.booksrandomized.backend.auth;

import com.booksrandomized.backend.shared.ApiException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwtEncoder;
    private final SecureRandom random = new SecureRandom();

    AuthService(UserRepository users, RefreshTokenRepository refreshTokens,
            PasswordResetTokenRepository resetTokens, PasswordEncoder passwords, JwtEncoder jwtEncoder) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.resetTokens = resetTokens;
        this.passwords = passwords;
        this.jwtEncoder = jwtEncoder;
    }

    @Transactional
    Session register(String email, String password) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalized)) {
            throw new ApiException(HttpStatus.CONFLICT, "email-in-use", "An account already exists for this email");
        }
        return issue(users.save(new User(normalized, passwords.encode(password), Instant.now())));
    }

    @Transactional
    Session login(String email, String password) {
        User user = users.findByEmailIgnoreCase(email.trim()).orElseThrow(AuthService::badCredentials);
        if (!passwords.matches(password, user.getPasswordHash())) {
            throw badCredentials();
        }
        return issue(user);
    }

    @Transactional
    Session refresh(String raw) {
        RefreshToken previous = refreshTokens.findByTokenHash(RefreshToken.hash(raw))
                .orElseThrow(AuthService::invalidRefresh);
        Instant now = Instant.now();
        if (previous.getRevokedAt() != null) {
            refreshTokens.revokeAll(previous.getUser().getId(), now);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "refresh-reuse", "Refresh token reuse detected");
        }
        if (!previous.getExpiresAt().isAfter(now)) {
            previous.revoke(now);
            throw invalidRefresh();
        }
        previous.revoke(now);
        return issue(previous.getUser());
    }

    @Transactional
    void logout(String raw) {
        refreshTokens.findByTokenHash(RefreshToken.hash(raw)).ifPresent(token -> token.revoke(Instant.now()));
    }

    User user(UUID id) {
        return users.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.UNAUTHORIZED, "invalid-access-token", "Unknown account"));
    }

    @Transactional
    void changePassword(UUID id, String current, String replacement) {
        User user = user(id);
        if (!passwords.matches(current, user.getPasswordHash())) {
            throw badCredentials();
        }
        user.changePassword(passwords.encode(replacement));
        refreshTokens.revokeAll(id, Instant.now());
    }

    @Transactional
    String createReset(String email) {
        return users.findByEmailIgnoreCase(email.trim()).map(user -> {
            String raw = opaqueToken();
            resetTokens.save(new PasswordResetToken(user, raw, Instant.now()));
            return raw;
        }).orElse(null);
    }

    @Transactional
    void resetPassword(String raw, String replacement) {
        PasswordResetToken token = resetTokens.findByTokenHash(RefreshToken.hash(raw))
                .orElseThrow(AuthService::invalidReset);
        Instant now = Instant.now();
        if (token.usedAt() != null || !token.expiresAt().isAfter(now)) {
            throw invalidReset();
        }
        token.use(now);
        token.user().changePassword(passwords.encode(replacement));
        refreshTokens.revokeAll(token.user().getId(), now);
    }

    private Session issue(User user) {
        Instant now = Instant.now();
        String raw = opaqueToken();
        refreshTokens.save(RefreshToken.fromRawToken(user, raw, now.plus(30, ChronoUnit.DAYS)));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("books-randomized")
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.MINUTES))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .build();
        String access = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new Session(access, raw, new UserView(user.getId(), user.getEmail()));
    }

    private String opaqueToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static ApiException badCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "bad-credentials", "Invalid email or password");
    }

    static ApiException invalidRefresh() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "invalid-refresh-token", "Invalid refresh token");
    }

    static ApiException invalidReset() {
        return new ApiException(HttpStatus.BAD_REQUEST, "invalid-reset-token", "Invalid or expired reset token");
    }

    record Session(String accessToken, String refreshToken, UserView user) {}
    record UserView(UUID id, String email) {}
}
