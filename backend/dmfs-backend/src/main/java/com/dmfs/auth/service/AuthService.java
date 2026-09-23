package com.dmfs.auth.service;

import com.dmfs.auth.dto.LoginRequest;
import com.dmfs.auth.dto.LoginResponse;
import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.auth.security.JwtService;
import com.dmfs.company.entity.CompanyStatus;
import com.dmfs.company.service.WorkspaceHostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WorkspaceHostService workspaceHostService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            WorkspaceHostService workspaceHostService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.workspaceHostService = workspaceHostService;
    }

    public LoginResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        if (request == null
                || request.getEmail() == null
                || request.getPassword() == null) {

            throw new RuntimeException(
                    "Email and password are required"
            );
        }

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid email or password"
                                )
                        );

        if (!user.isActive()) {

            throw new RuntimeException(
                    "User account is inactive"
            );
        }

        /*
         * HOSTNAME IS NOW THE COMPANY WORKSPACE.
         *
         * Example:
         *
         * tukupala.dmfs.com
         *        ↓
         * workspace = tukupala
         */

        String workspaceSlug =
                workspaceHostService
                        .resolveWorkspaceSlug(
                                httpRequest
                        );

        /*
         * SUPER ADMIN
         *
         * Super Admin belongs to the platform,
         * not a company workspace.
         */

        if (user.getRole() == Role.SUPER_ADMIN) {

            if (workspaceSlug != null
                    && !workspaceSlug.isBlank()) {

                throw new RuntimeException(
                        "Super Admin must use the platform login"
                );
            }

        } else {

            /*
             * COMPANY USER
             */

            if (user.getCompany() == null) {

                throw new RuntimeException(
                        "User is not associated with a company"
                );
            }

            if (user.getCompany().getStatus()
                    != CompanyStatus.ACTIVE) {

                throw new RuntimeException(
                        "Company account is not active"
                );
            }

            /*
             * Company users MUST come through
             * their company workspace.
             */

            if (workspaceSlug == null
                    || workspaceSlug.isBlank()) {

                throw new RuntimeException(
                        "Open your company workspace to sign in"
                );
            }

            String userWorkspace =
                    user.getCompany()
                            .getWorkspaceSlug();

            if (userWorkspace == null
                    || !userWorkspace.equals(
                            workspaceSlug
                    )) {

                throw new RuntimeException(
                        "This user does not belong to this company workspace"
                );
            }
        }

        /*
         * PASSWORD VALIDATION
         */

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        /*
         * JWT GENERATION
         */

        String token =
                jwtService.generateToken(user);

        if (token == null || token.isBlank()) {

            throw new RuntimeException(
                    "Failed to generate authentication token"
            );
        }

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
