package com.dmfs.survey.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SurveyDataPackageResponse {

    private Long id;
    private String packageCode;
    private Long surveyId;
    private String status;
    private Long submittedBy;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
    private LocalDateTime submittedAt;
    private List<SurveyDataFileResponse> files;

    public SurveyDataPackageResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public String getStatus() {
        return status;
    }

    public Long getSubmittedBy() {
        return submittedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public List<SurveyDataFileResponse> getFiles() {
        return files;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSubmittedBy(Long submittedBy) {
        this.submittedBy = submittedBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setFiles(List<SurveyDataFileResponse> files) {
        this.files = files;
    }
}