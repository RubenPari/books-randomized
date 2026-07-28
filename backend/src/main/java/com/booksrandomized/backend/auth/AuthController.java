package com.booksrandomized.backend.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {
    private static final String REFRESH = "refresh_token";
    private final AuthService auth;
    private final boolean secureCookie;
    private final PasswordResetNotifier resetNotifier;

    AuthController(AuthService auth, PasswordResetNotifier resetNotifier,
            @Value("${auth.cookie.secure:true}") boolean secureCookie) {
        this.auth = auth;
        this.resetNotifier = resetNotifier;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    SessionResponse register(@Valid @RequestBody Credentials request, HttpServletResponse response) {
        return respond(auth.register(request.email(), request.password()), response);
    }

    @PostMapping("/login")
    SessionResponse login(@Valid @RequestBody Credentials request, HttpServletResponse response) {
        return respond(auth.login(request.email(), request.password()), response);
    }

    @PostMapping("/refresh")
    SessionResponse refresh(@CookieValue(REFRESH) String raw, HttpServletResponse response) {
        return respond(auth.refresh(raw), response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@CookieValue(name = REFRESH, required = false) String raw, HttpServletResponse response) {
        if (raw != null) auth.logout(raw);
        clearCookie(response);
    }

    @GetMapping("/me")
    AuthService.UserView me(@AuthenticationPrincipal Jwt jwt) {
        return view(auth.user(UUID.fromString(jwt.getSubject())));
    }

    @GetMapping("/csrf")
    Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void forgot(@Valid @RequestBody ForgotPassword request) {
        String token = auth.createReset(request.email());
        if (token != null) resetNotifier.send(request.email(), token);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reset(@Valid @RequestBody ResetPassword request) {
        auth.resetPassword(request.token(), request.newPassword());
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void change(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePassword request) {
        auth.changePassword(UUID.fromString(jwt.getSubject()), request.currentPassword(), request.newPassword());
    }

    private SessionResponse respond(AuthService.Session session, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(session.refreshToken(), Duration.ofDays(30)).toString());
        return new SessionResponse(session.accessToken(), session.user());
    }

    private void clearCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    private ResponseCookie cookie(String value, Duration age) {
        return ResponseCookie.from(REFRESH, value).httpOnly(true).secure(secureCookie)
                .sameSite("Strict").path("/api/auth").maxAge(age).build();
    }

    private static AuthService.UserView view(User user) {
        return new AuthService.UserView(user.getId(), user.getEmail());
    }

    record Credentials(@NotBlank @Email @Size(max = 320) String email,
                       @NotBlank @Size(min = 12, max = 72) String password) {}
    record ForgotPassword(@NotBlank @Email @Size(max = 320) String email) {}
    record ResetPassword(@NotBlank @Size(max = 200) String token,
                         @NotBlank @Size(min = 12, max = 72) String newPassword) {}
    record ChangePassword(@NotBlank String currentPassword,
                          @NotBlank @Size(min = 12, max = 72) String newPassword) {}
    record SessionResponse(String accessToken, AuthService.UserView user) {}
}
