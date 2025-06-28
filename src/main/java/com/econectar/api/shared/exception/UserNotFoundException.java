package com.econectar.api.shared.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Long userId) {
        super("Usuario no encontrado con ID: " + userId);
    }

    public UserNotFoundException(String field, String value) {
        super("Usuario no encontrado con " + field + ": " + value);
    }
}
