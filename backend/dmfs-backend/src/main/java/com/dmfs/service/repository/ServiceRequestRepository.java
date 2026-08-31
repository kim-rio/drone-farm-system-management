package com.dmfs.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.service.entity.ServiceRequest;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long> {
}