package com.dmfs.mission.dto;

import com.dmfs.mission.entity.MissionStatus;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateMissionRequest {

    private LocalDate scheduledDate;

    private MissionStatus status;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;


    public UpdateMissionRequest() {
    }


    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }


    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}