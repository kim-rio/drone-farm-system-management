package com.dmfs.company.service;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.dto.ChangeCompanyStatusRequest;
import com.dmfs.company.dto.CompanyResponse;
import com.dmfs.company.dto.CreateCompanyRequest;
import com.dmfs.company.dto.UpdateCompanyRequest;
import com.dmfs.company.entity.CompanyStatus;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.repository.SubscriberCompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriberCompanyService {

    private final SubscriberCompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SubscriberCompanyService(
            SubscriberCompanyRepository companyRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {

        if (companyRepository.existsByRegistrationNumber(
                request.getRegistrationNumber()
        )) {
            throw new RuntimeException(
                    "Company registration number already exists"
            );
        }

        if (request.getInitialAdmin() == null) {
            throw new RuntimeException(
                    "Initial company admin information is required"
            );
        }

        if (userRepository.existsByEmail(
                request.getInitialAdmin().getEmail()
        )) {
            throw new RuntimeException(
                    "Admin email already exists"
            );
        }

        SubscriberCompany company = new SubscriberCompany();

        company.setName(request.getName());
        company.setRegistrationNumber(
                request.getRegistrationNumber()
        );
        company.setTin(request.getTin());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setCountry(request.getCountry());
        company.setRegion(request.getRegion());
        company.setCity(request.getCity());
        company.setPhysicalAddress(request.getPhysicalAddress());
        company.setStatus(CompanyStatus.ACTIVE);

        company = companyRepository.save(company);

        User admin = new User();

        admin.setEmail(
                request.getInitialAdmin().getEmail()
        );

        admin.setPassword(
                passwordEncoder.encode(
                        request.getInitialAdmin().getPassword()
                )
        );

        admin.setFirstName(
                request.getInitialAdmin().getFirstName()
        );

        admin.setLastName(
                request.getInitialAdmin().getLastName()
        );

        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setCompany(company);

        userRepository.save(admin);

        return toResponse(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAll() {
        return companyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse getById(Long id) {

        SubscriberCompany company =
                companyRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subscriber company not found"
                                )
                        );

        return toResponse(company);
    }

    @Transactional
    public CompanyResponse update(
            Long id,
            UpdateCompanyRequest request
    ) {

        SubscriberCompany company =
                companyRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subscriber company not found"
                                )
                        );

        if (!company.getRegistrationNumber()
                .equals(request.getRegistrationNumber())
                && companyRepository.existsByRegistrationNumber(
                        request.getRegistrationNumber()
                )) {

            throw new RuntimeException(
                    "Company registration number already exists"
            );
        }

        company.setName(request.getName());
        company.setRegistrationNumber(
                request.getRegistrationNumber()
        );
        company.setTin(request.getTin());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setCountry(request.getCountry());
        company.setRegion(request.getRegion());
        company.setCity(request.getCity());
        company.setPhysicalAddress(
                request.getPhysicalAddress()
        );

        company = companyRepository.save(company);

        return toResponse(company);
    }

    @Transactional
    public CompanyResponse changeStatus(
            Long id,
            ChangeCompanyStatusRequest request
    ) {

        SubscriberCompany company =
                companyRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subscriber company not found"
                                )
                        );

        company.setStatus(request.getStatus());

        company = companyRepository.save(company);

        return toResponse(company);
    }

    @Transactional
    public CompanyResponse activate(Long id) {
        return setStatus(id, CompanyStatus.ACTIVE);
    }

    @Transactional
    public CompanyResponse suspend(Long id) {
        return setStatus(id, CompanyStatus.SUSPENDED);
    }

    @Transactional
    public CompanyResponse expire(Long id) {
        return setStatus(id, CompanyStatus.EXPIRED);
    }

    private CompanyResponse setStatus(
            Long id,
            CompanyStatus status
    ) {

        SubscriberCompany company =
                companyRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subscriber company not found"
                                )
                        );

        company.setStatus(status);

        company = companyRepository.save(company);

        return toResponse(company);
    }

    private CompanyResponse toResponse(
            SubscriberCompany company
    ) {

        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getRegistrationNumber(),
                company.getTin(),
                company.getEmail(),
                company.getPhone(),
                company.getCountry(),
                company.getRegion(),
                company.getCity(),
                company.getPhysicalAddress(),
                company.getStatus(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
