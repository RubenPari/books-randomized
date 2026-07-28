package com.booksrandomized.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import java.security.interfaces.RSAPrivateKey;

class JwtKeyPairRestartSafetyTest {
    @Test
    void tokensSignedWithPemKeysDecodeAfterRebuildingDecoder() throws Exception {
        String privatePem = read("classpath:jwt/private.pem");
        String publicPem = read("classpath:jwt/public.pem");
        KeyPair first = SecurityConfiguration.parseRsaKeyPair(privatePem, publicPem);
        JwtEncoder encoder = encoder(first);
        Instant now = Instant.now();
        String token = encoder.encode(JwtEncoderParameters.from(JwtClaimsSet.builder()
                .issuer("books-randomized")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject("reader")
                .build())).getTokenValue();

        KeyPair second = SecurityConfiguration.parseRsaKeyPair(privatePem, publicPem);
        JwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) second.getPublic()).build();
        Jwt jwt = decoder.decode(token);
        assertThat(jwt.getSubject()).isEqualTo("reader");
        assertThat(SecurityConfiguration.stableKeyId((RSAPublicKey) first.getPublic()))
                .isEqualTo(SecurityConfiguration.stableKeyId((RSAPublicKey) second.getPublic()));
    }

    private static JwtEncoder encoder(KeyPair keyPair) {
        RSAKey key = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(SecurityConfiguration.stableKeyId((RSAPublicKey) keyPair.getPublic()))
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    private static String read(String location) throws Exception {
        return new String(new DefaultResourceLoader().getResource(location).getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
