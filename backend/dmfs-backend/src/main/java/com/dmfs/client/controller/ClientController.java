package com.dmfs.client.controller;

import com.dmfs.client.dto.ClientResponse;
import com.dmfs.client.entity.Client;
import com.dmfs.client.entity.ClientStatus;
import com.dmfs.client.entity.ClientType;
import com.dmfs.client.service.ClientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'MANAGEMENT',
                'GEOLOGIST',
                'DRONE_OPERATOR'
            )
            """)
    public ClientResponse create(
            @Valid @RequestBody CreateClientRequest request
    ) {

        return ClientResponse.from(
                clientService.create(
                        request.clientCode(),
                        request.type(),
                        request.companyName(),
                        request.firstName(),
                        request.lastName(),
                        request.email(),
                        request.phone(),
                        request.address(),
                        request.identificationNumber(),
                        request.tin()
                )
        );
    }

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'MANAGEMENT',
                'GEOLOGIST',
                'DRONE_OPERATOR'
            )
            """)
    public List<ClientResponse> findAll() {

        return clientService.findAll()
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'MANAGEMENT',
                'GEOLOGIST',
                'DRONE_OPERATOR'
            )
            """)
    public ClientResponse findById(
            @PathVariable Long id
    ) {

        return ClientResponse.from(
                clientService.findById(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'MANAGEMENT',
                'GEOLOGIST',
                'DRONE_OPERATOR'
            )
            """)
    public ClientResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientRequest request
    ) {

        return ClientResponse.from(
                clientService.update(
                        id,
                        request.type(),
                        request.companyName(),
                        request.firstName(),
                        request.lastName(),
                        request.email(),
                        request.phone(),
                        request.address(),
                        request.identificationNumber(),
                        request.tin(),
                        request.status()
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'MANAGEMENT'
            )
            """)
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        clientService.delete(id);

        return ResponseEntity.noContent().build();
    }

    public record CreateClientRequest(

            @NotBlank
            String clientCode,

            @NotNull
            ClientType type,

            String companyName,

            String firstName,

            String lastName,

            @Email
            String email,

            String phone,

            String address,

            String identificationNumber,

            String tin
    ) {
    }

    public record UpdateClientRequest(

            @NotNull
            ClientType type,

            String companyName,

            String firstName,

            String lastName,

            String email,

            String phone,

            String address,

            String identificationNumber,

            String tin,

            @NotNull
            ClientStatus status
    ) {
    }
}
