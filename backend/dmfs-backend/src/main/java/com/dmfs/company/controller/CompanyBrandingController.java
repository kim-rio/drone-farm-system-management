package com.dmfs.company.controller;

import com.dmfs.company.dto.CompanyBrandingResponse;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.service.CompanyBrandingService;
import com.dmfs.company.service.CompanyLogoStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/company/branding")
@PreAuthorize("isAuthenticated()")
public class CompanyBrandingController {

    private final CompanyBrandingService brandingService;
    private final CompanyLogoStorageService logoStorageService;

    public CompanyBrandingController(
            CompanyBrandingService brandingService,
            CompanyLogoStorageService logoStorageService
    ) {
        this.brandingService = brandingService;
        this.logoStorageService = logoStorageService;
    }

    @GetMapping
    public CompanyBrandingResponse branding() {
        return brandingService.getBranding();
    }

    @GetMapping("/logo")
    public ResponseEntity<InputStreamResource> logo()
            throws IOException {

        SubscriberCompany company =
                brandingService.getCurrentCompany();

        if (company.getLogoPath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path path =
                logoStorageService.resolve(
                        company.getLogoPath()
                );

        if (!Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }

        String contentType =
                Files.probeContentType(path);

        MediaType mediaType =
                contentType == null
                        ? MediaType.APPLICATION_OCTET_STREAM
                        : MediaType.parseMediaType(contentType);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(
                        new InputStreamResource(
                                Files.newInputStream(path)
                        )
                );
    }
}