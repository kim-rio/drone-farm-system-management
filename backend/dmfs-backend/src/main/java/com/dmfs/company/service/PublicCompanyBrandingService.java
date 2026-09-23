package com.dmfs.company.service;

import com.dmfs.company.dto.PublicCompanyBrandingResponse;
import com.dmfs.company.entity.CompanyStatus;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.company.repository.SubscriberCompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class PublicCompanyBrandingService {

    private final SubscriberCompanyRepository companyRepository;

    public PublicCompanyBrandingService(
            SubscriberCompanyRepository companyRepository
    ) {
        this.companyRepository = companyRepository;
    }

    public SubscriberCompany getActiveCompany(
            String workspaceSlug
    ) {

        if (workspaceSlug == null
                || workspaceSlug.isBlank()) {

            throw new RuntimeException(
                    "Company workspace is required"
            );
        }

        return companyRepository
                .findByWorkspaceSlug(
                        workspaceSlug.trim().toLowerCase()
                )
                .filter(company ->
                        company.getStatus()
                                == CompanyStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Company workspace not found"
                        )
                );
    }

    public PublicCompanyBrandingResponse getBranding(
            String workspaceSlug
    ) {

        SubscriberCompany company =
                getActiveCompany(workspaceSlug);

        String logoUrl =
                company.getLogoPath() == null
                        ? null
                        : "/api/public/company/logo";

        return new PublicCompanyBrandingResponse(
                company.getId(),
                company.getName(),
                company.getWorkspaceSlug(),
                logoUrl
        );
    }
}
