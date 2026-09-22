package com.dmfs.service.controller;

import com.dmfs.client.service.CustomerContextService;
import com.dmfs.service.dto.ServiceRequestResponse;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceRequestRepository;
import com.dmfs.service.service.ServiceRequestMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/service-requests")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerServiceRequestController {

    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerContextService customerContextService;

    public CustomerServiceRequestController(
            ServiceRequestRepository serviceRequestRepository,
            CustomerContextService customerContextService
    ) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.customerContextService = customerContextService;
    }

    @GetMapping
    public List<ServiceRequestResponse> getMine() {

        Long clientId =
                customerContextService
                        .currentClient()
                        .getId();

        return serviceRequestRepository
                .findByCustomerIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(ServiceRequestMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ServiceRequestResponse getMineById(
            @PathVariable Long id
    ) {

        Long clientId =
                customerContextService
                        .currentClient()
                        .getId();

        ServiceRequest request =
                serviceRequestRepository
                        .findByIdAndCustomerId(id, clientId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Service request not found"
                                )
                        );

        return ServiceRequestMapper.toResponse(request);
    }
}
