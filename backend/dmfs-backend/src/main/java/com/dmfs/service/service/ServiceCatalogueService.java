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
        validatePersonnelRole(service);
        if (serviceCatalogueRepository.existsByNameIgnoreCase(service.getName())) {
            throw new IllegalArgumentException("A service with this name already exists");
        }
        return serviceCatalogueRepository.save(service);
    }

    public ServiceCatalogue updateService(
            Long id,
            ServiceCatalogue updatedService
    ) {
        ServiceCatalogue existingService = getServiceById(id);

        validatePersonnelRole(updatedService);
        if (serviceCatalogueRepository.existsByNameIgnoreCaseAndIdNot(updatedService.getName(), id)) {
            throw new IllegalArgumentException("A service with this name already exists");
        }

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

    private void validatePersonnelRole(ServiceCatalogue service) {
        String role = service.getRequiredPersonnel();
        if (role == null || role.isBlank() || java.util.Arrays.stream(role.split(","))
                .anyMatch(value -> !"GEOLOGIST".equals(value) && !"DRONE_OPERATOR".equals(value))) {
            throw new IllegalArgumentException(
                    "Required personnel must be GEOLOGIST or DRONE_OPERATOR");
        }
    }
}
