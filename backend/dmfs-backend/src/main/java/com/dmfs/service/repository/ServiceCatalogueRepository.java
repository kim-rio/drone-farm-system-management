package com.dmfs.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.service.entity.ServiceCatalogue;

public interface ServiceCatalogueRepository extends JpaRepository<ServiceCatalogue, Long> {

    List<ServiceCatalogue> findByCompanyOrderByNameAsc(SubscriberCompany company);

    Optional<ServiceCatalogue> findByIdAndCompany(Long id, SubscriberCompany company);

    boolean existsByCompanyAndNameIgnoreCase(SubscriberCompany company, String name);

    boolean existsByCompanyAndNameIgnoreCaseAndIdNot(
            SubscriberCompany company,
            String name,
            Long id
    );
}
