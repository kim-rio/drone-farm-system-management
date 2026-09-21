package com.dmfs.client.service;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.client.entity.Client;
import com.dmfs.client.entity.ClientInvitation;
import com.dmfs.client.entity.ClientUser;
import com.dmfs.client.repository.ClientInvitationRepository;
import com.dmfs.client.repository.ClientRepository;
import com.dmfs.client.repository.ClientUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class ClientPortalService {

    private final ClientRepository clientRepository;
    private final ClientInvitationRepository invitationRepository;
    private final ClientUserRepository clientUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public ClientPortalService(
            ClientRepository clientRepository,
            ClientInvitationRepository invitationRepository,
            ClientUserRepository clientUserRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.clientRepository = clientRepository;
        this.invitationRepository = invitationRepository;
        this.clientUserRepository = clientUserRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public InviteResult invite(
            Long clientId,
            String email,
            User invitedBy,
            String frontendUrl
    ) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() ->
                        new RuntimeException("Client not found")
                );

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Client portal email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException(
                    "A user account already exists with this email"
            );
        }

        if (clientUserRepository.existsByClientId(clientId)) {
            throw new RuntimeException(
                    "This client already has a portal account"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        /*
         * Always create a fresh invitation.
         *
         * Existing unused invitations are NOT invalidated.
         * Every generated token is stored only as a SHA-256 hash,
         * while the raw token is returned once in the activation URL.
         */
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

        ClientInvitation invitation =
                new ClientInvitation();

        invitation.setClient(client);
        invitation.setEmail(normalizedEmail);
        invitation.setTokenHash(hashToken(rawToken));
        invitation.setExpiresAt(now.plusHours(48));
        invitation.setInvitedBy(invitedBy);

        invitationRepository.save(invitation);

        String activationUrl =
                frontendUrl
                        + "/customer/activate?token="
                        + rawToken;

        return new InviteResult(
                client.getId(),
                normalizedEmail,
                invitation.getExpiresAt(),
                activationUrl
        );
    }

    @Transactional(readOnly = true)
    public InvitationInfo validateInvitation(
            String rawToken
    ) {

        ClientInvitation invitation =
                findValidInvitation(rawToken);

        return new InvitationInfo(
                invitation.getClient().getId(),
                invitation.getClient().getClientCode(),
                invitation.getEmail(),
                invitation.getExpiresAt()
        );
    }

    @Transactional
    public void activate(
            String rawToken,
            String firstName,
            String lastName,
            String password
    ) {

        if (firstName == null
                || firstName.isBlank()
                || lastName == null
                || lastName.isBlank()) {

            throw new RuntimeException(
                    "First name and last name are required"
            );
        }

        if (password == null || password.length() < 8) {
            throw new RuntimeException(
                    "Password must contain at least 8 characters"
            );
        }

        ClientInvitation invitation =
                findValidInvitation(rawToken);

        String email =
                invitation.getEmail()
                        .trim()
                        .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "An account already exists with this email"
            );
        }

        Client client = invitation.getClient();

        if (clientUserRepository.existsByClientId(client.getId())) {
            throw new RuntimeException(
                    "This client already has a portal account"
            );
        }

        User user = new User();

        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(password)
        );
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        user.setCompany(client.getCompany());

        user = userRepository.save(user);

        ClientUser link = new ClientUser();

        link.setClientId(client.getId());
        link.setUserId(user.getId());

        clientUserRepository.save(link);

        /*
         * Consume the exact invitation that was successfully used.
         */
        invitation.setUsedAt(LocalDateTime.now());

        invitationRepository.save(invitation);
    }

    private ClientInvitation findValidInvitation(
            String rawToken
    ) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new RuntimeException(
                    "Invitation token is required"
            );
        }

        ClientInvitation invitation =
                invitationRepository
                        .findByTokenHash(
                                hashToken(rawToken)
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid invitation"
                                )
                        );

        if (invitation.getUsedAt() != null) {
            throw new RuntimeException(
                    "This invitation has already been used"
            );
        }

        if (invitation.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "This invitation has expired"
            );
        }

        return invitation;
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder builder =
                    new StringBuilder();

            for (byte b : hash) {
                builder.append(
                        String.format("%02x", b)
                );
            }

            return builder.toString();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to secure invitation token",
                    e
            );
        }
    }

    public record InviteResult(
            Long clientId,
            String email,
            LocalDateTime expiresAt,
            String activationUrl
    ) {
    }

    public record InvitationInfo(
            Long clientId,
            String clientCode,
            String email,
            LocalDateTime expiresAt
    ) {
    }
}
