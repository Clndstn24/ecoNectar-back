package com.econectar.api.security;

import com.econectar.api.user.model.User;
import com.econectar.api.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtCookieFilterTest {

    private JwtCookieFilter jwtCookieFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtCookieFilter = new JwtCookieFilter(jwtTokenProvider, userService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext(); // Limpia el contexto de seguridad antes de cada prueba
    }

    @Test
    @DisplayName("doFilterInternal sin cookie JWT debe continuar la cadena")
    void doFilterInternal_WithoutJwtCookie_ShouldContinueChain() throws ServletException, IOException {
        // Act
        jwtCookieFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validate(anyString());
    }

    @Test
    @DisplayName("doFilterInternal con cookie JWT inválida debe continuar la cadena")
    void doFilterInternal_WithInvalidJwtCookie_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        Cookie cookie = new Cookie("JWT", "invalid-token");
        request.setCookies(cookie);

        when(jwtTokenProvider.validate("invalid-token")).thenReturn(false);

        // Act
        jwtCookieFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validate("invalid-token");
        verify(userService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("doFilterInternal con cookie JWT válida debe autenticar al usuario")
    void doFilterInternal_WithValidJwtCookie_ShouldAuthenticateUser() throws ServletException, IOException {
        // Arrange
        String token = "valid-token";
        String username = "test@example.com";
        Cookie cookie = new Cookie("JWT", token);
        request.setCookies(cookie);

        User mockUser = mock(User.class);
        when(jwtTokenProvider.validate(token)).thenReturn(true);
        when(jwtTokenProvider.getUser(token)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(mockUser);

        // Act
        jwtCookieFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validate(token);
        verify(jwtTokenProvider).getUser(token);
        verify(userService).loadUserByUsername(username);

        // Verificar que el contexto de seguridad contiene la autenticación
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal con cookie JWT válida pero usuario no encontrado")
    void doFilterInternal_WithValidJwtButUserNotFound_ShouldContinueChainWithoutAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "valid-token-unknown-user";
        String username = "unknown@example.com";
        Cookie cookie = new Cookie("JWT", token);
        request.setCookies(cookie);

        when(jwtTokenProvider.validate(token)).thenReturn(true);
        when(jwtTokenProvider.getUser(token)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenThrow(new RuntimeException("User not found"));

        // Act
        jwtCookieFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validate(token);
        verify(jwtTokenProvider).getUser(token);
        verify(userService).loadUserByUsername(username);

        // Verificar que el contexto de seguridad no contiene autenticación
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}