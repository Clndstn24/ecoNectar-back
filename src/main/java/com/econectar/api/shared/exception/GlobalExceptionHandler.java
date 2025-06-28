package com.econectar.api.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Clase para estructurar la respuesta de error
    public static class ApiError {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        public ApiError(HttpStatus status, String message, String path) {
            this.timestamp = LocalDateTime.now();
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
            this.path = path;
        }

        // Getters y setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }

    // Manejo de excepciones de recursos no encontrados
    @ExceptionHandler({ResourceNotFoundException.class, UserNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            RuntimeException ex, WebRequest request) {

        String path = request.getDescription(false).substring(4); // Remove "uri="
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                path
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Manejo de excepciones de autenticación
    @ExceptionHandler({AuthenticationException.class, InvalidCredentialsException.class})
    public ResponseEntity<ApiError> handleAuthenticationException(
            RuntimeException ex, WebRequest request) {

        String path = request.getDescription(false).substring(4);
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                path
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // Manejo de excepciones de registros duplicados
    @ExceptionHandler({EmailAlreadyExistsException.class})
    public ResponseEntity<ApiError> handleConflictException(
            RuntimeException ex, WebRequest request) {

        String path = request.getDescription(false).substring(4);
        ApiError error = new ApiError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                path
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Manejo de excepciones en la creación de usuarios
    @ExceptionHandler({UserCreationException.class})
    public ResponseEntity<ApiError> handleUserCreationException(
            RuntimeException ex, WebRequest request) {

        String path = request.getDescription(false).substring(4);
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                path
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Manejo de excepciones de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String path = request.getDescription(false).substring(4);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Error de validación");
        body.put("path", path);
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Manejo de otras excepciones no especificadas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllUncaughtException(
            Exception ex, WebRequest request) {

        String path = request.getDescription(false).substring(4);
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado: " + ex.getMessage(),
                path
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}