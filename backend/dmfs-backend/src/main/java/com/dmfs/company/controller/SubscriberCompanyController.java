package com.dmfs.company.controller;

import com.dmfs.company.dto.ChangeCompanyStatusRequest;
import com.dmfs.company.dto.CompanyResponse;
import com.dmfs.company.dto.CreateCompanyRequest;
import com.dmfs.company.dto.UpdateCompanyRequest;
import com.dmfs.company.service.SubscriberCompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/companies")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SubscriberCompanyController {

    private final SubscriberCompanyService companyService;

    public SubscriberCompanyController(
            SubscriberCompanyService companyService
    ) {
        this.companyService = companyService;
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
