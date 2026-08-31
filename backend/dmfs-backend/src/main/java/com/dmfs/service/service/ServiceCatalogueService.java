package com.dmfs.service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dmfs.service.entity.ServiceCatalogue;
import com.dmfs.service.repository.ServiceCatalogueRepository;

@Service
public class ServiceCatalogueService {

    private final ServiceCatalogueRepository serviceCatalogueRepository;

    public ServiceCatalogueService(
            ServiceCatalogueRepository serviceCatalogueRepository
    ) {
        this.serviceCatalogueRepository = serviceCatalogueRepository;
    }

    public List<ServiceCatalogue> getAllServices() {
        return serviceCatalogueRepository.findAll();
    }

    public ServiceCatalogue getServiceById(Long id) {
        return serviceCatalogueRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Service not found with id: " + id)
                );
    }

    public ServiceCatalogue createService(ServiceCatalogue service) {
        return serviceCatalogueRepository.save(service);
    }

    public ServiceCatalogue updateService(
            Long id,
            ServiceCatalogue updatedService
    ) {
        ServiceCatalogue existingService = getServiceById(id);

        existingService.setName(updatedService.getName());
        existingService.setCategory(updatedService.getCategory());
        existingService.setDescription(updatedService.getDescription());
        existingService.setStatus(updatedService.getStatus());
        existingService.setUnitOfMeasurement(
                updatedService.getUnitOfMeasurement()
        );
        existingService.setStandardPrice(
                updatedService.getStandardPrice()
        );
        existingService.setMinimumArea(
                updatedService.getMinimumArea()
        );
        existingService.setRequiredEquipment(
                updatedService.getRequiredEquipment()
        );
        existingService.setRequiredPersonnel(
                updatedService.getRequiredPersonnel()
        );
        existingService.setEstimatedDurationMinutes(
                updatedService.getEstimatedDurationMinutes()
        );

        return serviceCatalogueRepository.save(existingService);
    }

    public void deactivateService(Long id) {
        ServiceCatalogue service = getServiceById(id);
        service.setStatus("INACTIVE");
        serviceCatalogueRepository.save(service);
    }
}