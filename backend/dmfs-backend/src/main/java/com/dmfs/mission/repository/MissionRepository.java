package com.dmfs.mission.repository;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.mission.entity.Mission;
import com.dmfs.auth.entity.User;
import com.dmfs.service.entity.ServiceRequest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionRepository
        extends JpaRepository<Mission, Long> {

    List<Mission> findByCompanyOrderByCreatedAtDesc(
            SubscriberCompany company
    );

    List<Mission> findByCompanyAndOperatorOrderByCreatedAtDesc(
            SubscriberCompany company,
            User operator
    );

    Optional<Mission> findByIdAndCompany(
            Long id,
            SubscriberCompany company
    );

    boolean existsByServiceRequest(
            ServiceRequest serviceRequest
    );
}
