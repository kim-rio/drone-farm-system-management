package com.dmfs.client.controller;

import com.dmfs.client.dto.ClientResponse;
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
                        request.registrationNumber(),
                        request.firstName(),
                        request.lastName(),
                        request.email(),
                        request.phone(),
                        request.address(),
                        request.tin()
                )
        );
    }

    @PostMapping("/with-initial-farm")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ClientResponse createWithInitialFarm(@Valid @RequestBody CreateClientWithFarmRequest request) {
        CreateClientRequest client = request.client();
        FarmDetails farm = request.farm();
        return ClientResponse.from(clientService.createWithInitialFarm(client.clientCode(), client.type(),
                client.companyName(), client.registrationNumber(), client.firstName(), client.lastName(),
                client.email(), client.phone(), client.address(), client.tin(), farm.name(), farm.description(),
                farm.latitude(), farm.longitude(), farm.areaHectares()));
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
                        request.registrationNumber(),
                        request.firstName(),
                        request.lastName(),
                        request.email(),
                        request.phone(),
                        request.address(),
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

            String registrationNumber,

            String firstName,

            String lastName,

            @Email
            @NotBlank
            String email,

            @NotBlank
            String phone,

            String address,

            String tin
    ) {
    }

    public record UpdateClientRequest(

            @NotNull
            ClientType type,

            String companyName,

            String registrationNumber,

            String firstName,

            String lastName,

            @Email
            @NotBlank
            String email,

            @NotBlank
            String phone,

            String address,

            String tin,

            @NotNull
            ClientStatus status
    ) {
    }

    public record CreateClientWithFarmRequest(@Valid @NotNull CreateClientRequest client,
            @Valid @NotNull FarmDetails farm) { }
    public record FarmDetails(@NotBlank String name, String description, @NotNull Double latitude,
            @NotNull Double longitude, @NotNull Double areaHectares) { }
}
