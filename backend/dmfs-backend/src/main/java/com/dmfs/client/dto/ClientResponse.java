package com.dmfs.client.dto;

import com.dmfs.client.entity.Client;
import com.dmfs.client.entity.ClientStatus;
import com.dmfs.client.entity.ClientType;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String clientCode,
        ClientType type,
        String companyName,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String identificationNumber,
        String tin,
        ClientStatus status,
        Long companyId,
        Long registeredBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ClientResponse from(Client client) {

        return new ClientResponse(
                client.getId(),
                client.getClientCode(),
                client.getType(),
                client.getCompanyName(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getIdentificationNumber(),
                client.getTin(),
                client.getStatus(),
                client.getCompany() != null
                        ? client.getCompany().getId()
                        : null,
                client.getRegisteredBy() != null
                        ? client.getRegisteredBy().getId()
                        : null,
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}
