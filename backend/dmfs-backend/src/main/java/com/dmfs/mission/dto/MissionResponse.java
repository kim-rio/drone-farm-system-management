package com.dmfs.mission.dto;

import com.dmfs.mission.entity.MissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MissionResponse {

    private Long id;

    private String missionCode;

    private MissionStatus status;

    private LocalDate scheduledDate;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private ServiceRequestInfo serviceRequest;

    private CustomerInfo customer;

    private FarmInfo farm;

    private BlockInfo farmBlock;

    private OperatorInfo operator;

    private DroneInfo drone;


    public MissionResponse() {
    }


    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getMissionCode() {
        return missionCode;
    }

    public void setMissionCode(String missionCode) {
        this.missionCode = missionCode;
    }


    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }


    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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


    public ServiceRequestInfo getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(ServiceRequestInfo serviceRequest) {
        this.serviceRequest = serviceRequest;
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


    public DroneInfo getDrone() {
        return drone;
    }

    public void setDrone(DroneInfo drone) {
        this.drone = drone;
    }


    // =========================================================
    // SERVICE REQUEST
    // =========================================================

    public static class ServiceRequestInfo {

        private Long id;

        private LocalDate requestedDate;

        private String status;


        public ServiceRequestInfo() {
        }


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }


        public LocalDate getRequestedDate() {
            return requestedDate;
        }

        public void setRequestedDate(LocalDate requestedDate) {
            this.requestedDate = requestedDate;
        }


        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
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
    // FARM BLOCK
    // =========================================================

    public static class BlockInfo {

        private Long id;

        private String name;

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


        public Double getAreaHectares() {
            return areaHectares;
        }

        public void setAreaHectares(Double areaHectares) {
            this.areaHectares = areaHectares;
        }


        public Double getCenterLatitude() {
            return centerLatitude;
        }

        public void setCenterLatitude(Double centerLatitude) {
            this.centerLatitude = centerLatitude;
        }


        public Double getCenterLongitude() {
            return centerLongitude;
        }

        public void setCenterLongitude(Double centerLongitude) {
            this.centerLongitude = centerLongitude;
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


    // =========================================================
    // DRONE
    // =========================================================

    public static class DroneInfo {

        private Long id;

        private String name;

        private String serialNumber;

        private String model;

        private String status;


        public DroneInfo() {
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


        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }


        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }


        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}