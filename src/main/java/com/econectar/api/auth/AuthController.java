package com.econectar.api.auth;

import com.econectar.api.security.JwtTokenProvider;
import com.econectar.api.shared.exception.AuthenticationException;
import com.econectar.api.shared.exception.EmailAlreadyExistsException;
import com.econectar.api.shared.exception.InvalidCredentialsException;
import com.econectar.api.shared.exception.UserCreationException;
import com.econectar.api.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authMgr;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authMgr) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authMgr = authMgr;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterRequest user) {
        logger.info("Intento de registro para email: {}", user.getEmail());
        try {
            userService.createUser(user);
            logger.info("Usuario registrado correctamente: {}", user.getEmail());
            return ResponseEntity.ok("User registered successfully");
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integración de datos al registrar usuario con email: {} - {}", user.getEmail(), e.getMessage());
            throw new EmailAlreadyExistsException("El correo electrónico ya está en uso.", e);
        } catch (Exception e) {
            logger.error("Error en registro para email: {} - {}", user.getEmail(), e.getMessage(), e);
            throw new UserCreationException("Error al registrar usuario: " + e.getMessage(), e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest loginRequest, HttpServletResponse response) {
        logger.info("Intento de login para email: {}", loginRequest.getEmail());
        try {
            Authentication auth = authMgr.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            logger.info("Autenticación exitosa para email: {}", loginRequest.getEmail());
            String jwt = jwtTokenProvider.generate(auth);
            Cookie cookie = new Cookie("JWT", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Cambia a false solo en desarrollo sin HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(3600); // 1 hora
            response.addCookie(cookie);
            return ResponseEntity.ok("Login successful");
        } catch (BadCredentialsException e) {
            logger.warn("Fallo de autenticación para email: {} - Credenciales inválidas", loginRequest.getEmail());
            throw new InvalidCredentialsException("Email o contraseña incorrectos");
        } catch (Exception e) {
            logger.warn("Fallo de autenticación para email: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw new AuthenticationException("Error al procesar la autenticación: " + e.getMessage(), e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
