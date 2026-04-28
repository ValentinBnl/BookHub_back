package com.eni.bookhub.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // 32 bytes minimum requis pour HMAC-SHA256
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
    }

    @Test
    void generateToken_subjectIsEmail() {
        String token = jwtService.generateToken("user@test.com", "UTILISATEUR");

        Claims claims = parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("user@test.com");
    }

    @Test
    void generateToken_containsRoleClaim() {
        String token = jwtService.generateToken("user@test.com", "ADMIN");

        Claims claims = parseClaims(token);

        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void generateToken_expiresIn24Hours() {
        long before = System.currentTimeMillis();
        String token = jwtService.generateToken("user@test.com", "UTILISATEUR");
        long after = System.currentTimeMillis();

        Claims claims = parseClaims(token);

        long expectedExpiry = 24L * 60 * 60 * 1000;
        assertThat(claims.getExpiration().getTime())
                .isBetween(before + expectedExpiry - 2000, after + expectedExpiry + 2000);
    }

    @Test
    void generateToken_issuedAtIsNow() {
        long before = System.currentTimeMillis();
        String token = jwtService.generateToken("user@test.com", "LIBRAIRE");
        long after = System.currentTimeMillis();

        Claims claims = parseClaims(token);

        assertThat(claims.getIssuedAt().getTime()).isBetween(before - 1000, after + 1000);
    }

    @Test
    void generateToken_differentRoles_produceDistinctTokens() {
        String tokenUser = jwtService.generateToken("user@test.com", "UTILISATEUR");
        String tokenAdmin = jwtService.generateToken("user@test.com", "ADMIN");

        assertThat(tokenUser).isNotEqualTo(tokenAdmin);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtService.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
