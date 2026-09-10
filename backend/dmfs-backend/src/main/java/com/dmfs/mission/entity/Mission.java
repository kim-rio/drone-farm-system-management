package com.dmfs.mission.entity;

import com.dmfs.auth.entity.User;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.drone.entity.Drone;
import com.dmfs.farm.entity.Block;
import com.dmfs.farm.entity.Farm;
import com.dmfs.service.entity.ServiceRequest;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "missions",
        indexes = {
                @Index(
                        name = "idx_missions_company_id",
                        columnList = "company_id"
                ),
                @Index(
                        name = "idx_missions_service_request_id",
                        columnList = "service_request_id"
                ),
                @Index(
                        name = "idx_missions_operator_id",
                        columnList = "operator_id"
                ),
                @Index(
                        name = "idx_missions_drone_id",
                        columnList = "drone_id"
                ),
                @Index(
                        name = "idx_missions_farm_id",
                        columnList = "farm_id"
                ),
                @Index(
                        name = "idx_missions_block_id",
                        columnList = "farm_block_id"
                )
        }
)
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Human-readable mission identifier.
     *
     * Example:
     * MIS-2026-0001
     */
    @Column(
            name = "mission_code",
            nullable = false,
            unique = true,
            length = 40
    )
    private String missionCode;


    /*
     * Every mission belongs to one subscriber company.
     *
     * This is important for multi-tenancy.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false
    )
    private SubscriberCompany company;


    /*
     * The service request that generated this mission.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "service_request_id",
            nullable = false
    )
    private ServiceRequest serviceRequest;


    /*
     * Customer receiving the service.
     *
     * We keep this relationship available directly on the mission
     * because operational screens frequently need customer information.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private com.dmfs.client.entity.Client customer;


    /*
     * Farm where the mission will take place.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "farm_id",
            nullable = false
    )
    private Farm farm;


    /*
     * Specific farm block being serviced.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "farm_block_id",
            nullable = false
    )
    private Block farmBlock;


    /*
     * Drone operator responsible for the mission.
     *
     * User.role must be DRONE_OPERATOR.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "operator_id"
    )
    private User operator;


    /*
     * Drone assigned to the mission.
     *
     * Drone management is intentionally kept simple for now.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "drone_id"
    )
    private Drone drone;


    /*
     * Planned date of field operation.
     */
    @Column(
            name = "scheduled_date"
    )
    private LocalDate scheduledDate;


    /*
     * Current mission state.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private MissionStatus status = MissionStatus.PLANNED;


    /*
     * Additional operational instructions.
     */
    @Column(
            columnDefinition = "TEXT"
    )
    private String notes;


    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @Column(
            name = "updated_at"
    )
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


    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }


    public String getMissionCode() {
        return missionCode;
    }

    public void setMissionCode(String missionCode) {
        this.missionCode = missionCode;
    }


    public SubscriberCompany getCompany() {
        return company;
    }

    public void setCompany(SubscriberCompany company) {
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


    public com.dmfs.client.entity.Client getCustomer() {
        return customer;
    }

    public void setCustomer(
            com.dmfs.client.entity.Client customer
    ) {
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


    public User getOperator() {
        return operator;
    }

    public void setOperator(User operator) {
        this.operator = operator;
    }


    public Drone getDrone() {
        return drone;
    }

    public void setDrone(Drone drone) {
        this.drone = drone;
    }


    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(
            LocalDate scheduledDate
    ) {
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


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}