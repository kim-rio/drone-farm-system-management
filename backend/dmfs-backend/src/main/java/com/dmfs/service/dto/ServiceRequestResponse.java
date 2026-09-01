package com.dmfs.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ServiceRequestResponse {

    private Long id;

    private CustomerInfo customer;

    private FarmInfo farm;

    private BlockInfo farmBlock;

    private ServiceCatalogueInfo serviceCatalogue;

    private LocalDate requestedDate;

    private String notes;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public ServiceRequestResponse() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public CustomerInfo getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerInfo customer) {
        this.customer = customer;
    }


    public FarmInfo getFarm() {
        return farm;
    }

    public void setFarm(FarmInfo farm) {
        this.farm = farm;
    }


    public BlockInfo getFarmBlock() {
        return farmBlock;
    }

    public void setFarmBlock(BlockInfo farmBlock) {
        this.farmBlock = farmBlock;
    }


    public ServiceCatalogueInfo getServiceCatalogue() {
        return serviceCatalogue;
    }

    public void setServiceCatalogue(
            ServiceCatalogueInfo serviceCatalogue
    ) {
        this.serviceCatalogue = serviceCatalogue;
    }


    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(
            LocalDate requestedDate
    ) {
        this.requestedDate = requestedDate;
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }


    /* ==============================
       CUSTOMER
       ============================== */

    public static class CustomerInfo {

        private Long id;

        private String customerCode;

        private String type;

        private String companyName;

        private String firstName;

        private String lastName;

        private String email;

        private String phone;

        private String status;


        public CustomerInfo() {
        }


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }


        public String getCustomerCode() {
            return customerCode;
        }

        public void setCustomerCode(
                String customerCode
        ) {
            this.customerCode = customerCode;
        }


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }


        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(
                String companyName
        ) {
            this.companyName = companyName;
        }


        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(
                String firstName
        ) {
            this.firstName = firstName;
        }


        public String getLastName() {
            return lastName;
        }

        public void setLastName(
                String lastName
        ) {
            this.lastName = lastName;
        }


        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }


        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }


        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    /* ==============================
       FARM
       ============================== */

    public static class FarmInfo {

        private Long id;

        private String name;

        private String description;

        private Double areaHectares;


        public FarmInfo() {
        }


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getDescription() {
            return description;
        }

        public void setDescription(
                String description
        ) {
            this.description = description;
        }


        public Double getAreaHectares() {
            return areaHectares;
        }

        public void setAreaHectares(
                Double areaHectares
        ) {
            this.areaHectares = areaHectares;
        }
    }


    /* ==============================
       BLOCK
       ============================== */

    public static class BlockInfo {

        private Long id;

        private String name;

        private String description;

        private Double areaHectares;

        private Double centerLatitude;

        private Double centerLongitude;


        public BlockInfo() {
        }


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getDescription() {
            return description;
        }

        public void setDescription(
                String description
        ) {
            this.description = description;
        }


        public Double getAreaHectares() {
            return areaHectares;
        }

        public void setAreaHectares(
                Double areaHectares
        ) {
            this.areaHectares = areaHectares;
        }


        public Double getCenterLatitude() {
            return centerLatitude;
        }

        public void setCenterLatitude(
                Double centerLatitude
        ) {
            this.centerLatitude = centerLatitude;
        }


        public Double getCenterLongitude() {
            return centerLongitude;
        }

        public void setCenterLongitude(
                Double centerLongitude
        ) {
            this.centerLongitude = centerLongitude;
        }
    }


    /* ==============================
       SERVICE CATALOGUE
       ============================== */

    public static class ServiceCatalogueInfo {

        private Long id;

        private String name;

        private String category;

        private String description;

        private String status;

        private String unitOfMeasurement;

        private BigDecimal standardPrice;

        private BigDecimal minimumArea;

        private Integer estimatedDurationMinutes;


        public ServiceCatalogueInfo() {
        }


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }


        public String getDescription() {
            return description;
        }

        public void setDescription(
                String description
        ) {
            this.description = description;
        }


        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }


        public String getUnitOfMeasurement() {
            return unitOfMeasurement;
        }

        public void setUnitOfMeasurement(
                String unitOfMeasurement
        ) {
            this.unitOfMeasurement =
                    unitOfMeasurement;
        }


        public BigDecimal getStandardPrice() {
            return standardPrice;
        }

        public void setStandardPrice(
                BigDecimal standardPrice
        ) {
            this.standardPrice =
                    standardPrice;
        }


        public BigDecimal getMinimumArea() {
            return minimumArea;
        }

        public void setMinimumArea(
                BigDecimal minimumArea
        ) {
            this.minimumArea =
                    minimumArea;
        }


        public Integer getEstimatedDurationMinutes() {
            return estimatedDurationMinutes;
        }

        public void setEstimatedDurationMinutes(
                Integer estimatedDurationMinutes
        ) {
            this.estimatedDurationMinutes =
                    estimatedDurationMinutes;
        }
    }
}