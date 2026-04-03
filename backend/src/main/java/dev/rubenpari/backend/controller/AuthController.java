package dev.rubenpari.backend.controller;

import dev.rubenpari.backend.dto.AuthResponse;
import dev.rubenpari.backend.dto.ConfirmRequest;
import dev.rubenpari.backend.dto.LoginRequest;
import dev.rubenpari.backend.dto.RefreshRequest;
import dev.rubenpari.backend.dto.RegisterRequest;
import dev.rubenpari.backend.dto.ResetConfirmRequest;
import dev.rubenpari.backend.dto.ResetRequest;
import dev.rubenpari.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@Valid @RequestBody ConfirmRequest request) {
        authService.confirmEmail(request.token());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/reset/request")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody ResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset/confirm")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody ResetConfirmRequest request) {
        authService.confirmPasswordReset(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
