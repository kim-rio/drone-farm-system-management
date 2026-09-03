package com.dmfs.company.dto;

import com.dmfs.company.entity.CompanyStatus;
import jakarta.validation.constraints.NotNull;

public class ChangeCompanyStatusRequest {

    @NotNull(message = "Status is required")
    private CompanyStatus status;

    public CompanyStatus getStatus() {
        return status;
    }

    public void setStatus(CompanyStatus status) {
        this.status = status;
    }
}
