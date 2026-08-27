package com.dmfs.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // JWT DECODER
    // =========================================================

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKeySpec key = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(
                        org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256
                )
                .build();
    }


    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Angular development server
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200",
                        "http://127.0.0.1:4200"
                )
        );

        // HTTP methods Angular can use
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        // Allow request headers
        configuration.setAllowedHeaders(
                List.of("*")
        );

        // IMPORTANT:
        // Required because Angular uses withCredentials: true
        // and the backend sends the JWT as a cookie.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }


    // =========================================================
    // JWT COOKIE RESOLVER
    // =========================================================

    /*
     * Read JWT from the HttpOnly access_token cookie.
     */
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return new CookieBearerTokenResolver();
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // -------------------------------------------------
                // ENABLE CORS
                // -------------------------------------------------
                .cors(cors -> {})

                // -------------------------------------------------
                // CSRF
                // -------------------------------------------------
                // Disabled because this is a stateless JWT API.
                .csrf(csrf -> csrf.disable())

                // -------------------------------------------------
                // SESSION MANAGEMENT
                // -------------------------------------------------
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // -------------------------------------------------
                // AUTHORIZATION
                // -------------------------------------------------
                .authorizeHttpRequests(auth -> auth

                        // Login and logout don't require authentication
                        .requestMatchers("/api/auth/**").permitAll()

                        // Health check
                        .requestMatchers("/api/v1/health").permitAll()

                        // Actuator health
                        .requestMatchers("/actuator/health").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // -------------------------------------------------
                // JWT RESOURCE SERVER
                // -------------------------------------------------
                .oauth2ResourceServer(oauth2 ->
                        oauth2

                                // Read JWT from access_token cookie
                                .bearerTokenResolver(
                                        bearerTokenResolver()
                                )

                                // Validate JWT
                                .jwt(jwt ->
                                        jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter()
                                        )
                                )
                );

        return http.build();
    }


    // =========================================================
    // JWT AUTHENTICATION CONVERTER
    // =========================================================

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }
}