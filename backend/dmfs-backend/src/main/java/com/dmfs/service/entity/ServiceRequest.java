package com.dmfs.service.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dmfs.client.entity.Client;
import com.dmfs.farm.entity.Block;
import com.dmfs.farm.entity.Farm;
import com.dmfs.auth.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "service_request")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Client customer;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne
    @JoinColumn(name = "farm_block_id", nullable = false)
    private Block farmBlock;

    @ManyToOne
    @JoinColumn(name = "service_catalogue_id", nullable = false)
    private ServiceCatalogue serviceCatalogue;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(name = "operator_decision_reason", columnDefinition = "TEXT")
    private String operatorDecisionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    @JsonProperty("client")
    public Client getCustomer() {
        return customer;
    }

    @JsonAlias("client")
    public void setCustomer(Client customer) {
        this.customer = customer;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    public Block getFarmBlock() {
        return farmBlock;
    }

    public void setFarmBlock(Block farmBlock) {
        this.farmBlock = farmBlock;
    }

    public ServiceCatalogue getServiceCatalogue() {
        return serviceCatalogue;
    }

    public void setServiceCatalogue(ServiceCatalogue serviceCatalogue) {
        this.serviceCatalogue = serviceCatalogue;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public User getOperator() { return operator; }
    public void setOperator(User operator) { this.operator = operator; }
    public String getOperatorDecisionReason() { return operatorDecisionReason; }
    public void setOperatorDecisionReason(String operatorDecisionReason) { this.operatorDecisionReason = operatorDecisionReason; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
