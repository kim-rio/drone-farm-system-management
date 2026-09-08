package com.dmfs.geologist.entity;

import com.dmfs.auth.entity.User;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.service.entity.ServiceRequest;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "survey")
public class Survey {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false) private SubscriberCompany company;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "service_request_id", nullable = false) private ServiceRequest serviceRequest;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "operator_id", nullable = false) private User operator;
    @Column(name = "survey_code", nullable = false) private String surveyCode;
    @Column(name = "survey_name") private String surveyName;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "started_at", nullable = false) private OffsetDateTime startedAt;
    @Column(name = "ended_at") private OffsetDateTime endedAt;
    @Column(name = "completed_at") private OffsetDateTime completedAt;
    @Column(name = "equipment_used", columnDefinition = "TEXT") private String equipmentUsed;
    @Column(nullable = false) private String status = "DRAFT";
    public Long getId() { return id; }
    public SubscriberCompany getCompany() { return company; } public void setCompany(SubscriberCompany v) { company = v; }
    public ServiceRequest getServiceRequest() { return serviceRequest; } public void setServiceRequest(ServiceRequest v) { serviceRequest = v; }
    public User getOperator() { return operator; } public void setOperator(User v) { operator = v; }
    public String getSurveyCode() { return surveyCode; } public void setSurveyCode(String v) { surveyCode = v; }
    public String getSurveyName() { return surveyName; } public void setSurveyName(String v) { surveyName = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public OffsetDateTime getStartedAt() { return startedAt; } public void setStartedAt(OffsetDateTime v) { startedAt = v; }
    public OffsetDateTime getEndedAt() { return endedAt; } public void setEndedAt(OffsetDateTime v) { endedAt = v; }
    public OffsetDateTime getCompletedAt() { return completedAt; } public void setCompletedAt(OffsetDateTime v) { completedAt = v; }
    public String getEquipmentUsed() { return equipmentUsed; } public void setEquipmentUsed(String v) { equipmentUsed = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
}
