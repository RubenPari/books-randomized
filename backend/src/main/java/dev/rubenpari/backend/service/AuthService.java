package dev.rubenpari.backend.service;

import dev.rubenpari.backend.dto.AuthResponse;
import dev.rubenpari.backend.dto.LoginRequest;
import dev.rubenpari.backend.dto.RegisterRequest;
import dev.rubenpari.backend.model.EmailConfirmation;
import dev.rubenpari.backend.model.PasswordReset;
import dev.rubenpari.backend.model.RefreshToken;
import dev.rubenpari.backend.model.User;
import dev.rubenpari.backend.repository.EmailConfirmationRepository;
import dev.rubenpari.backend.repository.PasswordResetRepository;
import dev.rubenpari.backend.repository.RefreshTokenRepository;
import dev.rubenpari.backend.repository.UserRepository;
import dev.rubenpari.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailConfirmationRepository emailConfirmationRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailtrapService mailtrapService;
    private final long refreshTokenDays;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            EmailConfirmationRepository emailConfirmationRepository,
            PasswordResetRepository passwordResetRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            MailtrapService mailtrapService,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailConfirmationRepository = emailConfirmationRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.mailtrapService = mailtrapService;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setPreferredLanguage(request.preferredLanguage() == null ? "it" : request.preferredLanguage());
        user.setEmailConfirmed(false);
        userRepository.save(user);

        EmailConfirmation confirmation = new EmailConfirmation();
        confirmation.setUser(user);
        confirmation.setToken(UUID.randomUUID().toString());
        confirmation.setExpiresAt(Instant.now().plus(2, ChronoUnit.DAYS));
        emailConfirmationRepository.save(confirmation);

        mailtrapService.sendEmail(
                user.getEmail(),
                "Conferma il tuo account",
                "<p>Usa questo token per confermare: <strong>" + confirmation.getToken() + "</strong></p>"
        );

        return issueTokens(user);
    }

    @Transactional
    public void confirmEmail(String token) {
        EmailConfirmation confirmation = emailConfirmationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token non valido"));
        if (confirmation.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Token scaduto");
        }

        confirmation.setConfirmedAt(Instant.now());
        confirmation.getUser().setEmailConfirmed(true);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password())
        );
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalStateException("Refresh token non valido"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Refresh token scaduto");
        }

        stored.setRevoked(true);
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            PasswordReset reset = new PasswordReset();
            reset.setUser(user);
            reset.setToken(UUID.randomUUID().toString());
            reset.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
            passwordResetRepository.save(reset);

            mailtrapService.sendEmail(
                    user.getEmail(),
                    "Reset password",
                    "<p>Token reset: <strong>" + reset.getToken() + "</strong></p>"
            );
        });
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordReset reset = passwordResetRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token non valido"));
        if (reset.getExpiresAt().isBefore(Instant.now()) || reset.getUsedAt() != null) {
            throw new IllegalStateException("Token scaduto");
        }

        reset.setUsedAt(Instant.now());
        User user = reset.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setTokenHash(hashToken(refreshToken));
        stored.setExpiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        stored.setRevoked(false);
        refreshTokenRepository.save(stored);
        return new AuthResponse(accessToken, refreshToken);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }
}
