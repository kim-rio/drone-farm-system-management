package com.dmfs.service.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.service.CurrentCompanyService;
import com.dmfs.service.entity.ServiceCatalogue;
import com.dmfs.service.repository.ServiceCatalogueRepository;

@Service
public class ServiceCatalogueService {

    private final ServiceCatalogueRepository serviceCatalogueRepository;
    private final CurrentCompanyService currentCompanyService;

    public ServiceCatalogueService(
            ServiceCatalogueRepository serviceCatalogueRepository,
            CurrentCompanyService currentCompanyService
    ) {
        this.serviceCatalogueRepository = serviceCatalogueRepository;
        this.currentCompanyService = currentCompanyService;
    }

    @Transactional(readOnly = true)
    public List<ServiceCatalogue> getAllServices() {
        SubscriberCompany company = currentCompanyService.getCurrentCompany();
        return serviceCatalogueRepository.findByCompanyOrderByNameAsc(company);
    }

    @Transactional(readOnly = true)
    public ServiceCatalogue getServiceById(Long id) {
        SubscriberCompany company = currentCompanyService.getCurrentCompany();
        return serviceCatalogueRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
    }

    @Transactional
    public ServiceCatalogue createService(ServiceCatalogue service) {
        SubscriberCompany company = currentCompanyService.getCurrentCompany();
        normalize(service);
        validate(service);

        if (serviceCatalogueRepository.existsByCompanyAndNameIgnoreCase(company, service.getName())) {
            throw new IllegalArgumentException("A service with this name already exists in your company");
        }

        // Never trust company_id from the client request.
        service.setCompany(company);
        return serviceCatalogueRepository.save(service);
    }

    @Transactional
    public ServiceCatalogue updateService(Long id, ServiceCatalogue updatedService) {
        SubscriberCompany company = currentCompanyService.getCurrentCompany();
        ServiceCatalogue existingService = serviceCatalogueRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));

        normalize(updatedService);
        validate(updatedService);

        if (serviceCatalogueRepository.existsByCompanyAndNameIgnoreCaseAndIdNot(
                company, updatedService.getName(), id)) {
            throw new IllegalArgumentException("A service with this name already exists in your company");
        }

        existingService.setName(updatedService.getName());
        existingService.setCategory(updatedService.getCategory());
        existingService.setDescription(updatedService.getDescription());
        existingService.setStatus(updatedService.getStatus());
        existingService.setUnitOfMeasurement(updatedService.getUnitOfMeasurement());
        existingService.setStandardPrice(updatedService.getStandardPrice());
        existingService.setMinimumArea(updatedService.getMinimumArea());
        existingService.setRequiredEquipment(updatedService.getRequiredEquipment());
        existingService.setRequiredPersonnel(updatedService.getRequiredPersonnel());
        existingService.setEstimatedDurationMinutes(updatedService.getEstimatedDurationMinutes());

        // Preserve the existing tenant even if a malicious body contains company data.
        existingService.setCompany(company);
        return serviceCatalogueRepository.save(existingService);
    }

    @Transactional
    public void deactivateService(Long id) {
        ServiceCatalogue service = getServiceById(id);
        service.setStatus("INACTIVE");
        serviceCatalogueRepository.save(service);
    }

    private void normalize(ServiceCatalogue service) {
        service.setName(trim(service.getName()));
        service.setCategory(normalizeCategory(service.getCategory()));
        service.setDescription(trim(service.getDescription()));
        service.setUnitOfMeasurement(trim(service.getUnitOfMeasurement()));
        service.setRequiredEquipment(trim(service.getRequiredEquipment()));

        if (service.getRequiredPersonnel() != null) {
            String normalizedPersonnel = Arrays.stream(service.getRequiredPersonnel().split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(value -> value.toUpperCase().replace(' ', '_'))
                    .distinct()
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");
            service.setRequiredPersonnel(normalizedPersonnel);
        }
    }

    private void validate(ServiceCatalogue service) {
        if (isBlank(service.getName())) throw new IllegalArgumentException("Service name is required");
        if (isBlank(service.getCategory())) throw new IllegalArgumentException("Category is required");
        if (!"MINING".equals(service.getCategory()) && !"AGRICULTURE".equals(service.getCategory()))
            throw new IllegalArgumentException("Category must be either MINING or AGRICULTURE");
        if (isBlank(service.getDescription())) throw new IllegalArgumentException("Description is required");
        if (isBlank(service.getUnitOfMeasurement())) throw new IllegalArgumentException("Unit of measurement is required");
        if (service.getStandardPrice() == null || service.getStandardPrice().signum() < 0)
            throw new IllegalArgumentException("Price must be zero or greater");
        if (service.getMinimumArea() == null || service.getMinimumArea().signum() <= 0)
            throw new IllegalArgumentException("Minimum area must be greater than zero");
        if (service.getEstimatedDurationMinutes() == null || service.getEstimatedDurationMinutes() <= 0)
            throw new IllegalArgumentException("Estimated duration must be greater than zero");
        if (isBlank(service.getRequiredPersonnel()))
            throw new IllegalArgumentException("At least one required personnel role must be selected");

        boolean invalidRole = Arrays.stream(service.getRequiredPersonnel().split(","))
                .anyMatch(value -> !"GEOLOGIST".equals(value) && !"DRONE_OPERATOR".equals(value));
        if (invalidRole)
            throw new IllegalArgumentException("Required personnel must be GEOLOGIST or DRONE_OPERATOR");
    }

    private String normalizeCategory(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("AGRICULTURAL".equals(normalized)) normalized = "AGRICULTURE";
        if ("MINERAL".equals(normalized)) normalized = "MINING";
        return normalized;
    }

    private String trim(String value) { return value == null ? null : value.trim(); }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
