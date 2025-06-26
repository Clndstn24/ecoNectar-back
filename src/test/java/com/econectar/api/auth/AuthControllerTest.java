package com.econectar.api.auth;

import com.econectar.api.security.JwtTokenProvider;
import com.econectar.api.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletResponse;

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
    private AuthenticationManager authMgr;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    public AuthControllerTest() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(userService, passwordEncoder, jwtTokenProvider, authMgr);
    }

    @Test
    void loginSuccess() {
        when(authMgr.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generate(any(Authentication.class))).thenReturn("jwt-token");
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("1234");
        ResponseEntity<String> res = authController.login(req, response);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("Login successful", res.getBody());
    }

    @Test
    void loginFail() {
        when(authMgr.authenticate(any())).thenThrow(new RuntimeException("Bad credentials"));
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("fail@test.com");
        req.setPassword("bad");
        ResponseEntity<String> res = authController.login(req, response);
        assertEquals(401, res.getStatusCodeValue());
        assertEquals("Invalid credentials", res.getBody());
    }

    @Test
    void logoutTest() {
        ResponseEntity<String> res = authController.logout(response);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("Logged out", res.getBody());
    }
}

