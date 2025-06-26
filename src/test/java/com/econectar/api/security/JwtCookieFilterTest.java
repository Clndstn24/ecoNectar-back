package com.econectar.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtCookieFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    private JwtCookieFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtCookieFilter(tokenProvider, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotAuthenticateWhenNoCookiesPresent() throws ServletException, IOException {
        // Given
        when(request.getCookies()).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateWhenNoJwtCookie() throws ServletException, IOException {
        // Given
        Cookie[] cookies = new Cookie[] { new Cookie("OTHER", "value") };
        when(request.getCookies()).thenReturn(cookies);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateWhenInvalidToken() throws ServletException, IOException {
        // Given
        Cookie[] cookies = new Cookie[] { new Cookie("JWT", "invalid-token") };
        when(request.getCookies()).thenReturn(cookies);
        when(tokenProvider.validate("invalid-token")).thenReturn(false);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateWithValidToken() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String username = "testuser@example.com";
        Cookie[] cookies = new Cookie[] { new Cookie("JWT", token) };

        when(request.getCookies()).thenReturn(cookies);
        when(tokenProvider.validate(token)).thenReturn(true);
        when(tokenProvider.getUser(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService).loadUserByUsername(username);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void shouldProceedWithFilterChainEvenWithException() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        Cookie[] cookies = new Cookie[] { new Cookie("JWT", token) };

        when(request.getCookies()).thenReturn(cookies);
        when(tokenProvider.validate(token)).thenReturn(true);
        when(tokenProvider.getUser(token)).thenReturn("username");
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new RuntimeException("Test exception"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}