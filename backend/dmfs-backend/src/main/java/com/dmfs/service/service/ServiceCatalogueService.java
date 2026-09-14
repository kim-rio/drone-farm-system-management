package com.dmfs.service.service;

import java.util.Arrays;
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
        long start = System.nanoTime();

        System.out.println(
                "[SERVICE-CATALOGUE] getAllServices START"
        );

        List<ServiceCatalogue> result =
                serviceCatalogueRepository.findAll();

        long elapsedMs =
                (System.nanoTime() - start) / 1_000_000;

        System.out.println(
                "[SERVICE-CATALOGUE] findAll() completed in "
                        + elapsedMs
                        + " ms; rows="
                        + result.size()
        );

        return result;
    }

    public ServiceCatalogue getServiceById(Long id) {
        return serviceCatalogueRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Service not found with id: " + id
                        )
                );
    }

    public ServiceCatalogue createService(ServiceCatalogue service) {
        normalize(service);
        validate(service);

        if (serviceCatalogueRepository.existsByNameIgnoreCase(
                service.getName()
        )) {
            throw new IllegalArgumentException(
                    "A service with this name already exists"
            );
        }

        return serviceCatalogueRepository.save(service);
    }

    public ServiceCatalogue updateService(
            Long id,
            ServiceCatalogue updatedService
    ) {
        ServiceCatalogue existingService = getServiceById(id);

        normalize(updatedService);
        validate(updatedService);

        if (serviceCatalogueRepository
                .existsByNameIgnoreCaseAndIdNot(
                        updatedService.getName(),
                        id
                )) {
            throw new IllegalArgumentException(
                    "A service with this name already exists"
            );
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

    private void normalize(ServiceCatalogue service) {
        service.setName(trim(service.getName()));
        service.setCategory(trim(service.getCategory()));
        service.setDescription(trim(service.getDescription()));
        service.setUnitOfMeasurement(
                trim(service.getUnitOfMeasurement())
        );
        service.setRequiredEquipment(
                trim(service.getRequiredEquipment())
        );

        if (service.getRequiredPersonnel() != null) {
            String normalizedPersonnel = Arrays.stream(
                            service.getRequiredPersonnel().split(",")
                    )
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(value ->
                            value.toUpperCase().replace(' ', '_')
                    )
                    .distinct()
                    .reduce(
                            (left, right) ->
                                    left + "," + right
                    )
                    .orElse("");

            service.setRequiredPersonnel(normalizedPersonnel);
        }
    }

    private void validate(ServiceCatalogue service) {
        if (isBlank(service.getName())) {
            throw new IllegalArgumentException(
                    "Service name is required"
            );
        }

        if (isBlank(service.getCategory())) {
            throw new IllegalArgumentException(
                    "Category is required"
            );
        }

        if (isBlank(service.getDescription())) {
            throw new IllegalArgumentException(
                    "Description is required"
            );
        }

        if (isBlank(service.getUnitOfMeasurement())) {
            throw new IllegalArgumentException(
                    "Unit of measurement is required"
            );
        }

        if (
                service.getStandardPrice() == null
                        || service.getStandardPrice().signum() < 0
        ) {
            throw new IllegalArgumentException(
                    "Price must be zero or greater"
            );
        }

        if (
                service.getMinimumArea() == null
                        || service.getMinimumArea().signum() <= 0
        ) {
            throw new IllegalArgumentException(
                    "Minimum area must be greater than zero"
            );
        }

        if (
                service.getEstimatedDurationMinutes() == null
                        || service.getEstimatedDurationMinutes() <= 0
        ) {
            throw new IllegalArgumentException(
                    "Estimated duration must be greater than zero"
            );
        }

        if (isBlank(service.getRequiredPersonnel())) {
            throw new IllegalArgumentException(
                    "At least one required personnel role must be selected"
            );
        }

        boolean invalidRole = Arrays.stream(
                        service.getRequiredPersonnel().split(",")
                )
                .anyMatch(value ->
                        !"GEOLOGIST".equals(value)
                                && !"DRONE_OPERATOR".equals(value)
                );

        if (invalidRole) {
            throw new IllegalArgumentException(
                    "Required personnel must be GEOLOGIST or DRONE_OPERATOR"
            );
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
