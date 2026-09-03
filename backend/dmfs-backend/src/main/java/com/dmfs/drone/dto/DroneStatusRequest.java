package com.dmfs.drone.dto;

import com.dmfs.drone.entity.DroneStatus;
import jakarta.validation.constraints.NotNull;

public class DroneStatusRequest {

    @NotNull
    private DroneStatus status;

    public DroneStatus getStatus() {
        return status;
    }

    public void setStatus(DroneStatus status) {
        this.status = status;
    }
}