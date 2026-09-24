package com.dmfs.service.repository;

import com.dmfs.service.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByStatusIgnoreCaseOrOperatorIdOrderByCreatedAtDesc(
            String status,
            Long operatorId
    );

    List<ServiceRequest> findByCustomerCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<ServiceRequest> findByCustomerCompanyId(Long companyId);

    long countByCustomerCompanyId(Long companyId);

    long countByCustomerCompanyIdAndPaymentStatusIgnoreCase(Long companyId, String paymentStatus);

    long countByCustomerCompanyIdAndStatusIgnoreCase(Long companyId, String status);

    List<ServiceRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<ServiceRequest> findByIdAndCustomerId(Long id, Long customerId);

    Optional<ServiceRequest> findByIdAndCustomerCompanyId(Long id, Long companyId);
}
