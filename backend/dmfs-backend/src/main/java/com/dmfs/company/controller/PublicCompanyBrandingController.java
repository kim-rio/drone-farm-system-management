package com.dmfs.company.controller;

import com.dmfs.company.dto.PublicCompanyBrandingResponse;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.service.CompanyLogoStorageService;
import com.dmfs.company.service.PublicCompanyBrandingService;
import com.dmfs.company.service.WorkspaceHostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/public/company")
public class PublicCompanyBrandingController {

    private final PublicCompanyBrandingService brandingService;
    private final CompanyLogoStorageService logoStorageService;
    private final WorkspaceHostService workspaceHostService;

    public PublicCompanyBrandingController(
            PublicCompanyBrandingService brandingService,
            CompanyLogoStorageService logoStorageService,
            WorkspaceHostService workspaceHostService
    ) {
        this.brandingService = brandingService;
        this.logoStorageService = logoStorageService;
        this.workspaceHostService = workspaceHostService;
    }

    @GetMapping
    public PublicCompanyBrandingResponse branding(
            HttpServletRequest request
    ) {

        String workspaceSlug =
                workspaceHostService.resolveWorkspaceSlug(
                        request
                );

        if (workspaceSlug == null) {
            throw new RuntimeException(
                    "Company workspace is required"
            );
        }

        return brandingService.getBranding(
                workspaceSlug
        );
    }

    @GetMapping("/logo")
    public ResponseEntity<InputStreamResource> logo(
            HttpServletRequest request
    ) throws IOException {

        String workspaceSlug =
                workspaceHostService.resolveWorkspaceSlug(
                        request
                );

        if (workspaceSlug == null) {
            return ResponseEntity.notFound()
                    .build();
        }

        SubscriberCompany company =
                brandingService.getActiveCompany(
                        workspaceSlug
                );

        if (company.getLogoPath() == null) {
            return ResponseEntity.notFound()
                    .build();
        }

        Path path =
                logoStorageService.resolve(
                        company.getLogoPath()
                );

        if (!Files.isRegularFile(path)) {
            return ResponseEntity.notFound()
                    .build();
        }

        String contentType =
                Files.probeContentType(path);

        MediaType mediaType =
                contentType == null
                        ? MediaType.APPLICATION_OCTET_STREAM
                        : MediaType.parseMediaType(
                                contentType
                        );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(
                        new InputStreamResource(
                                Files.newInputStream(path)
                        )
                );
    }
}
