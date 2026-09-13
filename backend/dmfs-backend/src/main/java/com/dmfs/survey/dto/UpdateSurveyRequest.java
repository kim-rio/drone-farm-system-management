package com.dmfs.survey.dto;

import com.dmfs.survey.entity.SurveyStatus;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public class UpdateSurveyRequest {

    @NotBlank(message = "Survey name is required")
    private String surveyName;

    private String description;

    private OffsetDateTime startedAt;

    private OffsetDateTime endedAt;

    private Double startLatitude;

    private Double startLongitude;

    private Double endLatitude;

    private Double endLongitude;

    private SurveyStatus status;


    public UpdateSurveyRequest() {
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


    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }


    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(OffsetDateTime endedAt) {
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
}