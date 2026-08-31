package com.dmfs.service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dmfs.farm.entity.Block;
import com.dmfs.farm.entity.Farm;
import com.dmfs.farm.repository.BlockRepository;
import com.dmfs.farm.repository.FarmRepository;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceCatalogueRepository;
import com.dmfs.service.repository.ServiceRequestRepository;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final FarmRepository farmRepository;
    private final BlockRepository blockRepository;
    private final ServiceCatalogueRepository serviceCatalogueRepository;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository,
            FarmRepository farmRepository,
            BlockRepository blockRepository,
            ServiceCatalogueRepository serviceCatalogueRepository
    ) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.farmRepository = farmRepository;
        this.blockRepository = blockRepository;
        this.serviceCatalogueRepository = serviceCatalogueRepository;
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

    public ServiceRequest createServiceRequest(
            ServiceRequest serviceRequest
    ) {
        validateRelationships(serviceRequest);

        return serviceRequestRepository.save(serviceRequest);
    }

    public ServiceRequest updateServiceRequest(
            Long id,
            ServiceRequest updatedServiceRequest
    ) {
        ServiceRequest existingServiceRequest =
                getServiceRequestById(id);

        validateRelationships(updatedServiceRequest);

        existingServiceRequest.setCustomer(
                updatedServiceRequest.getCustomer()
        );

        existingServiceRequest.setFarm(
                updatedServiceRequest.getFarm()
        );

        existingServiceRequest.setFarmBlock(
                updatedServiceRequest.getFarmBlock()
        );

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

    private void validateRelationships(
            ServiceRequest serviceRequest
    ) {

        if (serviceRequest.getCustomer() == null) {
            throw new RuntimeException(
                    "Customer is required"
            );
        }

        if (serviceRequest.getFarm() == null) {
            throw new RuntimeException(
                    "Farm is required"
            );
        }

        if (serviceRequest.getFarmBlock() == null) {
            throw new RuntimeException(
                    "Farm block is required"
            );
        }

        if (serviceRequest.getServiceCatalogue() == null) {
            throw new RuntimeException(
                    "Service catalogue is required"
            );
        }

        Farm farm = farmRepository.findById(
                serviceRequest.getFarm().getId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Farm not found"
                )
        );

        Block block = blockRepository.findById(
                serviceRequest.getFarmBlock().getId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Farm block not found"
                )
        );

        serviceCatalogueRepository.findById(
                serviceRequest.getServiceCatalogue().getId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Service catalogue not found"
                )
        );

        if (!farm.getCustomer().getId()
                .equals(serviceRequest.getCustomer().getId())) {

            throw new RuntimeException(
                    "Farm does not belong to the selected customer"
            );
        }

        if (!block.getFarm().getId()
                .equals(farm.getId())) {

            throw new RuntimeException(
                    "Farm block does not belong to the selected farm"
            );
        }
    }

    public void updateStatus(Long id, String status) {
        ServiceRequest serviceRequest =
                getServiceRequestById(id);

        serviceRequest.setStatus(status);

        serviceRequestRepository.save(serviceRequest);
    }

    public void deleteServiceRequest(Long id) {
        ServiceRequest serviceRequest =
                getServiceRequestById(id);

        serviceRequestRepository.delete(serviceRequest);
    }
}
