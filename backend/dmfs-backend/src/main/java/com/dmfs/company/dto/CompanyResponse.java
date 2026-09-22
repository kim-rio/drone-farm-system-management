package com.dmfs.company.dto;

import com.dmfs.company.entity.CompanyStatus;

import java.time.LocalDateTime;

public record CompanyResponse(
        Long id,
        String name,
        String registrationNumber,
        String tin,
        String email,
        String phone,
        String country,
        String region,
        String city,
        String physicalAddress,
        String logoUrl,
        CompanyStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}