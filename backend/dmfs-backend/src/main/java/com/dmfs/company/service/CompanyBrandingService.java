package com.dmfs.company.service;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.dto.CompanyBrandingResponse;
import com.dmfs.company.entity.SubscriberCompany;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CompanyBrandingService {

    private final UserRepository userRepository;

    public CompanyBrandingService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public SubscriberCompany getCurrentCompany() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
        ) {
            throw new IllegalStateException(
                    "Authenticated user is required."
            );
        }

        User user =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Authenticated user was not found."
                                )
                        );

        if (user.getCompany() == null) {
            throw new IllegalStateException(
                    "Authenticated user is not associated with a company."
            );
        }

        return user.getCompany();
    }

    public CompanyBrandingResponse getBranding() {

        SubscriberCompany company =
                getCurrentCompany();

        String logoUrl =
                company.getLogoPath() == null
                        ? null
                        : "/api/company/branding/logo";

        return new CompanyBrandingResponse(
                company.getId(),
                company.getName(),
                logoUrl
        );
    }
}