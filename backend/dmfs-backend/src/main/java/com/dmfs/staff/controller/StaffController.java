package com.dmfs.staff.controller;

import com.dmfs.staff.dto.CreateStaffRequest;
import com.dmfs.staff.dto.StaffResponse;
import com.dmfs.staff.dto.UpdateStaffRequest;
import com.dmfs.staff.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public List<StaffResponse> getStaff() {
        return staffService.getStaff();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffResponse createStaff(
            @Valid @RequestBody CreateStaffRequest request
    ) {
        return staffService.createStaff(request);
    }

    @PutMapping("/{id}")
    public StaffResponse updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request
    ) {
        return staffService.updateStaff(id, request);
    }

    @PatchMapping("/{id}/activate")
    public StaffResponse activateStaff(
            @PathVariable Long id
    ) {
        return staffService.activateStaff(id);
    }

    @PatchMapping("/{id}/deactivate")
    public StaffResponse deactivateStaff(
            @PathVariable Long id
    ) {
        return staffService.deactivateStaff(id);
    }
}
