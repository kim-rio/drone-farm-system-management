package com.dmfs.client.service;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.client.entity.Client;
import com.dmfs.client.entity.ClientStatus;
import com.dmfs.client.entity.ClientType;
import com.dmfs.client.repository.ClientRepository;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.repository.SubscriberCompanyRepository;
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

    public ClientService(
            ClientRepository clientRepository,
            UserRepository userRepository,
            SubscriberCompanyRepository companyRepository
    ) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Client create(
            String clientCode,
            ClientType type,
            String companyName,
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            String identificationNumber,
            String tin
    ) {

        User staff = getCurrentStaff();

        SubscriberCompany company = staff.getCompany();

        if (company == null) {
            throw new RuntimeException(
                    "Your account is not linked to a subscriber company"
            );
        }

        if (clientRepository.existsByClientCodeAndCompany(
                clientCode,
                company
        )) {
            throw new RuntimeException(
                    "Client code already exists"
            );
        }

        Client client = new Client();

        client.setClientCode(clientCode);
        client.setType(type);
        client.setCompanyName(companyName);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setPhone(phone);
        client.setAddress(address);
        client.setIdentificationNumber(identificationNumber);
        client.setTin(tin);
        client.setStatus(ClientStatus.ACTIVE);
        client.setCompany(company);
        client.setRegisteredBy(staff);

        return clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public List<Client> findAll() {

        User staff = getCurrentStaff();

        SubscriberCompany company = requireCompany(staff);

        return clientRepository.findByCompanyOrderByCreatedAtDesc(
                company
        );
    }

    @Transactional(readOnly = true)
    public Client findById(Long id) {

        User staff = getCurrentStaff();

        SubscriberCompany company = requireCompany(staff);

        return clientRepository.findByIdAndCompany(
                id,
                company
        ).orElseThrow(() ->
                new RuntimeException("Client not found")
        );
    }

    @Transactional
    public Client update(
            Long id,
            ClientType type,
            String companyName,
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            String identificationNumber,
            String tin,
            ClientStatus status
    ) {

        Client client = findById(id);

        client.setType(type);
        client.setCompanyName(companyName);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setPhone(phone);
        client.setAddress(address);
        client.setIdentificationNumber(identificationNumber);
        client.setTin(tin);
        client.setStatus(status);

        return clientRepository.save(client);
    }

    @Transactional
    public void delete(Long id) {

        Client client = findById(id);

        clientRepository.delete(client);
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
