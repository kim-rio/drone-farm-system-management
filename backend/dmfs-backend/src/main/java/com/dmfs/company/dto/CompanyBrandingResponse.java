package com.dmfs.company.dto;

public record CompanyBrandingResponse(
        Long companyId,
        String companyName,
        String logoUrl
) {
}