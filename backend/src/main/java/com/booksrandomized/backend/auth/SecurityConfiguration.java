package com.booksrandomized.backend.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import static org.springframework.security.web.util.matcher.RegexRequestMatcher.regexMatcher;

@Configuration
public class SecurityConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrf.setCookiePath("/");
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        return http
                .csrf(configuration -> configuration
                        .csrfTokenRepository(csrf)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(
                                regexMatcher(HttpMethod.POST, "/api/auth/register"),
                                regexMatcher(HttpMethod.POST, "/api/auth/login"),
                                regexMatcher(HttpMethod.POST, "/api/auth/forgot-password"),
                                regexMatcher(HttpMethod.POST, "/api/auth/reset-password"),
                                regexMatcher(HttpMethod.POST, "/api/auth/change-password"),
                                regexMatcher("/api/books/.*"),
                                regexMatcher("/api/reading-list.*"),
                                regexMatcher("/api/discovered.*"),
                                regexMatcher("/api/feedback.*")))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health", "/api/catalog/search",
                                "/api/auth/register", "/api/auth/login",
                                "/api/auth/forgot-password", "/api/auth/reset-password",
                                "/api/auth/refresh", "/api/auth/logout", "/api/auth/csrf").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .addFilterAfter(csrfCookieFilter(), CsrfFilter.class)
                .build();
    }

    private static OncePerRequestFilter csrfCookieFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {
                CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                if (token != null) {
                    token.getToken();
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    KeyPair jwtKeyPair(
            ResourceLoader resources,
            @Value("${auth.jwt.private-key:}") String privateKeyPem,
            @Value("${auth.jwt.public-key:}") String publicKeyPem,
            @Value("${auth.jwt.require-keys:false}") boolean requireKeys,
            @Value("${spring.profiles.active:}") String activeProfiles) {
        boolean prod = activeProfiles != null && activeProfiles.contains("prod");
        String privatePem = resolvePem(resources, privateKeyPem);
        String publicPem = resolvePem(resources, publicKeyPem);
        if (!privatePem.isBlank() && !publicPem.isBlank()) {
            return parseRsaKeyPair(privatePem, publicPem);
        }
        if (prod || requireKeys) {
            throw new IllegalStateException(
                    "AUTH_JWT_PRIVATE_KEY and AUTH_JWT_PUBLIC_KEY are required in this environment");
        }
        log.warn("JWT signing keys are ephemeral; tokens will not survive restart (dev-only fallback)");
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("RSA is unavailable", exception);
        }
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair keyPair) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair keyPair) {
        RSAKey key = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(stableKeyId((RSAPublicKey) keyPair.getPublic()))
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    static String stableKeyId(RSAPublicKey publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKey.getEncoded());
            return HexFormat.of().formatHex(hash).substring(0, 32);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    static KeyPair parseRsaKeyPair(String privatePem, String publicPem) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(
                    new PKCS8EncodedKeySpec(decodePem(privatePem)));
            RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(
                    new X509EncodedKeySpec(decodePem(publicPem)));
            return new KeyPair(publicKey, privateKey);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse JWT RSA PEM keys", exception);
        }
    }

    private static String resolvePem(ResourceLoader resources, String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.startsWith("classpath:") || trimmed.startsWith("file:")) {
            try {
                Resource resource = resources.getResource(trimmed);
                try (InputStream stream = resource.getInputStream()) {
                    return new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read JWT key resource: " + trimmed, exception);
            }
        }
        // Docker Compose /.env often stores PEMs with literal \n escapes.
        return trimmed.replace("\\n", "\n");
    }

    private static byte[] decodePem(String pem) {
        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }
}
