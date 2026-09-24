package com.dmfs.service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmfs.client.entity.Client;
import com.dmfs.client.repository.ClientRepository;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.service.CurrentCompanyService;
import com.dmfs.company.service.CompanyDocumentNumberService;
import com.dmfs.farm.entity.Block;
import com.dmfs.farm.entity.Farm;
import com.dmfs.farm.repository.BlockRepository;
import com.dmfs.farm.repository.FarmRepository;
import com.dmfs.service.entity.ServiceCatalogue;
import com.dmfs.service.entity.ServiceRequest;
import com.dmfs.service.repository.ServiceCatalogueRepository;
import com.dmfs.service.repository.ServiceRequestRepository;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ClientRepository clientRepository;
    private final FarmRepository farmRepository;
    private final BlockRepository blockRepository;
    private final ServiceCatalogueRepository serviceCatalogueRepository;
    private final CurrentCompanyService currentCompanyService;
    private final CompanyDocumentNumberService documentNumberService;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository,
            ClientRepository clientRepository,
            FarmRepository farmRepository,
            BlockRepository blockRepository,
            ServiceCatalogueRepository serviceCatalogueRepository,
            CurrentCompanyService currentCompanyService,
            CompanyDocumentNumberService documentNumberService
    ) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.clientRepository = clientRepository;
        this.farmRepository = farmRepository;
        this.blockRepository = blockRepository;
        this.serviceCatalogueRepository = serviceCatalogueRepository;
        this.currentCompanyService = currentCompanyService;
        this.documentNumberService = documentNumberService;
    }

    @Transactional(readOnly = true)
    public List<ServiceRequest> getAllServiceRequests() {
        return serviceRequestRepository.findByCustomerCompanyIdOrderByCreatedAtDesc(
                currentCompanyService.getCurrentCompany().getId()
        );
    }

    @Transactional(readOnly = true)
    public ServiceRequest getServiceRequestById(Long id) {
        Long companyId = currentCompanyService.getCurrentCompany().getId();
        return serviceRequestRepository.findByIdAndCustomerCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Service request not found with id: " + id));
    }

    @Transactional
    public ServiceRequest createServiceRequest(ServiceRequest serviceRequest) {
        SubscriberCompany company = currentCompanyService.getCurrentCompany();

        ResolvedRelationships relationships = resolveAndValidateRelationships(serviceRequest, company);

        // Never trust tenant-sensitive relationship objects, price, or date from the request body.
        serviceRequest.setCustomer(relationships.customer());
        serviceRequest.setFarm(relationships.farm());
        serviceRequest.setFarmBlock(relationships.block());
        serviceRequest.setServiceCatalogue(relationships.catalogue());
        serviceRequest.setAmount(relationships.catalogue().getStandardPrice());
        serviceRequest.setRequestNumber(
                documentNumberService.nextServiceRequestNumber(company)
        );
        // Payment/control number remains separate from the human-readable
        // company-scoped service request number.
        serviceRequest.setControlNumber("DMFS" + System.currentTimeMillis());
        serviceRequest.setPaymentStatus("PENDING");
        serviceRequest.setRequestedDate(LocalDate.now());

        return serviceRequestRepository.save(serviceRequest);
    }

    @Transactional
    public ServiceRequest updateServiceRequest(Long id, ServiceRequest updatedServiceRequest) {
        SubscriberCompany company = currentCompanyService.getCurrentCompany();
        ServiceRequest existing = serviceRequestRepository.findByIdAndCustomerCompanyId(id, company.getId())
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        ResolvedRelationships relationships = resolveAndValidateRelationships(updatedServiceRequest, company);

        existing.setCustomer(relationships.customer());
        existing.setFarm(relationships.farm());
        existing.setFarmBlock(relationships.block());
        existing.setServiceCatalogue(relationships.catalogue());
        existing.setAmount(relationships.catalogue().getStandardPrice());
        existing.setRequestedDate(updatedServiceRequest.getRequestedDate() != null
                ? updatedServiceRequest.getRequestedDate() : existing.getRequestedDate());
        existing.setNotes(updatedServiceRequest.getNotes());
        existing.setStatus(updatedServiceRequest.getStatus());

        return serviceRequestRepository.save(existing);
    }

    /**
     * Resolve every relationship from the authenticated tenant instead of trusting
     * partially populated objects deserialized from JSON. A request normally sends
     * only IDs, so request.getCustomer().getCompany() is not a valid tenant check.
     */
    private ResolvedRelationships resolveAndValidateRelationships(
            ServiceRequest request, SubscriberCompany company) {

        if (request == null) {
            throw new IllegalArgumentException("Service request is required");
        }
        if (request.getCustomer() == null || request.getCustomer().getId() == null)
            throw new IllegalArgumentException("Customer is required");
        if (request.getFarm() == null || request.getFarm().getId() == null)
            throw new IllegalArgumentException("Farm is required");
        if (request.getFarmBlock() == null || request.getFarmBlock().getId() == null)
            throw new IllegalArgumentException("Farm block is required");
        if (request.getServiceCatalogue() == null || request.getServiceCatalogue().getId() == null)
            throw new IllegalArgumentException("Service catalogue is required");

        Client customer = clientRepository
                .findByIdAndCompany(request.getCustomer().getId(), company)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer does not belong to your company or does not exist"));

        Farm farm = farmRepository.findById(request.getFarm().getId())
                .orElseThrow(() -> new IllegalArgumentException("Farm not found"));

        if (farm.getCustomer() == null || !farm.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Farm does not belong to the selected customer");
        }

        // The farm's customer is the tenant boundary for farms in the current schema.
        if (farm.getCustomer().getCompany() == null
                || !company.getId().equals(farm.getCustomer().getCompany().getId())) {
            throw new IllegalArgumentException("Farm does not belong to your company");
        }

        Block block = blockRepository.findById(request.getFarmBlock().getId())
                .orElseThrow(() -> new IllegalArgumentException("Farm block not found"));

        if (block.getFarm() == null || !block.getFarm().getId().equals(farm.getId())) {
            throw new IllegalArgumentException("Farm block does not belong to the selected farm");
        }

        ServiceCatalogue catalogue = serviceCatalogueRepository
                .findByIdAndCompany(request.getServiceCatalogue().getId(), company)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Service catalogue not found for your company"));

        if (!"ACTIVE".equalsIgnoreCase(catalogue.getStatus())) {
            throw new IllegalArgumentException("The selected service is inactive");
        }

        if (block.getAreaHectares() == null) {
            throw new IllegalArgumentException(
                    "The selected block must have an area before a service can be requested");
        }

        BigDecimal requestedArea = BigDecimal.valueOf(block.getAreaHectares());
        if (requestedArea.compareTo(catalogue.getMinimumArea()) < 0) {
            throw new IllegalArgumentException(
                    "Block area is below this service's minimum area of "
                            + catalogue.getMinimumArea() + " " + catalogue.getUnitOfMeasurement());
        }

        return new ResolvedRelationships(customer, farm, block, catalogue);
    }

    private record ResolvedRelationships(
            Client customer,
            Farm farm,
            Block block,
            ServiceCatalogue catalogue) {}

    @Transactional
    public void updateStatus(Long id, String status) {
        ServiceRequest serviceRequest = getServiceRequestById(id);
        serviceRequest.setStatus(status);
        serviceRequestRepository.save(serviceRequest);
    }

    @Transactional
    public void deleteServiceRequest(Long id) {
        ServiceRequest serviceRequest = getServiceRequestById(id);
        serviceRequestRepository.delete(serviceRequest);
    }
}
