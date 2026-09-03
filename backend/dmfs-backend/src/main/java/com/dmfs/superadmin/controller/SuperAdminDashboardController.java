package com.dmfs.superadmin.controller;

import com.dmfs.superadmin.dto.SuperAdminDashboardResponse;
import com.dmfs.superadmin.service.SuperAdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin/dashboard")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService dashboardService;

    public SuperAdminDashboardController(
            SuperAdminDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public SuperAdminDashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }
}