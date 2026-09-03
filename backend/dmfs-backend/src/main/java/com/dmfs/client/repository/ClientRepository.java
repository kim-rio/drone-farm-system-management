package com.dmfs.client.repository;

import com.dmfs.client.entity.Client;
import com.dmfs.company.entity.SubscriberCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByClientCode(String clientCode);

    Optional<Client> findByIdAndCompany(Long id, SubscriberCompany company);

    Optional<Client> findByClientCodeAndCompany(
            String clientCode,
            SubscriberCompany company
    );

    boolean existsByClientCode(String clientCode);

    boolean existsByClientCodeAndCompany(
            String clientCode,
            SubscriberCompany company
    );

    List<Client> findByCompanyOrderByCreatedAtDesc(
            SubscriberCompany company
    );
}
