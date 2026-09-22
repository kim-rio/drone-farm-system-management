package com.dmfs.company.controller;

import com.dmfs.company.dto.ChangeCompanyStatusRequest;
import com.dmfs.company.dto.CompanyResponse;
import com.dmfs.company.dto.CreateCompanyRequest;
import com.dmfs.company.dto.UpdateCompanyRequest;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.service.CompanyLogoStorageService;
import com.dmfs.company.service.SubscriberCompanyService;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/super-admin/companies")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SubscriberCompanyController {

    private final SubscriberCompanyService companyService;
    private final CompanyLogoStorageService logoStorageService;

    public SubscriberCompanyController(
            SubscriberCompanyService companyService,
            CompanyLogoStorageService logoStorageService
    ) {
        this.companyService = companyService;
        this.logoStorageService = logoStorageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(
            @Valid @RequestBody CreateCompanyRequest request
    ) {
        return companyService.create(request);
    }

    @GetMapping
    public List<CompanyResponse> getAll() {
        return companyService.getAll();
    }

    @GetMapping("/{id}")
    public CompanyResponse getById(
            @PathVariable Long id
    ) {
        return companyService.getById(id);
    }

    @PutMapping("/{id}")
    public CompanyResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyRequest request
    ) {
        return companyService.update(id, request);
    }

    @PostMapping(
            value = "/{id}/logo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CompanyResponse uploadLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return companyService.updateLogo(id, file);
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<InputStreamResource> getLogo(
            @PathVariable Long id
    ) throws IOException {

        SubscriberCompany company =
                companyService.getCompanyEntity(id);

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

    @PatchMapping("/{id}/status")
    public CompanyResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeCompanyStatusRequest request
    ) {
        return companyService.changeStatus(id, request);
    }

    @PatchMapping("/{id}/activate")
    public CompanyResponse activate(
            @PathVariable Long id
    ) {
        return companyService.activate(id);
    }

    @PatchMapping("/{id}/suspend")
    public CompanyResponse suspend(
            @PathVariable Long id
    ) {
        return companyService.suspend(id);
    }

    @PatchMapping("/{id}/expire")
    public CompanyResponse expire(
            @PathVariable Long id
    ) {
        return companyService.expire(id);
    }
}