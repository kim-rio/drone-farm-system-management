package com.dmfs.client.service;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.client.entity.Client;
import com.dmfs.client.entity.ClientUser;
import com.dmfs.client.repository.ClientUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CustomerContextService {

    private final UserRepository userRepository;
    private final ClientUserRepository clientUserRepository;

    public CustomerContextService(
            UserRepository userRepository,
            ClientUserRepository clientUserRepository
    ) {
        this.userRepository = userRepository;
        this.clientUserRepository = clientUserRepository;
    }

    public User currentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new RuntimeException("Authentication required");
        }

        User user = userRepository
                .findByEmail(
                        authentication.getName()
                                .trim()
                                .toLowerCase()
                )
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found")
                );

        if (user.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("Customer account required");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Customer account is inactive");
        }

        return user;
    }

    public Client currentClient() {

        User user = currentUser();

        ClientUser clientUser =
                clientUserRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer account is not linked to a client"
                                )
                        );

        return clientUser.getClient();
    }
}
