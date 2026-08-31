package com.dmfs.auth.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "access_token";

    @Override
    public String resolve(HttpServletRequest request) {

        // Never try to read JWT from the cookie on login/logout
        String path = request.getRequestURI();

        if (path.equals("/api/auth/login") ||
            path.equals("/api/auth/logout")) {
            return null;
        }

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if (COOKIE_NAME.equals(cookie.getName())) {

                String token = cookie.getValue();

                if (token != null && !token.isBlank()) {
                    return token;
                }
            }
        }

        return null;
    }
}