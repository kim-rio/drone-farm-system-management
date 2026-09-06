package com.dmfs.client.dto;

import com.dmfs.client.entity.Client;
import com.dmfs.client.entity.ClientCompany;
import com.dmfs.client.entity.ClientIndividual;
import com.dmfs.client.entity.ClientStatus;
import com.dmfs.client.entity.ClientType;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String clientCode,
        ClientType type,
        String companyName,
        String registrationNumber,
        String tin,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        ClientStatus status,
        Long companyId,
        Long registeredBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ClientResponse from(Client client) {

        ClientCompany companyProfile =
                client.getCompanyProfile();

        ClientIndividual individualProfile =
                client.getIndividualProfile();

        String companyName = null;
        String registrationNumber = null;
        String tin = null;

        String firstName = null;
        String lastName = null;

        String email = null;
        String phone = null;
        String address = null;

        if (client.getType() == ClientType.COMPANY
                && companyProfile != null) {

            companyName =
                    companyProfile.getCompanyName();

            registrationNumber =
                    companyProfile.getRegistrationNumber();

            tin =
                    companyProfile.getTin();

            email =
                    companyProfile.getEmail();

            phone =
                    companyProfile.getPhone();

            address =
                    companyProfile.getAddress();
        }

        if (client.getType() == ClientType.INDIVIDUAL
                && individualProfile != null) {

            firstName =
                    individualProfile.getFirstName();

            lastName =
                    individualProfile.getLastName();

            email =
                    individualProfile.getEmail();

            phone =
                    individualProfile.getPhone();

            address =
                    individualProfile.getAddress();
        }

        return new ClientResponse(
                client.getId(),
                client.getClientCode(),
                client.getType(),
                companyName,
                registrationNumber,
                tin,
                firstName,
                lastName,
                email,
                phone,
                address,
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
