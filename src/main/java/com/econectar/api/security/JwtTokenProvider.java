package com.econectar.api.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration-ms}")
    private long expMs;

    private Key key() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }

    public String generate(Authentication auth) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(auth.getName())        // username
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expMs)))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validate(String jwt) {
        try {
            Jwts.parser()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(jwt);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUser(String jwt) {
        return Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }
}

