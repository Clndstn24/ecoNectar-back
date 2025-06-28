package com.econectar.api.user.controller;

import com.econectar.api.shared.exception.AuthenticationException;
import com.econectar.api.shared.exception.UserNotFoundException;
import com.econectar.api.user.dto.UserDTO;
import com.econectar.api.user.model.User;
import com.econectar.api.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        try {
            // Obtener la autenticación del contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // El nombre de usuario es el email en nuestra implementación
            String email = authentication.getName();

            // Buscar y devolver el usuario como DTO (sin datos sensibles)
            UserDTO userDTO = userService.findUserByEmail(email);

            return ResponseEntity.ok(userDTO);
        } catch (AuthenticationException e) {
            throw  new AuthenticationException("Error al obtener el usuario actual. " + e.getMessage(), e);
        }
    }
}
