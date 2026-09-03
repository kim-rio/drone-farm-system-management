package com.dmfs.admin.company.service;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.dto.CompanyResponse;
import com.dmfs.company.entity.SubscriberCompany;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCompanyService {

    private final UserRepository userRepository;

    public AdminCompanyService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCurrentAdminCompany() {

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
                    "Only company administrators can access company information"
            );
        }

        if (admin.getCompany() == null) {
            throw new RuntimeException(
                    "Administrator is not assigned to a company"
            );
        }

        SubscriberCompany company =
                admin.getCompany();

        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getRegistrationNumber(),
                company.getTin(),
                company.getEmail(),
                company.getPhone(),
                company.getCountry(),
                company.getRegion(),
                company.getCity(),
                company.getPhysicalAddress(),
                company.getStatus(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
