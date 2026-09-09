package com.dmfs.client.service;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.client.entity.Client;
import com.dmfs.client.entity.ClientCompany;
import com.dmfs.client.entity.ClientIndividual;
import com.dmfs.client.entity.ClientStatus;
import com.dmfs.client.entity.ClientType;
import com.dmfs.client.repository.ClientRepository;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.repository.SubscriberCompanyRepository;
import com.dmfs.farm.entity.Farm;
import com.dmfs.farm.service.FarmService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final SubscriberCompanyRepository companyRepository;
    private final FarmService farmService;

    public ClientService(
            ClientRepository clientRepository,
            UserRepository userRepository,
            SubscriberCompanyRepository companyRepository,
            FarmService farmService
    ) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.farmService = farmService;
    }

    @Transactional
    public Client create(
            String clientCode,
            ClientType type,
            String companyName,
            String registrationNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            String tin
    ) {

        User staff = getCurrentStaff();
        SubscriberCompany company = requireCompany(staff);

        if (clientRepository.existsByClientCodeAndCompany(
                clientCode,
                company
        )) {
            throw new RuntimeException("Client code already exists");
        }

        validateClientData(
                type,
                companyName,
                firstName,
                lastName,
                email,
                phone
        );

        Client client = new Client();

        client.setClientCode(clientCode);
        client.setType(type);
        client.setStatus(ClientStatus.ACTIVE);
        client.setCompany(company);
        client.setRegisteredBy(staff);

        attachProfile(
                client,
                type,
                companyName,
                registrationNumber,
                firstName,
                lastName,
                email,
                phone,
                address,
                tin
        );

        return clientRepository.save(client);
    }

    @Transactional
    public Client createWithInitialFarm(String clientCode, ClientType type, String companyName,
            String registrationNumber, String firstName, String lastName, String email, String phone,
            String address, String tin, String farmName, String farmDescription, Double latitude,
            Double longitude, Double areaHectares) {
        Client client = create(clientCode, type, companyName, registrationNumber, firstName, lastName,
                email, phone, address, tin);
        farmService.createFarm(client.getId(), farmName, farmDescription, latitude, longitude, areaHectares);
        return client;
    }

    @Transactional(readOnly = true)
    public List<Client> findAll() {

        User staff = getCurrentStaff();

        SubscriberCompany company = requireCompany(staff);

        return clientRepository.findByCompanyOrderByCreatedAtDesc(company);
    }

    @Transactional(readOnly = true)
    public Client findById(Long id) {

        User staff = getCurrentStaff();

        SubscriberCompany company = requireCompany(staff);

        return clientRepository.findByIdAndCompany(id, company)
                .orElseThrow(() ->
                        new RuntimeException("Client not found")
                );
    }

    @Transactional
    public Client update(
            Long id,
            ClientType type,
            String companyName,
            String registrationNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            String tin,
            ClientStatus status
    ) {

        Client client = findById(id);

        validateClientData(
                type,
                companyName,
                firstName,
                lastName,
                email,
                phone
        );

        client.setType(type);
        client.setStatus(status);

        attachProfile(
                client,
                type,
                companyName,
                registrationNumber,
                firstName,
                lastName,
                email,
                phone,
                address,
                tin
        );

        return clientRepository.save(client);
    }

    @Transactional
    public void delete(Long id) {

        Client client = findById(id);

        clientRepository.delete(client);
    }

    private void attachProfile(
            Client client,
            ClientType type,
            String companyName,
            String registrationNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            String tin
    ) {

        if (type == ClientType.COMPANY) {

            ClientIndividual oldIndividual =
                    client.getIndividualProfile();

            if (oldIndividual != null) {
                client.setIndividualProfile(null);
            }

            ClientCompany profile =
                    client.getCompanyProfile();

            if (profile == null) {
                profile = new ClientCompany();
                profile.setClient(client);
            }

            profile.setCompanyName(
                    requireValue(
                            companyName,
                            "Company name is required"
                    )
            );

            profile.setRegistrationNumber(
                    clean(registrationNumber)
            );

            profile.setTin(clean(tin));

            profile.setEmail(
                    requireValue(
                            email,
                            "Company email is required"
                    )
            );

            profile.setPhone(
                    requireValue(
                            phone,
                            "Company phone is required"
                    )
            );

            profile.setAddress(clean(address));

            client.setCompanyProfile(profile);

        } else if (type == ClientType.INDIVIDUAL) {

            ClientCompany oldCompany =
                    client.getCompanyProfile();

            if (oldCompany != null) {
                client.setCompanyProfile(null);
            }

            ClientIndividual profile =
                    client.getIndividualProfile();

            if (profile == null) {
                profile = new ClientIndividual();
                profile.setClient(client);
            }

            profile.setFirstName(
                    requireValue(
                            firstName,
                            "First name is required"
                    )
            );

            profile.setLastName(
                    requireValue(
                            lastName,
                            "Last name is required"
                    )
            );

            profile.setEmail(
                    requireValue(
                            email,
                            "Individual email is required"
                    )
            );

            profile.setPhone(
                    requireValue(
                            phone,
                            "Individual phone is required"
                    )
            );

            profile.setAddress(clean(address));

            client.setIndividualProfile(profile);

        } else {
            throw new RuntimeException(
                    "Unsupported client type"
            );
        }
    }

    private void validateClientData(
            ClientType type,
            String companyName,
            String firstName,
            String lastName,
            String email,
            String phone
    ) {

        if (type == null) {
            throw new RuntimeException(
                    "Client type is required"
            );
        }

        if (type == ClientType.COMPANY) {

            requireValue(
                    companyName,
                    "Company name is required"
            );

            requireValue(
                    email,
                    "Company email is required"
            );

            requireValue(
                    phone,
                    "Company phone is required"
            );

        } else if (type == ClientType.INDIVIDUAL) {

            requireValue(
                    firstName,
                    "First name is required"
            );

            requireValue(
                    lastName,
                    "Last name is required"
            );

            requireValue(
                    email,
                    "Individual email is required"
            );

            requireValue(
                    phone,
                    "Individual phone is required"
            );
        }
    }

    private String requireValue(
            String value,
            String message
    ) {

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(message);
        }

        return value.trim();
    }

    private String clean(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private User getCurrentStaff() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.MANAGEMENT
                && user.getRole() != Role.GEOLOGIST
                && user.getRole() != Role.DRONE_OPERATOR) {

            throw new RuntimeException(
                    "Only company staff can manage clients"
            );
        }

        return user;
    }

    private SubscriberCompany requireCompany(User user) {

        if (user.getCompany() == null) {
            throw new RuntimeException(
                    "User is not linked to a subscriber company"
            );
        }

        return user.getCompany();
    }
}
