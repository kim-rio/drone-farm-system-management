package com.dmfs.staff.service;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.staff.dto.CreateStaffRequest;
import com.dmfs.staff.dto.StaffResponse;
import com.dmfs.staff.dto.StaffRole;
import com.dmfs.staff.dto.UpdateStaffRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaff() {

        SubscriberCompany company = getCurrentAdminCompany();

        List<Role> staffRoles = List.of(
                Role.MANAGEMENT,
                Role.GEOLOGIST,
                Role.DRONE_OPERATOR
        );

        return userRepository
                .findByCompanyAndRoleInOrderByFirstNameAsc(
                        company,
                        staffRoles
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StaffResponse createStaff(
            CreateStaffRequest request
    ) {

        SubscriberCompany company =
                getCurrentAdminCompany();

        String email =
                request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "A user with this email already exists"
            );
        }

        User user = new User();

        user.setFirstName(
                request.getFirstName().trim()
        );

        user.setLastName(
                request.getLastName().trim()
        );

        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(
                toEntityRole(request.getRole())
        );

        user.setActive(true);

        /*
         * IMPORTANT:
         * The company comes from the authenticated ADMIN.
         * The client cannot choose company_id.
         */
        user.setCompany(company);

        return toResponse(
                userRepository.save(user)
        );
    }

    @Transactional
    public StaffResponse updateStaff(
            Long id,
            UpdateStaffRequest request
    ) {

        SubscriberCompany company =
                getCurrentAdminCompany();

        User user = userRepository
                .findByIdAndCompany(id, company)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Staff member not found"
                        )
                );

        if (!isStaffRole(user.getRole())) {
            throw new RuntimeException(
                    "This account is not a staff account"
            );
        }

        String email =
                request.getEmail().trim().toLowerCase();

        if (!email.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(email)) {

            throw new RuntimeException(
                    "A user with this email already exists"
            );
        }

        user.setFirstName(
                request.getFirstName().trim()
        );

        user.setLastName(
                request.getLastName().trim()
        );

        user.setEmail(email);

        user.setRole(
                toEntityRole(request.getRole())
        );

        return toResponse(
                userRepository.save(user)
        );
    }

    @Transactional
    public StaffResponse activateStaff(Long id) {

        User user = getStaffForCurrentCompany(id);

        user.setActive(true);

        return toResponse(
                userRepository.save(user)
        );
    }

    @Transactional
    public StaffResponse deactivateStaff(Long id) {

        User user = getStaffForCurrentCompany(id);

        user.setActive(false);

        return toResponse(
                userRepository.save(user)
        );
    }

    private User getStaffForCurrentCompany(Long id) {

        SubscriberCompany company =
                getCurrentAdminCompany();

        User user = userRepository
                .findByIdAndCompany(id, company)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Staff member not found"
                        )
                );

        if (!isStaffRole(user.getRole())) {
            throw new RuntimeException(
                    "This account is not a staff account"
            );
        }

        return user;
    }

    private SubscriberCompany getCurrentAdminCompany() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getName() == null) {

            throw new RuntimeException(
                    "Authenticated user not found"
            );
        }

        User admin = userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException(
                    "Only company administrators can manage staff"
            );
        }

        if (admin.getCompany() == null) {
            throw new RuntimeException(
                    "Administrator is not assigned to a company"
            );
        }

        return admin.getCompany();
    }

    private Role toEntityRole(StaffRole role) {

        return switch (role) {

            case MANAGEMENT ->
                    Role.MANAGEMENT;

            case GEOLOGIST ->
                    Role.GEOLOGIST;

            case DRONE_OPERATOR ->
                    Role.DRONE_OPERATOR;
        };
    }

    private boolean isStaffRole(Role role) {

        return role == Role.MANAGEMENT
                || role == Role.GEOLOGIST
                || role == Role.DRONE_OPERATOR;
    }

    private StaffResponse toResponse(User user) {

        return new StaffResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
