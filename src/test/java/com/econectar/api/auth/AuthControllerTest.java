package com.econectar.api.auth;

import com.econectar.api.security.JwtTokenProvider;
import com.econectar.api.shared.exception.EmailAlreadyExistsException;
import com.econectar.api.shared.exception.InvalidCredentialsException;
import com.econectar.api.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Registro exitoso de usuario")
    void registerUser_Success() {
        // Arrange
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        doNothing().when(userService).createUser(any(UserRegisterRequest.class));

        // Act
        ResponseEntity<String> response = authController.register(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
        verify(userService, times(1)).createUser(any(UserRegisterRequest.class));
    }

    @Test
    @DisplayName("Registro de usuario con email ya existente")
    void registerUser_EmailAlreadyExists() {
        // Arrange
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        doThrow(new DataIntegrityViolationException("Email duplicado")).when(userService).createUser(any(UserRegisterRequest.class));

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> {
            authController.register(request);
        });
        verify(userService, times(1)).createUser(any(UserRegisterRequest.class));
    }

    @Test
    @DisplayName("Login exitoso")
    void login_Success() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generate(any(Authentication.class))).thenReturn("jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        ResponseEntity<String> result = authController.login(request, response);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Login successful", result.getBody());

        Cookie[] cookies = response.getCookies();
        assertNotNull(cookies);
        assertEquals(1, cookies.length);
        assertEquals("JWT", cookies[0].getName());
        assertEquals("jwt-token", cookies[0].getValue());
        assertTrue(cookies[0].isHttpOnly());
        assertEquals("/", cookies[0].getPath());
    }

    @Test
    @DisplayName("Login fallido por credenciales inválidas")
    void login_InvalidCredentials() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongpassword");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authController.login(request, response);
        });
    }

    @Test
    @DisplayName("Logout exitoso")
    void logout_Success() {
        // Arrange
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        ResponseEntity<String> result = authController.logout(response);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Logged out", result.getBody());

        Cookie[] cookies = response.getCookies();
        assertNotNull(cookies);
        assertEquals(1, cookies.length);
        assertEquals("JWT", cookies[0].getName());
        assertNull(cookies[0].getValue());
        assertTrue(cookies[0].isHttpOnly());
        assertEquals("/", cookies[0].getPath());
        assertEquals(0, cookies[0].getMaxAge());
    }

    @Test
    @DisplayName("Ping devuelve pong")
    void ping_ReturnsPong() {
        // Act
        ResponseEntity<String> response = authController.ping();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("pong", response.getBody());
    }
}
