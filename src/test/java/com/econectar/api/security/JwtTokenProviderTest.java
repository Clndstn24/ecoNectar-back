package com.econectar.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", "testSecretKeyThatIsAtLeast32BytesLongForHS256Algorithm");
        ReflectionTestUtils.setField(jwtTokenProvider, "expMs", 3600000L); // 1 hora
        jwtTokenProvider.init(); // Inicializa la clave secreta
    }

    @Test
    @DisplayName("Generar token JWT")
    void generate_ShouldReturnValidToken() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");

        // Act
        String token = jwtTokenProvider.generate(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 20); // Un token JWT válido debe tener cierta longitud
    }

    @Test
    @DisplayName("Validar token JWT válido")
    void validate_WithValidToken_ShouldReturnTrue() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        String token = jwtTokenProvider.generate(authentication);

        // Act
        boolean isValid = jwtTokenProvider.validate(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validar token JWT inválido")
    void validate_WithInvalidToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validate("invalid.token.format");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Obtener usuario desde token JWT")
    void getUser_ShouldReturnUsername() {
        // Arrange
        String username = "test@example.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        String token = jwtTokenProvider.generate(authentication);

        // Act
        String resultUsername = jwtTokenProvider.getUser(token);

        // Assert
        assertEquals(username, resultUsername);
    }
}