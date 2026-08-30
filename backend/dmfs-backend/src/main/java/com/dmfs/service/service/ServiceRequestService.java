package com.dmfs.service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceRequestRepository;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository
    ) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public List<ServiceRequest> getAllServiceRequests() {
        return serviceRequestRepository.findAll();
    }

    public ServiceRequest getServiceRequestById(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Service request not found with id: " + id
                        )
                );
    }

    public ServiceRequest createServiceRequest(ServiceRequest serviceRequest) {
        return serviceRequestRepository.save(serviceRequest);
    }

    public ServiceRequest updateServiceRequest(
            Long id,
            ServiceRequest updatedServiceRequest
    ) {
        ServiceRequest existingServiceRequest =
                getServiceRequestById(id);

        existingServiceRequest.setServiceCatalogue(
                updatedServiceRequest.getServiceCatalogue()
        );

        existingServiceRequest.setRequestedDate(
                updatedServiceRequest.getRequestedDate()
        );

        existingServiceRequest.setNotes(
                updatedServiceRequest.getNotes()
        );

        existingServiceRequest.setStatus(
                updatedServiceRequest.getStatus()
        );

        return serviceRequestRepository.save(existingServiceRequest);
    }

    public void updateStatus(Long id, String status) {
        ServiceRequest serviceRequest =
                getServiceRequestById(id);

        serviceRequest.setStatus(status);

        serviceRequestRepository.save(serviceRequest);
    }
    public void deleteServiceRequest(Long id) {
    ServiceRequest serviceRequest = getServiceRequestById(id);
    serviceRequestRepository.delete(serviceRequest);
}
}