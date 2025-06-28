package com.econectar.api.shared.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/resource");
    }

    @Test
    @DisplayName("Manejo de ResourceNotFoundException")
    void handleResourceNotFoundException_ShouldReturnNotFound() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Recurso no encontrado");

        // Act
        ResponseEntity<GlobalExceptionHandler.ApiError> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso no encontrado", response.getBody().getMessage());
        assertEquals("/api/resource", response.getBody().getPath());
    }

    @Test
    @DisplayName("Manejo de UserNotFoundException")
    void handleUserNotFoundException_ShouldReturnNotFound() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException("Usuario no encontrado");

        // Act
        ResponseEntity<GlobalExceptionHandler.ApiError> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Usuario no encontrado", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Manejo de UsernameNotFoundException")
    void handleUsernameNotFoundException_ShouldReturnNotFound() {
        // Arrange
        UsernameNotFoundException exception = new UsernameNotFoundException("Usuario no encontrado por username");

        // Act
        ResponseEntity<GlobalExceptionHandler.ApiError> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Usuario no encontrado por username", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Manejo de AuthenticationException")
    void handleAuthenticationException_ShouldReturnUnauthorized() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Error de autenticación");

        // Act
        ResponseEntity<GlobalExceptionHandler.ApiError> response = exceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Error de autenticación", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Manejo de InvalidCredentialsException")
    void handleInvalidCredentialsException_ShouldReturnUnauthorized() {
        // Arrange
        InvalidCredentialsException exception = new InvalidCredentialsException("Credenciales inválidas");

        // Act
        ResponseEntity<GlobalExceptionHandler.ApiError> response = exceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Credenciales inválidas", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Manejo de EmailAlreadyExistsException")
    void handleEmailAlreadyExistsException_ShouldReturnConflict() {
        // Arrange
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("Email ya registrado");

        // Act
        ResponseEntity<GlobalExceptionHandler.ApiError> response = exceptionHandler.handleConflictException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email ya registrado", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Manejo de UserCreationException")
    void handleUserCreationException_ShouldReturnBadRequest() {
        // Arrange
        UserCreationException exception = new UserCreationException("Error al crear usuario");

        // Act
        ResponseEntity<GlobalExceptionHandler.ApiError> response = exceptionHandler.handleUserCreationException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error al crear usuario", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Manejo de MethodArgumentNotValidException")
    void handleValidationExceptions_ShouldReturnBadRequest() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "email", "El email es requerido"));
        fieldErrors.add(new FieldError("object", "password", "La contraseña debe tener al menos 8 caracteres"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Error de validación", body.get("message"));

        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals(2, errors.size());
        assertEquals("El email es requerido", errors.get("email"));
        assertEquals("La contraseña debe tener al menos 8 caracteres", errors.get("password"));
    }

    @Test
    @DisplayName("Manejo de Exception genérica")
    void handleAllUncaughtException_ShouldReturnInternalServerError() {
        // Arrange
        Exception exception = new Exception("Error interno inesperado");

        // Act
        ResponseEntity<GlobalExceptionHandler.ApiError> response = exceptionHandler.handleAllUncaughtException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Error interno inesperado"));
    }
}
