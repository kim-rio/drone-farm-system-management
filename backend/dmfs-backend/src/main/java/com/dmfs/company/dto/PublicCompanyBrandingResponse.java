package com.dmfs.company.dto;

public record PublicCompanyBrandingResponse(
        Long companyId,
        String companyName,
        String workspaceSlug,
        String logoUrl
) {
}