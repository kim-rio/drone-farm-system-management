package com.dmfs.survey.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_data_packages")
public class SurveyDataPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Surveyy survey;

    @Column(name = "package_code", nullable = false, unique = true, length = 50)
    private String packageCode;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(name = "submitted_by")
    private Long submittedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToMany(
            mappedBy = "packageEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SurveyDataFile> files = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Surveyy getSurvey() {
        return survey;
    }

    public void setSurvey(Surveyy survey) {
        this.survey = survey;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(Long submittedBy) {
        this.submittedBy = submittedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<SurveyDataFile> getFiles() {
        return files;
    }

    public void setFiles(List<SurveyDataFile> files) {
        this.files = files;
    }

    public void addFile(SurveyDataFile file) {
        files.add(file);
        file.setPackageEntity(this);
    }

    public void removeFile(SurveyDataFile file) {
        files.remove(file);
        file.setPackageEntity(null);
    }
}