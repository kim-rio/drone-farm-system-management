package com.dmfs.company.repository;

import com.dmfs.company.entity.CompanyStatus;
import com.dmfs.company.entity.SubscriberCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriberCompanyRepository
        extends JpaRepository<SubscriberCompany, Long> {

    boolean existsByRegistrationNumber(String registrationNumber);

    Optional<SubscriberCompany> findByRegistrationNumber(
            String registrationNumber
    );

    long countByStatus(CompanyStatus status);
}
