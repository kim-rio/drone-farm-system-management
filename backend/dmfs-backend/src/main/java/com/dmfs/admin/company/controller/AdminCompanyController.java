package com.dmfs.admin.company.controller;

import com.dmfs.admin.company.service.AdminCompanyService;
import com.dmfs.company.dto.CompanyResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/company")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCompanyController {

    private final AdminCompanyService adminCompanyService;

    public AdminCompanyController(
            AdminCompanyService adminCompanyService
    ) {
        this.adminCompanyService = adminCompanyService;
    }

    @GetMapping
    public CompanyResponse getCompany() {
        return adminCompanyService.getCurrentAdminCompany();
    }
}
