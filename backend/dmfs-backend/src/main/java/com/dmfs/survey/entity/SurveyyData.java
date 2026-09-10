package com.dmfs.survey.entity;

import java.time.LocalDateTime;

import com.dmfs.auth.entity.User;
import com.dmfs.company.entity.SubscriberCompany;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "survey_data",
        indexes = {
                @Index(
                        name = "idx_survey_data_survey",
                        columnList = "survey_id"
                ),
                @Index(
                        name = "idx_survey_data_company",
                        columnList = "company_id"
                ),
                @Index(
                        name = "idx_survey_data_uploaded_by",
                        columnList = "uploaded_by"
                )
        }
)
public class SurveyyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Survey to which this uploaded data belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "survey_id",
            nullable = false
    )
    private Surveyy survey;


    /*
     * Subscriber company.
     *
     * This is kept directly on the record for
     * multi-tenant data isolation.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false
    )
    private SubscriberCompany company;


    /*
     * Original uploaded file name.
     */
    @Column(
            name = "file_name",
            nullable = false,
            length = 255
    )
    private String fileName;


    /*
     * Storage location of the uploaded file.
     */
    @Column(
            name = "file_path",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String filePath;


    /*
     * Example:
     * CSV, ZIP, JSON, XLSX
     */
    @Column(
            name = "file_type",
            length = 100
    )
    private String fileType;


    /*
     * File size in bytes.
     */
    @Column(
            name = "file_size"
    )
    private Long fileSize;


    /*
     * Number of data records contained
     * in the uploaded file.
     */
    @Column(
            name = "record_count"
    )
    private Long recordCount;


    /*
     * User who uploaded the survey data.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "uploaded_by",
            nullable = false
    )
    private User uploadedBy;


    /*
     * Time the data was uploaded.
     */
    @Column(
            name = "uploaded_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime uploadedAt;


    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }


    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }


    public Surveyy getSurvey() {
        return survey;
    }

    public void setSurvey(Surveyy survey) {
        this.survey = survey;
    }


    public SubscriberCompany getCompany() {
        return company;
    }

    public void setCompany(SubscriberCompany company) {
        this.company = company;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }


    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }


    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }


    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }


    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}