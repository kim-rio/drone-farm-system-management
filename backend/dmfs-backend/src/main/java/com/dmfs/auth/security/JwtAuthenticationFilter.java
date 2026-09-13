package com.dmfs.auth.security;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.entity.CompanyStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "access_token";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/")
                || path.equals("/api/v1/health")
                || path.equals("/actuator/health")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null && !token.isBlank()) {

            try {

                String email = jwtService.extractEmail(token);

                if (email != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    userRepository.findByEmail(email).ifPresent(user -> {

                        if (!user.isActive()) {
                            return;
                        }

                        if (user.getRole().name().equals("SUPER_ADMIN")) {
                            authenticate(user);
                            return;
                        }

                        if (user.getCompany() == null) {
                            return;
                        }

                        if (user.getCompany().getStatus() != CompanyStatus.ACTIVE) {
                            return;
                        }

                        authenticate(user);
                    });
                }

            } catch (Exception ignored) {
                // Invalid/expired token simply results in an unauthenticated request.
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(User user) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    private String extractToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if (COOKIE_NAME.equals(cookie.getName())) {

                String value = cookie.getValue();

                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        }

        return null;
    }
}
