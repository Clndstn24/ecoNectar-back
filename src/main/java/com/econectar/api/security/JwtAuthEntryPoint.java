package com.econectar.api.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);          // 401
        response.setContentType("application/json;charset=UTF-8");

        String body = """
            {
              "error": "UNAUTHORIZED",
              "message": "%s"
            }
            """.formatted(
                authException.getMessage() != null
                        ? authException.getMessage()
                        : "Token ausente o inválido"
        );

        response.getWriter().write(body);
    }
}


