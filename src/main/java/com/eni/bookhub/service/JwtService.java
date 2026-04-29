package com.eni.bookhub.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public SecretKey getSigningKey() {
        byte[] rawKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secretKey);
            if (keyBytes.length < 32 && rawKeyBytes.length >= 32) {
                keyBytes = rawKeyBytes;
            }
        } catch (IllegalArgumentException ex) {
            keyBytes = rawKeyBytes;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey())
                .compact();
    }
}
