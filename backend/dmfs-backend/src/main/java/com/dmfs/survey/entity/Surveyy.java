package com.dmfs.survey.entity;

import com.dmfs.auth.entity.User;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.service.entity.ServiceRequest;

import jakarta.persistence.*;

import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "survey",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_survey_company_code",
                        columnNames = {
                                "company_id",
                                "survey_code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_survey_company",
                        columnList = "company_id"
                ),
                @Index(
                        name = "idx_survey_service_request",
                        columnList = "service_request_id"
                ),
                @Index(
                        name = "idx_survey_operator",
                        columnList = "operator_id"
                )
        }
)
public class Surveyy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // TENANT / COMPANY
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false
    )
    private SubscriberCompany company;


    // =========================================================
    // SERVICE REQUEST
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "service_request_id",
            nullable = false
    )
    private ServiceRequest serviceRequest;


    // =========================================================
    // DRONE OPERATOR
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "operator_id",
            nullable = false
    )
    private User operator;


    // =========================================================
    // SURVEY INFORMATION
    // =========================================================

    @Column(
            name = "survey_code",
            nullable = false,
            length = 50
    )
    private String surveyCode;

    @Column(
            name = "survey_name",
            length = 150
    )
    private String surveyName;

    @Column(
            columnDefinition = "TEXT"
    )
    private String description;


    // =========================================================
    // TIME
    // =========================================================

    @Column(
            name = "started_at",
            nullable = false
    )
    private OffsetDateTime startedAt;

    @Column(
            name = "ended_at"
    )
    private OffsetDateTime endedAt;


    // =========================================================
    // LOCATION
    // =========================================================

    @Column(
            name = "start_location",
            columnDefinition = "geometry(Point,4326)"
    )
    private Point startLocation;

    @Column(
            name = "end_location",
            columnDefinition = "geometry(Point,4326)"
    )
    private Point endLocation;


    // =========================================================
    // STATUS
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private SurveyStatus status = SurveyStatus.DRAFT;


    // =========================================================
    // AUDIT
    // =========================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;


    // =========================================================
    // JPA LIFECYCLE
    // =========================================================

    @PrePersist
    protected void onCreate() {

        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = OffsetDateTime.now();
    }


    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }


    public SubscriberCompany getCompany() {
        return company;
    }

    public void setCompany(
            SubscriberCompany company
    ) {
        this.company = company;
    }


    public ServiceRequest getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(
            ServiceRequest serviceRequest
    ) {
        this.serviceRequest = serviceRequest;
    }


    public User getOperator() {
        return operator;
    }

    public void setOperator(
            User operator
    ) {
        this.operator = operator;
    }


    public String getSurveyCode() {
        return surveyCode;
    }

    public void setSurveyCode(
            String surveyCode
    ) {
        this.surveyCode = surveyCode;
    }


    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(
            String surveyName
    ) {
        this.surveyName = surveyName;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }


    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(
            OffsetDateTime startedAt
    ) {
        this.startedAt = startedAt;
    }


    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(
            OffsetDateTime endedAt
    ) {
        this.endedAt = endedAt;
    }


    public Point getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(
            Point startLocation
    ) {
        this.startLocation = startLocation;
    }


    public Point getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(
            Point endLocation
    ) {
        this.endLocation = endLocation;
    }


    public SurveyStatus getStatus() {
        return status;
    }

    public void setStatus(
            SurveyStatus status
    ) {
        this.status = status;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}