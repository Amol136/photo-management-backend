package com.pms.photo_management_backend.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Value("${app.frontend.url:}")
    private String frontendUrl;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    // =====================================
    // PASSWORD ENCODER
    // =====================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // =====================================
    // SECURITY FILTER CHAIN
    // =====================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // -------------------------
                // CORS
                // -------------------------

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // -------------------------
                // CSRF
                // JWT API असल्यामुळे disable
                // -------------------------

                .csrf(csrf ->
                        csrf.disable()
                )

                // -------------------------
                // STATELESS SESSION
                // -------------------------

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // -------------------------
                // 401 / 403 HANDLING
                // -------------------------

                .exceptionHandling(exception ->

                        exception

                                // Token नाही / authentication नाही
                                .authenticationEntryPoint(
                                        (request,
                                         response,
                                         authException) -> {

                                            response.setStatus(
                                                    HttpServletResponse
                                                            .SC_UNAUTHORIZED
                                            );

                                            response.setContentType(
                                                    "application/json"
                                            );

                                            response.setCharacterEncoding(
                                                    "UTF-8"
                                            );

                                            response
                                                    .getWriter()
                                                    .write(
                                                            """
                                                            {
                                                              "status": 401,
                                                              "error": "Unauthorized",
                                                              "message": "Authentication required."
                                                            }
                                                            """
                                                    );
                                        }
                                )

                                // Login आहे पण permission नाही
                                .accessDeniedHandler(
                                        (request,
                                         response,
                                         accessDeniedException) -> {

                                            response.setStatus(
                                                    HttpServletResponse
                                                            .SC_FORBIDDEN
                                            );

                                            response.setContentType(
                                                    "application/json"
                                            );

                                            response.setCharacterEncoding(
                                                    "UTF-8"
                                            );

                                            response
                                                    .getWriter()
                                                    .write(
                                                            """
                                                            {
                                                              "status": 403,
                                                              "error": "Forbidden",
                                                              "message": "Access denied."
                                                            }
                                                            """
                                                    );
                                        }
                                )
                )

                // -------------------------
                // API AUTHORIZATION
                // -------------------------

                .authorizeHttpRequests(auth ->
                        auth

                                // Browser CORS preflight
                                .requestMatchers(
                                        HttpMethod.OPTIONS,
                                        "/**"
                                )
                                .permitAll()

                                // Login public
                                .requestMatchers(
                                        "/api/auth/login"
                                )
                                .permitAll()

                                // All remaining API endpoints
                                // require authentication
                                .requestMatchers(
                                        "/api/**"
                                )
                                .authenticated()

                                // Other non API requests
                                .anyRequest()
                                .permitAll()
                )

                // -------------------------
                // JWT FILTER
                // -------------------------

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =====================================
    // CORS CONFIGURATION
    // =====================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "https://hotel-main-admin1.vercel.app",
                        "https://hotel-sub-admin1.vercel.app",
                        "https://hotel-manager-pearl.vercel.app"
                )
        );
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        configuration.setAllowCredentials(
                true
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}