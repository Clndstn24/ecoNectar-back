package com.econectar.api.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;
    private final String username = "testuser";
    private final String secret = "estaEsUnaClaveSecretaMuyLargaParaPruebas123456789012345678901234567890";
    private final long expirationMs = 60000; // 1 minuto

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "expMs", expirationMs);

        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
    }

    @Test
    void generateTokenShouldCreateValidToken() {
        // Act
        String token = jwtTokenProvider.generate(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validate(token));
        assertEquals(username, jwtTokenProvider.getUser(token));
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        // Arrange
        String token = jwtTokenProvider.generate(authentication);

        // Act & Assert
        assertTrue(jwtTokenProvider.validate(token));
    }

    @Test
    void validateTokenShouldReturnFalseForInvalidToken() {
        // Act & Assert
        assertFalse(jwtTokenProvider.validate("tokenInvalido"));
    }

    @Test
    void validateTokenShouldReturnFalseForExpiredToken() {
        // Arrange
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "secret", secret);
        ReflectionTestUtils.setField(expiredProvider, "expMs", -10000); // expirado hace 10 segundos

        String expiredToken = expiredProvider.generate(authentication);

        // Act & Assert
        assertFalse(jwtTokenProvider.validate(expiredToken));
    }

    @Test
    void getUserShouldReturnUsernameFromToken() {
        // Arrange
        String token = jwtTokenProvider.generate(authentication);

        // Act
        String extractedUsername = jwtTokenProvider.getUser(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void getUserShouldThrowExceptionForInvalidToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtTokenProvider.getUser("tokenInvalido"));
    }
}