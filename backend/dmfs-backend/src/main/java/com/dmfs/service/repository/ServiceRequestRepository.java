package com.dmfs.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.service.entity.ServiceRequest;
import java.util.List;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByStatusIgnoreCaseOrOperatorIdOrderByCreatedAtDesc(String status, Long operatorId);
}
