package com.dmfs.client.controller;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.client.service.ClientPortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ClientPortalController {

    private final ClientPortalService clientPortalService;
    private final UserRepository userRepository;

    public ClientPortalController(
            ClientPortalService clientPortalService,
            UserRepository userRepository
    ) {
        this.clientPortalService = clientPortalService;
        this.userRepository = userRepository;
    }

    @PostMapping("/clients/{clientId}/portal-invite")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGEMENT')")
    public ResponseEntity<?> invite(
            @PathVariable Long clientId,
            @RequestBody InviteRequest request
    ) {

        User invitedBy =
                userRepository
                        .findByEmail(
                                org.springframework.security.core.context
                                        .SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getName()
                                        .toLowerCase()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inviting user not found"
                                )
                        );

        String frontendUrl =
                System.getenv()
                        .getOrDefault(
                                "DMFS_FRONTEND_URL",
                                "http://localhost:4200"
                        );

        return ResponseEntity.ok(
                clientPortalService.invite(
                        clientId,
                        request.email(),
                        invitedBy,
                        frontendUrl
                )
        );
    }

    @GetMapping("/client-portal/invitations/{token}")
    public ResponseEntity<?> validate(
            @PathVariable String token
    ) {

        return ResponseEntity.ok(
                clientPortalService.validateInvitation(token)
        );
    }

    @PostMapping("/client-portal/activate")
    public ResponseEntity<?> activate(
            @RequestBody ActivateRequest request
    ) {

        clientPortalService.activate(
                request.token(),
                request.firstName(),
                request.lastName(),
                request.password()
        );

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message",
                        "Customer portal account activated successfully"
                )
        );
    }

    public record InviteRequest(
            String email
    ) {
    }

    public record ActivateRequest(
            String token,
            String firstName,
            String lastName,
            String password
    ) {
    }
}
