package com.dmfs.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmfs.service.entity.ServiceCatalogue;

public interface ServiceCatalogueRepository
        extends JpaRepository<ServiceCatalogue, Long> {
}