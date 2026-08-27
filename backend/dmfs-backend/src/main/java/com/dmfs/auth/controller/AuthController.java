package com.dmfs.auth.controller;

import com.dmfs.auth.dto.LoginRequest;
import com.dmfs.auth.dto.LoginResponse;
import com.dmfs.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        LoginResponse loginResponse = authService.login(request);

        Cookie cookie = new Cookie("access_token", loginResponse.getToken());

        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true when using HTTPS in production
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);

        response.addCookie(cookie);

        // Token is no longer returned in the response body
        loginResponse.setToken(null);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletResponse response
    ) {

        Cookie cookie = new Cookie("access_token", null);

        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }
}