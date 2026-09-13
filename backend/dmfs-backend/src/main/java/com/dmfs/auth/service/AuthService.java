package com.dmfs.auth.service;

import com.dmfs.auth.dto.LoginRequest;
import com.dmfs.auth.dto.LoginResponse;
import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.auth.security.JwtService;
import com.dmfs.company.entity.CompanyStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password")
                );

        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }

        /*
         * SUPER_ADMIN is not tied to a subscriber company.
         * All other users must belong to an ACTIVE company.
         */
        if (user.getRole() != Role.SUPER_ADMIN) {

            if (user.getCompany() == null) {
                throw new RuntimeException(
                        "User is not associated with a company"
                );
            }

            if (user.getCompany().getStatus() != CompanyStatus.ACTIVE) {
                throw new RuntimeException(
                        "Company account is not active"
                );
            }
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }
}