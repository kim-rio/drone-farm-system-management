package com.dmfs.service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dmfs.service.dto.ServiceRequestResponse;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.service.ServiceRequestMapper;
import com.dmfs.service.service.ServiceRequestService;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;


    public ServiceRequestController(
            ServiceRequestService serviceRequestService
    ) {
        this.serviceRequestService =
                serviceRequestService;
    }


    @GetMapping
    public List<ServiceRequestResponse>
    getAllServiceRequests() {

        return serviceRequestService
                .getAllServiceRequests()
                .stream()
                .map(ServiceRequestMapper::toResponse)
                .toList();
    }


    @GetMapping("/{id}")
    public ServiceRequestResponse
    getServiceRequestById(
            @PathVariable Long id
    ) {

        ServiceRequest request =
                serviceRequestService
                        .getServiceRequestById(id);

        return ServiceRequestMapper
                .toResponse(request);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceRequest createServiceRequest(
            @RequestBody ServiceRequest serviceRequest
    ) {

        return serviceRequestService
                .createServiceRequest(serviceRequest);
    }


    @PutMapping("/{id}")
    public ServiceRequest updateServiceRequest(
            @PathVariable Long id,
            @RequestBody ServiceRequest serviceRequest
    ) {

        return serviceRequestService
                .updateServiceRequest(
                        id,
                        serviceRequest
                );
    }


    @PatchMapping("/{id}/status")
    public void updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {

        serviceRequestService
                .updateStatus(id, status);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteServiceRequest(
            @PathVariable Long id
    ) {

        serviceRequestService
                .getServiceRequestById(id);

        serviceRequestService
                .deleteServiceRequest(id);
    }
}