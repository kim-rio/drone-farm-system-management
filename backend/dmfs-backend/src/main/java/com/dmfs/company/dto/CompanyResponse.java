package com.dmfs.company.dto;

import com.dmfs.company.entity.CompanyStatus;

import java.time.LocalDateTime;

public class CompanyResponse {

    private Long id;
    private String name;
    private String registrationNumber;
    private String tin;
    private String email;
    private String phone;
    private String country;
    private String region;
    private String city;
    private String physicalAddress;
    private CompanyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CompanyResponse(
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
            CompanyStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.tin = tin;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.region = region;
        this.city = city;
        this.physicalAddress = physicalAddress;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getTin() {
        return tin;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public CompanyStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
