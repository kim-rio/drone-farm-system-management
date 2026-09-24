package com.dmfs.company.service;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.entity.SubscriberCompany;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentCompanyService {

    private final UserRepository userRepository;

    public CurrentCompanyService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user not found");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    public SubscriberCompany getCurrentCompany() {
        User user = getCurrentUser();

        if (user.getCompany() == null || user.getCompany().getId() == null) {
            throw new IllegalStateException("Authenticated user is not assigned to a company");
        }

        return user.getCompany();
    }
}
