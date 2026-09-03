package com.dmfs.service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dmfs.service.entity.ServiceCatalogue;
import com.dmfs.service.service.ServiceCatalogueService;

@RestController
@RequestMapping("/api/service-catalogue")
public class ServiceCatalogueController {

    private final ServiceCatalogueService serviceCatalogueService;

    public ServiceCatalogueController(
            ServiceCatalogueService serviceCatalogueService
    ) {
        this.serviceCatalogueService = serviceCatalogueService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'GEOLOGIST', 'DRONE_OPERATOR')")
    public List<ServiceCatalogue> getAllServices() {
        return serviceCatalogueService.getAllServices();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'GEOLOGIST', 'DRONE_OPERATOR')")
    public ServiceCatalogue getServiceById(
            @PathVariable Long id
    ) {
        return serviceCatalogueService.getServiceById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceCatalogue createService(
            @RequestBody ServiceCatalogue service
    ) {
        return serviceCatalogueService.createService(service);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceCatalogue updateService(
            @PathVariable Long id,
            @RequestBody ServiceCatalogue service
    ) {
        return serviceCatalogueService.updateService(id, service);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateService(
            @PathVariable Long id
    ) {
        serviceCatalogueService.deactivateService(id);
    }
}
