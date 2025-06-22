package com.econectar.api.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
public class SecurityConfig {
    private final AuthService authService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(AuthService authService, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    public final static String LOGIN_URL_MATCHER = ApiConfig.API_BASE_PATH + "/auth/login";
    public final static String LOG_OUT_URL_MATCHER = ApiConfig.API_BASE_PATH + "/auth/logout";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        final Filter jwtFilter = jwtAuthenticationFilter();
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(HttpMethod.POST, LOGIN_URL_MATCHER).permitAll()
                        .requestMatchers(BASE_URL_MATCHER).authenticated()
                        .anyRequest().denyAll()
                )
                .logout(logout -> {
                    logout
                            .logoutRequestMatcher(new AntPathRequestMatcher(LOG_OUT_URL_MATCHER, HttpMethod.POST.name()))
                            .logoutSuccessHandler((request, response, authentication) -> {
                                response.setStatus(HttpStatus.NO_CONTENT.value());
                                final Cookie cookie = new Cookie(AuthCookieConstants.TOKEN_COOKIE_NAME, null);
                                cookie.setMaxAge(0);
                                response.addCookie(cookie);
                            })
                    ;
                })
                .addFilterBefore(jwtFilter, LogoutFilter.class)
                .csrf((csrf) -> {
                            try {
                                csrf.disable()
                                        .sessionManagement((sessionManagement) -> sessionManagement
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                        ).oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
                            } catch (Exception e) {
                                throw new AuthenticationException("Spring Security Config Issue",e) {
                                };
                            }
                        }
                )
                .authenticationManager(authenticationManager())
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                )
        ;

        return http.build();
    }
}
