package com.booksrandomized.backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "used_at") private Instant usedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected PasswordResetToken() {}

    PasswordResetToken(User user, String rawToken, Instant now) {
        this.user = user;
        this.tokenHash = RefreshToken.hash(rawToken);
        this.createdAt = now;
        this.expiresAt = now.plusSeconds(1800);
    }

    User user() { return user; }
    Instant expiresAt() { return expiresAt; }
    Instant usedAt() { return usedAt; }
    void use(Instant now) { usedAt = now; }
}
