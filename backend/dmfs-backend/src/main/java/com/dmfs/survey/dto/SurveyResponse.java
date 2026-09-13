package com.dmfs.survey.dto;

import com.dmfs.survey.entity.SurveyStatus;

import java.time.LocalDateTime;

public class SurveyResponse {

    private Long id;

    private String surveyCode;

    private String surveyName;

    private String description;

    private Long serviceRequestId;

    private String serviceRequestCode;

    private CustomerInfo customer;

    private FarmInfo farm;

    private BlockInfo farmBlock;

    private OperatorInfo operator;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Double startLatitude;

    private Double startLongitude;

    private Double endLatitude;

    private Double endLongitude;

    private SurveyStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public SurveyResponse() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getSurveyCode() {
        return surveyCode;
    }

    public void setSurveyCode(String surveyCode) {
        this.surveyCode = surveyCode;
    }


    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Long getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(Long serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }


    public String getServiceRequestCode() {
        return serviceRequestCode;
    }

    public void setServiceRequestCode(String serviceRequestCode) {
        this.serviceRequestCode = serviceRequestCode;
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


    public OperatorInfo getOperator() {
        return operator;
    }

    public void setOperator(OperatorInfo operator) {
        this.operator = operator;
    }


    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }


    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }


    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }


    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }


    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }


    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }


    public SurveyStatus getStatus() {
        return status;
    }

    public void setStatus(SurveyStatus status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    // =========================================================
    // CUSTOMER
    // =========================================================

    public static class CustomerInfo {

        private Long id;
        private String clientCode;
        private String type;
        private String status;

        public CustomerInfo() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getClientCode() {
            return clientCode;
        }

        public void setClientCode(String clientCode) {
            this.clientCode = clientCode;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    // =========================================================
    // FARM
    // =========================================================

    public static class FarmInfo {

        private Long id;
        private String name;
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

        public Double getAreaHectares() {
            return areaHectares;
        }

        public void setAreaHectares(Double areaHectares) {
            this.areaHectares = areaHectares;
        }
    }


    // =========================================================
    // BLOCK
    // =========================================================

    public static class BlockInfo {

        private Long id;
        private String name;
        private Double areaHectares;

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

        public Double getAreaHectares() {
            return areaHectares;
        }

        public void setAreaHectares(Double areaHectares) {
            this.areaHectares = areaHectares;
        }
    }


    // =========================================================
    // OPERATOR
    // =========================================================

    public static class OperatorInfo {

        private Long id;
        private String firstName;
        private String lastName;
        private String email;

        public OperatorInfo() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}