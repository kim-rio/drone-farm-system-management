package com.dmfs.mission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateMissionRequest {

    @NotNull(message = "Service request is required")
    private Long serviceRequestId;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;


    public CreateMissionRequest() {
    }


    public Long getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(Long serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
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
}