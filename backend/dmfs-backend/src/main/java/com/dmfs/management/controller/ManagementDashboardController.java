package com.dmfs.management.controller;

import com.dmfs.management.dto.ManagementDashboardResponse;
import com.dmfs.management.service.ManagementDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/management/dashboard")
@PreAuthorize("hasRole('MANAGEMENT')")
public class ManagementDashboardController {

    private final ManagementDashboardService dashboardService;

    public ManagementDashboardController(
            ManagementDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ManagementDashboardResponse dashboard(
            Authentication authentication
    ) {
        return dashboardService.getDashboard(
                authentication.getName()
        );
    }
}
